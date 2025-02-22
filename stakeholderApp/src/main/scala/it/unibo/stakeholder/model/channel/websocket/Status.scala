package it.unibo.stakeholder.model.channel.websocket

import io.vertx.lang.scala.json.{Json, JsonObject}

/**
 * an application protocol part for websocket communication.
 * Describe the result of a request
 */
sealed trait Status

/**
 * the operation has been complete successfully
 * @param dataId The data id created.
 */
case class Ok(dataId : Seq[String]) extends Status

/**
 * the operation fail. This object contains the reason of the failure.
 * @param reason The reason associated to this operation fail.
 */
case class Failed(reason : String) extends Status
object Status {
  //some utilities for status management
  import it.unibo.stakeholder.model.channel.rest.vertx._

  implicit class RichStatus(s : Status) {
    def toJson : JsonObject = s match {
      case Ok(elem) => Json.obj("status" -> "ok", "id" -> Json.arr(elem:_*))
      case Failed(r) => Json.obj("status" -> "failed", "reason" -> r)
    }
  }

  def fromJson(obj : JsonObject) : Option[Status] = obj.getAsString("status").flatMap {
    case "ok" => obj.getAsArray("id").flatMap(elem => elem.getAsStringSeq).map(Ok)
    case "failed" => obj.getAsString("reason").map(Failed)
    case _ => None
  }
}
