package sudokusolver.scala

extension[T] (seq: Seq[T])
  def zipEveryPair: Seq[(T, T)] =
    for
      (first, firstIndex) <- seq.zipWithIndex
      second <- seq.drop(firstIndex + 1)
    yield (first, second)