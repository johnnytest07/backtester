package backtester

data class Order(
  val action: Action,
  val size: Double
)

enum class Action {
  BUY,
  SELL,
  NO_ACTION,
}