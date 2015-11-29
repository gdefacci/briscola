package org.obl.briscola.web.util

import org.http4s.server._
import org.obl.raz.PathSg
import org.obl.raz.PathBase
import org.obl.raz.PathCodec
import org.obl.raz.AbsolutePath
import org.obl.raz.PathPosition
import org.obl.raz.PathConverter
import org.obl.raz.Path
import org.obl.raz.PathDecoder

trait Plan {

  def plan: HttpService

}

trait ServletPlan extends Plan {
  protected def routes: ServletRoutes
  
  def servletPath: PathSg = routes.servletPath
}

class BiPath(val decodePath:Path, val encodePath:Path) {
  
  private val pathDecoder = PathDecoder.fromPath(decodePath)
  
  def render = encodePath.render
  
  def unapply[P](pth:P)(implicit extPth:org.obl.raz.ext.ExtPathDecode[P]):Option[Path] = {
    val p = extPth(pth)
    pathDecoder.decodeFull(p).toOption
  }
  
}

trait PrefixedRoutes {

  def host: PathBase
  protected def prefix:PathSg

  implicit class PathWrapper(p: Path) {
    def decoderWrap: PathDecoder[Path] =
      Path(Path.baseOf(p), prefix.add(p.path), p.params, p.fragment)

    def encodersWrap = { 
      val enc = Path(Some(host), prefix.add(p.path), p.params, p.fragment)
      val dec = Path(None, p.path, p.params, p.fragment)
      new BiPath(dec, enc)
    }
  }

  implicit class PathCodecWrapper[D, E](codec: PathCodec[D, E]) {
    def decoderWrap: PathDecoder[D] =
      PathDecoder.prepend(prefix, codec)

    def encodersWrap =
      PathCodec.encoderAt(host, PathCodec.prependEncoder(prefix, codec))
  }

  implicit class PathConverterWrapper[D, E, UT, P <: PathPosition, S <: PathPosition](converter: PathConverter[D, E, UT, P, S]) {
    def decoderWrap: PathConverter[D, E, UT, P, S] =
      PathConverter[D, E, UT, P, S](PathDecoder.prepend(prefix, converter), converter, converter)

    def encodersWrap =
      PathConverter.encodersAt(host, PathConverter.prependEncoders(prefix, converter))

  }

}

trait ServletRoutes extends PrefixedRoutes {

  def host: PathBase
  def contextPath: PathSg
  def servletPath: PathSg

  protected def prefix = PathSg(contextPath.path ++ servletPath.path)

}

trait WebSocketRoutes extends PrefixedRoutes {

  def host: PathBase
  def contextPath: PathSg

  protected def prefix = PathSg(contextPath.path)

}