package com.termi.grotrack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar

class AddGroceriesActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etCount: EditText
    private lateinit var etLocation: EditText
    private lateinit var btnAdd: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_groceries)

        etName = findViewById(R.id.et_name)
        etCount = findViewById(R.id.et_count)
        etLocation = findViewById(R.id.et_location)
        btnAdd = findViewById(R.id.btn_add)

        btnAdd.setOnClickListener {
            val name: String = etName.text.toString()
            val count: Int? = etCount.text.toString().toIntOrNull()
            val location: String = etLocation.text.toString().ifBlank { "Unknown" }

            if (name.isBlank() || count == null) {
                Snackbar.make(it, "Missing name or count", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent()
            intent.putExtra("name", name)
            intent.putExtra("count", count)
            intent.putExtra("location", location)

//            Toast.makeText(applicationContext, "Adding $name with count $count", Toast.LENGTH_LONG).show()
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}