package redis

private[redis] object RediscalaCompat {

  type ConfigurationException = akka.ConfigurationException

  object actor {
    type Actor = akka.actor.Actor

    type ActorRefFactory = akka.actor.ActorRefFactory

    type ActorLogging = akka.actor.ActorLogging

    type ActorRef = akka.actor.ActorRef

    def ActorSystem = akka.actor.ActorSystem
    type ActorSystem = akka.actor.ActorSystem

    def Kill = akka.actor.Kill

    def OneForOneStrategy = akka.actor.OneForOneStrategy

    def Props = akka.actor.Props

    def PoisonPill = akka.actor.PoisonPill

    type SupervisorStrategy = akka.actor.SupervisorStrategy

    val Terminated = akka.actor.Terminated
    type Terminated = akka.actor.Terminated

    object SupervisorStrategy {
      def Stop = akka.actor.SupervisorStrategy.Stop
    }
  }

  object event {
    def Logging = akka.event.Logging
  }

  object io {
    val Tcp = akka.io.Tcp
    def IO = akka.io.IO
  }

  object util {
    def ByteString = akka.util.ByteString
    type ByteString = akka.util.ByteString

    type ByteStringBuilder = akka.util.ByteStringBuilder

    def Helpers = akka.util.Helpers

    def Timeout = akka.util.Timeout
    type Timeout = akka.util.Timeout
  }
}
