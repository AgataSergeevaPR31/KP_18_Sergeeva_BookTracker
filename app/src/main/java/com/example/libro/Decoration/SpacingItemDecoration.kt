package com.example.libro.Decoration

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class SpacingItemDecoration(
    private val spacingPx: Int,
    private val context: Context
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)

        if (position == 0) {
            outRect.left = spacingPx
        }

        outRect.right = spacingPx
        outRect.top = spacingPx / 2
        outRect.bottom = spacingPx / 2
    }
}