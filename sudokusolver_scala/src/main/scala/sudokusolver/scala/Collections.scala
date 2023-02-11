package sudokusolver.scala

extension[T] (seq: IndexedSeq[T])
  def zipEveryPair: IndexedSeq[(T, T)] =
    for
      (first, firstIndex) <- seq.zipWithIndex
      second <- seq.drop(firstIndex + 1)
    yield (first, second)

  def zipEveryTriple: IndexedSeq[(T, T, T)] =
    for
      (first, firstIndex) <- seq.zipWithIndex
      (second, secondIndex) <- seq.zipWithIndex.drop(firstIndex + 1)
      third <- seq.drop(secondIndex + 1)
    yield (first, second, third)