package com.example

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.example.Orders.{Address, Order, OrderResponse}
import spray.json.{DefaultJsonProtocol, NullOptions}

object Orders {

  final case class Address(street:String, city:Option[String], country:String)
  final case class Order(orderId:String, billingAddress:Address)
  final case class OrderResponse(page:Int, results:Int, resultsPerPage:Int, items: List[Order]) {
    def hasMorePages = page * resultsPerPage < results
  }
  final case class OrderPage(page:Int, items:List[Order])
}

object OrderJsonSupport extends DefaultJsonProtocol with SprayJsonSupport with NullOptions {
  implicit val addressFormat = jsonFormat3(Address)
  implicit val orderFormat = jsonFormat2(Order)
  implicit val orderResponseFormat = jsonFormat4(OrderResponse)
}

