package com.example

import akka.http.scaladsl.model._
import akka.stream.scaladsl.{FileIO, Sink}
import com.example.order.client.OrderClient
import java.io.File

import akka.stream.Materializer

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class MockOrderClient extends OrderClient {
  override def getOrders(page: Int, since: DateTime)(implicit fm: Materializer): Future[HttpResponse] = {
    val jsonFuture = FileIO.fromFile(new File("src/test/resources/orderResponse.json")).runWith(Sink.head)
    val json = Await.result(jsonFuture, Duration.Inf)
    Future.successful(HttpResponse(StatusCodes.OK, Nil, HttpEntity.Strict(ContentTypes.`application/json`, json)))
  }
}
