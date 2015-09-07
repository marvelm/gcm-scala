package gcm

import javax.net.ssl.SSLSocketFactory

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

  val listener = config.listener

  conn.addConnectionListener(new ConnectionListener {
    override def connected(connection: XMPPConnection): Unit = {
      conn.login()
      listener ! ("Connected", connection)
    }

    override def reconnectionFailed(e: Exception): Unit ={
      listener ! ("ReconnectionFailed", e)
    }

    override def reconnectionSuccessful(): Unit ={
      listener ! "ReconnectionSuccessful"
    }

    override def authenticated(connection: XMPPConnection, resumed: Boolean): Unit = {
      listener ! ("Authenticated", connection, resumed)
    }

    override def connectionClosedOnError(e: Exception): Unit = {
      listener ! ("ConnectionClosedOnError", e)
    }

    override def connectionClosed(): Unit = {
      listener ! "ConnectionClosed"
    }

    override def reconnectingIn(seconds: Int): Unit = {
      listener ! ("ReconnectingIn", seconds)
    }
  })

  conn.addAsyncStanzaListener(new StanzaListener {
    override def processPacket(packet: Stanza): Unit = {
      config.listener ! packet
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
