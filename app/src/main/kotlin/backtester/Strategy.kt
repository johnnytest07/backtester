package backtester

interface Strategy {

  fun onTick(
    position: Double,
    history: List<Pair<String, Double>>,
    tick: Pair<String, Double>
  ): Order

}