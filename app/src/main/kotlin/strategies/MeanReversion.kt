import backtester.*
import kotlin.math.exp
import kotlin.math.log

class MeanReversionStrategy(val windowSize: Int): Strategy {

  override fun onTick(
    balance: Double,
    position: Int,
    history: List<Tick>,
    tick: Tick
  ): Order {

    // Strategy only works starting with 60 days of history
    if (history.size < windowSize) return Order(Action.NO_ACTION, 0)

    // Size of window can be changed
    val window = history.takeLast(windowSize).reversed()
    val windowPairing = mutableListOf<Pair<Double, Double>>()
    for ((index, tick) in window.dropLast(1).withIndex()) { // Create list of pairs of prices which are in log base e
      windowPairing.add(Pair(log(tick.price, exp(1.0)), log(window[index+1].price, exp(1.0))))
    }

    return Order(Action.NO_ACTION, 0)
  }
}