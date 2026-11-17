package com.arffsnia.simap.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.arffsnia.simap.data.dto.Asset
import com.google.gson.Gson

/**
 * SQLite lokal (tanpa Room, supaya build ringan & deterministik).
 *   - assets: cache daftar/detail aset untuk mode offline.
 *   - pending_checks: antrian inspeksi yang belum tersinkron.
 */
class OfflineDb(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "simap.db", null, 1) {

    private val gson = Gson()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE assets (
                id INTEGER PRIMARY KEY,
                slug TEXT,
                nama TEXT,
                category TEXT,
                room TEXT,
                periode TEXT,
                berat TEXT,
                lokasi TEXT,
                tahun INTEGER,
                status TEXT,
                foto_url TEXT,
                updated_at TEXT,
                checks_json TEXT,
                cached_at INTEGER
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE pending_checks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                asset_id INTEGER,
                slug TEXT,
                nama TEXT,
                category_id INTEGER,
                category TEXT,
                result_json TEXT,
                foto_path TEXT,
                checked_at TEXT,
                latitude TEXT,
                longitude TEXT,
                synced INTEGER DEFAULT 0,
                created_at INTEGER
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS assets")
        db.execSQL("DROP TABLE IF EXISTS pending_checks")
        onCreate(db)
    }

    // ===== Cache aset =====
    fun cacheAssets(items: List<Asset>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.execSQL("DELETE FROM assets")
            val now = System.currentTimeMillis()
            for (a in items) {
                val cv = ContentValues().apply {
                    put("id", a.id)
                    put("slug", a.slug)
                    put("nama", a.nama)
                    put("category", a.category)
                    put("room", a.room)
                    put("periode", a.periode)
                    put("berat", a.berat)
                    put("lokasi", a.lokasi)
                    put("tahun", a.tahun)
                    put("foto_url", a.fotoUrl)
                    put("updated_at", a.updatedAt)
                    put("checks_json", gson.toJson(a.checks))
                    put("cached_at", now)
                }
                db.insertWithOnConflict("assets", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun cachedAssets(search: String? = null): List<Asset> {
        val db = readableDatabase
        val sql = if (search.isNullOrBlank()) {
            "SELECT * FROM assets ORDER BY nama"
        } else {
            "SELECT * FROM assets WHERE nama LIKE ? OR slug LIKE ? OR lokasi LIKE ? ORDER BY nama"
        }
        val args = if (search.isNullOrBlank()) null
        else arrayOf("%$search%", "%$search%", "%$search%")
        val list = mutableListOf<Asset>()
        db.rawQuery(sql, args).use { c ->
            while (c.moveToNext()) {
                list.add(
                    Asset(
                        id = c.getInt(c.getColumnIndexOrThrow("id")),
                        nama = c.getString(c.getColumnIndexOrThrow("nama")),
                        slug = c.getString(c.getColumnIndexOrThrow("slug")),
                        category = c.getString(c.getColumnIndexOrThrow("category")),
                        room = c.getString(c.getColumnIndexOrThrow("room")),
                        periode = c.getString(c.getColumnIndexOrThrow("periode")),
                        berat = c.getString(c.getColumnIndexOrThrow("berat")),
                        lokasi = c.getString(c.getColumnIndexOrThrow("lokasi")),
                        tahun = c.getInt(c.getColumnIndexOrThrow("tahun")),
                        coords = null,
                        checks = gson.fromJson(
                            c.getString(c.getColumnIndexOrThrow("checks_json")),
                            com.arffsnia.simap.data.dto.Checks::class.java
                        ),
                        fotoUrl = c.getString(c.getColumnIndexOrThrow("foto_url")),
                        qrcode = null,
                        updatedAt = c.getString(c.getColumnIndexOrThrow("updated_at")),
                    )
                )
            }
        }
        return list
    }

    fun hasCachedAssets(): Boolean {
        val db = readableDatabase
        db.rawQuery("SELECT COUNT(*) FROM assets", null).use { c ->
            return c.moveToFirst() && c.getInt(0) > 0
        }
    }

    // ===== Antrian inspeksi =====
    data class PendingCheck(
        val id: Long,
        val assetId: Int,
        val slug: String?,
        val nama: String?,
        val categoryId: Int,
        val category: String?,
        val resultJson: String,
        val fotoPath: String?,
        val checkedAt: String,
        val latitude: String?,
        val longitude: String?,
        val synced: Boolean,
    )

    fun queueCheck(
        assetId: Int,
        slug: String?,
        nama: String?,
        categoryId: Int,
        category: String?,
        result: Map<String, String>,
        fotoPath: String?,
        checkedAt: String,
        latitude: String?,
        longitude: String?,
    ): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("asset_id", assetId)
            put("slug", slug)
            put("nama", nama)
            put("category_id", categoryId)
            put("category", category)
            put("result_json", gson.toJson(result))
            put("foto_path", fotoPath)
            put("checked_at", checkedAt)
            put("latitude", latitude)
            put("longitude", longitude)
            put("synced", 0)
            put("created_at", System.currentTimeMillis())
        }
        return db.insert("pending_checks", null, cv)
    }

    fun pendingChecks(onlyUnsynced: Boolean = true): List<PendingCheck> {
        val db = readableDatabase
        val where = if (onlyUnsynced) "WHERE synced = 0" else ""
        val list = mutableListOf<PendingCheck>()
        db.rawQuery("SELECT * FROM pending_checks $where ORDER BY created_at", null).use { c ->
            while (c.moveToNext()) {
                list.add(
                    PendingCheck(
                        id = c.getLong(c.getColumnIndexOrThrow("id")),
                        assetId = c.getInt(c.getColumnIndexOrThrow("asset_id")),
                        slug = c.getString(c.getColumnIndexOrThrow("slug")),
                        nama = c.getString(c.getColumnIndexOrThrow("nama")),
                        categoryId = c.getInt(c.getColumnIndexOrThrow("category_id")),
                        category = c.getString(c.getColumnIndexOrThrow("category")),
                        resultJson = c.getString(c.getColumnIndexOrThrow("result_json")),
                        fotoPath = c.getString(c.getColumnIndexOrThrow("foto_path")),
                        checkedAt = c.getString(c.getColumnIndexOrThrow("checked_at")),
                        latitude = c.getString(c.getColumnIndexOrThrow("latitude")),
                        longitude = c.getString(c.getColumnIndexOrThrow("longitude")),
                        synced = c.getInt(c.getColumnIndexOrThrow("synced")) == 1,
                    )
                )
            }
        }
        return list
    }

    fun pendingCount(): Int {
        val db = readableDatabase
        db.rawQuery("SELECT COUNT(*) FROM pending_checks WHERE synced = 0", null).use { c ->
            return if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    fun markSynced(id: Long) {
        writableDatabase.execSQL("UPDATE pending_checks SET synced = 1 WHERE id = ?", arrayOf(id))
    }

    fun deletePending(id: Long) {
        writableDatabase.execSQL("DELETE FROM pending_checks WHERE id = ?", arrayOf(id))
    }
}