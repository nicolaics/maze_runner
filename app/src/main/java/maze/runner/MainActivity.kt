package maze.runner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class MainActivity: AppCompatActivity() {
    companion object{
        const val EXT_USERNAME = "extra_key_username"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signInButton = findViewById<Button>(R.id.signInButton)
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)

        val client = OkHttpClient()

        signInButton.setOnClickListener {
            val username = usernameEditText.text.toString()

            val host = "http://swui.skku.edu:1399"
            val path = "/users"

            val json = Gson().toJson(Post(username))
            val mediaType = "application/json; charset=utf-8".toMediaType()

            val req = Request.Builder().url(host + path).post(json.toString().toRequestBody(mediaType)).build()

            client.newCall(req).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use{
                        if(!response.isSuccessful){
                            throw IOException("Unexpected code $response")
                        }

                        val successStr = response.body!!.string()

                        val successDataModel = Gson().fromJson(successStr, DataModel::class.java)

                        if(successDataModel.success == "true"){
                            val intent = Intent(applicationContext, MazeSelectionActivity::class.java).apply{
                                putExtra(EXT_USERNAME, username)
                            }

                            startActivity(intent)
                        }
                        else{
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(applicationContext, "Wrong User Name",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            })
        }
    }
}