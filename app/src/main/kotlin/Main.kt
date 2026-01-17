import backtester.*
import org.jetbrains.kotlinx.dataframe.*
import org.jetbrains.kotlinx.dataframe.io.*
import org.jetbrains.kotlinx.dataframe.api.*

class TestStrategy: Strategy {

  override fun onTick(
    position: Double,
    history: List<Tick>,
    tick: Tick
  ):Order {

    val order = Order(Action.BUY, 12.0)
    return order
  }

}

fun main() {
  val df = DataFrame.readCSV("app/src/main/resources/testdata.csv")

  val dataInput: List<Tick> = df.rows().map { row ->
    Tick(
      date = row["Date"]!!.toString(),
      price = row["Close/Last"]!!.toString().drop(1).toDouble(),
    )
  }

  val strategy = TestStrategy()

  val client = Backtester(strategy, dataInput)
  client.run()
}