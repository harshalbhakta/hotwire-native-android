package dev.hotwire.core.turbo.http

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import dev.hotwire.core.config.HotwireCore
import dev.hotwire.core.logging.logEvent
import dev.hotwire.core.turbo.session.Session
import dev.hotwire.core.turbo.util.isHttpGetRequest

internal class TurboWebViewRequestInterceptor(val session: Session) {
    private val offlineRequestHandler get() = HotwireCore.config.offlineRequestHandler
    private val httpRepository get() = session.httpRepository
    private val currentVisit get() = session.currentVisit

    fun interceptRequest(request: WebResourceRequest): WebResourceResponse? {
        val requestHandler = offlineRequestHandler ?: return null

        if (!shouldInterceptRequest(request)) {
            return null
        }

        val url = request.url.toString()
        val isCurrentVisitRequest = url == currentVisit?.location
        val result = httpRepository.fetch(requestHandler, request)

        return if (isCurrentVisitRequest) {
            logCurrentVisitResult(url, result)
            currentVisit?.completedOffline = result.offline

            // If the request resulted in a redirect, don't return the response. This
            // lets the WebView handle the request/response and Turbo can see the redirect,
            // so a redirect "replace" visit can be proposed.
            when (result.redirectToLocation) {
                null -> result.response
                else -> null
            }
        } else {
            result.response
        }
    }

    private fun shouldInterceptRequest(request: WebResourceRequest): Boolean {
        return request.isHttpGetRequest()
    }

    private fun logCurrentVisitResult(url: String, result: TurboHttpRepository.Result) {
        logEvent(
            "location" to url,
            "redirectToLocation" to result.redirectToLocation.toString(),
            "statusCode" to (result.response?.statusCode ?: "<none>"),
            "completedOffline" to result.offline
        )
    }

    private fun logEvent(vararg params: Pair<String, Any>) {
        val attributes = params.toMutableList().apply {
            add(0, "session" to session.sessionName)
        }
        logEvent("interceptRequest", attributes)
    }
}
