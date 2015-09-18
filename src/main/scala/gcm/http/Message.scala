package gcm.http

import gcm.Notification
import gcm.util.ToJson
import org.json4s.JsonDSL._
import org.json4s._

object Priority {
  val High = "high"
  val Low = "low"
}

case class Message(
  to: String,
  registrationIds: Option[List[String]] = None,
  collapseKey: Option[String] = None,
  priority: Option[String] = None,
  contentAvailable: Option[Boolean] = None,
  delayWhileIdle: Option[Boolean] = None,
  timeToLive: Option[Int] = None,
  restrictedPackageName: Option[String] = None,
  data: Option[JValue] = None,
  notification: Option[Notification] = None
)
    extends ToJson {
  override def ast: JsonAST.JObject =
    ("to" -> to) ~
      ("registration_ids" -> registrationIds) ~
      ("collapse_key" -> collapseKey) ~
      ("priority" -> priority) ~
      ("content_available" -> contentAvailable) ~
      ("delay_while_idle" -> delayWhileIdle) ~
      ("time_to_live" -> timeToLive) ~
      ("restricted_package_name" -> restrictedPackageName) ~
      ("data" -> data) ~
      ("notification" -> notification.map(_.ast))
}

object Messages {
  def sendToSync(to: String) = Message(to)

  def notification(to: String, notification: Notification) = Message(to, notification = Some(notification))

  def data(
    to: String,
    data: JValue,
    timeToLive: Option[Int] = None,
    delayWhileIdle: Option[Boolean] = None
  ) =
    Message(to, data = Some(data), timeToLive = timeToLive, delayWhileIdle = delayWhileIdle)
}
