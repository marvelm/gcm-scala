package gcm

import gcm.util.ToJson
import org.json4s.JsonAST
import org.json4s.JsonDSL._

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
) extends ToJson {
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

