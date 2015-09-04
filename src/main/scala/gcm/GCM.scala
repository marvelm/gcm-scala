package gcm

import java.util.concurrent.TimeUnit

import scala.concurrent.Future

import akka.util.Timeout
import akka.actor.{ ActorRef, ActorSystem }

import spray.client.pipelining._
import spray.http._
import spray.http.Uri
import spray.can.Http
import spray.httpx.SprayJsonSupport
import spray.httpx.TransformerAux._
import spray.json._
import DefaultJsonProtocol._

case class GCMConfig(
  apiKey: String,
  system: Option[ActorSystem] = None,
  listener: ActorRef
)

case class Message[T](
  to: String,
  data: T
)
object Message

object MessageJsonProtocol extends DefaultJsonProtocol {
  implicit def messageFormat[T: JsonFormat] = jsonFormat2(Message.apply[T])
}

class GCM(
    config: GCMConfig
) extends SprayJsonSupport with AdditionalFormats {
  implicit val system = config.system.getOrElse(ActorSystem.apply)
  import system.dispatcher

  val sendUri = "https://gcm-http.googlapis.com/gcm/send"

  val pipeline: HttpRequest => Future[HttpResponse] =
    addHeader("Authorization", s"key=${config.apiKey}") ~>
      sendReceive

  def isApiKeyValid(): Future[Boolean] = {
    val content = """{"registration_ids": ["ABC"]}""".parseJson.asJsObject
    val req = pipeline(Post(sendUri, content))
    for (res <- req) yield res.status != StatusCodes.Unauthorized
  }

  // There is some scoping nastiness going on.
  // TODO Fix this.
  def parseMessage[T: JsonFormat](msg: Message[T]): JsObject = {
    import MessageJsonProtocol._
    msg.toJson.asJsObject
  }

  def sendMessage[T: JsonFormat](msg: Message[T]): Future[HttpResponse] = {
    val content = parseMessage(msg)
    pipeline(Post(sendUri, content))
  }
}
