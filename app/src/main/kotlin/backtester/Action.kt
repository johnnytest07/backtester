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
  NO_ACTION;

  operator fun plus(str: String): String{
    return "$this $str"
  }
}