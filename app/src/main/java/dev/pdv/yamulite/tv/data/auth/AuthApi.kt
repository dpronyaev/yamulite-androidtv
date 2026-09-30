package dev.pdv.yamulite.tv.data.auth

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi {
    @FormUrlEncoded
    @POST("device/code")
    suspend fun requestCode(
        @Field("client_id") clientId: String,
        @Field("device_id") deviceId: String,
        @Field("device_name") deviceName: String,
    ): DeviceCodeResponse

    @FormUrlEncoded
    @POST("token")
    suspend fun pollToken(
        @Field("grant_type") grantType: String,
        @Field("code") code: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
    ): Response<TokenResponse>

    @FormUrlEncoded
    @POST("token")
    suspend fun refreshToken(
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
    ): Response<TokenResponse>
}
