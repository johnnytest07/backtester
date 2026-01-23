package backtester

import kotlin.math.abs

class Backtester(
  val strategy: Strategy,
  val dataSets: List<List<Tick>>,
  val initialBalance: Double = 100000.0
) {

  private var balance = initialBalance
  private var position = 0
  private val lots = mutableListOf<Lot>()

  private var realizedPnl = 0.0
  private val PnLs = mutableListOf<Double>()

  // Prevent infinite leverage
  private var shortProceeds = 0.0

  fun run() {
    for (data in dataSets) {
      for ((index, tick) in data.withIndex()) {

        val actions = strategy.onTick(
          balance,
          position,
          data.take(index + 1),
          tick
        )

        val price = tick.price

        actions.forEach { (action, size) ->
          when (action) {

            Action.LONG -> openLot(size, price)
            Action.SHORT -> openLot(-size, price)

            Action.SELL_LONG -> closeLots(size, price, side = +1)
            Action.SELL_SHORT -> closeLots(size, price, side = -1)


            Action.NO_ACTION -> {}
          }
        }
      }

      val lastPrice = data.last().price
      val unrealizedPnl = position * lastPrice
      val finalEquity = balance + unrealizedPnl

//      println("Results:")
//      println("Initial balance: $initialBalance")
//      println("Final balance: $balance")
//      println("Short proceeds locked: $shortProceeds")
//      println("Realized PnL: $realizedPnl")
//      println("Unrealized PnL: $unrealizedPnl")
//      println("Final equity: $finalEquity")

      PnLs.add(finalEquity - initialBalance)

      // Reset for next dataset
      balance = initialBalance
      position = 0
      lots.clear()
      realizedPnl = 0.0
      shortProceeds = 0.0
    }

    println("Result:")
    println("Average final PnLs: ${PnLs.average()}")
    println("Average percentage return: ${PnLs.average() / initialBalance * 100}%")
  }

  private fun openLot(qty: Int, price: Double) {
    if (qty == 0){return}

    if (qty > 0){
      balance -= qty * price
      position += qty
    } else {
      shortProceeds += -qty * price
      position -= qty
    }
  }

  private fun closeLots(
    qty: Int,
    price: Double,
    side: Int
  ) {
    if (abs(position) < qty) return // Exit if invalid action
    if (side == 1) {                    // sell long
      balance += qty * price
      position -= qty
    }
    if (side == -1) {                   // sell short
      if (abs(position) < qty) return
      val amountDeducted = qty * price
      if (amountDeducted < shortProceeds) {
        shortProceeds -= amountDeducted
      } else {
        balance += shortProceeds - amountDeducted
      }
    }
  }
}
