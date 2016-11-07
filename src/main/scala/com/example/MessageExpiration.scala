package com.example

import java.util.Date
import java.util.concurrent.TimeUnit

import akka.actor.Actor.Receive
import akka.actor._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration.Duration
import scala.util.Random

trait ExpiringMessage {
  val occurredOn = System.currentTimeMillis()
  val timeToLive: Long

  def isExpired: Boolean = {
    val elapsed = System.currentTimeMillis() - occurredOn

    elapsed > timeToLive
  }
}

case class PlaceOrder(id: String, itemId: String, price: Double, timeToLive: Long) extends ExpiringMessage

object MessageExpirationDriver extends CompletableApp(3) {
}

class PurchaseRouter(purchaseAgent: ActorRef) extends Actor {
  val random = new Random((new Date()).getTime)

  override def receive: Receive = {
    case message: Any =>
      val millis = random.nextInt(100) + 1
      println(s"PurchaseRouter: delaying delivery of $message for $millis milliseconds")
      val duration = Duration.create(millis, TimeUnit.MILLISECONDS)
      context.system.scheduler.scheduleOnce(duration, purchaseAgent, message)
  }
}

class PurchaseAgent extends Actor {
  override def receive: Receive = {
    case placeOrder: PlaceOrder =>
      if (placeOrder.isExpired) {
        context.system.deadLetters ! placeOrder
        println(s"PurchaseAgent: delivered expired $placeOrder to dead letters")
      } else {
        println(s"PurchaseAgent: placing order for $placeOrder")
      }

      MessageExpirationDriver.completedStep()
    case message: Any =>
      println(s"PurchaseAgent: received unexpected: $message")
  }
}
