package maze.runner

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.*
import kotlin.math.roundToInt

class CellGridAdapter(var list: ArrayList<Cell>, val context: Context, val gridSize: Int,
                      val direction: Char, val curIndex : Int, val hintPosIndex : Int?) : BaseAdapter() {
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(p0: Int): Any {
        return list.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val inflater : LayoutInflater = LayoutInflater.from(context)
        val generatedView = inflater.inflate(R.layout.maze_cell, null)

        val cellImageView = generatedView.findViewById<ImageView>(R.id.cellImageView)
        val layoutParameter = cellImageView.layoutParams as ConstraintLayout.LayoutParams

        val dp = context.resources.displayMetrics.densityDpi.toDouble() / DisplayMetrics.DENSITY_DEFAULT.toDouble()

        val px = 350 * dp

        val margin = (dp * 3).roundToInt()
        val iconImageView = generatedView.findViewById<ImageView>(R.id.iconImageView)

        layoutParameter.height = (px.toFloat() / gridSize.toFloat()).roundToInt()
        layoutParameter.width = (px.toFloat() / gridSize.toFloat()).roundToInt()

        if(p0 == curIndex) {
            iconImageView.setImageResource(R.drawable.user)
        }
        else if(p0 == ((gridSize * gridSize) - 1)){
            iconImageView.setImageResource(R.drawable.goal)
        }
        else{
            iconImageView.visibility = View.GONE
        }

        if(hintPosIndex != null){
            if(p0 == hintPosIndex) {
                iconImageView.visibility = View.VISIBLE
                iconImageView.setImageResource(R.drawable.hint)
            }
        }

        if(list.get(p0).hasLeft){
            layoutParameter.width -= margin
        }

        if(list.get(p0).hasRight){
            layoutParameter.width -= margin
        }

        if(list.get(p0).hasBottom){
            layoutParameter.height -= margin
        }

        if(list.get(p0).hasTop){
            layoutParameter.height -= margin
        }

        var top = 0
        var bottom = 0
        var left = 0
        var right = 0

        when(list.get(p0).appearance) {
            // right = 1
            1 -> right = margin
            // bottom = 2
            2 -> bottom = margin
            // r, b = 1 + 2
            3 -> {
                right = margin
                bottom = margin
            }
            // left = 4
            4 -> left = margin
            // r, l = 1 + 4
            5 -> {
                right = margin
                left = margin
            }
            // b, l = 2 + 4
            6 -> {
                bottom = margin
                left = margin
            }
            // r, b, l = 1 + 2 + 4
            7 -> {
                right = margin
                bottom = margin
                left = margin
            }
            // top = 8
            8 -> top = margin
            // r, t = 1 + 8
            9 -> {
                right = margin
                top = margin
            }
            // t, b = 2 + 8
            10 -> {
                top = margin
                bottom = margin
            }
            // t, r, b = 1 + 2 + 8
            11 -> {
                top = margin
                right = margin
                bottom = margin
            }
            // t, l = 4 + 8
            12 -> {
                top = margin
                left = margin
            }
            // l, t, r = 1 + 4 + 8
            13 -> {
                left = margin
                top = margin
                right = margin
            }
            // b, l, t = 2 + 4 + 8
            14 -> {
                bottom = margin
                left = margin
                top = margin
            }
            // t, r, b, l = 1 + 2 + 4 + 8
            15 -> {
                bottom = margin
                left = margin
                top = margin
                right = margin
            }
        }

        layoutParameter.setMargins(left, top, right, bottom)

        cellImageView.layoutParams = layoutParameter

        val drawableState = iconImageView.drawable.constantState
        val resourceState = iconImageView.resources.getDrawable(R.drawable.goal).constantState

        if(p0 != ((gridSize * gridSize) - 1) || drawableState != resourceState) {
            when(direction) {
                'R' -> iconImageView.rotation = 90F
                'L' -> iconImageView.rotation = 270F
                'U' -> iconImageView.rotation = 0F
                'D' -> iconImageView.rotation = 180F
            }
        }

        return generatedView
    }
}