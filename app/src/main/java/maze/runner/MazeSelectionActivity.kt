package maze.runner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class MazeSelectionActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maze_selection)

        val username = intent.getStringExtra(MainActivity.EXT_USERNAME)

        val usernameTextView = findViewById<TextView>(R.id.nameTextView)

        usernameTextView.text = username

        val client = OkHttpClient()
        val host = "https://uqlxpr9zb2.execute-api.ap-northeast-2.amazonaws.com/default/Maze_Maps"

        val req = Request.Builder().url(host).build()

        client.newCall(req).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use{
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }

                    val data = response.body!!.string()
                    val typeToken = object : TypeToken<ArrayList<MazeList>>() {}.type
                    val mazeArrayList = Gson().fromJson<ArrayList<MazeList>>(data, typeToken)

                    CoroutineScope(Dispatchers.Main).launch {
                        val listAdapter = MazeListAdapter(mazeArrayList, this@MazeSelectionActivity)
                        var listView = findViewById<ListView>(R.id.mazeListView)
                        listView.adapter = listAdapter
                    }
                }
            }
        })
    }
}