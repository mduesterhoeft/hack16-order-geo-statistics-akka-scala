package com.example

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.example.SyncTriggerActor.SyncTrigger

import scala.concurrent.duration._

class SyncTriggerActor(actor: ActorRef) extends Actor with ActorLogging {


  case class TimerTick()

  override def preStart: Unit = {
    super.preStart
    implicit val ec = context.dispatcher
    context.system.scheduler.schedule(1 second, 1 minute, self, new TimerTick())
  }

  override def receive: Receive = {
    case tick:TimerTick =>
      log.info("received timer tick")
      actor ! new SyncTrigger()
  }
}

object SyncTriggerActor {
  def props(actor: ActorRef) = Props(new SyncTriggerActor(actor))
  case class SyncTrigger()
}

