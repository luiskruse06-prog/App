package de.luis.bulktracker.data.db

import androidx.room.migration.Migration

/**
 * Alle Schema-Migrationen. Es gibt BEWUSST kein `fallbackToDestructiveMigration()`:
 * Die Daten existieren nur auf diesem Handy (kein Cloud-Sync), ein vergessener
 * Migrationsschritt darf deshalb nie still alles löschen – lieber stürzt die App
 * im Test ab. `MigrationTest` prüft, dass jede Versionsstufe abgedeckt ist.
 *
 * Neue Version: `AppDatabase.VERSION` erhöhen, bauen (Schema-JSON entsteht unter
 * app/schemas), hier `Migration(n, n + 1)` ergänzen, Test dafür schreiben.
 */
object Migrations {
    val ALL: Array<Migration> = arrayOf()
}
