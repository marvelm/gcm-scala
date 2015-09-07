package gcm.http

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

object Priority {
  val High = "high"
  val Low = "low"
}

abstract class RenderJson {
  private implicit val formats = DefaultFormats

  def ast: JsonAST.JObject

  def toJson = render(ast)

  def toJsonString = compact(render(toJson))
}

case class Notification(
    title: String,
    body: Option[String] = None,
    icon: String,
    sound: Option[String] = None,
    badge: Option[String] = None,
    tag: Option[String] = None,
    color: Option[String] = None,
    clickAction: Option[String] = None,
    bodyLocKey: Option[String] = None,
    bodyLocArgs: Option[String] = None,
    titleLocKey: Option[String] = None,
    titleLocArgs: Option[String] = None
) extends RenderJson {
  override def ast: JsonAST.JObject =
    ("title" -> title) ~
      ("body" -> body) ~
      ("icon" -> icon) ~
      ("sound" -> sound) ~
      ("badge" -> badge) ~
      ("tag" -> tag) ~
      ("color" -> color) ~
      ("click_action" -> clickAction) ~
      ("body_loc_key" -> bodyLocKey) ~
      ("body_loc_args" -> bodyLocArgs) ~
      ("title_loc_key" -> titleLocKey) ~
      ("title_loc_args" -> titleLocArgs)
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
    data: Option[RenderJson] = None,
    notification: Option[Notification] = None
) extends RenderJson {
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
