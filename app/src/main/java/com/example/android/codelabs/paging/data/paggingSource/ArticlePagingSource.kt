package com.example.android.codelabs.paging.data.paggingSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.android.codelabs.paging.data.Article
import kotlinx.coroutines.delay
import java.lang.StrictMath.max
import java.time.LocalDateTime

private val firstArticleCreatedTime = LocalDateTime.now()
// to simulate real delay
private const val LOAD_DELAY_MILLIS = 1_500L


class ArticlePagingSource : PagingSource<Int, Article>() {

    private val STARTING_KEY =0

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {


        // Start paging with the STARTING_KEY if this is the first load
        val start = params.key ?: STARTING_KEY
        // Load as many items as hinted by params.loadSize
        val range = start.until(start + params.loadSize)

        if (start != STARTING_KEY) delay(LOAD_DELAY_MILLIS)

        return LoadResult.Page(
            data = range.map { number ->
                Article(
                    // Generate consecutive increasing numbers as the article id
                    id = number,
                    title = "Article $number",
                    description = "This describes article $number",
                    created = firstArticleCreatedTime.minusDays(number.toLong())
                )
            },

            // Make sure we don't try to load items behind the STARTING_KEY
            prevKey = when (start) {
                STARTING_KEY -> null
                else -> ensureValidKey(key = range.first - params.loadSize)
            },
            nextKey = range.last + 1
        )
    }

    private fun ensureValidKey(key: Int): Int {
        return max(STARTING_KEY, key)
    }

    // The refresh key is used for the initial load of the next PagingSource, after invalidation
    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        // In our case we grab the item closest to the anchor position
        // then return its id - (state.config.pageSize / 2) as a buffer
        val anchorPosition = state.anchorPosition ?: return null
        val article = state.closestItemToPosition(anchorPosition) ?: return null
        return ensureValidKey(key = article.id - (state.config.pageSize / 2))
    }
}