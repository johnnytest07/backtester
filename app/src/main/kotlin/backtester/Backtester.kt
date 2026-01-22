package backtester

class Backtester(
  val strategy: Strategy,
  val data: List<Tick>,
  val initialBalance:Double = 100000.0 // Can modify initial amount
) {

  // Initial variables
  private var balance: Double = initialBalance
  private var longPosition: Int = 0
  private var shortPosition: Int = 0
  private var shortEntry = mutableListOf<shorts>()

  data class shorts(var size: Int, val price: Double):Comparable<shorts> {
    override fun compareTo(other: shorts): Int = when {
      this.price != other.price -> this.price compareTo other.price // compareTo() in the infix form
      this.price != other.price -> this.price compareTo other.price
      else -> 0
    }
  }

  // Main loop
  fun run() {

    for ((index, tick) in data.withIndex()) {

      var (action, size) = strategy.onTick(balance, longPosition, data.take(index+1), tick)

      val timestamp = tick.date
      val price = String.format("%.2f", tick.price).toDouble()

      when (action) {
        Action.LONG -> {
          if (price*size > balance) {continue} // Handle insufficient fund later
          balance -= size*price
          longPosition += size
        }

        Action.SELL_LONG -> {
          if (longPosition < size) { // If size > actual size owned sell all
            balance += longPosition*price
            longPosition = 0
            continue
          }
          balance += size*price
          longPosition -= size
        }

        Action.SHORT -> {
          if (price*size > balance) {continue} // Handle insufficient fund later
          balance -= size*price
          shortPosition += size
          shortEntry.add(shorts(size, price))
        }

        Action.SELL_SHORT -> {                 // need alteration
          if (shortPosition <= 0){
            shortPosition = 0
            continue
          }
          shortEntry.sortDescending()
          for (position in shortEntry) {
            if (position.size < shortPosition){ // sufficient positions
              shortPosition -= position.size
              balance += size*(tick.price - position.price)
              shortEntry.remove(position)
            } else {
              position.size -= shortPosition
              shortPosition = 0
              balance += size*(tick.price - position.price)
              break
            }
          }
          continue
        }

        Action.NO_ACTION -> {}
      }
    }

    val finalLiquidation = balance + longPosition * data.last().price

    println("Initial balance: $initialBalance")
    println("Final balance: $balance")
    println("Remaining position: $longPosition")
    println("Liquidation value: $finalLiquidation")  }
}

