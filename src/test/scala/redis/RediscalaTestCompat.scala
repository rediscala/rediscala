package redis

import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

private[redis] object RediscalaTestCompat {
  def loggersKey = "pekko.loggers"

  object testkit {
    def EventFilter = org.apache.pekko.testkit.EventFilter

    def TestActorRef = org.apache.pekko.testkit.TestActorRef

    type TestEventListener = org.apache.pekko.testkit.TestEventListener

    def TestKit = org.apache.pekko.testkit.TestKit
    type TestKit = org.apache.pekko.testkit.TestKit

    def TestProbe = org.apache.pekko.testkit.TestProbe
    type TestProbe = org.apache.pekko.testkit.TestProbe

    type ImplicitSender = org.apache.pekko.testkit.ImplicitSender

    implicit def TestDuration(duration: FiniteDuration): org.apache.pekko.testkit.TestDuration =
      new org.apache.pekko.testkit.TestDuration(duration)
  }

}
