package org.obl.briscola
package service
package impl.simple

import org.obl.ddd.Repository

trait SimpleRepository[Id,T] extends Repository[Id,T] {
  
  protected val valuesMap = collection.concurrent.TrieMap.empty[Id, T]
  
  def get(id:Id) = valuesMap.get(id)
  
  def put(id:Id, t:T):Option[T] = {
    valuesMap += (id -> t)
    Some(t)
  }
  
}