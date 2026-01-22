package backtester

data class Order(
  val action: Action,
  val size: Int
)

enum class Action {
  LONG,
  SELL_LONG,
  SHORT,
  SELL_SHORT,
  NO_ACTION,
}

data class Lot(
  var qty: Int,          // signed: +long, -short
  val price: Double     // entry price
)
