package it.unibo.stakeholder.model.channel.websocket

import io.vertx.lang.scala.json.{Json, JsonArray, JsonObject}
import it.unibo.stakeholder.model.channel.parser.ParserLike
import it.unibo.stakeholder.model.channel.rest.HttpCode
import it.unibo.stakeholder.model.channel.rest.vertx._

/**
 * A set of message and parser used by websocket to build an upper protocol to manage different
 * type of communications (e.g. request and response).
 *
 * REQUEST/RESPONSE schema:
 *
 * REQUEST JSON:
 * {
 *    id : #generated by the client incrementally making the request unique during a communication
 *    value : #the value that the client what to send to the server
 * }
 * RESPONSE JSON:
 * {
 *    id : #same of the client request, to associate this response to the request
 *    value : #STATUS_SCHEMA
 * }
 *
 * #STATUS_SCHEMA = #OK || #FAIL
 * #OK
 * {
 *  "status" : "ok"
 * }
 * #FAIL
 * {
 *  "status" : "failed"
 *  "reason" : "reason of failure"
 * }
 *
 * ASYNC UPDATE
 * {
 *  "updated" : ... # value
 * }
 */
object CitizenProtocol {

  /**
   * the response when the client speaks other "protocol" .
   */
  val unknown : JsonObject = Json.obj("reason" -> "unknown protocol", "code" -> HttpCode.InternalError.code)
  /**
   * in the request, some data category are unknown by the server
   */
  val unknownDataCategoryError : Failed = Failed("unknown data")
  /**
   * some internal operations (e.g. citizen dt updates) go wrong.
   */
  val internalError : Failed = Failed("internal error")
  /**
   * the operations requested can be satisfied due to permission problems linked to data category
   */
  val unauthorized : Failed = Failed("category not allowed")

  val responseParser = ParserLike[String, WebsocketResponse[Status]] {
    rep => Json.obj(
      "id" -> rep.id,
      "value" -> rep.value.toJson
    ).toString
  }{
    data => for {
      json <- JsonConversion.objectFromString(data)
      id <- json.getAsInt("id")
      valueJson <- json.getAsObject("value")
      status <- Status.fromJson(valueJson)
    } yield WebsocketResponse[Status](id, status)
  }

  val requestParser = ParserLike[String, WebsocketRequest[JsonArray]] {
    rep => Json.obj(
      "id" -> rep.id,
      "value" -> rep.value
    ).toString
  }{
    data => for {
      json <- JsonConversion.objectFromString(data)
      id <- json.getAsInt("id")
      value <- json.getAsArray("value")
    } yield WebsocketRequest[JsonArray](id, value)
  }

  val updateParser = ParserLike[String, WebsocketUpdate[JsonObject]]{
    rep => Json.obj(
      "updated" -> rep.value
    ).toString
  }{
    data => for {
      json <- JsonConversion.objectFromString(data)
      value <- json.getAsObject("updated")
      if Status.fromJson(value).isEmpty
    } yield WebsocketUpdate[JsonObject](value)
  }
}
