package tech.picker.mappicker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import dev.hackwithsodiq.mappicker.K
import dev.hackwithsodiq.mappicker.Location
import dev.hackwithsodiq.mappicker.MapPickerActivity

class MainActivity : AppCompatActivity() {

    private val PICKER_REQUEST_CODE = 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_picker).setOnClickListener {
            val pickerIntent = Intent(this, MapPickerActivity::class.java)
            startActivityForResult(pickerIntent, PICKER_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICKER_REQUEST_CODE){
            if (requestCode == RESULT_OK){
                // Location to use -> Latlong, name and address
                val location = data?.getParcelableExtra<Location>(K.LOCATION_DATA)
                Log.v("MainActivity", location.toString())
            }else{
                // If result is not okay, get error message
                val errorMessage = data?.getStringExtra(K.LOCATION_ERROR)
                Log.v("MainActivity", errorMessage ?: "Unknown error")
            }
        }
    }
}