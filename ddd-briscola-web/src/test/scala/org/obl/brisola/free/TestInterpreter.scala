package org.obl.brisola.free

import scalaz.Free
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import org.obl.raz.Path
import org.obl.brisola.webtest.TestDecoders
import argonaut.JsonParser
import scalaz.{-\/, \/, \/-}
import com.ning.http.client.Response
import argonaut.DecodeJson
import org.obl.briscola.presentation.SiteMap

case class TestInterpreterConfig(secondsTimeout:Int, siteMapUrl:String, siteMapDecoder:DecodeJson[SiteMap])

sealed trait TestResult

case class Error(error:Throwable) extends TestResult
case class Assert(value: Boolean, description: String) extends TestResult

class TestInterpreter(config:TestInterpreterConfig) {
  
  def apply(step:Step.FreeStep[Any]):Seq[TestResult] = apply(step, Nil)
  
  private def apply(step:Step.FreeStep[Any], current:Seq[TestResult]):Seq[TestResult] = step.resume.fold( {
    
    case Check(v, desc, next) =>
      Assert(v,desc) +: apply(next, current)
    
    case HTTPCall(method, pth, body, next) =>
      val resp = httpCall(method, pth, body)
      resp match {
        case Failure(err) => current :+ Error(err)
        case Success(v) => apply(next(v), current)
      }
      
    case Parse(text, decoder, next) =>
      val resp = JsonParser.parse(text).flatMap( decoder.decodeJson(_).toDisjunction )
      resp match {
        case -\/(err) => current :+ Error(new RuntimeException(s"error parsing json, error: $err \n$text"))
        case \/-(v) => apply(next(v), current)
      }
    
    case GetSiteMap(next) =>
      val httpResp = httpCall(GET, config.siteMapUrl,  None)
      val resp = httpResp.map { httpResp =>
        httpResp.getResponseBody -> JsonParser.parse(httpResp.getResponseBody).flatMap( config.siteMapDecoder.decodeJson(_).toDisjunction ) 
      }
      resp match {
        case Failure(err) => current :+ Error(err)
        case Success((text, -\/(err))) => current :+ Error(new RuntimeException(s"error parsing json, error: $err \n$text"))
        case Success((text, \/-(v))) => apply(next(v), current)
      }
      
      
  }, _ => current)
 
  private def httpCall(method:HTTPMethod, pth:String, body:Option[String]):Try[Response] = {
    import dispatch._
    val req0 = url(pth).setMethod(method.toString).setContentType("application/json", "UTF-8")
    val req = body match {
      case None => req0
      case Some(bdy) => req0 << bdy
    }
    Await.ready(Http(req > (i => i)), Duration(config.secondsTimeout, SECONDS)).value.get
  }
}