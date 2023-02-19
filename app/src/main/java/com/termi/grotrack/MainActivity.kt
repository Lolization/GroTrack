package com.termi.grotrack

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue

class MainActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var groceriesRef: DatabaseReference

    private lateinit var rvGroceries: RecyclerView
    private lateinit var fabAddTask: FloatingActionButton

    private val groceries: ArrayList<Grocery> = ArrayList()
    private lateinit var rvAdapter: GroceryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvGroceries = findViewById(R.id.rv_groceries)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        val dividerItemDecoration =
            DividerItemDecoration(applicationContext, linearLayoutManager.orientation)

        rvGroceries.layoutManager = linearLayoutManager
        rvGroceries.addItemDecoration(dividerItemDecoration)

        rvAdapter = GroceryAdapter(groceries)
        rvGroceries.adapter = rvAdapter

        fabAddTask = findViewById(R.id.fab_add_task)

        fabAddTask.setOnClickListener {
            val intent = Intent(applicationContext, AddGroceriesActivity::class.java)
            startActivityForResult(intent, Consts.ADD_GROCERY_REQ_CODE)
        }

        // Handle Firebase connection
        database = FirebaseDatabase.getInstance(Consts.DATABASE_URL)
        Log.d(Consts.TAG, "Value is: $database")
        groceriesRef = database.getReference("Groceries")
        Log.d(Consts.TAG, "Value is: $groceriesRef")

        // Read from the database
        groceriesRef.addValueEventListener(object : ValueEventListener {
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
            startActivityForResult(intent, Consts.EDIT_GROCERY_REQ_CODE)
        }

        rvAdapter.setOnItemRemoveClickListener {
            // Show a confirm dialogue, and then delete
            AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete ${it.name}?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val groceryToRemove = groceriesRef.child(it.name)
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

    private fun loadDataFromSnapshot(dataSnapshot: DataSnapshot) {
        Log.d(Consts.TAG, "Reloaded data!")
        Toast.makeText(applicationContext, "Data updated :)", Toast.LENGTH_SHORT).show()

        groceries.clear()  // Clear all data

        dataSnapshot.children.forEach {
            try {
                val objectData: Map<String, Any>? = it.getValue<Map<String, Any>>()
                if (objectData == null) {
                    Log.e(Consts.TAG, "No child :(")
                    return@forEach
                }

                Log.d(Consts.TAG, "Got object data $objectData")

                val name: String? = it.key
                if (name == null) {
                    Log.e(Consts.TAG, "no key?")
                    return@forEach
                }

                val count: Long = objectData["Count"] as Long
                val location: String = objectData["Location"] as String

                val grocery = Grocery(name, count, location)
                Log.d(Consts.TAG, "Got Grocery $name:$count:$location")
                groceries.add(grocery)

            } catch (de: DatabaseException) {
                Log.d(Consts.TAG, "Failed to fetch $it")
            }
        }
        updateRecyclerView()
    }

    private fun updateRecyclerView() {
//        rvGroceries.adapter = rvAdapter
        // Do you need groceries?
        rvAdapter.notifyDataSetChanged()
        Log.d(Consts.TAG, "Groceries ${groceries.size}::$groceries")

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

            val name: String = grocery.name
            val count: Long = grocery.count
            val location: String = grocery.location

            val groceryRef = groceriesRef.child(name)

            val groceryObj = mapOf(
                "Count" to count,
                "Location" to location
            )

            Log.v(Consts.TAG, "Got name of $name")
            Log.v(Consts.TAG, "Got count of $count")
            Log.v(Consts.TAG, "Got location of $location")

            groceryRef.updateChildren(groceryObj).addOnSuccessListener {
                // Updated children!
                Toast.makeText(
                    this,
                    "Updated (name=$name, count=$count, location=$location",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Could not update :(((", Toast.LENGTH_SHORT).show()

            }
        }
    }
}