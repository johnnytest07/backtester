package backtester

data class Tick(
  val date: String,
  val price: Double
)

interface Strategy {

  fun onTick(
    position: Double,
    history: List<Tick>,
    tick: Tick
  ): Order

}