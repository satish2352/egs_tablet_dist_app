package com.sumagoinfotech.egsregistration.webservice
import com.sumagoinfotech.egsregistration.model.LoginModel
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    // masters initial
    @POST("login")
    fun loginUser(
        @Query("email") email: String,
        @Query("password") password: String,
        @Query("device_id") deviceId: String="device_id",
    ): Call<LoginModel>


    @Multipart
    @POST("auth/add-tablet-info")
    suspend fun uploadLaborInfo(
        @Query("full_name") fullName: String,
        @Query("gram_panchayat_name") grampanchayatName: String,
        @Query("adhar_card_number") aadharNumber: String,
        @Query("village_id") villageId: String,
        @Query("taluka_id") talukaId: String,
        @Query("district_id") districtId: String,
        @Query("mobile_number") mobileNumber: String,
        @Query("longitude") longitude: String,
        @Query("latitude") latitude: String,
        @Part aadharFile: MultipartBody.Part,
        @Part photoFile: MultipartBody.Part,
        @Part gramsevakIdFile: MultipartBody.Part,
        @Part tabletImeiFile: MultipartBody.Part,
    ): Response<LoginModel>




}