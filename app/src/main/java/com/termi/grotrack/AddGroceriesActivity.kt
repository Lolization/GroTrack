package com.termi.grotrack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.android.material.snackbar.Snackbar

class AddGroceriesActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etCount: EditText
    private lateinit var actvLocation: AutoCompleteTextView
    private lateinit var btnAdd: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_groceries)

        etName = findViewById(R.id.et_name)
        etCount = findViewById(R.id.et_count)
        actvLocation = findViewById(R.id.et_location)
        btnAdd = findViewById(R.id.btn_add)

        intent.getSerializableExtra("grocery")?.let {
            val grocery = it as Grocery

            etName.setText(grocery.name)
            etCount.setText(grocery.count.toString())
            actvLocation.setText(grocery.location)
        }

        intent.getStringArrayListExtra("locations")?.let {
            // TODO: Yeah? Sending through the intent?
            // TODO: https://stackoverflow.com/questions/15544943/show-all-items-in-autocompletetextview-without-writing-text
            Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
            val adapter = ArrayAdapter(this, android.R.layout.select_dialog_item, it.distinct())
            actvLocation.threshold = 1
            actvLocation.setAdapter(adapter)
        }

        btnAdd.setOnClickListener {
            val name: String = etName.text.toString()
            val count: Long? = etCount.text.toString().toLongOrNull()
            val location: String = actvLocation.text.toString().ifBlank { Consts.LOCATION_UNKNOWN }

            if (name.isBlank() || count == null) {
                Snackbar.make(it, "Missing name or count", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val grocery = Grocery(name, count, location)

            val intent = Intent()
            intent.putExtra("grocery", grocery)

//            Toast.makeText(applicationContext, "Adding $name with count $count", Toast.LENGTH_LONG).show()
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}