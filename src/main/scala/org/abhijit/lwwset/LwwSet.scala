package org.abhijit.lwwset

import java.time.{Instant}
import scala.collection.mutable.ListBuffer
import org.abhijit.lwwset._

// A last writer wins set LwwSet[T] is an implementation of trait Crdt[T].
// In this Crdt, the key has to be passed alongside the timestamp(or a time instant).
// The time instant stored for every key is used for resolving conflicts when 
// multiple users store/remove the same key. 
// In case of a conflict, the user who has stored/removed the key
// with the most recent timestamp(or time instant) wins, hence the name last writer wins

// e.g. A stores 3 at (2018-10-14T00:31:12.231Z) and B stores 3 at (2018-10-14T00:31:12.234Z)
// The value stored in the lwwSet is 2018-10-14T00:31:12.234Z because it is more recent
// than 2018-10-14T00:31:12.231Z. 
// Similarly the same rule can be applied for removing keys as well.

// The values for the keys are restricted to java.time.Instant and two different sets of type
// ElemSet[T] are used for storing and removing keys and their instants respectively.

// addSet stores the elements which are added along with their time instants
// removeSet stores the elements which are removed along with their time
class LwwSet[T]  (protected val addSet: ElemSet[T] = new ElemSet[T](),
                  protected val removeSet: ElemSet[T] = new ElemSet[T]()) extends Crdt[T, java.time.Instant] {
  
  // When an element is added to a LwwSet, the addSet adds an element
  // The value has to be a type which encapsulates time(i.e. java.time.Instant)
  override def add(elem: T, newInstant: java.time.Instant) = {
    addSet.add(elem, newInstant)
  }
  
  // When an element is removed from a LwwSet, the removeSet adds an element
  // The value has to be a type which encapsulates time(i.e. java.time.Instant)
  override def remove(elem: T, newInstant: java.time.Instant) = {
    removeSet.add(elem, newInstant)
  }

  // If the presence of an element is to be checked, the addSet and the
  // removeSet are checked for their values. If the instant corresponding
  // to removeSet is greater or the element was added to the removeSet more
  // recently than the addSet, it returns false, else true
  override def exists(elem: T): Boolean = {
    val minInstant = java.time.Instant.MIN
    val lastAddInstant = addSet.fetch(elem).getOrElse(minInstant)
    val lastRemoveInstant = removeSet.fetch(elem).getOrElse(minInstant)
    if (lastRemoveInstant.compareTo(lastAddInstant) >= 0) {
      false
    }
    else {
      true
    }
  }

  // When a LwwSet is merged with other Crdt[T, V], it can be only merged with
  // another LwwSet. First the crdtSet is matched if it is an LwwSet
  // To merge, the elements of addSets and removeSets of both the LwwSets are merged together
  override def merge(crdtSet: Crdt[T, java.time.Instant]): Option[Crdt[T, java.time.Instant]] = {
    crdtSet match {
      case lwwset: LwwSet[T] => Some(new LwwSet(addSet.merge(lwwset.addSet), removeSet.merge(lwwset.removeSet)))
      case default => None
    }
  }

  // This method returns all the keys which exist in the LwwSet.
  // All the keys in the addSet are traversed and checked if they have been
  // stored before the keys were stored in the removeSet. For the keys which
  // were stored more recently, those keys are appended to a List[T] and returned
  override def existingKeys(): List[T] = {
    var finalKeys = new ListBuffer[T]()
    val minInstant = java.time.Instant.MIN
    addSet.elementMap.foreach{ case (key, addInstantVal) => 

      val removeInstantVal = removeSet.fetch(key).getOrElse(minInstant)

      if (addInstantVal.compareTo(removeInstantVal) > 0){
        finalKeys += key
      }
    }
    finalKeys.toList
  }

  // Returns the size of addSet
  def addSetSize(): Int = {
    addSet.size
  }

  // Returns the size of removeSet
  def removeSetSize(): Int = {
    removeSet.size
  }
  
  // Returns addSet. Only access
  def getAddSet(): ElemSet[T] = {
    addSet
  }

  // Returns removeSet. Only access
  def getRemoveSet(): ElemSet[T] = {
    removeSet
  }
}
