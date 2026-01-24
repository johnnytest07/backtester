import backtester.*
import strategies.*

fun main() {

  val dataGenerator = DataGenerator()
  val dataSet = dataGenerator.generateTickData(5)

  val data = listOf(Tick("", 1000.00), Tick("", 1100.00))

  val strategy = TestStrategy()
  val client = Backtester(strategy, listOf(data))

  val strategy2 = MeanReversionStrategy(30)
  val client2 = Backtester(strategy2, dataSet)

  client.run()
  client2.run()
}