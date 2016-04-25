package com.example

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.example.Orders.{OrderPage, OrderResponse}
import com.example.SyncTriggerActor.SyncTrigger
import com.example.order.client.OrderClient

class SyncMasterActor(pageProcessorActor:ActorRef, orderClient:OrderClient) extends Actor with ActorLogging {

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  implicit val ec = context.dispatcher

  import OrderJsonSupport._
  implicit def unmarshaller: FromEntityUnmarshaller[OrderResponse] = SprayJsonSupport.sprayJsonUnmarshaller[OrderResponse]

  var lastSync:DateTime = DateTime.MinValue

  def syncing: Receive = {
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>

      implicit val ec = context.dispatcher

      val now = DateTime.now
      val orderResponseFuture = Unmarshal(entity).to[OrderResponse]
      orderResponseFuture.map(orderResponse => {
        if (orderResponse.items.nonEmpty) {
          pageProcessorActor ! OrderPage(orderResponse.page, orderResponse.items)
        }

        if (orderResponse.hasMorePages) {
          log.info(s"get next page ${orderResponse.page}")
          requestPageAndPipeToSelf(orderResponse.page + 1)
        } else {
          lastSync = now
          log.info(s"finished processing become receive - update lastSync to $lastSync")
          context.become(receive)
        }
      })
      case HttpResponse(code, _, _, _) =>
        log.info("Request failed, response code: " + code)
  }

  override def receive: Receive = {
    case msg:SyncTrigger =>
      requestPageAndPipeToSelf()
      context.become(syncing)
  }

  def requestPageAndPipeToSelf(page:Int = 1): Unit = {
    import akka.pattern.pipe

    pipe(orderClient.getOrders(page, lastSync)) to self
  }
}

object SyncMasterActor {
  def props(pageProcessorActor:ActorRef, orderClient:OrderClient) = Props(new SyncMasterActor(pageProcessorActor, orderClient))
}