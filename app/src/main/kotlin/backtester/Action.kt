package backtester

data class Order(
  val action: Action,
  val size: Int
)

enum class Action {
  BUY,
  SELL,
  NO_ACTION,
}