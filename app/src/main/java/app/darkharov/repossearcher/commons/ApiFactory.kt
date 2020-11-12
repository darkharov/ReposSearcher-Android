package app.darkharov.repossearcher.commons

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiFactory {

    private const val timeout = 30L

    inline fun <reified API : Any> create(baseUrl: String): API =
        builder()
            .baseUrl(baseUrl)
            .build()
            .create(API::class.java)

    fun builder() =
        provideRetrofitBuilder(
            provideOkHttpClient(
                provideLoggingInterceptor()
            ),
            provideGson()
        )

    private fun provideLoggingInterceptor() =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    private fun provideOkHttpClient(logger: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(timeout, TimeUnit.SECONDS)
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .addInterceptor(logger)
            .build()

    private fun provideGson(): Gson = GsonBuilder().create()

    private fun provideRetrofitBuilder(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit.Builder =
        Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
}
