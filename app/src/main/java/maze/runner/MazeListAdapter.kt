package maze.runner

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity

data class MazeList(var name: String?= null, var size: Int?= null)

class MazeListAdapter(val items: ArrayList<MazeList>, val context: Context) : BaseAdapter() {
    companion object{
        const val EXT_MAZE_NAME = "extra_key_maze_name"    
    }
    
    override fun getCount(): Int {
        return items!!.size
    }

    override fun getItem(p0: Int): Any {
        return items!!.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val inflater : LayoutInflater = LayoutInflater.from(context)
        val generatedView = inflater.inflate(R.layout.maze_entry, null)

        val mazeNameTextView = generatedView.findViewById<TextView>(R.id.mazeNameTextView)
        val mazeSizeTextView = generatedView.findViewById<TextView>(R.id.mazeSizeTextView)
        val mazeStartButton = generatedView.findViewById<Button>(R.id.startButton)

        mazeNameTextView.setText(items.get(p0).name)
        mazeSizeTextView.setText(items.get(p0).size.toString())

        mazeStartButton.setOnClickListener {
            val intentMazeActivity = Intent(context, MazeActivity::class.java).apply {
                putExtra(EXT_MAZE_NAME, items.get(p0).name)
            }

            startActivity(context, intentMazeActivity, null)
        }

        return generatedView
    }

}