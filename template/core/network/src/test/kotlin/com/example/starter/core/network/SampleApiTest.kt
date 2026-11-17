package com.example.starter.core.network

import com.example.starter.core.network.model.SampleItemResponse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SampleApiTest {
    private val server = MockWebServer()
    private lateinit var api: SampleApi

    @Before
    fun setUp() {
        server.start()
        api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(SampleApi::class.java)
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `getItems parses response`() =
        runTest {
            server.enqueue(MockResponse.Builder().body("""[{"id":1,"title":"a"}]""").build())

            assertEquals(listOf(SampleItemResponse(1, "a")), api.getItems())
        }

    @Test
    fun `getItems throws HttpException on 500`() =
        runTest {
            server.enqueue(MockResponse.Builder().code(500).build())

            assertFailsWith<HttpException> { api.getItems() }
        }
}
