package gcm.xmpp

import javax.net.ssl.SSLSocketFactory

import gcm.GcmConfig
import org.jivesoftware.smack._
import org.jivesoftware.smack.filter.StanzaFilter
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smack.tcp.{ XMPPTCPConnection, XMPPTCPConnectionConfiguration }

import scala.xml.Elem

class SmackGcm(config: GcmConfig) {
  val smackConf = {
    val host =
      if (config.testing)
        "gcm-preprod.googleapis.com"
      else
        "gcm.googleapis.com"

    XMPPTCPConnectionConfiguration.builder()
      .setDebuggerEnabled(true)
      .setHost(host)
      .setServiceName(host)
      .setPort(5236)
      .setSocketFactory(SSLSocketFactory.getDefault)
      .setUsernameAndPassword(config.senderId, config.apiKey)
      .build()
  }

  val conn = new XMPPTCPConnection(smackConf)

  conn.addConnectionListener(new ConnectionListener {
    val listener = config.listener

    override def connected(connection: XMPPConnection): Unit = {
      conn.login()
      listener.foreach(_ ! Connected())
    }

    override def reconnectionFailed(e: Exception): Unit = {
      listener.foreach(_ ! ReconnectionFailed(e))
    }

    override def reconnectionSuccessful(): Unit = {
      listener.foreach(_ ! ReconnectionSuccessful())
    }

    override def authenticated(connection: XMPPConnection, resumed: Boolean): Unit = {
      listener.foreach(_ ! Authenticated(connection, resumed))
    }

    override def connectionClosedOnError(e: Exception): Unit = {
      listener.foreach(_ ! ConnectionClosed(Some(e)))
    }

    override def connectionClosed(): Unit = {
      listener.foreach(_ ! ConnectionClosed())
    }

    override def reconnectingIn(seconds: Int): Unit = {
      listener.foreach(_ ! ReconnectingIn(seconds))
    }
  })

  conn.addAsyncStanzaListener(new StanzaListener {
    override def processPacket(packet: Stanza): Unit = {
      config.listener.foreach(_ ! packet)
    }
  }, new StanzaFilter {
    override def accept(stanza: Stanza): Boolean = true
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
