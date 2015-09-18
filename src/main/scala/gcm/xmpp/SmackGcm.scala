package gcm.xmpp

import javax.net.ssl.SSLSocketFactory

import akka.actor.{ Props, ActorSystem }
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

  implicit val system = config.system getOrElse ActorSystem(s"SmackGcm ${config.senderId}")

  val conn = new XMPPTCPConnection(smackConf)

  // Create empty actor if listener is None
  val listener = config.listener getOrElse system.actorOf(Props.empty)

  conn.addConnectionListener(new ConnectionListener {
    override def connected(connection: XMPPConnection) {
      conn.login()
      listener ! Connected
    }

    override def reconnectionFailed(e: Exception) {
      listener ! ReconnectionFailed(e)
    }

    override def reconnectionSuccessful() {
      listener ! ReconnectionSuccessful
    }

    override def authenticated(connection: XMPPConnection, resumed: Boolean) {
      listener ! Authenticated(connection, resumed)
    }

    override def connectionClosedOnError(e: Exception) {
      listener ! ConnectionClosed(Some(e))
    }

    override def connectionClosed() {
      listener ! ConnectionClosed
    }

    override def reconnectingIn(seconds: Int) {
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
              listener ! XML.loadString(message.toString)
          }
        })
      }
    }
  })

  def connect = conn.connect

  implicit class ScalaStanza(elem: Elem) extends Stanza {
    override val toXML = elem.toString()
  }

  def sendMessage(msg: Message) =
    conn.sendStanza(
      <gcm xmlns="google:mobile:data">
        { msg.toJsonString }
      </gcm>
    )

  def sendRawStanza(elem: Elem) = conn.sendStanza(elem)
}
