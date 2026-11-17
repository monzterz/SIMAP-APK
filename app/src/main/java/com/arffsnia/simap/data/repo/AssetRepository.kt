package com.arffsnia.simap.data.repo

import com.arffsnia.simap.data.api.ApiClient
import com.arffsnia.simap.data.api.ApiService
import com.arffsnia.simap.data.db.OfflineDb
import com.arffsnia.simap.data.dto.Asset
import com.arffsnia.simap.data.dto.AssetDetail
import com.arffsnia.simap.data.dto.AssetsResponse
import com.arffsnia.simap.data.dto.ChecklistItem
import com.arffsnia.simap.data.dto.SimpleItem

class AssetRepository(
    private val api: ApiService,
    private val db: OfflineDb,
) {
    suspend fun assets(
        page: Int = 1,
        search: String? = null,
        status: String? = null,
        categoryId: Int? = null,
        roomId: Int? = null,
        periodeId: Int? = null,
    ): AppResult<AssetsResponse> {
        return try {
            val resp = api.assets(
                page = page, search = search, status = status,
                categoryId = categoryId, roomId = roomId, periodeId = periodeId,
            )
            // cache halaman pertama untuk mode offline
            if (page == 1 && search.isNullOrBlank()) db.cacheAssets(resp.items)
            AppResult.Success(resp)
        } catch (e: Exception) {
            // fallback ke cache offline
            val cached = db.cachedAssets(search)
            if (cached.isNotEmpty() || db.hasCachedAssets()) {
                AppResult.Success(
                    AssetsResponse(
                        page = 1, limit = cached.size, total = cached.size, items = cached,
                    )
                )
            } else {
                AppResult.Error(ApiClient.errorMessage(e))
            }
        }
    }

    suspend fun assetBySlug(slug: String): AppResult<AssetDetail> {
        return try {
            AppResult.Success(api.assetBySlug(slug))
        } catch (e: Exception) {
            AppResult.Error(ApiClient.errorMessage(e))
        }
    }

    suspend fun checklist(categoryId: Int): AppResult<List<ChecklistItem>> {
        return try {
            AppResult.Success(api.checklist(categoryId).items)
        } catch (e: Exception) {
            AppResult.Error(ApiClient.errorMessage(e))
        }
    }

    suspend fun categories(): List<SimpleItem> = runCatching { api.categories() }.getOrDefault(emptyList())

    suspend fun rooms(): List<SimpleItem> = runCatching { api.rooms() }.getOrDefault(emptyList())

    fun cached(search: String? = null): List<Asset> = db.cachedAssets(search)
}