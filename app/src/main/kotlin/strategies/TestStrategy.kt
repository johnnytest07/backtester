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
  ):Order {

    val random = Random.nextInt(1,3)
    var ret = Order(Action.NO_ACTION, 0)
    when (random) {
      1 -> {
        val size = (balance / (tick.price * 5)).toInt()
        ret = Order(Action.BUY, size)
      }
      2 -> {
        ret = Order(Action.SELL, position)
      }
      3 -> {}
    }

    return ret
  }
} // Purely random inputs to test if the program is working
