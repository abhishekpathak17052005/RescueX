package com.rescuex.data.repository

import com.rescuex.data.model.EmergencyContact

interface ContactRepository {
    fun getContacts(): List<EmergencyContact>
    fun addContact(contact: EmergencyContact)
    fun deleteContact(id: String)
}

class MockContactRepository : ContactRepository {
    private val contacts = mutableListOf(
        EmergencyContact("1", "John Doe", "Brother", "+91 98765 43210"),
        EmergencyContact("2", "Jane Smith", "Spouse", "+91 91234 56789")
    )

    override fun getContacts(): List<EmergencyContact> = contacts
    override fun addContact(contact: EmergencyContact) { contacts.add(contact) }
    override fun deleteContact(id: String) { contacts.removeIf { it.id == id } }
}
