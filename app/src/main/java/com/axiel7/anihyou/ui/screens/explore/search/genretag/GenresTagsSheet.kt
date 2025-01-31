package com.axiel7.anihyou.ui.screens.explore.search.genretag

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.anihyou.R
import com.axiel7.anihyou.data.model.genre.GenresAndTagsForSearch
import com.axiel7.anihyou.data.model.genre.SelectableGenre
import com.axiel7.anihyou.data.model.genre.SelectableGenre.Companion.genreTagLocalized
import com.axiel7.anihyou.ui.common.TabRowItem
import com.axiel7.anihyou.ui.composables.SegmentedButtons
import com.axiel7.anihyou.ui.composables.common.ErrorTextButton
import com.axiel7.anihyou.ui.composables.common.TextTriCheckbox
import com.axiel7.anihyou.ui.theme.AniHyouTheme
import kotlinx.coroutines.launch

private enum class GenresTagsSheetTab {
    GENRES, TAGS;

    companion object {
        val tabRows = arrayOf(
            TabRowItem(value = GENRES, title = R.string.genres),
            TabRowItem(value = TAGS, title = R.string.tags),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GenresTagsSheet(
    sheetState: SheetState,
    bottomPadding: Dp = 0.dp,
    externalGenre: SelectableGenre?,
    externalTag: SelectableGenre?,
    onDismiss: (GenresAndTagsForSearch) -> Unit,
) {
    val viewModel: GenresTagsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    SideEffect {
        scope.launch { if (isKeyboardVisible) sheetState.expand() }
    }

    // TODO: pass these by savedStateHandle?
    LaunchedEffect(externalGenre) {
        if (uiState.externalGenre == null)
            viewModel.setExternalGenre(externalGenre)
    }
    LaunchedEffect(externalTag) {
        if (uiState.externalTag == null)
            viewModel.setExternalTag(externalTag)
    }

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss(viewModel.genresAndTagsForSearch)
        },
        sheetState = sheetState,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .imePadding()
                .bringIntoViewRequester(bringIntoViewRequester)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ErrorTextButton(
                    text = stringResource(R.string.clear),
                    onClick = viewModel::unselectAllGenresAndTags,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                TextButton(
                    onClick = {
                        onDismiss(viewModel.genresAndTagsForSearch)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(text = stringResource(R.string.close))
                }
            }

            SegmentedButtons(
                items = GenresTagsSheetTab.tabRows,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                selectedIndex = selectedTabIndex,
                onItemSelection = {
                    selectedTabIndex = it
                }
            )

            OutlinedTextField(
                value = viewModel.filter,
                onValueChange = viewModel::onFilterChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(text = stringResource(R.string.filter))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.search_24),
                        contentDescription = stringResource(R.string.search)
                    )
                },
                trailingIcon = {
                    if (viewModel.filter.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onFilterChanged("") }) {
                            Icon(
                                painter = painterResource(R.drawable.cancel_24),
                                contentDescription = stringResource(R.string.clear)
                            )
                        }
                    }
                }
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(bottom = bottomPadding)
            ) {
                when (GenresTagsSheetTab.tabRows[selectedTabIndex].value) {
                    GenresTagsSheetTab.GENRES ->
                        items(viewModel.displayGenres) { item ->
                            TextTriCheckbox(
                                text = item.name.genreTagLocalized(),
                                state = item.state.toggleableState,
                                onStateChange = {
                                    viewModel.onGenreUpdated(item.setState(it))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                    GenresTagsSheetTab.TAGS ->
                        items(viewModel.displayTags) { item ->
                            TextTriCheckbox(
                                text = item.name,
                                state = item.state.toggleableState,
                                onStateChange = {
                                    viewModel.onTagUpdated(item.setState(it))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                }
            }
        }//: Column
    }//: Sheet
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun GenresTagsSheetPreview() {
    AniHyouTheme {
        Surface {
            GenresTagsSheet(
                externalGenre = null,
                externalTag = null,
                sheetState = rememberModalBottomSheetState(),
                onDismiss = { }
            )
        }
    }
}