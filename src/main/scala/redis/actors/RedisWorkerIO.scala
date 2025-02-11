package redis.actors

import java.net.InetSocketAddress
import redis.RediscalaCompat.actor.Actor
import redis.RediscalaCompat.actor.ActorLogging
import redis.RediscalaCompat.actor.ActorRef
import redis.RediscalaCompat.io.IO
import redis.RediscalaCompat.io.Tcp
import redis.RediscalaCompat.io.Tcp.*
import redis.RediscalaCompat.io.Tcp.CommandFailed
import redis.RediscalaCompat.io.Tcp.Connect
import redis.RediscalaCompat.io.Tcp.Connected
import redis.RediscalaCompat.io.Tcp.Received
import redis.RediscalaCompat.io.Tcp.Register
import redis.RediscalaCompat.util.ByteString
import redis.RediscalaCompat.util.ByteStringBuilder
import scala.concurrent.duration.*

abstract class RedisWorkerIO(val address: InetSocketAddress, onConnectStatus: Boolean => Unit, connectTimeout: Option[FiniteDuration] = None)
    extends Actor
    with ActorLogging {

  private var currAddress = address

  import context.*

  val tcp: ActorRef = IO(Tcp)(using context.system)

  // todo watch tcpWorker
  private var tcpWorker: ActorRef = null

  val bufferWrite: ByteStringBuilder = new ByteStringBuilder

  private var readyToWrite = false

  override def preStart(): Unit = {
    if (tcpWorker != null) {
      tcpWorker ! Close
    }
    log.info(s"Connect to $currAddress")
    // Create a new InetSocketAddress to clear the cached IP address.
    currAddress = new InetSocketAddress(currAddress.getHostName, currAddress.getPort)
    tcp ! Connect(remoteAddress = currAddress, options = SO.KeepAlive(on = true) :: Nil, timeout = connectTimeout)
  }

  def reconnect(): Unit = {
    become(receive)
    preStart()
  }

  override def postStop(): Unit = {
    log.info("RedisWorkerIO stop")
  }

  def initConnectedBuffer(): Unit = {
    readyToWrite = true
  }

  def receive: Receive = connecting orElse writing

  def connecting: Receive = {
    case a: InetSocketAddress => onAddressChanged(a)
    case c: Connected => onConnected(c)
    case Reconnect => reconnect()
    case c: CommandFailed => onConnectingCommandFailed(c)
    case c: ConnectionClosed => onClosingConnectionClosed() // not the current opening connection
  }

  def onConnected(cmd: Connected): Unit = {
    sender() ! Register(self)
    tcpWorker = sender()
    initConnectedBuffer()
    tryInitialWrite() // TODO write something in head buffer
    become(connected)
    log.info("Connected to " + cmd.remoteAddress)
    onConnectStatus(true)
  }

  def onConnectingCommandFailed(cmdFailed: CommandFailed): Unit = {
    log.error(cmdFailed.toString)
    scheduleReconnect()
  }

  def connected: Receive = writing orElse reading

  private def reading: Receive = {
    case WriteAck => tryWrite()
    case Received(dataByteString) =>
      if (sender() == tcpWorker)
        onDataReceived(dataByteString)
      else
        onDataReceivedOnClosingConnection(dataByteString)
    case a: InetSocketAddress => onAddressChanged(a)
    case c: ConnectionClosed =>
      if (sender() == tcpWorker)
        onConnectionClosed(c)
      else {
        onConnectStatus(false)
        onClosingConnectionClosed()
      }
    case c: CommandFailed => onConnectedCommandFailed(c)
  }

  def onAddressChanged(addr: InetSocketAddress): Unit = {
    log.info(s"Address change [old=$address, new=$addr]")
    tcpWorker ! ConfirmedClose // close the sending direction of the connection (TCP FIN)
    currAddress = addr
    scheduleReconnect()
  }

  def onConnectionClosed(c: ConnectionClosed): Unit = {
    log.warning(s"ConnectionClosed $c")
    scheduleReconnect()
  }

  /** O/S buffer was full
    * Maybe to much data in the Command ?
    */
  def onConnectedCommandFailed(commandFailed: CommandFailed): Unit = {
    log.error(commandFailed.toString) // O/S buffer was full
    tcpWorker ! commandFailed.cmd
  }

  def scheduleReconnect(): Unit = {
    cleanState()
    log.info(s"Trying to reconnect in $reconnectDuration")
    this.context.system.scheduler.scheduleOnce(reconnectDuration, self, Reconnect)
    become(receive)
  }

  def cleanState(): Unit = {
    onConnectStatus(false)
    onConnectionClosed()
    readyToWrite = false
    bufferWrite.clear()
  }

  def writing: Receive

  def onConnectionClosed(): Unit

  def onDataReceived(dataByteString: ByteString): Unit

  def onDataReceivedOnClosingConnection(dataByteString: ByteString): Unit

  def onClosingConnectionClosed(): Unit

  def onWriteSent(): Unit

  def restartConnection(): Unit = reconnect()

  def onConnectWrite(): ByteString

  def tryInitialWrite(): Unit = {
    val data = onConnectWrite()

    if (data.nonEmpty) {
      writeWorker(data ++ bufferWrite.result())
      bufferWrite.clear()
    } else {
      tryWrite()
    }
  }

  def tryWrite(): Unit = {
    if (bufferWrite.length == 0) {
      readyToWrite = true
    } else {
      writeWorker(bufferWrite.result())
      bufferWrite.clear()
    }
  }

  def write(byteString: ByteString): Unit = {
    if (readyToWrite) {
      writeWorker(byteString)
    } else {
      bufferWrite.append(byteString)
    }
  }

  def reconnectDuration: FiniteDuration = 2.seconds

  private def writeWorker(byteString: ByteString): Unit = {
    onWriteSent()
    tcpWorker ! Write(byteString, WriteAck)
    readyToWrite = false
  }

}
