package gcm

import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.http._
import spray.httpx.SprayJsonSupport
import spray.httpx.TransformerAux._
import spray.json._

import scala.concurrent.Future

class HttpGcm(
    config: GCMConfig
) extends SprayJsonSupport with AdditionalFormats {
  implicit val system = config.system.getOrElse(ActorSystem())
  import system.dispatcher

  val sendUri = "https://gcm-http.googlapis.com/gcm/send"

  val pipeline: HttpRequest => Future[HttpResponse] =
    addHeader("Authorization", s"key=${config.apiKey}") ~>
      sendReceive

  def isApiKeyValid: Future[Boolean] = {
    val content = """{"registration_ids": ["ABC"]}""".parseJson.asJsObject
    val req = pipeline(Post(sendUri, content))
    for (res <- req) yield res.status != StatusCodes.Unauthorized
  }

  // There is some scoping nastiness going on.
  // TODO Fix this.
  private def parseMessage[T: JsonFormat](msg: Message[T]): JsObject = {
    import MessageJsonProtocol._
    msg.toJson.asJsObject
  }

  def sendMessage[T: JsonFormat](msg: Message[T]): Future[HttpResponse] = {
    val content = parseMessage(msg)
    pipeline(Post(sendUri, content))
  }
}

