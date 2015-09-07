package gcm.xmpp

import java.util.UUID

import gcm.util.ToJson
import gcm.Notification
import org.json4s.JsonAST
import org.json4s.JsonDSL._

/**
 * Downstream message
 * @param to The target (registration_id or notification_key) of the message
 * @param messageId Automatically generated UUID
 * @param collapseKey
 * @param priority
 * @param contentAvailable
 * @param delayWhileIdle
 * @param timeToLive
 * @param restrictedPackageName
 * @param data
 * @param notification
 */
case class Message(
    to: String,
    messageId: String = UUID.randomUUID.toString,
    registrationIds: Option[List[String]] = None,
    collapseKey: Option[String] = None,
    priority: Option[String] = None,
    contentAvailable: Option[Boolean] = None,
    delayWhileIdle: Option[Boolean] = None,
    timeToLive: Option[Int] = None,
    restrictedPackageName: Option[String] = None,
    data: Option[ToJson] = None,
    notification: Option[Notification] = None
) extends ToJson {
  override def ast: JsonAST.JObject =
    ("to" -> to) ~
      ("message_id" -> messageId) ~
      ("collapse_key" -> collapseKey) ~
      ("priority" -> priority) ~
      ("content_available" -> contentAvailable) ~
      ("delay_while_idle" -> delayWhileIdle) ~
      ("time_to_live" -> timeToLive) ~
      ("restricted_package_name" -> restrictedPackageName) ~
      ("data" -> data.map(_.ast)) ~
      ("notification" -> notification.map(_.ast))
}

object Messages {
  def sendToSync(to: String) = Message(to)

  def notification(to: String, notification: Notification) = Message(to, notification = Some(notification))

  def data(
    to: String,
    data: ToJson,
    registrationIds: Option[List[String]] = None,
    timeToLive: Option[Int] = None,
    delayWhileIdle: Option[Boolean] = None
  ) = Message(to, data = Some(data), registrationIds = registrationIds, timeToLive = timeToLive, delayWhileIdle = delayWhileIdle)
}
