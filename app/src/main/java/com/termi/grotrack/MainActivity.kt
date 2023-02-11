package com.termi.grotrack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
            // TODO: Current grocery list + All groceries + locations
        }

        // Handle Firebase connection
        database =
            FirebaseDatabase.getInstance(Consts.DATABASE_URL)
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
    }

    private fun loadDataFromSnapshot(dataSnapshot: DataSnapshot) {
        Log.d(Consts.TAG, "Data add / change")
        Toast.makeText(applicationContext, "Data updated :)", Toast.LENGTH_LONG).show()

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
        // TODO: Change to serializable

        Log.d(Consts.TAG, "$requestCode")
        Log.d(Consts.TAG, "$resultCode")

        if (data == null) {
            Log.d(Consts.TAG, "Problematic..")
            return
        }

        if (!data.hasExtra("name") || !data.hasExtra("count")) {
            Log.d(Consts.TAG, "Not from AddGroceries!")
            return
        }

        val name: String = data.getStringExtra("name")!!
        val count: Int = data.getIntExtra("count", -1)
        val location: String = data.getStringExtra("location")!!
        val groceryRef = groceriesRef.child(name)

        val grocery = mapOf(
            "Count" to count,
            "Location" to location
        )

        Log.d(Consts.TAG, "Got name of $name")
        Log.d(Consts.TAG, "Got count of $count")
        Log.d(Consts.TAG, "Got location of $location")

        groceryRef.updateChildren(grocery).addOnSuccessListener {
            // Updated children!
            Toast.makeText(
                this,
                "Updated new grocery (name=$name, count=$count, location=$location",
                Toast.LENGTH_SHORT
            ).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Could not update :(((", Toast.LENGTH_SHORT).show()

        }

//        groceryRef.setValue(count)
    }
}