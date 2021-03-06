package com.novocode.ornate

import java.net.URI

import com.novocode.ornate.commonmark.PageProcessor
import com.typesafe.config.Config
import org.commonmark.node._
import org.commonmark.parser.Parser

class Site(val pages: Vector[Page], val toc: Vector[TocEntry]) {
  private[this] val pageMap: Map[String, Page] = pages.map(p => (p.uri.getPath, p)).toMap

  def getPageFor(uri: URI): Option[Page] =
    if(uri.getScheme == Util.siteRootURI.getScheme) pageMap.get(uri.getPath)
    else None
}

class Page(val sourceFileURI: Option[URI], val uri: URI, val suffix: String, val doc: Node, val config: Config,
           val section: PageSection, val extensions: Extensions, parser: Parser) {
  override def toString: String = s"Page($uri)"

  def uriWithSuffix(ext: String): URI = Util.replaceSuffix(uri, suffix, ext)

  var processors: Seq[PageProcessor] = null // Initialized by Main after the Site object has been built

  def applyProcessors(): Unit = processors.foreach(_.apply(this))

  def parseAndProcessSnippet(content: String): Page = {
    val doc = parser.parse(content)
    val snippetPage = new Page(None, uri, suffix, doc, config, section, extensions, parser)
    processors.foreach(_.apply(snippetPage))
    snippetPage
  }
}

sealed abstract class Section {
  def children: Vector[Section]
  def level: Int
  final def findFirstHeading: Option[HeadingSection] = {
    val it = allHeadings
    if(it.hasNext) Some(it.next) else None
  }
  def allHeadings: Iterator[HeadingSection] =
    children.iterator.flatMap(_.allHeadings)
  def getTitle: Option[String]
  def getID: Option[String]
}

final case class HeadingSection(id: String, level: Int, title: String, children: Vector[Section])(val heading: Heading) extends Section {
  override def allHeadings: Iterator[HeadingSection] =
    Iterator(this) ++ super.allHeadings
  def getTitle = Some(title)
  def getID = Some(id)
}

final case class UntitledSection(level: Int, children: Vector[Section]) extends Section {
  def getTitle = None
  def getID = None
}

final case class PageSection(title: Option[String], children: Vector[Section]) extends Section {
  def level = 0
  def getTitle = title
  def getID = None
}

case class TocEntry(val page: Page, val title: Option[String])
