package strategies

import backtester.Action
import backtester.Order
import backtester.Strategy
import backtester.Tick
import kotlin.random.Random

class TestStrategy: Strategy {

  override fun onTick(
    balance: Double,
    position: Int,
    history: List<Tick>,
    tick: Tick
  ):List<Order> {

    val random = Random.nextInt(1,5)
    var ret = Order(Action.NO_ACTION, 0)

    if (tick.price == 1000.0){return listOf(Order(Action.SHORT, 1))}
    if (tick.price == 1100.0){return listOf(Order(Action.SELL_SHORT, 1))}

    when (random) {
      1 -> {
        val size = (balance / (tick.price * 5)).toInt()
        ret = Order(Action.LONG, size)
      }
      2 -> {
        ret = Order(Action.SELL_LONG, position)
      }
      3 -> {
        val size = (balance / (tick.price * 5)).toInt()
        ret = Order(Action.SHORT, size)
      }
      4 -> {
        ret = Order(Action.SELL_SHORT, position)
      }
      5 -> {}
    }

    return listOf(ret)
  }
} // Purely random inputs to test if the program is working
