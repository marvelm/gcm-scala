package gcm.xmpp

import javax.net.ssl.SSLSocketFactory

import gcm.GcmConfig
import org.jivesoftware.smack._
import org.jivesoftware.smack.chat.{ChatMessageListener, Chat, ChatManagerListener, ChatManager}
import org.jivesoftware.smack.filter.StanzaFilter
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smack.tcp.{ XMPPTCPConnection, XMPPTCPConnectionConfiguration }

import scala.xml.{XML, Elem}

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

  // first circle of callback hell
  ChatManager.getInstanceFor(conn).addChatListener(new ChatManagerListener {
    override def chatCreated(chat: Chat, createdLocally: Boolean): Unit = {
      if (!createdLocally) {
        chat.addMessageListener(new ChatMessageListener {
          val user = conn.getUser
          override def processMessage(chat: Chat, message: packet.Message): Unit = {
            if (message.getFrom != user )
              config.listener.foreach(_ ! XML.loadString(message.toString))
          }
        })
      }
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
