package com.dreamweddingstories.tv.utils

object Constants {
    const val VIMEO_BASE_URL = "https://api.vimeo.com/"
    const val VIMEO_ACCESS_TOKEN = "e10762aed7e664273dacce1c3d2b80b0"

    const val FIREBASE_USERS_COLLECTION = "users"
    const val FIREBASE_VIDEOS_COLLECTION = "videos"
    const val FIREBASE_WEDDINGS_COLLECTION = "weddings"   // doc ID = access code

    const val SESSION_PREFS_NAME = "dws_session"
    const val SESSION_KEY_CODE = "saved_code"

    const val SPLASH_DELAY_MS = 2500L
    const val PLAYER_CONTROLS_HIDE_DELAY_MS = 3000L
    const val SEEK_INTERVAL_MS = 10_000L
}
