package backtester

interface Strategy {

  fun onTick(): Pair<Action, Double> {
    TODO()
  }

}