package de.luis.bulktracker.data

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import de.luis.bulktracker.data.db.AppDatabase
import de.luis.bulktracker.data.db.Migrations
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Wächter für die Datenhaltung: Es gibt keinen Cloud-Sync, ein Update ohne passende Migration
 * würde also alle Daten kosten. Diese Tests schlagen fehl, sobald VERSION erhöht wird, ohne
 * dass eine Migration und das exportierte Schema dazu existieren.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val dbName = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun jedeVersionsstufeHatEineMigration() {
        val covered = Migrations.ALL.map { it.startVersion to it.endVersion }
        (1 until AppDatabase.VERSION).forEach { version ->
            assertThat(covered).contains(version to version + 1)
        }
    }

    @Test
    fun datenAusVersion1UeberlebenAlleMigrationen() {
        helper.createDatabase(dbName, 1).apply {
            execSQL("INSERT INTO body_weight (date, kg) VALUES (20393, 82.4)")
            close()
        }

        val db = Room.databaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java, dbName)
            .addMigrations(*Migrations.ALL)
            .allowMainThreadQueries()
            .build()
        try {
            db.openHelper.writableDatabase.query("SELECT kg FROM body_weight").use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getDouble(0)).isEqualTo(82.4)
            }
        } finally {
            db.close()
        }
    }
}
