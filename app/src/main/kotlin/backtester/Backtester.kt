package backtester

class Backtester(
  val strategy: Strategy,
  val data: List<Tick>,
  val initialBalance:Double = 100000.0 // Can modify initial amount
) {

  // Initial variables
  private var balance: Double = initialBalance
  private var position: Int = 0

  // Main loop
  fun run() {

    for ((index, tick) in data.withIndex()) {

      var (action, size) = strategy.onTick(balance, position, data.take(index+1), tick)

      val timestamp = tick.date
      val price = String.format("%.2f", tick.price).toDouble()

      when (action) {
        Action.BUY -> {
          if (price*size > balance) {continue} // Handle this error later
          balance -= size*price
          position += size
        }

        Action.SELL -> {
          if (position < size) {continue} // Handle this error later consider the case if they're shorting
          balance += size*price
          position -= size
        }

        Action.NO_ACTION -> {}
      }
    }

    val finalLiquidation = balance + position * data.last().price

    println("Initial balance: $initialBalance")
    println("Final balance: $balance")
    println("Remaining position: $position")
    println("Liquidation value: $finalLiquidation")  }
}

