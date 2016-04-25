package com.example.order.client

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.{DateTime, Uri}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Materializer}
import com.example.Orders.OrderResponse
import com.example.order.client.OrderClientImpl.OrderClientConfig

import scala.concurrent.Future

trait OrderClient {
  def getOrders(page:Int = 1, since:DateTime)(implicit fm: Materializer): Future[HttpResponse]
}

class OrderClientImpl(orderClientConfig: OrderClientConfig, http:HttpExt) extends OrderClient{

  val ordersUri:Uri = s"https://sandbox.epages.com/rs/shops/${orderClientConfig.shopId}/orders?resultsPerPage=50"

  override def getOrders(page: Int=1, since: DateTime)(implicit fm: Materializer): Future[HttpResponse] = {

    val auth = headers.Authorization(OAuth2BearerToken(orderClientConfig.token))
    val accept = headers.Accept(MediaTypes.`application/json`)
    var uri = ordersUri + s"&page=$page"
    since match {
      case DateTime.MinValue =>
      case _ =>
        val createdAfter = since.toIsoDateTimeString()
        uri += s"&createdAfter=$createdAfter"
    }
    http.singleRequest(HttpRequest(HttpMethods.GET, uri, List(auth, accept)))
  }
}

object OrderClientImpl {
  case class OrderClientConfig(token:String, shopId:String)
}
