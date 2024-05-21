package dev.hotwire.core.navigation.routing

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri
import dev.hotwire.core.lib.logging.logError
import dev.hotwire.core.navigation.activities.HotwireActivity
import dev.hotwire.core.navigation.session.NavigatorConfiguration

class BrowserRoute : Router.Route {
    override val name = "browser"

    override val result = Router.RouteResult.STOP

    override fun matches(
        location: String,
        configuration: NavigatorConfiguration
    ): Boolean {
        return configuration.startLocation.toUri().host != location.toUri().host
    }

    override fun handle(
        location: String,
        configuration: NavigatorConfiguration,
        activity: HotwireActivity
    ) {
        val intent = Intent(Intent.ACTION_VIEW, location.toUri())

        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            logError("BrowserRoute", e)
        }
    }
}
