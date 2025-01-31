package com.axiel7.anihyou.ui.screens.explore.season

import com.axiel7.anihyou.SeasonalAnimeQuery
import com.axiel7.anihyou.data.model.media.AnimeSeason
import com.axiel7.anihyou.ui.common.state.PagedUiState

data class SeasonAnimeUiState(
    val season: AnimeSeason? = null,
    val selectedItem: SeasonalAnimeQuery.Medium? = null,
    override val page: Int = 1,
    override val hasNextPage: Boolean = true,
    override val isLoading: Boolean = true,
    override val error: String? = null,
): PagedUiState<SeasonAnimeUiState> {
    override fun setError(value: String?) = copy(error = value)
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setPage(value: Int) = copy(page = value)
    override fun setHasNextPage(value: Boolean) = copy(hasNextPage = value)
}
