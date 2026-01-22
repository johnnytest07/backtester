import backtester.*
import strategies.*

fun main() {

  val dataGenerator = DataGenerator("AAPL_5_years.csv")
  val data = dataGenerator.generateTickData()

  val strategy = TestStrategy()
  val client = Backtester(strategy, data)

  val strategy2 = MeanReversionStrategy(120)
  val client2 = Backtester(strategy2, data)

  client.run()
  client2.run()
}