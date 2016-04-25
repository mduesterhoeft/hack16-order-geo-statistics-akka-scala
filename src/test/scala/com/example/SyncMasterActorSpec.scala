package com.example

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.example.Orders.{Address, Order, OrderPage}
import com.example.SyncTriggerActor.SyncTrigger
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class SyncMasterActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("MySpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A SyncMasterActor" must {
    "sends a page after receiving it" in {

      val endProbe = TestProbe()
      val mockOrderClient = new MockOrderClient()

      val syncMaster = system.actorOf(SyncMasterActor.props(endProbe.ref, mockOrderClient))
      syncMaster ! new SyncTrigger()
      endProbe.expectMsg(OrderPage(1, List(Order("5704208B-1DED-0DB1-312D-0A0C05E643DB",Address("Pilatuspool 2",Some("Hamburg"), "DE")))))
    }
  }
}
