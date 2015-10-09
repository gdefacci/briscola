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

  def routes:ServletRoutes
  def plan:HttpService

  def servletPath:PathSg = routes.servletPath
}

trait ServletRoutes {
  
  def host:PathBase
  def contextPath:PathSg
  def servletPath:PathSg
  
  private[ServletRoutes] def servletPrefix = PathSg(contextPath.path ++ servletPath.path)
  
  implicit class PathWrapper(p:Path) {
    def decoderWrap:PathDecoder[Path] =
      Path(Path.baseOf(p), servletPrefix.add(p.path), p.params, p.fragment)
    
    def encodersWrap = {
      val p1 = Path(Some(host), p.path, p.params, p.fragment)
      Path.renderPrepend(servletPrefix, p1)
    }
  }
  
  implicit class PathCodecWrapper[D,E](codec:PathCodec[D,E]) {
    def decoderWrap:PathDecoder[D] =
      PathDecoder.prepend(servletPrefix, codec)
    
    def encodersWrap = 
      PathCodec.encoderAt(host, PathCodec.prependEncoder(servletPrefix, codec))
  }
  
  implicit class PathConverterWrapper[D,E,UT,P <: PathPosition, S <: PathPosition](converter:PathConverter[D,E,UT,P,S]) {
    def decoderWrap:PathDecoder[D] =
      PathDecoder.prepend(servletPrefix, converter)
    
    def encodersWrap = 
      PathConverter.encodersAt(host, PathConverter.prependEncoders(servletPrefix, converter))
      
  }
  
  
  
  
}