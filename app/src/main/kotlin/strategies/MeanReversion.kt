package strategies

import backtester.*
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

class MeanReversionStrategy(
  val windowSize: Int,
  val hl_min: Double = 3.0,
  val hl_max: Double = 600.0,
  val entryThreshold: Double = 1.0,
  val exitThreshold: Double = 0.5,
  val stopLossThreshold: Double = 3.0,
  val timeBasedExit: Boolean = false
): Strategy {

  private var daysHeld: Int = 0

  override fun onTick(
    balance: Double,
    position: Int,
    history: List<Tick>,
    tick: Tick
  ): Order {


    // Strategy only works starting with 60 days of history
    if (history.size < windowSize + 1) return Order(Action.NO_ACTION, 0)

    // Take rolling window
    val window = history.takeLast(windowSize)

    // Convert prices to log-prices
    val logPrices = window.map { ln(it.price) }

    // Build aligned sequences
    val x = logPrices.dropLast(1) // today
    val y = logPrices.drop(1)     // tomorrow

    // Estimate phi using OLS
    val phi = estimatePhiOLS(x, y)

    if (phi <= 0.0 || phi >= 1.0) {
      return Order(Action.NO_ACTION, 0)
    }

    val phiHalfLife = ln(0.5)/ln(phi)

    if (phiHalfLife < hl_min || phiHalfLife > hl_max) {
      return Order(Action.NO_ACTION, 0)
    }

    // Time-based exit
    if (daysHeld >= 2*phiHalfLife && timeBasedExit) return Order(Action.SELL_LONG, position)

    val mu = logPrices.average()
    val deviations = logPrices.map { x -> x - mu }
    val standardDeviation = sqrt(deviations.sumOf { x -> x.pow(2) } / windowSize)

    val currentZ = (ln(tick.price) - mu)/standardDeviation

    // Long logic

    if (currentZ <= -entryThreshold){
      if (position != 0){ // if a position is already held return
        return if (currentZ <= -stopLossThreshold) Order(Action.SELL_LONG, position)
        else Order(Action.NO_ACTION,0)
      }
      val size = ((balance*0.05)/tick.price).toInt()              // Buy 5% of capital
      daysHeld = 0                                                // Reset daysHeld
      return Order(Action.LONG, size)
    }

    if (abs(currentZ) <= exitThreshold){
      if (position == 0) return Order(Action.NO_ACTION, 0)
      return Order(Action.SELL_LONG, position)
    }

    if (position != 0) daysHeld += 1
    return Order(Action.NO_ACTION, 0)
  }

  private fun estimatePhiOLS(x: List<Double>, y: List<Double>): Double {

    val n = x.size

    val meanX = x.average()
    val meanY = y.average()

    var numerator = 0.0
    var denominator = 0.0

    for (i in 0 until n) {
      val dx = x[i] - meanX
      numerator += dx * (y[i] - meanY)
      denominator += dx * dx
    }

    // Avoid divide-by-zero
    if (denominator == 0.0) return Double.NaN

    return numerator / denominator
  }
}