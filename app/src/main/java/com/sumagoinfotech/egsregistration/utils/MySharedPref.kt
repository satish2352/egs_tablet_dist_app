package com.sumagoinfotech.egsregistration.utils


import android.content.Context
import android.content.SharedPreferences

class MySharedPref(context: Context) {
    companion object {
        const val KEY_STATUS = "status"
        const val KEY_ID = "id"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        const val KEY_ROLE_ID = "role_id"
        const val KEY_F_NAME = "f_name"
        const val KEY_M_NAME = "m_name"
        const val KEY_L_NAME = "l_name"
        const val KEY_NUMBER = "number"
        const val KEY_IMEI_NO = "imei_no"
        const val KEY_AADHAR_NO = "aadhar_no"
        const val KEY_ADDRESS = "address"
        const val KEY_STATE = "state"
        const val KEY_DISTRICT = "district"
        const val KEY_TALUKA = "taluka"
        const val KEY_VILLAGE = "village"
        const val KEY_PINCODE = "pincode"
        const val KEY_IP_ADDRESS = "ip_address"
        const val KEY_OTP = "otp"
        const val KEY_USER_AGENT = "user_agent"
        const val KEY_USER_PROFILE = "user_profile"
        const val KEY_REMEMBER_TOKEN = "remember_token"
        const val KEY_IS_ACTIVE = "is_active"
        const val KEY_CREATED_AT = "created_at"
        const val KEY_UPDATED_AT = "updated_at"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val KEY_OFFICER_DISTRICT = "officer_district"
        const val KEY_DEVICE_ID = "deviceId"
        const val KEY_USER_DISTRICT = "user_district"
        const val KEY_USER_TALUKA = "user_taluka"
        const val KEY_USER_VILLAGE = "user_village"
    }

    val KEY_ALL_AREA_ENTRIES = "all_area_entries"
    val KEY_IS_LOGGED_IN = "is_logged_in"
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)



    fun setUserDistrictId(districtId: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_USER_DISTRICT, districtId)
            apply()
        }
    }
    fun getUserDistrictId(): String? {
        return sharedPreferences.getString(
            KEY_USER_DISTRICT,
            "0"
        )
    }
    fun setUserTalukaId(talukaId: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_USER_TALUKA, talukaId)
            apply()
        }
    }
    fun getUserTalukaId(): String? {
        return sharedPreferences.getString(
            KEY_USER_TALUKA,
            "0"
        )
    }
    fun setUserVillageId(villageId: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_USER_VILLAGE, villageId)
            apply()
        }
    }
    fun getUserVillageId(): String? {
        return sharedPreferences.getString(
            KEY_USER_VILLAGE,
            "0"
        )
    }


    fun setOfficerDistrictID(districtId: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_OFFICER_DISTRICT, districtId)
            apply()
        }
    }
    fun getOfficerDistrictId(): String? {
        return sharedPreferences.getString(
            KEY_OFFICER_DISTRICT,
            "0"
        )
    }

    fun setDeviceId(deviceId: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_DEVICE_ID, deviceId)
            apply()
        }
    }
    fun getDeviceId(): String? {
        return sharedPreferences.getString(
            KEY_DEVICE_ID,
            "0"
        )
    }


    // Custom method to set boolean value for all area entries
    fun setAllAreaEntries(value: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(KEY_ALL_AREA_ENTRIES, value)
            apply()
        }
    }

    fun setLatitude(latitude: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_LATITUDE, latitude)
            apply()
        }
    }
    fun setLongitude(longitude: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_LONGITUDE, longitude)
            apply()
        }
    }
    fun getLatitude(): String? {
        return sharedPreferences.getString(
            KEY_LATITUDE,
            "0.0"
        )
    }
    fun getLongitude(): String? {
        return sharedPreferences.getString(
            KEY_LONGITUDE,
            "0.0"
        )
    }

    // Custom method to get boolean value for all area entries
    fun getAllAreaEntries(): Boolean {
        return sharedPreferences.getBoolean(
            KEY_ALL_AREA_ENTRIES,
            false
        )
    }

    fun setIsLoggedIn(status: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(KEY_IS_LOGGED_IN, status)
            apply()
        }
    }

    fun getIsLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setStatus(status: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_STATUS, status)
            apply()
        }
    }

    fun getStatus(): String? {
        return sharedPreferences.getString(KEY_STATUS, null)
    }

    fun setId(id: Int) {
        with(sharedPreferences.edit()) {
            putInt(KEY_ID, id)
            apply()
        }
    }

    fun getId(): Int {
        return sharedPreferences.getInt(KEY_ID, -1)
    }

    fun setEmail(email: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_EMAIL, email)
            apply()
        }
    }

    fun getEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }

    fun setFName(fname: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_F_NAME, fname)
            apply()
        }
    }

    fun getFName(): String? {
        return sharedPreferences.getString(KEY_F_NAME, null)
    }

    fun setMName(mname: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_M_NAME, mname)
            apply()
        }
    }

    fun getMName(): String? {
        return sharedPreferences.getString(KEY_M_NAME, null)
    }

    fun setLName(lname: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_L_NAME, lname)
            apply()
        }
    }

    fun getLName(): String? {
        return sharedPreferences.getString(KEY_L_NAME, null)
    }

    fun setNumber(number: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_NUMBER, number)
            apply()
        }
    }

    fun getNumber(): String? {
        return sharedPreferences.getString(KEY_NUMBER, null)
    }

    fun setIMEINo(imeiNo: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_IMEI_NO, imeiNo)
            apply()
        }
    }

    fun getIMEINo(): String? {
        return sharedPreferences.getString(KEY_IMEI_NO, null)
    }

    fun setAadharNo(aadharNo: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_AADHAR_NO, aadharNo)
            apply()
        }
    }

    fun getAadharNo(): String? {
        return sharedPreferences.getString(KEY_AADHAR_NO, null)
    }

    fun setAddress(address: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_ADDRESS, address)
            apply()
        }
    }

    fun getAddress(): String? {
        return sharedPreferences.getString(KEY_ADDRESS, null)
    }

    fun setState(state: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_STATE, state)
            apply()
        }
    }

    fun getState(): String? {
        return sharedPreferences.getString(KEY_STATE, null)
    }

    fun setDistrict(district: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_DISTRICT, district)
            apply()
        }
    }

    fun getDistrict(): String? {
        return sharedPreferences.getString(KEY_DISTRICT, null)
    }

    fun setTaluka(taluka: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_TALUKA, taluka)
            apply()
        }
    }

    fun getTaluka(): String? {
        return sharedPreferences.getString(KEY_TALUKA, null)
    }

    fun setVillage(village: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_VILLAGE, village)
            apply()
        }
    }

    fun getVillage(): String? {
        return sharedPreferences.getString(KEY_VILLAGE, null)
    }

    fun setPincode(pincode: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_PINCODE, pincode)
            apply()
        }
    }

    fun getPincode(): String? {
        return sharedPreferences.getString(KEY_PINCODE, null)
    }

    fun setRememberToken(token: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_REMEMBER_TOKEN, token)
            apply()
        }
    }

    fun getRememberToken(): String? {
        return sharedPreferences.getString(KEY_REMEMBER_TOKEN, null)
    }

    fun setIsActive(isActive: Int) {
        with(sharedPreferences.edit()) {
            putInt(KEY_IS_ACTIVE, isActive)
            apply()
        }
    }

    fun getIsActive(): Int {
        return sharedPreferences.getInt(KEY_IS_ACTIVE, 0)
    }

    fun getRoleId():Int{
        return sharedPreferences.getInt(KEY_ROLE_ID, 0)
    }

    fun setRoleId(roleId: Int) {
        with(sharedPreferences.edit()) {
            putInt(KEY_ROLE_ID, roleId)
            apply()
        }
    }

    fun clearAll(){
        sharedPreferences.edit().clear().commit()
    }
}
