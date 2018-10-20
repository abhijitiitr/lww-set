package org.abhijit.lwwset

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import org.abhijit.lwwset._
import java.time.{Instant}

class ElemSetTest extends FunSuite with BeforeAndAfter {
  var elemSet: ElemSet[Int] = _

  before {
    elemSet = new ElemSet[Int]
  }

  // a new element set should have the size as zero
  test("new elem set has zero members") {
    assert(elemSet.size == 0)
  }

  test("merging two element sets with non overlapping keys has all the keys") {
    val firstElemSet = new ElemSet[Int]
    val secondElemSet = new ElemSet[Int]

    // Get the current instant
    var timeInMillis = System.currentTimeMillis()
    var instant = Instant.ofEpochMilli(timeInMillis)

    // Two non overlapping keys
    val firstKey = 1
    val secondKey = 2

    firstElemSet.add(firstKey, instant)

    // To make sure the next instant is more recent than the previous 
    // instant
    Thread.sleep(1)

    // The next instant
    timeInMillis = System.currentTimeMillis()
    instant = Instant.ofEpochMilli(timeInMillis)

    secondElemSet.add(secondKey, instant)
    val newElemSet = firstElemSet.merge(secondElemSet)

    assert(newElemSet.size === 2)
  }

  test("merging two element sets with overlapping keys overrides the old values") {
    val firstElemSet = new ElemSet[Int]
    val secondElemSet = new ElemSet[Int]

    // Get the current instant
    var timeInMillis = System.currentTimeMillis()
    var instant = Instant.ofEpochMilli(timeInMillis)

    val key = 1

    firstElemSet.add(key, instant)

    // To make sure the next instant is more recent than the previous 
    // instant
    Thread.sleep(1)

    // The next instant
    timeInMillis = System.currentTimeMillis()
    val newInstant = Instant.ofEpochMilli(timeInMillis)

    secondElemSet.add(key, newInstant)

    val newElemSet = firstElemSet.merge(secondElemSet)

    assert(newElemSet.size === 1)
    assert(newElemSet.fetch(key).getOrElse(0) === newInstant)

  }

}
