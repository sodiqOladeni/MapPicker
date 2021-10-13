package tech.picker.mappicker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import dev.hackwithsodiq.mappicker.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_picker).setOnClickListener {
//            val pickerIntent = Intent(this, MapPickerActivity::class.java)
//            startActivity(pickerIntent)
        }
    }
}