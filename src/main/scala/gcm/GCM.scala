package gcm

import akka.actor.{ ActorSystem, ActorRef }

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

case class GcmConfig(
  apiKey: String,
  senderId: String,
  listener: Option[ActorRef] = None,
  system: Option[ActorSystem] = None,
  testing: Boolean = false
)

abstract class Message {
  private implicit val formats = DefaultFormats

  def ast: JsonAST.JObject

  def toJson = render(ast)

  def toJsonString = compact(render(toJson))
}

class SendToSync(to: String) extends Message {
  override val ast: JsonAST.JObject = "to" -> to
}

class Notification(
    to: String,
    title: String,
    text: String,
    timeToLive: Int
) extends Message {
  override val ast =
    ("to" -> to) ~
      ("title" -> title) ~
      ("notification" ->
        ("title" -> title) ~
        ("text" -> text)) ~
        ("time_to_live" -> timeToLive)
}

class Data(
    to: String,
    messageId: String,
    data: JValue,
    timeToLive: String,
    delayWhileIdle: Boolean,
    deliveryReceiptRequested: Boolean
) extends Message {
  override val ast =
    ("to" -> to) ~
      ("message_id" -> messageId) ~
      ("data" -> data) ~
      ("delay_while_idle" -> delayWhileIdle) ~
      ("delivery_receipt_requested" -> deliveryReceiptRequested)
}
