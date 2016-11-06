package com.github.gdefacci.briscola.service

import rx.lang.scala.Observable
import rx.lang.scala.Subject
import rx.lang.scala.subjects.ReplaySubject

trait EventsStore[E] {
  def put(event:E):Unit
  
  def events:Observable[E]
}

object EventsStore {
  def apply[E]():EventsStore[E] = new EventsStoreImpl[E]()
}

class EventsStoreImpl[E]() extends EventsStore[E] {
  
  private val subj = ReplaySubject[E]
  
  def put(event:E):Unit = subj.onNext(event)
  def events:Observable[E] = subj
}