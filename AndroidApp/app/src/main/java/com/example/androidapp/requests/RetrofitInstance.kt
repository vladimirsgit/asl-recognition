package com.example.androidapp.requests

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance{
//    private const val BASE_URL = "https://192.168.1.134:443/api/v1/" // bucuresti
//private const val BASE_URL = "https://192.168.1.7:443/api/v1/" // deva
    //private const val BASE_URL ="https://10.0.2.2:443/api/v1/" // for emulator
    private const val BASE_URL = "https://192.168.43.114:443/api/v1/" // mobile hotspot
    val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    val client = OkHttpClient.Builder().hostnameVerifier { hostname, _ -> hostname == "10.0.2.2" || hostname == "192.168.1.134"  || hostname == "192.168.1.7" || hostname == "192.168.43.114"}
        .addInterceptor(interceptor)
        .build()

    val clientBuilder: OkHttpClient.Builder =
        client.newBuilder().addInterceptor(interceptor)



    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(clientBuilder.build())
                .build()
    }
}