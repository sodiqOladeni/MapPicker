package dev.hackwithsodiq.mappicker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MarkerInfoWindowAdapter(private val context: Context,
                              private val title:String, private val address:String) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(marker: Marker?): View? {

        return LayoutInflater.from(context).inflate(R.layout.marker_content, null).apply {
            this.findViewById<TextView>(R.id.text_view_title).text = title
            this.findViewById<TextView>(R.id.text_view_address).text = address
        }
    }

    override fun getInfoWindow(marker: Marker?): View? {
        // Return null to indicate that the
        // default window (white bubble) should be used
        return null
    }
}