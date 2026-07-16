package com.example.miqatapp.core.constants

/** App-wide constants. Only truly global values belong here. */
object AppConst {
    const val APP_NAME = "Miqat"
    const val DATASTORE_FILE = "dev.miqat.preferences_pb"
    const val DATABASE_NAME = "dev.miqat.asdDDf.db"   // change this to start on a fresh (empty) db while testing
    const val DB_VERSION = 1                   // bump when an entity's schema changes
}
