package com.github.gdefacci.briscola.web

import com.github.gdefacci.di.IOC
import com.github.gdefacci.briscola.service.GameApp
import com.github.gdefacci.briscola.service.GameAppModule

import modules._
import com.github.gdefacci.sgrafamento._
import com.github.gdefacci.di.graph.Dependency
import com.github.gdefacci.briscola.presentation.BriscolaWebApp

object WebAppDIMain extends App {

  import com.github.gdefacci.briscola.service.impl
  
  val gameApp = IOC.get[GameApp](new GameAppModule, impl.simple.idFactories, new impl.simple.repositories)

  val deps = IOC.graph[BriscolaWebApp](
new GameLayerModule(IOC.get[GameApp](new GameAppModule, impl.simple.idFactories, new impl.simple.repositories)),
      ConfModule,
      WebModules)
  
//  println(IOC.get[BriscolaWebApp](
//    new GameAppModule,
//    impl.simple.idFactories,
//    new impl.simple.repositories,
//    RoutesModule,
//    PlayersModule,
//    GameModule,
//    CompetitionModule,
//    SiteMapModule,
//    ConfModule,
//    JsonEncodeModule,
//    WebModule))

//  println(IOC.getSource[BriscolaWebApp](
//    new GameAppModule,
//    impl.simple.idFactories,
//    new impl.simple.repositories,
//    RoutesModule,
//    PlayersModule,
//    GameModule,
//    CompetitionModule,
//    SiteMapModule,
//    ConfModule,
//    JsonEncodeModule,
//    WebModule))

  val deps1:List[Dependency] = IOC.graph[BriscolaWebApp](
    new GameAppModule,
    impl.simple.idFactories,
    new impl.simple.repositories,
      ConfModule,
      WebModules)
//  println(deps.sortBy(_.dependencyId.id).mkString("\n"))

    
  println(IOC.getSource[BriscolaWebApp](
    new GameAppModule,
    impl.simple.idFactories,
    new impl.simple.repositories,
        ConfModule,
      WebModules))

  import com.github.gdefacci.di.graph._

  def permOf3[T](seq: List[T]): List[(T, T, T)] = {
    for {
      x <- seq
      y <- seq
      z <- seq
    } yield (x, y, z)
  }
  
  def getTypeName(typ: Type): String = {
    typ match {
      case PolymorphicType(owner, nm, args) => s"$nm[${args.map(getTypeName).mkString(", ")}]"
      case x => x.name
    }
  }

  val cols = permOf3(List(190, 210, 250)).map(t => RGB(t._1, t._2, t._3)).toArray

  val packages: List[Package] = {
    val entities = List(
      Nil,
      List("game"),
      List("player"),
      List("competition"),
      List("sitemap"),
      List("tournament"))
    val layers = List(
      Nil,
      List("service"),
      List("presentation"))
    val prefix = List("com", "github", "gdefacci", "briscola")
    layers.flatMap { l =>
      entities.map(ents => prefix ::: l ::: ents)
    }.map(Package) :+ Package(prefix :+ "web")
  }

  val packgColsMap = packages.zip(cols).toMap

  def graphwizAttrs(dep: Dependency): Seq[NodeAttr] = {

    val white = RGB(255, 255, 255)
    val typeName = getTypeName(dep.returnType)
    val fillColor = packgColsMap.get(dep.returnType.enclosingPackage).getOrElse(white)

    Seq(
      Label(typeName),
      Shape.Box,
      Style.Filled,
      FillColor(fillColor))
  }

  val rankings = packages.map { pkg =>
    RankValue.Same[Dependency](dep => dep.returnType.enclosingPackage == pkg)
  }
  
  // case class SubGraph[T](name: String, attrs:Seq[GraphAttr], predicate: T => Boolean)
  val subGraphCols = permOf3(List(200, 130, 240)).map(t => RGB(t._1, t._2, t._3)).toArray
  val subGraphs = packages.zip(subGraphCols).map { 
    case (pkg, col) =>
      SubGraph[Dependency]("cluster."+pkg.segments.mkString("."),  
          (dep1, dep2) => 
            dep1.returnType.enclosingPackage == pkg && dep2.returnType.enclosingPackage == pkg,
          Seq(
              Color(col),
              Style.Filled,
              Label(pkg.segments.mkString("."))
          )
      )
  }
  
  val renderer = DigraphRenderer(DigraphOptions[Dependency](
    d => d.dependencyId.id.toString,
    d => d.dependencies.map(_.id.toString),
    graphAttrs = Seq(
        Rankdir.LeftToRight
    ),
    nodeAttrs = graphwizAttrs,
    supbgraphs = subGraphs
//    rankings = rankings
    ))

  println(renderer("g1", deps))
  
  
}