package com.twitter.finatra.http.internal.routing

import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.inject.conversions.iterable._
import com.twitter.util.Future
//import java.util.{HashMap => JMap}

import RouteTrie

private[http] object Routes {

  def createForMethod(routes: Seq[Route], method: Method) = {
    new Routes((routes filter { _.method == method }).toArray)
  }
}

// optimized
private[http] class Routes(routes: Array[Route]) {

  //Assert unique paths
  {
    val distinctRoutes = routes.toSeq.distinctBy { _.path }
    assert(
      routes.length == distinctRoutes.length,
      "Found non-unique routes " + routes.diff(distinctRoutes).map(_.summary).mkString(", ")
    )
  }

  private[this] val (constantRoutes, nonConstantRoutes) = {
    routes partition { _.constantRoute }
  }

  //Note we subtract 1 because our while loop starts at -1 and increments before the array lookup
  private[this] val nonConstantRoutesLimit = nonConstantRoutes.length - 1

  //change Jmap to Trie
//  private[this] val constantRouteMap: JMap[String, Route] = {
//    val jMap = new JMap[String, Route]()
//    for (route <- constantRoutes) {
//      jMap.put(route.path, route)
//    }
//    jMap
//  }
  private[this] var RouteTrie: RouteTrie = new RouteTrie()
  for (route <- constantRoutes){
    constantRouteMap.addRoute(route)
  }
  private[this] val constantROuteMap: RouteTrie = RouteTrie

  def handle(request: Request, bypassFilters: Boolean = false): Option[Future[Response]] = {
    val path = request.path // Store path since Request#path is derived
    val secondaryPath = if (!path.endsWith("/")) path + "/" else null

    val method = request.method

    // look for constant route matches
    val constantRouteResult = constantRouteMap.get(path, method)
    val secondaryConstantRouteResult = constantRouteMap.get(secondaryPath, method)
    if (constantRouteResult != null) {
      constantRouteResult.handleMatch(request, bypassFilters)
    } else if (secondaryPath != null && secondaryConstantRouteResult != null) {
      if (secondaryConstantRouteResult.hasOptionalTrailingSlash)
        secondaryConstantRouteResult.handleMatch(request, bypassFilters)
      else
        None
    } else {
      var response: Option[Future[Response]] = None
      var nonConstantRouteIdx = -1
      while (response.isEmpty && nonConstantRouteIdx < nonConstantRoutesLimit) {
        nonConstantRouteIdx += 1
        val currentRoute = nonConstantRoutes(nonConstantRouteIdx)
        response = currentRoute.handle(request, path, bypassFilters)
      }
      response
    }
  }
}
