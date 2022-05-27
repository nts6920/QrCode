package com.nhnextsoft.qrcode.ui.screen.scanresult

import com.nhnextsoft.qrcode.model.data.Product
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import org.jetbrains.annotations.NotNull
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import javax.inject.Singleton
import retrofit2.converter.gson.GsonConverterFactory


interface ProductResult {

    @GET("/api/products")
    fun getProduct() : Call<List<Product>>

    companion object {
        @Singleton
        var BASE_URL = "http://192.168.76.243:3000"

        fun create() : ProductResult {

            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(ProductResult::class.java)

        }
    }
    //
}