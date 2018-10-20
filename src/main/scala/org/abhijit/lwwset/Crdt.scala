package org.abhijit.lwwset

import java.time.{Instant}
import org.abhijit.lwwset._

// A Crdt should be able to add an element,
// remove an element, check if the element exists
// and provide all the keys currently present in
// the crdt
// Any class satisfying this trait is a Crdt
trait Crdt[T, V] {
  
  // Method which defines behavior when an element is added
  def add(elem: T, value:  V)
  
  // Method which defines behavior when an element is removed
  def remove(elem: T, value:  V)
  
  // Method which checks whether an element is present
  def exists(elem: T): Boolean

  // Returns all the existing keys in the Crdt
  def existingKeys(): List[T]
  
  // Method which defines behavior when a Crdt is merged with
  // other Crdt. 
  def merge(crdtSet: Crdt[T, V]): Option[Crdt[T, V]]
}
