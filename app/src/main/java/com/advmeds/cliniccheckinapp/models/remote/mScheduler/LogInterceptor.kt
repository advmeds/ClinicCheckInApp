package com.advmeds.cliniccheckinapp.models.remote.mScheduler

import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import timber.log.Timber
import kotlin.system.measureTimeMillis

class LogInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        var requestString = ""
        val requestBody = request.body()
        requestBody?.let {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            requestString = buffer.readUtf8()
        }

        Timber.d(
            "[--> "
                .plus("request: ${request.method()} ${request.url()}")
                .plus("\nheader: ${request.headers()}")
                .plus(
                    if (request.headers()["Content-Type"]?.startsWith("multipart/form-data") == true) {
                        ""
                    } else {
                        "\nbody: $requestString"
                    }
                )
                .plus(" <--]")
        )

        val response: Response
        val timeTaken: Long
        try {
            timeTaken = measureTimeMillis {
                response = chain.proceed(request)
            }
        } catch (e: Exception) {
            Timber.e(e)
            throw e
        }

        val contentType = response.headers()["Content-Type"]
        contentType?.let {
            if (it == "video/mp4" || it == "application/zip" || it == "image/jpeg" || it == "image/png" || it == "application/pdf" || it == "application/vnd.android.package-archive" || it == "application/octet-stream") {
                Timber.d("[<-- download Url: ${request.url()}]")
                return response
            }
        }

        val responseBodyString = response.peekBody(1024 * 1024).string()

        Timber.d(
            "[--> "
                .plus("response: ${request.method()} ${response.code()} ${request.url()} (${timeTaken}ms)")
                .plus(
                    when (response.headers()["Content-Type"]) {
                        "text/html",
                        "video/mp4",
                        "image/jpeg",
                        "image/png" -> ""
                        else -> "\nbody: $responseBodyString"
                    }
                )
                .plus(" <--]")
        )

        return response
    }
}