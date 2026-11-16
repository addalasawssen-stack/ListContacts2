package com.example.listcontacts2.model

import android.os.Parcel
import android.os.Parcelable
import java.util.UUID

data class Contact(
    val id: String = UUID.randomUUID().toString(),
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String,
    val photoUri: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: UUID.randomUUID().toString(),
        nom = parcel.readString() ?: "",
        prenom = parcel.readString() ?: "",
        telephone = parcel.readString() ?: "",
        email = parcel.readString() ?: "",
        photoUri = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nom)
        parcel.writeString(prenom)
        parcel.writeString(telephone)
        parcel.writeString(email)
        parcel.writeString(photoUri)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Contact> {
        override fun createFromParcel(parcel: Parcel): Contact = Contact(parcel)
        override fun newArray(size: Int): Array<Contact?> = arrayOfNulls(size)
    }

    val fullName: String
        get() = "$prenom $nom".trim()

    val initials: String
        get() {
            val p = prenom.take(1).uppercase()
            val n = nom.take(1).uppercase()
            return if (p.isNotEmpty() || n.isNotEmpty()) "$p$n" else "?"
        }
}
