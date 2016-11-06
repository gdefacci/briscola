package com.github.gdefacci.briscola.service

trait IdFactory[Id] {
  
  def newId:Id
  
}

object IdFactory {
  
  def apply[Id](f:() => Id) = new IdFactory[Id] {
    def newId:Id = f()
  }
  
}