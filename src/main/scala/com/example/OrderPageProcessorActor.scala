package com.example

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, Props}
import com.example.Orders.OrderPage

class OrderPageProcessorActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case page:OrderPage => log.info(s"processing page $page")
  }
}

object OrderPageProcessorActor {
  val props = Props[OrderPageProcessorActor]
}
