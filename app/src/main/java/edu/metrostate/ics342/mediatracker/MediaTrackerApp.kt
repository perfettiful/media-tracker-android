package edu.metrostate.ics342.mediatracker

import android.app.Application
import edu.metrostate.ics342.mediatracker.data.TokenStore

class MediaTrackerApp : Application() {

    // one token store for the whole app, datastore wants a single instance
    lateinit var tokenStore: TokenStore
        private set

    override fun onCreate() {
        super.onCreate()
        tokenStore = TokenStore(this)
    }
}
