package gcm

import javax.net.ssl.SSLSocketFactory

import gcm.GCMConfig
import org.jivesoftware.smack._
import org.jivesoftware.smack.debugger.{ ConsoleDebugger, JulDebugger, ReflectionDebuggerFactory }
import org.jivesoftware.smack.filter.StanzaFilter
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smack.tcp.{ XMPPTCPConnection, XMPPTCPConnectionConfiguration }

class Smack(config: GCMConfig) {
  SmackConfiguration.DEBUG = true
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
    override def connected(connection: XMPPConnection): Unit = {
      println("Connected")
      conn.login()
    }

    override def reconnectionFailed(e: Exception): Unit = e.printStackTrace()

    override def reconnectionSuccessful(): Unit = println("Reconnected")

    override def authenticated(connection: XMPPConnection, resumed: Boolean): Unit = println("Authenticated")

    override def connectionClosedOnError(e: Exception): Unit = e.printStackTrace()

    override def connectionClosed(): Unit = println("Connection closed")

    override def reconnectingIn(seconds: Int): Unit = println(seconds)
  })
  conn.addAsyncStanzaListener(new StanzaListener {
    override def processPacket(packet: Stanza): Unit = {
      config.listener ! packet
      println(packet)
    }
  }, new StanzaFilter {
    override def accept(stanza: Stanza): Boolean = {
      true
    }
  })

  //SmackConfiguration.getDebuggerFactory.create(conn, null, null)
  conn.connect()
}

