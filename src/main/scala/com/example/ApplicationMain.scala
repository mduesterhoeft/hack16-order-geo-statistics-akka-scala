package com.example

import akka.actor.{ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ApplicationMain extends App {
  val system = ActorSystem("MyActorSystem")

  val pageProcessorActor = system.actorOf(OrderPageProcessorActor.props, "pageProcessor")
  val syncMasterActor = system.actorOf(SyncMasterActor.props(pageProcessorActor), "orderSyncMaster")
  val syncTriggerActor = system.actorOf(SyncTriggerActor.props(syncMasterActor), "orderSyncTrigger")
  Await.result(system.whenTerminated, Duration.Inf)
}