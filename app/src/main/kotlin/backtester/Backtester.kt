package backtester

class Backtester(
  val strategy: Strategy,
  val data: List<Tick>,
  val initialBalance:Double = 100000.0 // Can modify initial amount
) {

  // Initial variables
  private var balance: Double = initialBalance
  private var position: Double = 0.0

  // Main loop
  fun run() {

    for ((index, tick) in data.withIndex()) {

      val (action, size) = strategy.onTick(position, data.take(index+1), tick)

      val timestamp = tick.date
      val price = tick.price

      when (action) {
        Action.BUY -> {
          if (price*size > balance) {continue} // Handle this error later
          balance -= price
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

    print("Initial balance: ${initialBalance}, Final balance: ${balance}, Remaining positions: ${position}")
  }
}

