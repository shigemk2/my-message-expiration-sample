package com.example

import akka.actor._

trait ExpiringMessage {
  val occurredOn = System.currentTimeMillis()
  val timeToLive: Long

  def isExpired: Boolean = {
    val elapsed = System.currentTimeMillis() - occurredOn

    elapsed > timeToLive
  }
}

object MessageExpirationDriver extends CompletableApp(3) {
}
