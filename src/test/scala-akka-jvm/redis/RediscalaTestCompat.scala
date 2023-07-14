package redis

import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

private[redis] object RediscalaTestCompat {
  def loggersKey = "akka.loggers"

  object testkit {
    def EventFilter = akka.testkit.EventFilter

    def TestActorRef = akka.testkit.TestActorRef

    type TestEventListener = akka.testkit.TestEventListener

    def TestKit = akka.testkit.TestKit
    type TestKit = akka.testkit.TestKit

    def TestProbe = akka.testkit.TestProbe
    type TestProbe = akka.testkit.TestProbe

    type ImplicitSender = akka.testkit.ImplicitSender

    implicit def TestDuration(duration: FiniteDuration): akka.testkit.TestDuration =
      new akka.testkit.TestDuration(duration)
  }

}
