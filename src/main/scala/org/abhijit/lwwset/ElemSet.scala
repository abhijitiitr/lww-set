package org.abhijit.lwwset

import scala.collection.concurrent.{Map, TrieMap}
import java.time.{Instant}
import org.abhijit.lwwset._

// This class stores elements in an elementMap with their
// timestamps(represented as java.time.Instant).
// The storage being used is a TrieMap[T, java.time.Instant]
class ElemSet[T](val elementMap: Map[T, java.time.Instant] = new TrieMap[T, java.time.Instant]())  {

  // When an element is to be added, it is checked for its previous value
  // Only if the instant stored previously is less recent that the instant 
  // to be stored OR the key is absent in the TrieMap.
  def add(elem: T, newInstant: java.time.Instant) = {
    val lastInstant = elementMap.get(elem)
    lastInstant match {
      case Some(instant) => {
        // If the newInstant is greater(or more recent) than 
        // the current value of key stored in the elementMap
        // we replace the value of the key with the newInstant
        if(instant.compareTo(newInstant) < 0) {
          elementMap.put(elem, newInstant)
        }
      }
      // If the key is absent, we set the value of the key
      case None => elementMap.put(elem, newInstant)
    }
  }
  
  // When an elementSet has to be merged with another elementSet,
  // a union of keys is computed first, after which all the keys are
  // iterated over. For each key in the union, the value in both the
  // elementSets are checked. The value which is more recent is stored
  // in a new elementSet which is returned at the end.
  def merge(otherElemSet: ElemSet[T]): ElemSet[T] = {
    val currentElemKeys = elementMap.keySet
    val otherElemMap = otherElemSet.elementMap
    val otherElemKeys = otherElemMap.keySet
    var allElemKeys = currentElemKeys | otherElemKeys

    val newElementSet = new ElemSet[T]()

    allElemKeys.foreach{ key =>
      // default value if the key doesn't exist in the elementMap
      val minInstant = java.time.Instant.MIN

      val currentElemInstant = elementMap.get(key).getOrElse(minInstant)
      val otherElemInstant = otherElemMap.get(key).getOrElse(minInstant)
      
      var newElemInstant: java.time.Instant = minInstant
      
      // Compares whether the value of the key in the current elementMap
      // is greater(more recent) than the value of the key in the other
      // elementMap.
      if (currentElemInstant.compareTo(otherElemInstant) >= 0 ) {
        newElemInstant = currentElemInstant
      } 
      else {
        newElemInstant =  otherElemInstant
      }

      newElementSet.elementMap.put(key, newElemInstant)
    }

    newElementSet
  }

  // This method fetches the value of element stored in
  // the elementMap
  def fetch(elem: T): Option[java.time.Instant] = {
    elementMap.get(elem)
  }

  // Returns the size.
  def size(): Int = {
    elementMap.size
  }
}