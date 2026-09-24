package id.tilik.app.network

import id.tilik.app.model.VerifyResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface TilikApiService {

    @Multipart
    @POST("api/v1/verify")
    suspend fun verifyClaim(
        @Part image: MultipartBody.Part,
        @Part audio: MultipartBody.Part?,
        @Part("detected_ticker") detectedTicker: RequestBody?,
        @Part("extracted_text") extractedText: RequestBody?
    ): VerifyResponse
}
