package com.sumagoinfotech.egsregistration.webservice
import com.sumagoinfotech.egsregistration.model.LoginModel
import com.sumagoinfotech.egsregistration.model.TabDistList.TabDistListModel
import com.sumagoinfotech.egsregistration.model.addTabInfo.AddTabInfo
import com.sumagoinfotech.egsregistration.model.areamaster.MasterUpdateModel
import com.sumagoinfotech.egsregistration.model.delete.DeleteModel
import com.sumagoinfotech.egsregistration.model.detailsbyid.UserDetailsModel
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
    ): Response<AddTabInfo>

    @POST("auth/list-tablet-distribution-info")
    suspend fun getTabDistributionList(
        @Query("start") startPageNumber: String,
        @Query("length") pageLength: String,
    ): Response<TabDistListModel>


    @POST("auth/adhar-card-exist")
    suspend fun checkIfAadharExists(
        @Query("adhar_card_number") aadharCardNumber: String,
    ): Response<LoginModel>

    @POST("list-masters-updated")
    fun fetchMastersDataTobeUpdated(): Call<MasterUpdateModel>

    @POST("auth/get-tablet-distribution-perticular-info")
    fun getBeneficiaryById(
        @Query("id") id: String,
    ): Call<UserDetailsModel>

    @POST("auth/get-tablet-distribution-perticular-delete")
    fun deleteBeneficiaryById(
        @Query("id") id: String,
    ): Call<DeleteModel>





}