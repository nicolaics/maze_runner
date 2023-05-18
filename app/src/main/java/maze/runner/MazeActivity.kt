package maze.runner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MazeActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maze)

        val mazeName = intent.getStringExtra(MazeListAdapter.EXT_MAZE_NAME)

        val gridView = findViewById<GridView>(R.id.gridView)
        val turnTextView = findViewById<TextView>(R.id.turnTextView)
        val hintButton = findViewById<Button>(R.id.hintButton)
        val rightButton = findViewById<Button>(R.id.rightButton)
        val leftButton = findViewById<Button>(R.id.leftButton)
        val upButton = findViewById<Button>(R.id.upButton)
        val downButton = findViewById<Button>(R.id.downButton)

        val host = "http://swui.skku.edu:1399"
        val path = "/maze/map?name="

        val client = OkHttpClient()

        val req = Request.Builder().url(host + path + mazeName).build()

        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use{
                    if(!response.isSuccessful){
                        throw IOException("Unexpected code $response")
                    }

                    val data = response.body!!.string()

                    val jsonObjectData = JSONObject(data).get("maze").toString()

                    val gridSize = jsonObjectData.get(0).toString().toInt()

                    val cells = jsonObjectData.split(" ", "\n").toTypedArray().toCollection(ArrayList())
                    cells.removeIf {
                        x -> x in " "
                    }

                    val cellArrayList = ArrayList<Cell>()

                    for(i in 1 until cells.size){
                        var hasRight = false
                        var hasLeft = false
                        var hasTop = false
                        var hasBottom = false

                        when(cells.get(i).toInt()) {
                            // right = 1
                            1 -> hasRight = true
                            // bottom = 2
                            2 -> hasBottom = true
                            // r, b = 1 + 2
                            3 -> {
                                hasRight = true
                                hasBottom = true
                            }
                            // left = 4
                            4 -> hasLeft = true
                            // r, l = 1 + 4
                            5 -> {
                                hasRight = true
                                hasLeft = true
                            }
                            // b, l = 2 + 4
                            6 -> {
                                hasBottom = true
                                hasLeft = true
                            }
                            // r, b, l = 1 + 2 + 4
                            7 -> {
                                hasRight = true
                                hasBottom = true
                                hasLeft = true
                            }
                            // top = 8
                            8 -> hasTop = true
                            // r, t = 1 + 8
                            9 -> {
                                hasRight = true
                                hasTop = true
                            }
                            // t, b = 2 + 8
                            10 -> {
                                hasTop = true
                                hasBottom = true
                            }
                            // t, r, b = 1 + 2 + 8
                            11 -> {
                                hasTop = true
                                hasRight = true
                                hasBottom = true
                            }
                            // t, l = 4 + 8
                            12 -> {
                                hasTop = true
                                hasLeft = true
                            }
                            // l, t, r = 1 + 4 + 8
                            13 -> {
                                hasLeft = true
                                hasTop = true
                                hasRight = true
                            }
                            // b, l, t = 2 + 4 + 8
                            14 -> {
                                hasBottom = true
                                hasLeft = true
                                hasTop = true
                            }
                            // t, r, b, l = 1 + 2 + 4 + 8
                            15 -> {
                                hasBottom = true
                                hasLeft = true
                                hasTop = true
                                hasRight = true
                            }
                        }

                        cellArrayList.add(Cell(cells.get(i).toInt(), hasRight,
                                hasLeft, hasTop, hasBottom))
                    }

                    var currentPosition = Position(0, 0)

                    CoroutineScope(Dispatchers.Main).launch {
                        var direction = 'X'
                        var numberOfTurn = 0

                        var index = findIndex(currentPosition, gridSize)

                        gridView.numColumns = gridSize
                        var gridAdapter = CellGridAdapter(cellArrayList, this@MazeActivity,
                                            gridSize, direction, index, null)

                        gridView.adapter = gridAdapter

                        rightButton.setOnClickListener{
                            if(currentPosition.column != (gridSize - 1) &&
                                !cellArrayList.get(index).hasRight){
                                direction = 'R'

                                currentPosition.column++

                                numberOfTurn++

                                index = findIndex(currentPosition, gridSize)

                                turnTextView.text = "Turn : $numberOfTurn"

                                gridAdapter = CellGridAdapter(
                                    cellArrayList, this@MazeActivity,
                                    gridSize, direction, index, null)

                                gridView.adapter = gridAdapter

                                if(index == ((gridSize * gridSize) - 1)){
                                    Toast.makeText(this@MazeActivity, "Finish!",
                                        Toast.LENGTH_LONG).show()

                                    rightButton.setEnabled(false)
                                    leftButton.setEnabled(false)
                                    upButton.setEnabled(false)
                                    downButton.setEnabled(false)
                                }
                            }
                        }

                        leftButton.setOnClickListener{
                            if(currentPosition.column != 0 &&
                                !cellArrayList.get(index).hasLeft) {
                                direction = 'L'
                                numberOfTurn++

                                turnTextView.text = "Turn : $numberOfTurn"

                                currentPosition.column--

                                index = findIndex(currentPosition, gridSize)

                                gridAdapter = CellGridAdapter(
                                    cellArrayList, this@MazeActivity,
                                    gridSize, direction, index, null)

                                gridView.adapter = gridAdapter

                                if(index == ((gridSize * gridSize) - 1)){
                                    Toast.makeText(this@MazeActivity, "Finish!",
                                        Toast.LENGTH_LONG).show()

                                    rightButton.setEnabled(false)
                                    leftButton.setEnabled(false)
                                    upButton.setEnabled(false)
                                    downButton.setEnabled(false)
                                }
                            }
                        }

                        upButton.setOnClickListener {
                            if(currentPosition.row != 0 &&
                                !cellArrayList.get(index).hasTop) {
                                direction = 'U'
                                numberOfTurn++

                                turnTextView.text = "Turn : $numberOfTurn"

                                currentPosition.row--

                                index = findIndex(currentPosition, gridSize)

                                gridAdapter = CellGridAdapter(
                                    cellArrayList, this@MazeActivity,
                                    gridSize, direction, index, null)

                                gridView.adapter = gridAdapter

                                if(index == ((gridSize * gridSize) - 1)){
                                    Toast.makeText(this@MazeActivity, "Finish!",
                                        Toast.LENGTH_LONG).show()

                                    rightButton.setEnabled(false)
                                    leftButton.setEnabled(false)
                                    upButton.setEnabled(false)
                                    downButton.setEnabled(false)
                                }
                            }
                        }

                        downButton.setOnClickListener{
                            if(currentPosition.row != (gridSize - 1) &&
                                !cellArrayList.get(index).hasBottom) {
                                direction = 'D'
                                numberOfTurn++
                                turnTextView.text = "Turn : $numberOfTurn"

                                currentPosition.row++

                                index = findIndex(currentPosition, gridSize)

                                gridAdapter = CellGridAdapter(
                                    cellArrayList, this@MazeActivity,
                                    gridSize, direction, index, null)

                                gridView.adapter = gridAdapter

                                if(index == ((gridSize * gridSize) - 1)){
                                    Toast.makeText(this@MazeActivity, "Finish!",
                                        Toast.LENGTH_LONG).show()

                                    rightButton.setEnabled(false)
                                    leftButton.setEnabled(false)
                                    upButton.setEnabled(false)
                                    downButton.setEnabled(false)
                                }
                            }
                        }

                        hintButton.setOnClickListener{
                            val hintPosIndex = dijkstraAlgorithm(cellArrayList, gridSize, currentPosition)

                            gridAdapter = CellGridAdapter(
                                cellArrayList, this@MazeActivity,
                                gridSize, direction, index, hintPosIndex)

                            gridView.adapter = gridAdapter

                            hintButton.setEnabled(false)
                        }
                    }
                }
            }
        })
    }
}

fun dijkstraAlgorithm(cellArrayList : ArrayList<Cell>, gridSize: Int, initialPos: Position): Int {
    val posMatrix = fillMatrix(cellArrayList, gridSize)
    val setOfVertex = mutableSetOf<Int>()
    val mapOfLevels = mutableMapOf<Int, Level>()

    val INFINITE = 9999
    var curRow: Int
    var curColumn: Int

    var k = 0

    // initialize
    for(i in 0 until gridSize) {
        for(j in 0 until gridSize) {
            setOfVertex.add(k)
            mapOfLevels[k] = Level(INFINITE, Position(-1, -1))
            k++
        }
    }

    var top: Int
    var topAndCur: Int
    var left: Int
    var leftAndCur: Int
    var right: Int
    var rightAndCur: Int
    var bottom: Int
    var bottomAndCur: Int
    var curPos: Position

    var curVertex = findIndex(initialPos, gridSize)

    mapOfLevels[curVertex] = Level(0, Position(-1, -1))

    var counter = 0

    while(setOfVertex.contains(((gridSize * gridSize) - 1))) {
        var minimum = INFINITE
        counter++

        for(i in 0 until setOfVertex.size) {
            val index = setOfVertex.elementAt(i)

            if(minimum > mapOfLevels.getValue(index).value) {
                minimum = mapOfLevels.getValue(index).value
                curVertex = index
            }
        }

        setOfVertex.remove(curVertex)

        curPos = findRowAndColumn(curVertex, gridSize)
        curRow = curPos.row
        curColumn = curPos.column

        var nextVertex: Position

        when(posMatrix[curRow][curColumn].appearance) {
            // hasRight
            1 -> {
                if(curRow - 1 >= 0) {
                    nextVertex = Position((curRow - 1), curColumn)
                    top = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    topAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(topAndCur < top) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = topAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }

                if(curRow + 1 < gridSize) {
                    nextVertex = Position((curRow + 1), curColumn)
                    bottom = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    bottomAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(bottomAndCur < bottom) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = bottomAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }

                if(curColumn - 1 >= 0) {
                    nextVertex = Position(curRow, (curColumn - 1))
                    left = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    leftAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(leftAndCur < left) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = leftAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }
            }
            // hasBottom
            2 -> {
                if(curRow - 1 >= 0) {
                    nextVertex = Position((curRow - 1), curColumn)
                    top = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    topAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(topAndCur < top) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = topAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }

                if(curColumn + 1 < gridSize) {
                    nextVertex = Position(curRow, (curColumn + 1))
                    right = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    rightAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(rightAndCur < right) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = rightAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }

                if(curColumn - 1 >= 0) {
                    nextVertex = Position(curRow, (curColumn - 1))
                    left = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    leftAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(leftAndCur < left) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = leftAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }
            }
            // hasRight and hasBottom
            3 -> {
                if(curRow - 1 >= 0) {
                    nextVertex = Position((curRow - 1), curColumn)
                    top = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    topAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(topAndCur < top) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = topAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }

                if(curColumn - 1 >= 0) {
                    nextVertex = Position(curRow, (curColumn - 1))
                    left = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    leftAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(leftAndCur < left) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = leftAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }
            }
            // hasLeft
            4 -> {
                if(curRow - 1 >= 0) {
                    nextVertex = Position((curRow - 1), curColumn)
                    top = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    topAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(topAndCur < top) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = topAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }

                if(curRow + 1 < gridSize) {
                    nextVertex = Position((curRow + 1), curColumn)
                    bottom = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    bottomAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(bottomAndCur < bottom) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = bottomAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }

                if(curColumn + 1 < gridSize) {
                    nextVertex = Position(curRow, (curColumn + 1))
                    right = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    rightAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(rightAndCur < right) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = rightAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }
            }
            // hasRight & hasLeft
            5 -> {
                if(curRow - 1 >= 0) {
                    nextVertex = Position((curRow - 1), curColumn)
                    top = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    topAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(topAndCur < top) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = topAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }

                if(curRow + 1 < gridSize) {
                    nextVertex = Position((curRow + 1), curColumn)
                    bottom = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    bottomAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(bottomAndCur < bottom) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = bottomAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }
            }
            // hasBottom & hasLeft
            6 -> {
                if(curRow - 1 >= 0) {
                    nextVertex = Position((curRow - 1), curColumn)
                    top = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    topAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(topAndCur < top) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = topAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }

                if(curColumn + 1 < gridSize) {
                    nextVertex = Position(curRow, (curColumn + 1))
                    right = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    rightAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(rightAndCur < right) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = rightAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }
            }
            // hasRight, hasBottom, hasLeft
            7 -> {
                if(curRow - 1 >= 0) {
                    nextVertex = Position((curRow - 1), curColumn)
                    top = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    topAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(topAndCur < top) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = topAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }
            }
            // hasTop
            8 -> {
                if(curRow + 1 < gridSize) {
                    nextVertex = Position((curRow + 1), curColumn)
                    bottom = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    bottomAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(bottomAndCur < bottom) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = bottomAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }

                if(curColumn + 1 < gridSize) {
                    nextVertex = Position(curRow, (curColumn + 1))
                    right = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    rightAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(rightAndCur < right) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = rightAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }

                if(curColumn - 1 >= 0) {
                    nextVertex = Position(curRow, (curColumn - 1))
                    left = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    leftAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(leftAndCur < left) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = leftAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }
            }
            // hasRight & hasTop
            9 -> {
                if(curRow + 1 < gridSize) {
                    nextVertex = Position((curRow + 1), curColumn)
                    bottom = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    bottomAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(bottomAndCur < bottom) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = bottomAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }

                if(curColumn - 1 >= 0) {
                    nextVertex = Position(curRow, (curColumn - 1))
                    left = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    leftAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(leftAndCur < left) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = leftAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }
            }
            // hasTop & hasBottom
            10 -> {
                if(curColumn + 1 < gridSize) {
                    nextVertex = Position(curRow, (curColumn + 1))
                    right = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    rightAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(rightAndCur < right) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = rightAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }

                if(curColumn - 1 >= 0) {
                    nextVertex = Position(curRow, (curColumn - 1))
                    left = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    leftAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(leftAndCur < left) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = leftAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }
            }
            // hasTop, hasRight, hasBottom
            11 -> {
                if(curColumn - 1 >= 0) {
                    nextVertex = Position(curRow, (curColumn - 1))
                    left = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    leftAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(leftAndCur < left) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = leftAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }
            }
            // hasTop & hasLeft
            12 -> {
                if(curRow + 1 < gridSize) {
                    nextVertex = Position((curRow + 1), curColumn)
                    bottom = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    bottomAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(bottomAndCur < bottom) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = bottomAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }

                if(curColumn + 1 < gridSize) {
                    nextVertex = Position(curRow, (curColumn + 1))
                    right = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    rightAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(rightAndCur < right) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = rightAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }
            }
            // hasLeft, hasTop, hasRight
            13 -> {
                if(curRow + 1 < gridSize) {
                    nextVertex = Position((curRow + 1), curColumn)
                    bottom = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    bottomAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(bottomAndCur < bottom) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = bottomAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }
            }
            // hasBottom, hasLeft, hasTop
            14 -> {
                if(curColumn + 1 < gridSize) {
                    nextVertex = Position(curRow, (curColumn + 1))
                    right = mapOfLevels.getValue(findIndex(nextVertex, gridSize)).value
                    rightAndCur = mapOfLevels.getValue(curVertex).value + 1
                    if(rightAndCur < right) {
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.value = rightAndCur
                        mapOfLevels[findIndex(nextVertex, gridSize)]!!.changedBy =
                            Position(curRow, curColumn)
                    }
                }
            }
        }
    }

    var hintPosIndex = (gridSize * gridSize) - 1

    do {
        val prevIndex = findIndex(mapOfLevels[hintPosIndex]!!.changedBy, gridSize)

        if(prevIndex == findIndex(initialPos, gridSize)) {
            break
        }
        else {
            hintPosIndex = prevIndex
        }
    } while(prevIndex != findIndex(initialPos, gridSize));

    return hintPosIndex
}

fun fillMatrix(cellArrayList : ArrayList<Cell>, gridSize : Int) : Array<Array<Cell>>{
    val pos = Array(gridSize) {Array(gridSize) {
        Cell(
            0, false, false,
            false, false
        )
    }
    }

    var k = 0

    for(i in 0 until gridSize){
        for(j in 0 until gridSize){
            pos[i][j] = cellArrayList.get(k)
            k++
        }
    }

    return pos
}

fun findIndex(position: Position, gridSize: Int): Int {
    return position.column + (gridSize * position.row)
}

fun findRowAndColumn(index : Int, gridSize: Int) : Position{
    val row = index / gridSize
    val column = index - (gridSize * row)

    return Position(row, column)
}