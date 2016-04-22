package com.example

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal, Unmarshaller}
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import com.example.Orders.{OrderPage, OrderResponse}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}

class SyncMasterActor(pageProcessorActor:ActorRef) extends Actor with ActorLogging {

  val config = ConfigFactory.load()
  val shopId:String = config.getString("epages.shopId")
  val token:String = config.getString("epages.token")

  val ordersUri:Uri = s"https://sandbox.epages.com/rs/shops/$shopId/orders?resultsPerPage=50"

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  implicit val ec = context.dispatcher


  import OrderJsonSupport._
  implicit def unmarshaller: FromEntityUnmarshaller[OrderResponse] = SprayJsonSupport.sprayJsonUnmarshaller[OrderResponse]

  val http = Http(context.system)

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
          requestPageAndPipeToSelf(orderResponse.page + 1)
        } else {
          lastSync = now
          log.info(s"finished processing become receive - update lastSync to $lastSync")
          context.become(receive)
        }
      })
//      entity.toStrict(Duration(1, SECONDS))
//        .map(s => {
//          val orderResponse = JsonParser(s.data.utf8String).convertTo[OrderResponse]
//          if (orderResponse.items.nonEmpty) {
//            pageProcessorActor ! OrderPage(orderResponse.page, orderResponse.items)
//          }
//
//          if (orderResponse.hasMorePages) {
//            requestPageAndPipeToSelf(orderResponse.page + 1)
//          } else {
//            lastSync = now
//            log.info(s"finished processing become receive - update lastSync to $lastSync")
//            context.become(receive)
//          }
//        })
    case HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
  }

  override def receive: Receive = {
    case msg:SyncTriggerActor.SyncTrigger =>

      requestPageAndPipeToSelf()
      context.become(syncing)

  }

  def requestPageAndPipeToSelf(page:Int = 1): Unit = {
    import akka.pattern.pipe

    val auth = headers.Authorization(OAuth2BearerToken(token))
    val accept = headers.Accept(MediaTypes.`application/json`)
    var uri = ordersUri + s"&page=$page"
    lastSync match {
      case DateTime.MinValue =>
      case _ =>
        val createdAfter = lastSync.toIsoDateTimeString()
        uri += s"&createdAfter=$createdAfter"
    }
    log.info(s"getting page $page - uri is $uri")
    pipe(http.singleRequest(HttpRequest(HttpMethods.GET, uri, List(auth, accept)))) to self
  }
}

object SyncMasterActor {
  def props(pageProcessorActor:ActorRef) = Props(new SyncMasterActor(pageProcessorActor))
}