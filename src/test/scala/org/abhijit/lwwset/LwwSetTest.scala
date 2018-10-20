package org.abhijit.lwwset

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import org.abhijit.lwwset._
import java.time.{Instant}

class LwwSetTest extends FunSuite with BeforeAndAfter {
  var lwwSet: LwwSet[Int] = _

  before {
    lwwSet = new LwwSet[Int]
  }

  // A new lwwSet should have zero addSetSize and removeSetSize
  test("new lww set has zero members") {
    assert(lwwSet.addSetSize == 0)
    assert(lwwSet.removeSetSize == 0)
  }

  // If an element is added to a lwwSet, the addSet size increases
  // by one.
  test("adding one to the set increases addSetSize by 1") {
    val key = 1
    
    // current instant
    val timeInMillis = System.currentTimeMillis()
    val instant = Instant.ofEpochMilli(timeInMillis)
    
    // key is added
    lwwSet.add(key, instant)
    
    // the addSet should have increased by 1
    assert(lwwSet.addSetSize === 1)
  }

  // If an element is removed from a lwwSet, the removeSet size increases
  // by one.
  test("removing one from the set increases removeSetSize by 1") {
    val key = 1
    val timeInMillis = System.currentTimeMillis()
    
    // current instant
    val instant = Instant.ofEpochMilli(timeInMillis)

    // key is removed
    lwwSet.remove(key, instant)

    // the removeSet should have increased by 1
    assert(lwwSet.removeSetSize === 1)
  }


  test("removing a key after adding it makes the key absent in the set "){
    val key = 1
    
    // current instant
    var timeInMillis = System.currentTimeMillis()
    var instant = Instant.ofEpochMilli(timeInMillis)

    // First a key is added
    lwwSet.add(key, instant)

    // To make sure, the next instant is more recent(or greater)
    // than the previous instant
    Thread.sleep(1)

    // next instant
    timeInMillis = System.currentTimeMillis()
    instant = Instant.ofEpochMilli(timeInMillis)

    // The a key is removed after a key is added
    lwwSet.remove(key, instant)

    // the key shouldn't exist
    assert(lwwSet.exists(key) ===  false)

    // Also, the existingKeys should be an empty list
    assert(lwwSet.existingKeys.size ===  0)
  }


  test("adding a key after remove it makes the key present in the set "){
    val key = 1
    
    // current instant
    var timeInMillis = System.currentTimeMillis()
    var instant = Instant.ofEpochMilli(timeInMillis)
    
    // First a key is removed
    lwwSet.remove(key, instant)

    // To make sure, the next instant is more recent(or greater)
    // than the previous instant   
    Thread.sleep(1)
    
    // Next instant
    timeInMillis = System.currentTimeMillis()
    instant = Instant.ofEpochMilli(timeInMillis)
    
    // Then the same key is added
    lwwSet.add(key, instant)

    // The lwwSet should posses the key
    assert(lwwSet.exists(key) ===  true)

    // Also, the existingKeys should be a list with size 1
    assert(lwwSet.existingKeys.size ===  1)
  }


  test("merging two lww sets with non intersecting keys has all the keys from both the sets") {
    val firstLwwSet = new LwwSet[Int]
    val secondLwwSet = new LwwSet[Int]

    // current instant
    var timeInMillis = System.currentTimeMillis()
    var instant = Instant.ofEpochMilli(timeInMillis)

    // Two non overlapping keys
    val firstKey = 1
    val secondKey = 2

    // Both keys are added to the lwwSets
    firstLwwSet.add(firstKey, instant)
    secondLwwSet.add(secondKey, instant)

    // To make sure, the next instant is more recent(or greater)
    // than the previous instant   
    Thread.sleep(1)

    // The next instant
    timeInMillis = System.currentTimeMillis()
    instant = Instant.ofEpochMilli(timeInMillis)

    // Both keys which were added are now removed at a later
    // instant
    firstLwwSet.remove(firstKey, instant)
    secondLwwSet.remove(secondKey, instant)

    val emptyLwwSet = new LwwSet[Int]

    // Both lwwSets are merged now.
    val newLwwSet = firstLwwSet.merge(secondLwwSet).getOrElse(emptyLwwSet)

    // The new lwwSet should have two elements in the addSet and two elements
    // in the removeSet as the keys are non overlapping
    newLwwSet match {
      case lwwSet: LwwSet[Int] => {
        assert(lwwSet.addSetSize === 2)
        assert(lwwSet.removeSetSize === 2)
      }
      case _ => {}
    }
    
  }

  test("merging two lww sets with overlapping keys overrides the old values for addSet") {
    // Two lwwSets
    val firstLwwSet = new LwwSet[Int]
    val secondLwwSet = new LwwSet[Int]

    // Current instant
    var timeInMillis = System.currentTimeMillis()
    val oldInstant = Instant.ofEpochMilli(timeInMillis)

    // overlapping key which is to be
    // added in both the lwwSets
    val key = 1

    // Add the key in the first lwwSet
    firstLwwSet.add(key, oldInstant)
 
    // To make sure, the next instant is more recent(or greater)
    // than the previous instant
    Thread.sleep(1)

    // The next instant
    timeInMillis = System.currentTimeMillis()
    val newInstant = Instant.ofEpochMilli(timeInMillis)

    // Add the key on the second lwwSet with the newInstant value
    // which is more recent than the oldInstant
    secondLwwSet.add(key, newInstant)

    val emptyLwwSet = new LwwSet[Int]
    // Merge both the lwwSets
    val newLwwSet = firstLwwSet.merge(secondLwwSet).getOrElse(emptyLwwSet)

    // The number of keys in the new lwwSet should be one and the value
    // for that key should be equal to newInstant.
    newLwwSet match {
      case lwwSet: LwwSet[Int] => {
        assert(lwwSet.addSetSize === 1)
        assert(lwwSet.getAddSet.fetch(key).getOrElse(0) === newInstant)
      }
      case _ => {}
    }

  }

  test("merging two lww sets with overlapping keys overrides the old values for removeSet") {
    // Two lwwSets
    val firstLwwSet = new LwwSet[Int]
    val secondLwwSet = new LwwSet[Int]

    // Current instant
    var timeInMillis = System.currentTimeMillis()
    val oldInstant = Instant.ofEpochMilli(timeInMillis)

    // The overlapping key which is to be removed in both the lwwSets
    val key = 1

    // Remove the key from the first lwwSet with the current instant
    firstLwwSet.remove(key, oldInstant)

    // To make sure, the next instant is more recent(or greater)
    // than the previous instant
    Thread.sleep(1)

    // The new instant which is more recent(or greater) than the 
    // oldInstant.
    timeInMillis = System.currentTimeMillis()
    val newInstant = Instant.ofEpochMilli(timeInMillis)

    // Remove the overlapping from the second lwwSet with a value
    // of newInstant
    secondLwwSet.remove(key, newInstant)

    val emptyLwwSet = new LwwSet[Int]
    
    // Merge both the lwwSets with a default value of an emptyLwwSet
    // if they are not mergeable
    val newLwwSet = firstLwwSet.merge(secondLwwSet).getOrElse(emptyLwwSet)

    // The removeSetSize of the new LwwSet should be only one as the key
    // is overlapping and the value for that key should be equal to
    // new instant
    newLwwSet match {
      case lwwSet: LwwSet[Int] => {
        assert(lwwSet.removeSetSize === 1)
        assert(lwwSet.getRemoveSet.fetch(key).getOrElse(0) === newInstant)
      }
      case _ => {}
    }

  }

}
