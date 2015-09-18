package gcm.xmpp

import javax.net.ssl.SSLSocketFactory

import akka.actor.{Props, ActorSystem}
import gcm.GcmConfig
import org.jivesoftware.smack._
import org.jivesoftware.smack.chat.{ Chat, ChatManager, ChatManagerListener, ChatMessageListener }
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smack.tcp.{ XMPPTCPConnection, XMPPTCPConnectionConfiguration }

import scala.xml.{ Elem, XML }

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

  implicit val system = config.system getOrElse ActorSystem()

  val conn = new XMPPTCPConnection(smackConf)

  conn.addConnectionListener(new ConnectionListener {
    val listener = config.listener getOrElse system.actorOf(Props.empty)

    override def connected(connection: XMPPConnection): Unit = {
      conn.login()
      listener ! Connected()
    }

    override def reconnectionFailed(e: Exception): Unit = {
      listener ! ReconnectionFailed(e)
    }

    override def reconnectionSuccessful(): Unit = {
      listener ! ReconnectionSuccessful()
    }

    override def authenticated(connection: XMPPConnection, resumed: Boolean): Unit = {
      listener ! Authenticated(connection, resumed)
    }

    override def connectionClosedOnError(e: Exception): Unit = {
      listener ! ConnectionClosed(Some(e))
    }

    override def connectionClosed(): Unit = {
      listener ! ConnectionClosed()
    }

    override def reconnectingIn(seconds: Int): Unit = {
      listener ! ReconnectingIn(seconds)
    }
  })

  // first circle of callback hell
  ChatManager.getInstanceFor(conn).addChatListener(new ChatManagerListener {
    override def chatCreated(chat: Chat, createdLocally: Boolean): Unit = {
      if (!createdLocally) {
        chat.addMessageListener(new ChatMessageListener {
          val user = conn.getUser
          override def processMessage(chat: Chat, message: packet.Message): Unit = {
            if (message.getFrom != user)
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
