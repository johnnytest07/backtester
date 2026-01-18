package backtester

data class Tick(
  val date: String,
  val price: Double
)

interface Strategy {

  fun onTick(
    balance: Double,
    position: Int,
    history: List<Tick>,
    tick: Tick
  ): Order

}