package com.rescuex.viewmodel

import androidx.lifecycle.ViewModel
import com.rescuex.data.model.EmergencyContact
import com.rescuex.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ContactsViewModel(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val contacts: StateFlow<List<EmergencyContact>> = _contacts

    init {
        _contacts.value = contactRepository.getContacts()
    }

    fun addContact(contact: EmergencyContact) {
        contactRepository.addContact(contact)
        _contacts.value = contactRepository.getContacts()
    }

    fun deleteContact(id: String) {
        contactRepository.deleteContact(id)
        _contacts.value = contactRepository.getContacts()
    }
}
