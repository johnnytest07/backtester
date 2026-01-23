package strategies

import backtester.*
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

class MeanReversionStrategy(
  val windowSize: Int,
  val hlMin: Double = 3.0,
  val hlMaxMultiplier: Double = 2.0,
  val entryThreshold: Double = 1.0,
  val exitThreshold: Double = 0.5,
  val stopLossThreshold: Double = 3.0,
  val maxExposure: Int = 5000,
  val timeBasedExit: Boolean = false
) : Strategy {
  var hlMax: Double

  init {
    hlMax = hlMaxMultiplier * windowSize
  }

  private var daysHeld = 0

  override fun onTick(
    balance: Double,
    position: Int,
    history: List<Tick>,
    tick: Tick
  ): List<Order> {

    val actions = mutableListOf<Order>()

    // if not enough info break
    if (history.size < windowSize + 1) {
      actions.add(Order(Action.NO_ACTION, 0))
      return actions
    }

    // OU estimation
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

    // stop loss
    if (position > 0 && z <= -stopLossThreshold) {
      actions.add(Order(Action.SELL_LONG, position))
      return actions
    }

    if (position < 0 && z >= stopLossThreshold) {
      actions.add(Order(Action.SELL_SHORT, abs(position)))
      return actions
    }

    // exit logic
    if (position > 0 && z >= -exitThreshold && z <= 0.0) {
      actions.add(Order(Action.SELL_LONG, position))
      return actions
    }

    if (position < 0 && z <= exitThreshold && z >= 0.0) {
      actions.add(Order(Action.SELL_SHORT, abs(position)))
      return actions
    }

    // entry logic
    if (z <= -entryThreshold && position >= 0 && position < maxExposure) {
      val size = calcSize(balance, tick.price, position, maxExposure)
      if (size > 0)
        actions.add(Order(Action.LONG, size))
    }

    if (z >= entryThreshold && position <= 0 && abs(position) < maxExposure) {
      val size = calcSize(balance, tick.price, abs(position), maxExposure)
      if (size > 0)
        actions.add(Order(Action.SHORT, size))
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

    if (actions.isEmpty())
      actions.add(Order(Action.NO_ACTION, 0))

    return actions
  }

  // OLS estimator
  private fun estimatePhiOLS(x: List<Double>, y: List<Double>): Double {
    val meanX = x.average()
    val meanY = y.average()

    var num = 0.0
    var den = 0.0

    for (i in x.indices) {
      val dx = x[i] - meanX
      num += dx * (y[i] - meanY)
      den += dx * dx
    }

    if (den == 0.0) return Double.NaN
    return num / den
  }

  private fun calcSize(
    balance: Double,
    price: Double,
    currentExposure: Int,
    maxExposure: Int
  ): Int {
    val base = (balance * 0.05) / price
    val scale = maxOf(0.0, 1.0 - currentExposure.toDouble() / maxExposure)
    return (base * scale).toInt()
  }
}
