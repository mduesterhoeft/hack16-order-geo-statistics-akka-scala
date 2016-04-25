package com.example

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import com.example.order.client.OrderClientImpl
import com.example.order.client.OrderClientImpl.OrderClientConfig
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ApplicationMain extends App {
  val system = ActorSystem("MyActorSystem")

  val config = ConfigFactory.load()
  val shopId:String = config.getString("epages.shopId")
  val token:String = config.getString("epages.token")

  val orderClientConfig = OrderClientConfig(token, shopId)
  val orderClient = new OrderClientImpl(orderClientConfig, Http(system))

  val pageProcessorActor = system.actorOf(OrderPageProcessorActor.props, "pageProcessor")
  val syncMasterActor = system.actorOf(SyncMasterActor.props(pageProcessorActor, orderClient), "orderSyncMaster")
  val syncTriggerActor = system.actorOf(SyncTriggerActor.props(syncMasterActor), "orderSyncTrigger")
  Await.result(system.whenTerminated, Duration.Inf)
}