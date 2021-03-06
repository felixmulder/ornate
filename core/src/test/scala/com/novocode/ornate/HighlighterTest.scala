package com.novocode.ornate

import java.net.URI

import com.novocode.ornate.highlight.{HighlightTarget, NoHighlighter, HighlightResult, HighlightJSHighlighter}
import com.novocode.ornate.config.Global
import org.junit.Test
import org.junit.Assert._

import better.files._
import play.twirl.api.HtmlFormat

class HighlighterTest {
  val startDir = file"doc"
  val global = new Global(startDir, startDir / "ornate.conf")
  val p1 = PageParser.parseWithFrontMatter(None, global.referenceConfig, new URI("site:/p1"), "", "p1 content")
  val p2 = PageParser.parseWithFrontMatter(None, global.referenceConfig, new URI("site:/p2"), "",
    """---
      |highlight.highlightjs.fenced: rust
      |---
      |p2 content
    """.stripMargin)
  val p3 = PageParser.parseWithFrontMatter(None, global.referenceConfig, new URI("site:/p3"), "",
    """---
      |highlight.highlightjs.fenced: [json, scala, rust]
      |---
      |p3 content
    """.stripMargin)
  val p4 = PageParser.parseWithFrontMatter(None, global.referenceConfig, new URI("site:/p4"), "",
    """---
      |highlight.highlightjs.fenced: null
      |---
      |p4 content
    """.stripMargin)

  @Test def testHighlightJS: Unit = {
    val hl = new HighlightJSHighlighter(global, global.userConfig.highlight)
    def check(p: Page, src: String, lang: Option[String], exp: String, expLang: Option[String]): Unit = {
      val hlr = hl.highlightTextAsHTML(src, lang, HighlightTarget.FencedCodeBlock, p)
      assertEquals(HtmlFormat.raw(exp), hlr.html)
      assertEquals(expLang, hlr.language)
    }

    check(p1,
      """val x = "<foo>"""", Some("scala"),
      """<span class="hljs-keyword">val</span> x = <span class="hljs-string">"&lt;foo&gt;"</span>""", Some("scala"))
    check(p2,
      """val x = "<foo>"""", None,
      """val x = <span class="hljs-string">"&lt;foo&gt;"</span>""", Some("rust"))
    check(p3,
      """val x = "<foo>"""", Some("scala"),
      """<span class="hljs-keyword">val</span> x = <span class="hljs-string">"&lt;foo&gt;"</span>""", Some("scala"))
    check(p3,
      """val x = "<foo>"""", None,
      """<span class="hljs-keyword">val</span> x = <span class="hljs-string">"&lt;foo&gt;"</span>""", Some("scala"))
    check(p4,
      """val x = "<foo>"""", None,
      """val x = &quot;&lt;foo&gt;&quot;""", None)
    check(p2,
      """val x = "<foo>"""", Some("text"),
      """val x = &quot;&lt;foo&gt;&quot;""", None)
    check(p1,
      """val x = "<foo>"""", None,
      """<span class="hljs-keyword">val</span> x = <span class="hljs-string">"&lt;foo&gt;"</span>""", Some("scala"))
    check(p1,
      """val x = "<foo>"""", Some("foo"),
      """val x = &quot;&lt;foo&gt;&quot;""", None)
  }

  @Test def testNoHighlight: Unit = {
    val hl = new NoHighlighter(global, null)
    val exp = """val x = &quot;&lt;foo&gt;&quot;"""
    assertEquals(HighlightResult(HtmlFormat.raw(exp), None),
      hl.highlightTextAsHTML("val x = \"<foo>\"", None, HighlightTarget.FencedCodeBlock, p1))
  }
}
