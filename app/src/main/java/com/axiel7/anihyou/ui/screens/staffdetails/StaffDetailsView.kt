package com.axiel7.anihyou.ui.screens.staffdetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.anihyou.ui.composables.DefaultScaffoldWithSmallTopAppBar
import com.axiel7.anihyou.ui.composables.SegmentedButtons
import com.axiel7.anihyou.ui.composables.common.BackIconButton
import com.axiel7.anihyou.ui.composables.common.FavoriteIconButton
import com.axiel7.anihyou.ui.composables.common.ShareIconButton
import com.axiel7.anihyou.ui.screens.mediadetails.edit.EditMediaSheet
import com.axiel7.anihyou.ui.screens.staffdetails.content.StaffCharacterView
import com.axiel7.anihyou.ui.screens.staffdetails.content.StaffInfoView
import com.axiel7.anihyou.ui.screens.staffdetails.content.StaffMediaView
import com.axiel7.anihyou.ui.theme.AniHyouTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDetailsView(
    navigateBack: () -> Unit,
    navigateToMediaDetails: (Int) -> Unit,
    navigateToCharacterDetails: (Int) -> Unit,
    navigateToFullscreenImage: (String) -> Unit,
) {
    val viewModel: StaffDetailsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val editSheetState = rememberModalBottomSheetState()

    if (editSheetState.isVisible) {
        uiState.selectedMediaItem?.second?.value?.node?.let { node ->
            EditMediaSheet(
                sheetState = editSheetState,
                mediaDetails = node.basicMediaDetails,
                listEntry = node.mediaListEntry?.basicMediaListEntry,
                onDismiss = { updatedListEntry ->
                    scope.launch {
                        viewModel.onUpdateListEntry(updatedListEntry)
                        editSheetState.hide()
                    }
                }
            )
        }
    }

    DefaultScaffoldWithSmallTopAppBar(
        title = "",
        navigationIcon = { BackIconButton(onClick = navigateBack) },
        actions = {
            FavoriteIconButton(
                isFavorite = uiState.details?.isFavourite ?: false,
                favoritesCount = uiState.details?.favourites ?: 0,
                onClick = {
                    viewModel.toggleFavorite()
                }
            )
            ShareIconButton(url = uiState.details?.siteUrl.orEmpty())
        },
        scrollBehavior = topAppBarScrollBehavior
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(
                    start = padding.calculateStartPadding(LocalLayoutDirection.current),
                    top = padding.calculateTopPadding(),
                    end = padding.calculateEndPadding(LocalLayoutDirection.current)
                )
        ) {
            SegmentedButtons(
                items = StaffInfoType.tabRows,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                selectedIndex = selectedTabIndex,
                onItemSelection = {
                    selectedTabIndex = it
                }
            )
            when (StaffInfoType.tabRows[selectedTabIndex].value) {
                StaffInfoType.INFO ->
                    StaffInfoView(
                        uiState = uiState,
                        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
                        contentPadding = PaddingValues(
                            bottom = padding.calculateBottomPadding()
                        ),
                        navigateToFullscreenImage = navigateToFullscreenImage
                    )

                StaffInfoType.MEDIA -> {
                    StaffMediaView(
                        staffMedia = viewModel.media,
                        isLoading = uiState.isLoadingMedia,
                        loadMore = viewModel::loadNextPageMedia,
                        mediaOnMyList = uiState.mediaOnMyList,
                        setMediaOnMyList = viewModel::setMediaOnMyList,
                        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
                        contentPadding = PaddingValues(
                            bottom = padding.calculateBottomPadding()
                        ),
                        showEditSheet = {
                            scope.launch {
                                viewModel.selectMediaItem(it)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                editSheetState.show()
                            }
                        },
                        navigateToMediaDetails = navigateToMediaDetails
                    )
                }

                StaffInfoType.CHARACTER -> {
                    StaffCharacterView(
                        staffCharacters = viewModel.characters,
                        isLoading = uiState.isLoadingCharacters,
                        loadMore = viewModel::loadNextPageCharacters,
                        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
                        contentPadding = PaddingValues(
                            bottom = padding.calculateBottomPadding()
                        ),
                        navigateToCharacterDetails = navigateToCharacterDetails
                    )
                }
            }
        }//: Column
    }
}

@Preview
@Composable
fun StaffDetailsViewPreview() {
    AniHyouTheme {
        Surface {
            StaffDetailsView(
                navigateBack = {},
                navigateToMediaDetails = {},
                navigateToCharacterDetails = {},
                navigateToFullscreenImage = {}
            )
        }
    }
}