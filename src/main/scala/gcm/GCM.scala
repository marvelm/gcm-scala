package gcm

import java.util.concurrent.TimeUnit

import scala.concurrent.Future

import akka.util.Timeout
import akka.actor.ActorSystem

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
  system: Option[ActorSystem] = None
)

class GCM(
  config: GCMConfig
) {
  implicit val system = config.system.getOrElse(ActorSystem.apply)
  import akka.io.IO
  import akka.pattern.ask
  import system.dispatcher

  val root = Uri("https://gcm-http.googlapis.com/gcm")
  val sendUri = root / "send"

  val pipeline: HttpRequest => Future[HttpResponse] =
    addHeader("Authorization", s"key=${config.apiKey}") ~>
      sendReceive

  def isApiKeyValid(): Future[Boolean] = {
    val req = pipeline(Post(send, """{"registration_ids": ["ABC"]}""".toJson))
    for (res <- req) yield
      res.status != StatusCodes.Unauthorized
  }
}
