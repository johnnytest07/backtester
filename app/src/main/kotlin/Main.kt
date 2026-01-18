import backtester.*
import strategies.*
import kotlin.math.exp
import kotlin.math.log
import kotlin.random.Random

fun main() {

  val dataGenerator = DataGenerator("AAPL_5_years.csv")
  val data = dataGenerator.generateTickData()

  val strategy = TestStrategy()

  val client = Backtester(strategy, data)
  client.run()
}