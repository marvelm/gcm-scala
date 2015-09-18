package gcm.xmpp

import org.jivesoftware.smack.XMPPConnection

abstract class Status

case object Connected extends Status

case class ReconnectionFailed(exception: Exception) extends Status

case object ReconnectionSuccessful extends Status

case class Authenticated(connection: XMPPConnection, resumed: Boolean) extends Status

case class ConnectionClosed(exception: Option[Exception] = None) extends Status

case class ReconnectingIn(seconds: Int) extends Status
