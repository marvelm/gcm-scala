package gcm.http

import akka.actor.ActorSystem
import gcm.GcmConfig
import spray.client.pipelining._
import spray.http.ContentTypes._
import spray.http.HttpMethods._
import spray.http._
import spray.httpx.TransformerAux._

import scala.concurrent.Future

class HttpGcm(
    config: GcmConfig
) {
  implicit val system = config.system getOrElse ActorSystem(s"HttpGcm ${config.senderId}")

  import system.dispatcher

  val sendUri = "https://gcm-http.googlapis.com/gcm/send"

  val pipeline: HttpRequest => Future[HttpResponse] =
    addHeader("Authorization", s"key=${config.apiKey}") ~>
      sendReceive

  private def jsonRequest(content: String) =
    HttpRequest(
      method = POST,
      uri = sendUri,
      entity = HttpEntity(`application/json`, content)
    )

  def isApiKeyValid: Future[Boolean] = {
    val content = """{"registration_ids": ["ABC"]}"""
    val req = pipeline(jsonRequest(content))
    for (res <- req) yield res.status != StatusCodes.Unauthorized
  }

  def sendMessage(msg: Message): Future[HttpResponse] =
    pipeline(jsonRequest(msg.toJsonString))
}

