package gcm.util

import org.json4s.jackson.JsonMethods._
import org.json4s.{ DefaultFormats, JsonAST }

abstract class ToJson {
  implicit val formats = DefaultFormats

  def ast: JsonAST.JObject

  def toJson = render(ast)

  def toJsonString = compact(render(toJson))
}

abstract class FromJson[T] {
  implicit val formats = DefaultFormats

  def fromJson(json: JsonAST.JObject): T

  def fromJsonString(json: String): T
}