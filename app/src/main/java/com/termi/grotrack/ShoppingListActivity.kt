package com.termi.grotrack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue

class ShoppingListActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private lateinit var database: FirebaseDatabase
    private lateinit var shoppingRef: DatabaseReference

    private lateinit var rvGroceries: RecyclerView
    private lateinit var fabAddTask: FloatingActionButton

    private val shopping: ArrayList<Grocery> = ArrayList()
    private lateinit var rvAdapter: ShoppingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        rvGroceries = findViewById(R.id.rv_groceries)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        val dividerItemDecoration =
            DividerItemDecoration(applicationContext, linearLayoutManager.orientation)

        rvGroceries.layoutManager = linearLayoutManager
        rvGroceries.addItemDecoration(dividerItemDecoration)

        rvAdapter = ShoppingAdapter(shopping)
        rvGroceries.adapter = rvAdapter

        fabAddTask = findViewById(R.id.fab_add_task)
        fabAddTask.setOnClickListener {
            val intent = Intent(applicationContext, AddGroceriesActivity::class.java)

            intent.putExtra("showLocation", false)
            startActivityForResult(intent, Consts.ADD_GROCERY_REQ_CODE)
        }

        // Handle Firebase connection
        database = FirebaseDatabase.getInstance(Consts.DATABASE_URL)
        Log.d(Consts.TAG, "Value is: $database")
        shoppingRef = database.getReference("Shopping")
        Log.d(Consts.TAG, "Value is: $shoppingRef")

        // Read from the database
        shoppingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                loadDataFromSnapshot(dataSnapshot)

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(Consts.TAG, "Failed to read value.", error.toException())
            }
        })

        rvAdapter.setOnItemClickListener {
            Toast.makeText(applicationContext, "Editing ${it.name}", Toast.LENGTH_SHORT).show()

            val intent = Intent(applicationContext, AddGroceriesActivity::class.java)
            intent.putExtra("grocery", it)
            intent.putExtra("showLocation", false)
//            intent.putStringArrayListExtra("locations", allLocations)  // Make the whole intent thing in a function
            startActivityForResult(intent, Consts.EDIT_GROCERY_REQ_CODE)
        }

        rvAdapter.setOnItemRemoveClickListener {
            // Show a confirm dialogue, and then delete
            AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete ${it.name}?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val groceryToRemove = shoppingRef.child(it.name)
                    groceryToRemove.removeValue().addOnSuccessListener { _ ->
                        Toast.makeText(
                            applicationContext,
                            "Removed ${it.name}!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.filter_items, menu)
        val searchItem: MenuItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        // Here is where we are going to implement the filter logic
        if (query == null || query.isBlank()) {
            // No filter
            updateRecyclerView(shopping)
            return false
        }

        val filter: String = query.trim()
        val filteredGroceries: List<Grocery> =
            shopping.filter { it.name.lowercase().contains(filter.lowercase()) }
        updateRecyclerView(filteredGroceries)
        rvGroceries.scrollToPosition(0)
        return false
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    private fun loadDataFromSnapshot(dataSnapshot: DataSnapshot) {
        Log.d(Consts.TAG, "Reloaded data!")
        Toast.makeText(applicationContext, "Data updated :)", Toast.LENGTH_SHORT).show()

        dataSnapshot.children.forEach {
            try {
                val objectData: Map<String, Any>? = it.getValue<Map<String, Any>>()
                if (objectData == null) {
                    Log.e(Consts.TAG, "No child :(")
                    return@forEach
                }

                Log.d(Consts.TAG, "Got object data $objectData")

                val name: String? = it.key?.trim()
                if (name == null) {
                    Log.e(Consts.TAG, "no key?")
                    return@forEach
                }

                val count: Long = objectData["Count"] as Long

                val grocery = Grocery(name, count)
                Log.d(Consts.TAG, "Got Grocery $name:$count")
                shopping.add(grocery)

            } catch (de: DatabaseException) {
                Log.d(Consts.TAG, "Failed to fetch $it:: $de")
            }
        }
        updateRecyclerView(shopping)
    }

    private fun updateRecyclerView(newGroceries: List<Grocery>) {
        rvAdapter.replaceData(newGroceries)
        rvAdapter.notifyDataSetChanged()
        Log.d(Consts.TAG, "Groceries ${shopping.size}::$shopping")

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(Consts.TAG, "$requestCode")
        Log.d(Consts.TAG, "$resultCode")

        if (data == null) {
            Log.d(Consts.TAG, "Problematic..")
            return
        }

        when (requestCode) {
            Consts.ADD_GROCERY_REQ_CODE, Consts.EDIT_GROCERY_REQ_CODE -> addGrocery(data)
//            Consts.EDIT_GROCERY_REQ_CODE -> editGrocery(data)
        }
    }

    private fun addGrocery(data: Intent) {
        if (!data.hasExtra("grocery")) {
            Log.d(Consts.TAG, "Not from AddGroceries!")
            Toast.makeText(
                this,
                "Failed to update!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        data.getSerializableExtra("grocery")?.let {
            val grocery = it as Grocery

            val name: String = grocery.name.trim()
            val count: Long = grocery.count

            val groceryRef = shoppingRef.child(name)

            val groceryObj = mapOf(
                "Count" to count,
            )

            Log.v(Consts.TAG, "Got name of $name")
            Log.v(Consts.TAG, "Got count of $count")

            groceryRef.updateChildren(groceryObj).addOnSuccessListener {
                // Updated children!
                Toast.makeText(
                    this,
                    "Updated (name=$name, count=$count",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Could not update :(((", Toast.LENGTH_SHORT).show()

            }
        }
    }
}