package com.sipl.egstabdistribution.model.detailsbyid

data class Data(
    val aadhar_image: String,
    val district_id: Int,
    val district_name: String,
    val full_name: String,
    val gram_sevak_id_card_photo: String,
    val id: Int,
    val latitude: String,
    val longitude: String,
    val mobile_number: String,
    val photo_of_beneficiary: String,
    val photo_of_tablet_imei: String,
    val taluka_id: Int,
    val taluka_name: String,
    val village_id: Int,
    val village_name: String,
    val gram_panchayat_name: String,
    val adhar_card_number: Long
)