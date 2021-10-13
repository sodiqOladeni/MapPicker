package dev.hackwithsodiq.mappicker

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Location(
    val latLng: LatLng, val name:String, val address:String
):Parcelable