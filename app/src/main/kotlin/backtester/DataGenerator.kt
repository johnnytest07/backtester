package backtester
import org.jetbrains.kotlinx.dataframe.*
import org.jetbrains.kotlinx.dataframe.io.*
import org.jetbrains.kotlinx.dataframe.api.*
import java.io.File
import kotlin.text.get

class DataGenerator {

  private val directoryPath = "app/src/main/resources/tests/"  // test directory path
  private lateinit var df: DataFrame<*>
  val dataSet = mutableListOf<List<Tick>>()

  fun generateTickData(numberOfTests: Int): List<List<Tick>> {
    val files = File(directoryPath).listFiles()
    var count = 0;
    print(files)
    for (file in files){
      if (count > numberOfTests){break}
      df = DataFrame.readCSV(directoryPath + file.name)
      val dataInput: List<Tick> = df.rows().map { row ->
        Tick(
          date = row["Date"]!!.toString(),
          price = row["Close/Last"]!!.toString().drop(1).toDouble(),
        )
      }
      dataSet.add(dataInput)
      count++
    }

    return dataSet
  }

}