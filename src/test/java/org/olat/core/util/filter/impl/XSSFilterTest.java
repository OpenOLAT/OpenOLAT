/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.util.filter.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter.Variant;

/**
 * Description:<br>
 * This test case tests the cross site scripting filter
 * 
 * <P>
 * Initial Date:  14.07.2009 <br>
 * @author gnaegi
 * @author Roman Haag, roman.haag@frentix.com
 */
@RunWith(JUnit4.class)
public class XSSFilterTest {

	protected Filter vFilter;
	private int counter;
	private int testsToRun;

	@Before
	public void setup() {
		vFilter = new OWASPAntiSamyXSSFilter(-1, true);
		counter = 0;
		testsToRun = 0;
	}

	@After
	public void tearDown() {
		vFilter = null;
		System.out.println("Run " + counter + " out of " + testsToRun + " testcases successfully.\n\n");
	}

	private void t(String input, String result) {
		t(input, result, vFilter);
	}

	private void t(String input, String result, Filter f) {
		String filterRes = f.filter(input);
		if (filterRes == result || filterRes.equals(result)){
			counter ++;
			System.out.println("------------------------------------------------");
		} else {
			System.out.println("---------------- E R R O R ---------------------");
		}
		System.out.println("           Expected: " + result);
		System.out.println("************************************************\n\n");
		Assert.assertEquals(result, filterRes);
	}

	@Test
	public void test_basics() {
		testsToRun = 8;
		t(null,null);
		t("", "");
		t("hello", "hello");
		t("°+\"*ç%&/()=?`", "&deg;+&quot;*&ccedil;%&amp;/()=?`");
		t("Du &amp; ich", "Du &amp; ich");
		t("Du & ich", "Du &amp; ich");
		t("1<2", "1&lt;2");
		t("2>1", "2&gt;1");
		t("&nbsp;","&nbsp;");
	}

	@Test
	public void test_balancing_tags() {
		testsToRun = 9;
		t("<b>hello", "<b>hello</b>");
		t("<b>hello", "<b>hello</b>");
		t("hello<b>", "hello");
		t("hello</b>", "hello");
		t("hello<b/>", "hello");
		t("<b><b><b>hello", "<b><b><b>hello</b></b></b>");
		t("</b><b>", "");
		t("<b><i>hello</b>", "<b><i>hello</i></b>");
		t("<b><i><em>hello</em></b>", "<b><i><em>hello</em></i></b>");
	}

	@Test
	public void test_end_slashes() {
		testsToRun = 3;
		t("<img>", "<img />");
		t("<img/>", "<img />");
		t("<b/></b>", "");
	}

	@Test
	public void test_balancing_angle_brackets() {
		testsToRun = 9;
		t("<img src=\"foo\"", "<img src=\"foo\" />");
		t("b>", "b&gt;");
		t("<img src=\"foo\"/", "<img src=\"foo\" />");
		t(">", "&gt;");
		//FIXME: what to do? it should work if in another tag!
//		t("foo<b", "foo&lt;b");
//		t("<span>foo<b</span>", "<span>foo<b</span>");
//		t("b>foo", "b&gt;foo");
//		t("><b", "&gt;&lt;b");
//		t("><f", "&gt;&lt;f");
		t("b><", "b&gt;&lt;");
		t("><b>", "&gt;");
	}
	
	@Test
	public void test_attributes() {
		testsToRun = 6;
		t("<img src=foo>", "<img src=\"foo\" />");
		t("<img asrc=foo>", "<img />");
		t("<span       title=\"bli\"  >&nbsp;</span>", "<span title=\"bli\">&nbsp;</span>");
		t("<img src=test test>", "<img src=\"test\" />");
		t("<img src=\"blibla\" alt=\"blubb\">", "<img alt=\"blubb\" src=\"blibla\" />");
		//alt cannot contain < , title will allow it for jsMath
		t("<img src=\"blibla\" alt=\"a>b\">", "<img src=\"blibla\" />"); 
	}

	@Test
	public void test_disallow_script_tags() {
		testsToRun = 14;
		t("script", "script");
		t("<script>", "");
//		t("<script", "&lt;script");
		t("<script", "");
		t("<script/>", "");
		t("</script>", "");
		t("<script woo=yay>", "");
		t("<script woo=\"yay\">", "");
		t("<script woo=\"yay>", "");
		t("<script woo=\"yay<b>", "");
		t("<script<script>>", "");
		t("<<script>script<script>>", "&lt;");
		t("<<script><script>>", "&lt;");
		t("<<script>script>>", "&lt;");
		t("<<script<script>>", "&lt;");
	}

	@Test
	public void test_protocols() {
		testsToRun = 11;
		t("<a href=\"http://foo\">bar</a>", "<a href=\"http://foo\">bar</a>");
		// we don't allow ftp. 
		//FIXME: is this ok? (strip link)
//		t("<a href=\"ftp://foo\">bar</a>", "<a href=\"#foo\">bar</a>");
		t("<a href=\"ftp://foo\">bar</a>", "bar");
		t("<a href=\"mailto:foo\">bar</a>",	"<a href=\"mailto:foo\">bar</a>");
		t("<a href=\"javascript:foo\">bar</a>", "bar");
		t("<a href=\"java script:foo\">bar</a>", "bar");
		t("<a href=\"java\tscript:foo\">bar</a>", "bar");
		t("<a href=\"java\nscript:foo\">bar</a>", "bar");
		t("<a href=\"java" + String.valueOf((char) 1) + "script:foo\">bar</a>", "bar");
		t("<a href=\"jscript:foo\">bar</a>", "bar");
		t("<a href=\"vbscript:foo\">bar</a>", "bar");
		t("<a href=\"view-source:foo\">bar</a>", "bar");
	}
	
	@Test
	public void test_link() {
		testsToRun = 1;
		t("<a href=\"blibla.html\" alt=\"blub\" target=\"_blank\">new window link</A>", "<a alt=\"blub\" href=\"blibla.html\" target=\"_blank\">new window link</a>");
	}
	
	@Test
	public void test_link_htmlEntities() {
		testsToRun = 1;
		t("<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&amp;gen--496\">new window link</a>", "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&amp;gen--496\">new window link</a>");
		t("<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&auml;gen--496\">new window link</a>", "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&auml;gen--496\">new window link</a>");
		t("<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&aacute;gen--496\">new window link</a>", "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&aacute;gen--496\">new window link</a>");
		
		//escape unkown entity
		t("<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&xss;gen--496\">new window link</a>", "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&amp;xss;gen--496\">new window link</a>");
		//check if escaped result is allowed
		t("<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&amp;xss;gen--496\">new window link</a>", "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&amp;xss;gen--496\">new window link</a>");
	}
	
	@Test
	public void test_link_complexer(){
		testsToRun = 1;
		t("<a class=\"o_icon_link_extern\" target=\"_blank\" href=\"http://www.frentix.com\" onclick=\"javascript:alert('hallo');\" title=\"a good link\">a complicated link</a>",
				"<a class=\"o_icon_link_extern\" href=\"http://www.frentix.com\" target=\"_blank\" title=\"a good link\">a complicated link</a>");
	}

	@Test
	public void test_self_closing_tags() {
		testsToRun = 3;
		t("<img src=\"a\">", "<img src=\"a\" />");
		t("<img src=\"a\">foo</img>", "<img src=\"a\" />foo");
		t("</img>", "");
	}

	@Test
	public void test_comments() {
		testsToRun = 4;
		t("<!-- a<b --->", "");
		t("<!-- a<b -->don't remove me<!-- hello world -->", "don't remove me");
		t("<!-- a<b  \n <!-- hello world \n -->", "");
		t("<!--comments1--> visible text <!--comments2-->", " visible text ");
	}
	
	@Test
	public void test_tiny_paragraph(){
		testsToRun = 8;
		t("<span>bliblablu</span>", "<span>bliblablu</span>");
		t("<p style=\"text-align: right;\">right orientation</p>", "<p style=\"text-align: right;\">right orientation</p>");
		t("<h1>Big font</h1>", "<h1>Big font</h1>");
		t("<h7>small font</h7>", "small font");
		t("<span style=\"font-family: wingdings;\">invalid font</span>", "<span style=\"font-family: wingdings;\">invalid font</span>");
		t("<span style=\"font-family: serif;\">invalid font</span>", "<span style=\"font-family: serif;\">invalid font</span>");
		//FIXME:RH: to allow multiple fonts (as output from tiny)
		//committed as bug: http://code.google.com/p/owaspantisamy/issues/detail?id=49
//		t("<span style=\"font-family: serif, arial;\">preformated</span>", "<span style=\"font-family: courier new , courier;\">preformated</span>");
		t("<span class=\"schoen\">irgendwas</span>", "<span class=\"schoen\">irgendwas</span>");
	}

	@Test
	public void test_tiny_lists(){
		testsToRun = 2;
		//lists (output without \n as policy has formatOutput = false		
		t("<ul>\n<li>a list: adsf</li>\n<li>adsf</li>\n<li>adsfas</li>\n</ul>", "<ul>\n<li>a list: adsf</li>\n<li>adsf</li>\n<li>adsfas</li>\n</ul>");
		t("<ol style=\"font-size: 20pt;\">\n<li>numbered list</li>\n<li>adf</li>\n<li>asdfa</li>\n</ol>", "<ol style=\"font-size: 20.0pt;\">\n<li>numbered list</li>\n<li>adf</li>\n<li>asdfa</li>\n</ol>");
	}

	@Test
	public void test_tiny_tables(){
		testsToRun = 2;
	//tables
		t("<table border=\"1\" style=\"width: 268px; height: 81px;\" class=\"table\">\n<caption>bliblablue</caption>\n<tbody>\n<tr>\n<td>\n<p>adsfadsf</p>\n</td>\n<td>asdf</td>\n</tr>\n<tr>\n<td>asf</td>\n<td>\n<p>asdf</p>\n</td>\n</tr>\n</tbody>\n</table>", "<table border=\"1\" class=\"table\" style=\"width: 268.0px;height: 81.0px;\">\n<caption>bliblablue</caption>\n<tbody>\n<tr>\n<td>\n<p>adsfadsf</p>\n</td>\n<td>asdf</td>\n</tr>\n<tr>\n<td>asf</td>\n<td>\n<p>asdf</p>\n</td>\n</tr>\n</tbody>\n</table>");
		t("<tr style=\"background-color: rgb(46, 147, 209);\">\n<td style=\"border: 1px solid rgb(240, 68, 14);\">asf</td>\n<td>\n<p>asdf</p>\n</td>\n</tr>","\nasf\n\n<p>asdf</p>\n");
	}
	
	@Test
	public void test_tiny_singleElements(){
		testsToRun = 1;
		//sup/sub
		t("<p><sup>super</sup>script <sub>sub</sub>script</p>", "<p><sup>super</sup>script <sub>sub</sub>script</p>");

		
	}

	@Test
	public void test_tiny_jsmath(){
		testsToRun = 2;
		t("<span title=\"a%20%3C%20b%20%3E%20c%20%3C%20/b%20%3E\">&nbsp;</span>","<span title=\"a%20%3C%20b%20%3E%20c%20%3C%20/b%20%3E\">&nbsp;</span>");
		// should be saved with entities not with < etc...
//		t("<span title=\"a>b\">&nbsp;</span>", "<span title=\"a&gt;b\">&nbsp;</span>");
	}
	
	@Test
	public void test_font_awesome() {
		// for now i tags must have at least a space to not b removed
		t("<i class=\"o_icon o_icon_dev\"> </i> ", "<i class=\"o_icon o_icon_dev\"> </i> ");
	}

	@Test
	public void test_figure() {
		// for now i tags must have at least a space to not b removed
		t("<figure class=\"image\"><img src=\"bla.png\" /><figcaption>gugs</figcaption></figure>", "<figure class=\"image\"><img src=\"bla.png\" /><figcaption>gugs</figcaption></figure>");
	}
	

	@Test
	public void test_big_tiny_output(){
		testsToRun = 1;
		String input = "<br>";
		String output = "<br />";
		t(input,output);
	}
	
	@Test
	public void test_rawText() {
		OWASPAntiSamyXSSFilter intlFilter = new OWASPAntiSamyXSSFilter(-1, false, Variant.tinyMce, true);
		t("Stéphane Rossé", "Stéphane Rossé", intlFilter);
	}
	
	@Test
	public void test_rawTextAttaqu() {
		OWASPAntiSamyXSSFilter intlFilter = new OWASPAntiSamyXSSFilter(-1, false, Variant.tinyMce, true);
		t("&lt;script&gt;alert('hello');&lt;//script&gt;", "&lt;script&gt;alert('hello');&lt;//script&gt;", intlFilter);
	}

}
