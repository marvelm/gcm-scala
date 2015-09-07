package gcm

import javax.net.ssl.SSLSocketFactory

import akka.actor.{ ActorPath, ActorRef }
import org.jivesoftware.smack._
import org.jivesoftware.smack.filter.StanzaFilter
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smack.tcp.{ XMPPTCPConnection, XMPPTCPConnectionConfiguration }

import scala.xml.{ Elem, Node }

class SmackGcm(config: GcmConfig) {
  val smackConf = XMPPTCPConnectionConfiguration.builder()
    .setDebuggerEnabled(true)
    .setHost("gcm-preprod.googleapis.com")
    .setPort(5236)
    .setSocketFactory(SSLSocketFactory.getDefault)
    .setUsernameAndPassword(config.senderId, config.apiKey)
    .setServiceName("gcm.googleapis.com")
    .build()

  val conn = new XMPPTCPConnection(smackConf)

  conn.addConnectionListener(new ConnectionListener {
    val listener = config.listener

    override def connected(connection: XMPPConnection): Unit = {
      conn.login()
      listener.foreach(_ ! ("Connected", connection))
    }

    override def reconnectionFailed(e: Exception): Unit = {
      listener.foreach(_ ! ("ReconnectionFailed", e))
    }

    override def reconnectionSuccessful(): Unit = {
      listener.foreach(_ ! "ReconnectionSuccessful")
    }

    override def authenticated(connection: XMPPConnection, resumed: Boolean): Unit = {
      listener.foreach(_ ! ("Authenticated", connection, resumed))
    }

    override def connectionClosedOnError(e: Exception): Unit = {
      listener.foreach(_ ! ("ConnectionClosedOnError", e))
    }

    override def connectionClosed(): Unit = {
      listener.foreach(_ ! "ConnectionClosed")
    }

    override def reconnectingIn(seconds: Int): Unit = {
      listener.foreach(_ ! ("ReconnectingIn", seconds))
    }
  })

  conn.addAsyncStanzaListener(new StanzaListener {
    override def processPacket(packet: Stanza): Unit = {
      packet.toString
      config.listener.foreach(_ ! packet)
    }
  }, new StanzaFilter {
    override def accept(stanza: Stanza): Boolean = {
      true
    }
  })

  def connect = conn.connect

  implicit class ScalaStanza(elem: Elem) extends Stanza {
    override val toXML = elem.toString()
  }

  def sendMessage(msg: Message) {
    conn.sendStanza(
      <gcm xmlns="google:mobile:data">
        { msg.toJsonString }
      </gcm>
    )
  }

  def sendRawStanza(elem: Elem) {
    conn.sendStanza(elem)
  }
}
