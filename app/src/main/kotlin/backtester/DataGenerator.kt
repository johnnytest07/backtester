package backtester
import org.jetbrains.kotlinx.dataframe.*
import org.jetbrains.kotlinx.dataframe.io.*
import org.jetbrains.kotlinx.dataframe.api.*
import kotlin.text.get

class DataGenerator(val fileName: String) {

  private val realPath = "app/src/main/resources/" + fileName
  private var df: DataFrame<*>

  init {
    df = DataFrame.readCSV(realPath)
  }

  fun generateTickData(): List<Tick> {

    val dataInput: List<Tick> = df.rows().map { row ->
      Tick(
        date = row["Date"]!!.toString(),
        price = row["Close/Last"]!!.toString().drop(1).toDouble(),
      )
    }

    return dataInput
  }

}