package strategies

import backtester.*
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

class MeanReversionStrategy(
  val windowSize: Int,
  val hlMin: Double = 3.0,
  val hlMax: Double = 600.0,
  val entryThreshold: Double = 1.0,
  val exitThreshold: Double = 0.5,
  val stopLossThreshold: Double = 3.0,
  val timeBasedExit: Boolean = false
) : Strategy {

  private var daysHeld = 0

  override fun onTick(
    balance: Double,
    position: Int,
    history: List<Tick>,
    tick: Tick
  ): List<Order> {

    val actions = mutableListOf<Order>()

    // Insufficient data
    if (history.size < windowSize + 1) {
      actions.add(Order(Action.NO_ACTION, 0))
      return actions
    }

    // Estimate OU parameters
    val window = history.takeLast(windowSize)
    val logPrices = window.map { ln(it.price) }

    val x = logPrices.dropLast(1)
    val y = logPrices.drop(1)

    val phi = estimatePhiOLS(x, y)
    if (phi <= 0.0 || phi >= 1.0) {
      actions.add(Order(Action.NO_ACTION, 0))
      return actions
    }

    val halfLife = ln(0.5) / ln(phi)
    if (halfLife < hlMin || halfLife > hlMax) {
      actions.add(Order(Action.NO_ACTION, 0))
      return actions
    }

    val mu = logPrices.average()
    val sd = sqrt(logPrices.sumOf { (it - mu).pow(2) } / windowSize)
    if (sd == 0.0) {
      actions.add(Order(Action.NO_ACTION, 0))
      return actions
    }

    val z = (ln(tick.price) - mu) / sd

    // Stop-loss
    if (position > 0 && z <= -stopLossThreshold) {
      actions.add(Order(Action.SELL_LONG, position))
    }

    if (position < 0 && z >= stopLossThreshold) {
      actions.add(Order(Action.SELL_SHORT, abs(position)))
    }

    // Exit logic mean reversion
    if (position > 0 && z >= -exitThreshold && z <= 0.0) {
      actions.add(Order(Action.SELL_LONG, position))
    }

    if (position < 0 && z <= exitThreshold && z >= 0.0) {
      actions.add(Order(Action.SELL_SHORT, abs(position)))
    }

    // Entry logic
    if (position == 0) {
      val size = ((balance * 0.05) / tick.price).toInt()

      if (size > 0 && z <= -entryThreshold) {
        actions.add(Order(Action.LONG, size))
        daysHeld = 0
      }

      if (size > 0 && z >= entryThreshold) {
        actions.add(Order(Action.SHORT, size))
        daysHeld = 0
      }
    }

    // Time-based exit
    if (position != 0) {
      daysHeld++
      if (timeBasedExit && daysHeld >= 2 * halfLife.toInt()) {
        if (position > 0)
          actions.add(Order(Action.SELL_LONG, position))
        else
          actions.add(Order(Action.SELL_SHORT, abs(position)))
      }
    }

    if (actions.isEmpty()) {
      actions.add(Order(Action.NO_ACTION, 0))
    }

    return actions
  }

  private fun estimatePhiOLS(x: List<Double>, y: List<Double>): Double {
    val n = x.size
    val meanX = x.average()
    val meanY = y.average()

    var num = 0.0
    var den = 0.0

    for (i in 0 until n) {
      val dx = x[i] - meanX
      num += dx * (y[i] - meanY)
      den += dx * dx
    }

    if (den == 0.0) return Double.NaN
    return num / den
  }
}
