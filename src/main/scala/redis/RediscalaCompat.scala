package redis

private[redis] object RediscalaCompat {

  type ConfigurationException = org.apache.pekko.ConfigurationException

  object actor {
    type Actor = org.apache.pekko.actor.Actor

    type ActorRefFactory = org.apache.pekko.actor.ActorRefFactory

    type ActorLogging = org.apache.pekko.actor.ActorLogging

    type ActorRef = org.apache.pekko.actor.ActorRef

    def ActorSystem = org.apache.pekko.actor.ActorSystem
    type ActorSystem = org.apache.pekko.actor.ActorSystem

    def Kill = org.apache.pekko.actor.Kill

    def OneForOneStrategy = org.apache.pekko.actor.OneForOneStrategy

    def Props = org.apache.pekko.actor.Props

    def PoisonPill = org.apache.pekko.actor.PoisonPill

    type SupervisorStrategy = org.apache.pekko.actor.SupervisorStrategy

    val Terminated = org.apache.pekko.actor.Terminated
    type Terminated = org.apache.pekko.actor.Terminated

    object SupervisorStrategy {
      def Stop = org.apache.pekko.actor.SupervisorStrategy.Stop
    }
  }

  object event {
    def Logging = org.apache.pekko.event.Logging
  }

  object io {
    val Tcp = org.apache.pekko.io.Tcp
    def IO = org.apache.pekko.io.IO
  }

  object util {
    def ByteString = org.apache.pekko.util.ByteString
    type ByteString = org.apache.pekko.util.ByteString

    type ByteStringBuilder = org.apache.pekko.util.ByteStringBuilder

    def Helpers = org.apache.pekko.util.Helpers

    def Timeout = org.apache.pekko.util.Timeout
    type Timeout = org.apache.pekko.util.Timeout
  }
}
