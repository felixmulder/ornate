package com.novocode.ornate.theme

import java.io.FileNotFoundException
import java.net.{URL, URI}

import com.novocode.ornate._
import com.novocode.ornate.commonmark.NodeExtensionMethods._

import better.files._
import com.novocode.ornate.config.Global
import com.novocode.ornate.js.NashornSupport

import scala.collection.JavaConverters._

/** Base class for themes. */
abstract class Theme(global: Global) extends Logging {

  /** Render the site. May create additional synthetic pages and copy resources on demand. */
  def render(site: Site): Unit

  /** Synthesize configured synthetic pages pre-TOC. Not all requested pages have to be
    * created but only the ones that are returned will be available for resolving the TOC. */
  def synthesizePages(uris: Vector[(String, URI)]): Vector[Page] = Vector.empty

  /** Get synthetic page names and the mapped URIs for pages that should be created by the theme.
    * Any pages that have to be created before resolving the TOC should be part of this. */
  def syntheticPageURIs: Vector[(String, URI)] = {
    if(global.userConfig.theme.config.hasPath("global.pages")) {
      val co = global.userConfig.theme.config.getObject("global.pages")
      co.entrySet().asScala.iterator.filter(_.getValue.unwrapped ne null).map(e =>
        (e.getKey, Util.siteRootURI.resolve(e.getValue.unwrapped.asInstanceOf[String]))
      ).toVector
    } else Vector.empty
  }

  /** Resolve a resource URI to a URL. Resource URIs can use use the following protocols:
    * file, site (static site resources), webjar (absolute WebJar resource), theme (relative
    * to theme class), classpath (relative to classpath root) */
  def resolveResource(uri: URI): URL = uri.getScheme match {
    case "file" => uri.toURL
    case "site" => global.userConfig.resourceDir.path.toUri.resolve(uri.getPath.replaceFirst("^/*", "")).toURL
    case "webjar" =>
      val parts = uri.getPath.split('/').filter(_.nonEmpty)
      val path = NashornSupport.locator.getFullPathExact(parts.head, parts.tail.mkString("/"))
      if(path eq null) throw new FileNotFoundException("WebJar resource not found: "+uri)
      getClass.getClassLoader.getResource(path)
    case "theme" =>
      val url = getClass.getResource(uri.getPath.replaceFirst("^/*", ""))
      if(url eq null) throw new FileNotFoundException("Theme resource not found: "+uri)
      url
    case "classpath" =>
      val url = getClass.getClassLoader.getResource(uri.getPath)
      if(url eq null) throw new FileNotFoundException("Classpath resource not found: "+uri)
      url
    case _ => throw new IllegalArgumentException("Unsupported scheme in resource URI "+uri)
  }

  /** Get a default relative path for a resource URI */
  def suggestRelativePath(uri: URI, tpe: String): String = {
    val s = uri.getScheme match {
      case "file" => uri.getPath.split('/').last
      case "site" => global.userConfig.resourceDir.path.toUri.resolve(uri.getPath).getPath.replaceFirst("^/*", "")
      case _ => uri.getPath.replaceFirst("^/*", "")
    }
    val prefix = tpe + "/"
    if(s.startsWith(prefix) && s.length > prefix.length) s.substring(prefix.length)
    else s
  }
}

trait Resources {
  protected def mappings: Iterable[ResourceSpec]
  protected def page: Page
  protected def getURI(uri: URI, targetFile: String, keepLink: Boolean): URI

  final def get(path: String, targetFile: String = null, keepLink: Boolean = false): URI =
    Util.relativeSiteURI(page.uri, getURI(Util.themeRootURI.resolve(path), targetFile, keepLink))
  final def require(path: String, targetFile: String = null, keepLink: Boolean = true): Unit =
    get(path, targetFile, keepLink)
  final def links: Iterable[URI] =
    mappings.collect { case r: ResourceSpec if r.keepLink => Util.relativeSiteURI(page.uri, r.uri) }
}

case class ResourceSpec(sourceURI: URI, url: URL, uri: URI, keepLink: Boolean)
