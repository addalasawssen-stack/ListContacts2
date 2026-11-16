package com.example.listcontacts2.data

import com.example.listcontacts2.model.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ContactRepository {
    private val initialContacts = listOf(
        Contact(
            id = "1",
            nom = "Dupont",
            prenom = "Jean",
            telephone = "06 12 34 56 78",
            email = "jean.dupont@email.com",
            photoUri = null
        ),
        Contact(
            id = "2",
            nom = "Martin",
            prenom = "Marie",
            telephone = "06 98 76 54 32",
            email = "marie.martin@email.com",
            photoUri = null
        ),
        Contact(
            id = "3",
            nom = "Durand",
            prenom = "Pierre",
            telephone = "07 11 22 33 44",
            email = "pierre.durand@email.com",
            photoUri = null
        ),
        Contact(
            id = "4",
            nom = "Bernard",
            prenom = "Sophie",
            telephone = "06 55 44 33 22",
            email = "sophie.bernard@email.com",
            photoUri = null
        ),
        Contact(
            id = "5",
            nom = "Petit",
            prenom = "Lucas",
            telephone = "07 88 99 00 11",
            email = "lucas.petit@email.com",
            photoUri = null
        )
    )

    private val _contacts = MutableStateFlow<List<Contact>>(initialContacts)
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    fun getContactById(id: String): Contact? {
        return _contacts.value.find { it.id == id }
    }

    fun saveOrUpdateContact(contact: Contact) {
        val currentList = _contacts.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == contact.id }
        if (index >= 0) {
            currentList[index] = contact
        } else {
            currentList.add(0, contact)
        }
        _contacts.value = currentList
    }

    fun deleteContact(id: String) {
        _contacts.value = _contacts.value.filter { it.id != id }
    }
}
