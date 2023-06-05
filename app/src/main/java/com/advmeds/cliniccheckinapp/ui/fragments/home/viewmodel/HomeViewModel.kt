package com.advmeds.cliniccheckinapp.ui.fragments.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferencesRepo = SharedPreferencesRepo.getInstance(getApplication())

    /** @see SharedPreferencesRepo.doctors */
    var doctors: Set<String>
        get() = sharedPreferencesRepo.doctors
        set(value) {
            sharedPreferencesRepo.doctors = value
        }

    /** @see SharedPreferencesRepo.rooms */
    var rooms: Set<String>
        get() = sharedPreferencesRepo.rooms
        set(value) {
            sharedPreferencesRepo.rooms = value
        }


    /** @see SharedPreferencesRepo.logoUrl */
    var logoUrl: String?
        get() = sharedPreferencesRepo.logoUrl
        set(value) {
            sharedPreferencesRepo.logoUrl = value
        }

    /** @see SharedPreferencesRepo.checkInItemList */
    var checkInItemList: List<EditCheckInItemDialog.EditCheckInItem>
        get() = sharedPreferencesRepo.checkInItemList
        set(value) {
            sharedPreferencesRepo.checkInItemList = value
        }

    /** @see SharedPreferencesRepo.password */
    var password: String
        get() = sharedPreferencesRepo.password
        set(value) {
            sharedPreferencesRepo.password = value
        }

    /** @see SharedPreferencesRepo.machineTitle */
    var machineTitle: String
        get() = sharedPreferencesRepo.machineTitle
        set(value) {
            sharedPreferencesRepo.machineTitle = value
        }

    /** @see SharedPreferencesRepo.queueingMachineSettingIsEnable */
    val queueingMachineSettingIsEnable: Boolean
        get() = sharedPreferencesRepo.queueingMachineSettingIsEnable

}