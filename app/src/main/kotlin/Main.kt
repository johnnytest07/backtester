import backtester.*
import strategies.*

fun main() {

  val dataGenerator = DataGenerator()
  val dataSet = dataGenerator.generateTickData(5)

  val strategy = TestStrategy()
  val client = Backtester(strategy, dataSet)

  val strategy2 = MeanReversionStrategy(30)
  val client2 = Backtester(strategy2, dataSet)

  client.run()
  client2.run()
}