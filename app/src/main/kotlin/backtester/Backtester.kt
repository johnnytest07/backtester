package backtester

class Backtester(
  val strategy: Strategy,
  val data: List<Tick>,
  val initialBalance: Double = 100000.0
) {

  private var cash = initialBalance
  private var position = 0
  private val lots = mutableListOf<Lot>()
  private var realizedPnl = 0.0

  fun run() {

    for ((index, tick) in data.withIndex()) {

      val actions = strategy.onTick(
        cash,
        position,
        data.take(index + 1),
        tick
      )

      val price = tick.price

      actions.forEach { (action, size) ->
        when (action) {

          Action.LONG -> openLot(size, price)
          Action.SHORT -> openLot(-size, price)

          Action.SELL_LONG -> closeLots(size, price)
          Action.SELL_SHORT -> closeLots(size, price)

          Action.NO_ACTION -> {}
        }
      }
    }

    val lastPrice = data.last().price
    val unrealizedPnl = lots.sumOf { it.qty * (lastPrice - it.price) }

    val finalEquity = cash + unrealizedPnl

    println("Initial balance: $initialBalance")
    println("Final cash: $cash")
    println("Realized PnL: $realizedPnl")
    println("Unrealized PnL: $unrealizedPnl")
    println("Final equity: $finalEquity")
  }

  private fun openLot(qty: Int, price: Double) {
    if (qty == 0) return

    if (qty > 0) {
      val cost = qty * price
      if (cash < cost) return
      cash -= cost
    } else {
      cash += -qty * price
    }

    lots.add(Lot(qty, price))
    position += qty
  }

  private fun closeLots(requested: Int, price: Double) {
    var remaining = requested
    val iterator = lots.iterator()

    while (iterator.hasNext() && remaining > 0) {
      val lot = iterator.next()

      // Only close lots that oppose the request
      if (lot.qty > 0 && requested < 0) continue
      if (lot.qty < 0 && requested > 0) continue

      val closeQty = minOf(kotlin.math.abs(lot.qty), remaining)
      val signedClose = if (lot.qty > 0) closeQty else -closeQty

      // Realized PnL
      realizedPnl += signedClose * (price - lot.price)

      // Cash movement
      cash += signedClose * price

      lot.qty -= signedClose
      position -= signedClose
      remaining -= closeQty

      if (lot.qty == 0) iterator.remove()
    }
  }
}
