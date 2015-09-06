package gcm

import akka.actor.{ ActorSystem, ActorRef }
import spray.httpx.SprayJsonSupport
import spray.json.{ AdditionalFormats, JsObject, JsonFormat, DefaultJsonProtocol }

case class GCMConfig(
  apiKey: String,
  senderId: String,
  listener: ActorRef,
  system: Option[ActorSystem] = None,
  host: String,
  port: Int
)

case class Message[T](to: String, data: T)

object MessageJsonProtocol extends DefaultJsonProtocol {
  implicit def messageFormat[T: JsonFormat] = jsonFormat2(Message.apply[T])
}
