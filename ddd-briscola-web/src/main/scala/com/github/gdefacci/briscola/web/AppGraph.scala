package com.github.gdefacci.briscola.web

import com.github.gdefacci.di.IOC
import com.github.gdefacci.di.graph._
import com.github.gdefacci.sgrafamento._
import com.github.gdefacci.briscola.presentation.BriscolaWebApp

object AppGraph extends scala.App {

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

  val cols = permOf3(List(170, 210, 250)).map(t => RGB(t._1, t._2, t._3)).toArray

  val packages: List[Package] = {
    val entities = List(
      Nil,
      List("game"),
      List("player"),
      List("competition"),
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

  val subGraphCols = permOf3(List(120, 130, 140)).map(t => RGB(t._1, t._2, t._3)).toArray
  val subGraphs = packages.zip(subGraphCols).map {
    case (pkg, col) =>
      SubGraph[Dependency]("cluster." + pkg.segments.mkString("."),
        (dep1, dep2) =>
          dep1.returnType.enclosingPackage == pkg && dep2.returnType.enclosingPackage == pkg,
        Seq(
          Color(col),
          Style.Filled,
          Label(pkg.segments.mkString("."))))
  }

  val renderer = DigraphRenderer(DigraphOptions[Dependency](
    d => d.dependencyId.id.toString,
    d => d.dependencies.map(_.id.toString),
    graphAttrs = Seq(
      Rankdir.LeftToRight),
    nodeAttrs = graphwizAttrs,
    supbgraphs = subGraphs //    rankings = rankings
    ))

  import modules._
  import com.github.gdefacci.briscola.service.impl
  import com.github.gdefacci.briscola.service.GameApp
  import com.github.gdefacci.briscola.service.GameAppModule

  val deps = {

    val gameApp = {

      IOC.get[GameApp](new GameAppModule, impl.simple.idFactories, new impl.simple.repositories)
    }

    IOC.graph[BriscolaWebApp](
      new GameLayerModule(gameApp),
      ConfModule,
      WebModules)

  }

  val deps1 = {

    IOC.graph[BriscolaWebApp](
      new GameAppModule,
      impl.simple.idFactories,
      new impl.simple.repositories,
      ConfModule,
      WebModules)
  }

  val deps2 = IOC.graph[GameApp](new GameAppModule, impl.simple.idFactories, new impl.simple.repositories)

  println("================")
  println(renderer("g1", deps2))

}