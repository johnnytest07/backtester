import backtester.*
import org.jetbrains.kotlinx.dataframe.*

class TestStrategy: Strategy {

  override fun onTick(
    position: Double,
    history: List<Pair<String, Double>>,
    tick: Pair<String, Double>
  ):Order {

    val order = Order(Action.BUY, 12.0)
    return order
  }

}

fun main() {
  val data = DataFrame.read()
  println(data)

  val strategy = TestStrategy()

  val client = Backtester(strategy, listOf(Pair("0", 10.0), Pair("0", 11.0), Pair("0", 12.0)))
  client.run()
}