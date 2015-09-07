package gcm.http

import gcm.util.ToJson
import gcm.Notification
import org.json4s._
import org.json4s.JsonDSL._

object Priority {
  val High = "high"
  val Low = "low"
}

case class Message(
    to: String,
    registrationIds: List[String],
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
      ("registration_ids" -> registrationIds) ~
      ("collapse_key" -> collapseKey) ~
      ("priority" -> priority) ~
      ("content_available" -> contentAvailable) ~
      ("delay_while_idle" -> delayWhileIdle) ~
      ("time_to_live" -> timeToLive) ~
      ("restricted_package_name" -> restrictedPackageName) ~
      ("data" -> data.map(_.ast)) ~
      ("notification" -> notification.map(_.ast))
}
