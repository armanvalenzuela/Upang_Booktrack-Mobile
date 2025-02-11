package com.arc_templars.upangbooktrack

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2/UPBooktrack/" // Replace with your server's IP or domain
    //NOTE*** 10.0.1.1 PAG SA EMULATOR
    // PERO IF TRA TRY NYO SA PHONE USE THE IP ADDRESS OF THE SERVER (KUNG SAAN NAKA CONNECT) **

    private var retrofit: Retrofit? = null

    fun getRetrofitInstance(): Retrofit {
        if (retrofit == null) {
            // Create a Gson instance with lenient parsing
            val gson: Gson = GsonBuilder()
                .setLenient() // Enable lenient parsing
                .create()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // For plain string responses
                .addConverterFactory(GsonConverterFactory.create(gson)) // Use custom Gson instance
                .build()
        }
        return retrofit!!
    }
}