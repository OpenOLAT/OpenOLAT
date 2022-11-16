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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.owasp.html.PolicyFactory;

/**
 * Description:<br>
 * This test case tests the cross site scripting filter
 * 
 * <P>
 * Initial Date:  14.07.2009 <br>
 * @author gnaegi
 * @author Roman Haag, roman.haag@frentix.com
 */
@RunWith(Parameterized.class)
public class XSSFilterParamTest {

	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
        	{ "", "" },
	        { "hello", "hello" },
			{ "<a href=\"mailto:foo@frentix.com\">bar</a>",	"<a href=\"mailto:foo&#64;frentix.com\">bar</a>" },
			{ "°+\"*ç%&/()=?`", "°&#43;&#34;*ç%&amp;/()&#61;?&#96;" },
			{ "Du &amp; ich", "Du &amp; ich" },
			{ "Du & ich", "Du &amp; ich" },
			{ "1<2", "1&lt;2" },
			{ "2>1", "2&gt;1" },
			{ "&nbsp;","\u00A0" },// was &nbsp;
			// test_balancing_tags
			{ "<b>hello", "<b>hello</b>" },
			{ "<b>hello", "<b>hello</b>" },
			{ "hello<b>", "hello<b></b>" },// was skipped <b> // 10
			{ "hello</b>", "hello" },
			{ "hello<b/>", "hello<b></b>" },// was skipped <b> // 12
			{ "<b><b><b>hello", "<b><b><b>hello</b></b></b>" },
			{ "</b><b>", "<b></b>" }, // was skipped
			{ "<b><i>hello</b>", "<b><i>hello</i></b>" },
			{ "<b><i><em>hello</em></b>", "<b><i><em>hello</em></i></b>" },
			// test_end_slashes()
			{ "<img src='test.html' />", "<img src=\"test.html\" />" },
			{ "<img/>", "<img />" },
/* 20 */	{ "<b/></b>", "<b></b>" }, // was empty
			// test_balancing_angle_brackets()
			{ "<img src=\"foo\"", "<img src=\"foo\" />" },
			{ "b>", "b&gt;" },
			{ "<img src=\"foo\"/", "<img src=\"foo\" />" },
			{ ">", "&gt;" },
			{ "foo<b", "foo<b></b>" },
			{ "<span>foo<b</span>", "<span>foo<b></b></span>" },
			{ "b>foo", "b&gt;foo" },
			{ "><b", "&gt;<b></b>" },
			{ "><f", "&gt;" },
/* 30 */	{ "b><", "b&gt;&lt;" },
			{ "><b>", "&gt;<b></b>" },
			// test_attributes()
			{ "<img src=foo>", "<img src=\"foo\" />" },
			{ "<img asrc=foo>", "<img />" },
			{ "<span       title=\"bli\"  >&nbsp;</span>", "<span title=\"bli\">\u00A0</span>" },
			{ "<img src=test test>", "<img src=\"test%20test\" />" },
			{ "<img src=\"blibla\" alt=\"blubb\">", "<img src=\"blibla\" alt=\"blubb\" />" },
			//alt cannot contain < , title will allow it for jsMath
			{ "<img src=\"blibla\" alt=\"a>b\">", "<img src=\"blibla\" />" }, 
			// test_disallow_script_tags()
			{ "script", "script" },
			{ "<script>", "" },
	//		{ "<script", "&lt;script" },
/* 40 */	{ "<script", "" },
			{ "<script/>", "" },
			{ "</script>", "" },
			{ "<script woo=yay>", "" },
			{ "<script woo=\"yay\">", "" },
			{ "<script woo=\"yay>", "" },
			{ "<script woo=\"yay<b>", "" },
			{ "<script<script>>", "" },
			{ "<<script>script<script>>", "&lt;" },
			{ "<<script><script>>", "&lt;" },
/* 50 */	{ "<<script>script>>", "&lt;" },
			{ "<<script<script>>", "&lt;" },
			// test_protocols()
			{ "<a href=\"http://foo\">bar</a>", "<a href=\"http://foo\">bar</a>" },
			// we don't allow ftp. 
			{ "<a href=\"ftp://foo\">bar</a>", "<a>bar</a>" },
			{ "<a href=\"ftp://foo\">bar</a>", "<a>bar</a>" },
			{ "<a href=\"mailto:foo\">bar</a>",	"<a href=\"mailto:foo\">bar</a>" },
			{ "<a href=\"javascript:foo\">bar</a>", "<a>bar</a>" },
			{ "<a href=\"java script:foo\">bar</a>", "<a>bar</a>" },
			{ "<a href=\"java\tscript:foo\">bar</a>", "<a>bar</a>" },
			{ "<a href=\"java\nscript:foo\">bar</a>", "<a>bar</a>" },
/* 60 */	{ "<a href=\"java" + String.valueOf((char) 1) + "script:foo\">bar</a>", "<a>bar</a>" },
			{ "<a href=\"jscript:foo\">bar</a>", "<a>bar</a>" },
			{ "<a href=\"vbscript:foo\">bar</a>", "<a>bar</a>" },
			{ "<a href=\"view-source:foo\">bar</a>", "<a>bar</a>" },
			// test_link() {
			{ "<a href=\"blibla.html\" alt=\"blub\" target=\"_blank\">new window link</A>", "<a href=\"blibla.html\" alt=\"blub\" target=\"_blank\" rel=\"noopener noreferrer\">new window link</a>" },
			// test_link_htmlEntities() {
			{ "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&amp;gen--496\">new window link</a>", "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&amp;gen--496\">new window link</a>" },
			{ "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&auml;gen--496\">new window link</a>", "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S\u00E4gen--496\">new window link</a>" },
			{ "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&agrave;gen--496\">new window link</a>", "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S\u00E0gen--496\">new window link</a>" },
			
			//escape unkown entity
			{ "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&xss;gen--496\">new window link</a>", "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&amp;xss;gen--496\">new window link</a>" },
			//check if escaped result is allowed
			{ "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&amp;xss;gen--496\">new window link</a>", "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&amp;xss;gen--496\">new window link</a>" },
			// test_link_complexer(){
/* 70 */	{ "<a class=\"o_icon_link_extern\" target=\"_blank\" href=\"http://www.frentix.com\" onclick=\"javascript:alert('hallo');\" title=\"a good link\">a complicated link</a>",
					"<a class=\"o_icon_link_extern\" target=\"_blank\" href=\"http://www.frentix.com\" title=\"a good link\" rel=\"noopener noreferrer\">a complicated link</a>" },
			// test_self_closing_tags() {
			{ "<img src=\"a\">", "<img src=\"a\" />" },
			{ "<img src=\"a\">foo</img>", "<img src=\"a\" />foo" },
			{ "</img>", "" },
			// test_comments()
			{ "<!-- a<b --->", "" },
			{ "<!-- a<b -->don't remove me<!-- hello world -->", "don&#39;t remove me" },
			{ "<!-- a<b  \n <!-- hello world \n -->", "" },
			{ "<!--comments1--> visible text <!--comments2-->", " visible text " },
			// test_tiny_paragraph()
			{ "<span>bliblablu</span>", "<span>bliblablu</span>" },
			{ "<p style=\"text-align: right;\">right orientation</p>", "<p style=\"text-align:right\">right orientation</p>" },
/* 80 */	{ "<h1>Big font</h1>", "<h1>Big font</h1>" },
			{ "<h7>small font</h7>", "small font" },
			{ "<span style=\"font-family: wingdings;\">Wingdings font</span>", "<span style=\"font-family:&#39;wingdings&#39;\">Wingdings font</span>" },
			{ "<span style=\"font-family: serif;\">Serif font</span>", "<span style=\"font-family:serif\">Serif font</span>" },
			{ "<span style=\"font-family: serif, arial;\">preformated</span>", "<span style=\"font-family:serif , &#39;arial&#39;\">preformated</span>" },
			{ "<span class=\"schoen\">irgendwas</span>", "<span class=\"schoen\">irgendwas</span>" },
			// test_style_rgb(){
			{ "<p style=\"background-color: rgb(0%,0,0);\">background</p>", "<p style=\"background-color:rgb( 0% , 0 , 0 )\">background</p>" },
			{ "<p style=\"background-color: rgba(100%,0,0);\">background</p>", "<p style=\"background-color:rgba( 100% , 0 , 0 )\">background</p>" },
			{ "<p style=\"background-color: rgb(100,50,50);\">background</p>", "<p style=\"background-color:rgb( 100 , 50 , 50 )\">background</p>" },
			// test_tiny_lists(){
			//lists (output without \n as policy has formatOutput = false		
			{ "<ul>\n<li>a list: adsf</li>\n<li>adsf</li>\n<li>adsfas</li>\n</ul>", "<ul><li>a list: adsf</li><li>adsf</li><li>adsfas</li></ul>" },
/* 90 */	{ "<ol style=\"font-size: 20pt;\">\n<li>numbered list</li>\n<li>adf</li>\n<li>asdfa</li>\n</ol>", "<ol style=\"font-size:20pt\"><li>numbered list</li><li>adf</li><li>asdfa</li></ol>" },
			// test_tiny_tables()
			//tables
			{ "<table border=\"1\" style=\"width: 268px; height: 81px;\" class=\"table\">\n<caption>bliblablue</caption>\n<tbody>\n<tr>\n<td>\n<p>adsfadsf</p>\n</td>\n<td>asdf</td>\n</tr>\n<tr>\n<td>asf</td>\n<td>\n<p>asdf</p>\n</td>\n</tr>\n</tbody>\n</table>",
				"<table border=\"1\" style=\"width:268px;height:81px\" class=\"table\"><caption>bliblablue</caption><tbody><tr><td>\n<p>adsfadsf</p>\n</td><td>asdf</td></tr><tr><td>asf</td><td>\n<p>asdf</p>\n</td></tr></tbody></table>" },
			{ "<tr style=\"background-color: rgb(46, 147, 209);\">\n<td style=\"border: 1px solid rgb(240, 68, 14);\">asf</td>\n<td>\n<p>asdf</p>\n</td>\n</tr>",
					"<table><tbody><tr style=\"background-color:rgb( 46 , 147 , 209 )\"><td style=\"border:1px solid rgb( 240 , 68 , 14 )\">asf</td><td>\n<p>asdf</p>\n</td></tr></tbody></table>" },
			// test_tiny_singleElements(){
			//sup/sub
			{ "<p><sup>super</sup>script <sub>sub</sub>script</p>", "<p><sup>super</sup>script <sub>sub</sub>script</p>" },
			// test_tiny_jsmath(){
			{ "<span title=\"a%20%3C%20b%20%3E%20c%20%3C%20/b%20%3E\">&nbsp;</span>","<span title=\"a%20%3C%20b%20%3E%20c%20%3C%20/b%20%3E\">\u00A0</span>" },
			// should be saved with entities not with < etc...
			//{ "<span title=\"a&gt;b\">&nbsp;</span>", "<span title=\"a&gt;b\">&nbsp;</span>" },
			// test_font_awesome() {
			// for now i tags must have at least a space to not b removed
			{ "<i class=\"o_icon o_icon_dev\"> </i> ", "<i class=\"o_icon o_icon_dev\"> </i> " },
			// test_figure() {
			// for now i tags must have at least a space to not b removed
			{ "<figure class=\"image\"><img src=\"bla.png\" /><figcaption>gugs</figcaption></figure>", "<figure class=\"image\"><img src=\"bla.png\" /><figcaption>gugs</figcaption></figure>" },
			// more
			{ "&lt;script&gt;alert('hello');&lt;//script&gt;", "&lt;script&gt;alert(&#39;hello&#39;);&lt;//script&gt;" },
			{ "Stéphane Rossé", "Stéphane Rossé" },
			{ "<a href=\"http://localhost/win?test=go&go=test\">Test</a>",
				"<a href=\"http://localhost/win?test&#61;go&amp;go&#61;test\">Test</a>" },
/* 100 */	{ "<img src=\"/olat/edusharing/preview?objectUrl=ccrep://OpenOLAT/d5130470-14b4-4ad4-88b7-dfb3ebe943da&version=1.0\" data-es_identifier=\"2083dbe64f00b07232b11608ec0842fc\" data-es_objecturl=\"ccrep://OpenOLAT/d5130470-14b4-4ad4-88b7-dfb3ebe943da\" data-es_version=\"1.0\" data-es_version_current=\"1.0\" data-es_mediatype='i23' data-es_mimetype=\"image/png\" data-es_width=\"1000\" data-es_height=\"446\" data-es_first_edit=\"false\" class=\"edusharing\" alt=\"Bildschirmfoto 2018-11-07 um 16.09.49.png\" title=\"Bildschirmfoto 2018-11-07 um 16.09.49.png\" width=\"1000\" height=\"446\">",
				"<img src=\"/olat/edusharing/preview?objectUrl&#61;ccrep://OpenOLAT/d5130470-14b4-4ad4-88b7-dfb3ebe943da&amp;version&#61;1.0\" data-es_identifier=\"2083dbe64f00b07232b11608ec0842fc\" data-es_objecturl=\"ccrep://OpenOLAT/d5130470-14b4-4ad4-88b7-dfb3ebe943da\" data-es_version=\"1.0\" data-es_version_current=\"1.0\" data-es_mediatype=\"i23\" data-es_mimetype=\"image/png\" data-es_width=\"1000\" data-es_height=\"446\" data-es_first_edit=\"false\" class=\"edusharing\" alt=\"Bildschirmfoto 2018-11-07 um 16.09.49.png\" title=\"Bildschirmfoto 2018-11-07 um 16.09.49.png\" width=\"1000\" height=\"446\" />"	
			},
			{ "<a href=\"javascript:parent.gotonode(100055283652712)\">Test</a>", "<a href=\"javascript:parent.gotonode(100055283652712)\">Test</a>" },
			{ "<a href='javascript:parent.gototool(\"blog\")'>Blog</a>", "<a href=\"javascript:parent.gototool(&#34;blog&#34;)\">Blog</a>" },
			{ "<a href=\"javascript:parent.gototool('blog')\">Blog</a>", "<a href=\"javascript:parent.gototool(&#39;blog&#39;)\">Blog</a>" },
			{ "<a href=\"media/LTT ZUJ SCM 09.09.2019.pdf\">doc</a>", "<a href=\"media/LTT%20ZUJ%20SCM%2009.09.2019.pdf\">doc</a>" },
			{ "<a href=\"media/LTT%20ZUJ%20SCM%2009.09.2019.pdf\">doc</a>", "<a href=\"media/LTT%20ZUJ%20SCM%2009.09.2019.pdf\">doc</a>" },
			{ "<p><img class=\"b_float_left\" src=\"media/IMG 1484.jpg\" width=\"74\" height=\"74\" /></p>", "<p><img class=\"b_float_left\" src=\"media/IMG%201484.jpg\" width=\"74\" height=\"74\" /></p>" },
			// link with anchor
			{ "<a href=\"#Summary\">Summary</a>", "<a href=\"#Summary\">Summary</a>" },
			{ "<a href=\"#Title_1\">Title 1</a>", "<a href=\"#Title_1\">Title 1</a>" },
			{ "<a href=\"#Title 1\">Title with space</a>", "<a>Title with space</a>" },
			{ "<a href=\"#Title#1\">Title with #</a>", "<a>Title with #</a>" },
			// video tag
			{ "<video src=\"http://localhost/win/video.mp4\" width=\"320\" height=\"240\"></video>", "<video src=\"http://localhost/win/video.mp4\" width=\"320\" height=\"240\"></video>" },
			{ "<audio src=\"http://localhost/win/video.mp4\"></audio>", "<audio src=\"http://localhost/win/video.mp4\"></audio>" },
			// some variables found in mails (exact match)
			{ "<a href=\"$courseUrl\">Href with variable</a>", "<a href=\"$courseUrl\">Href with variable</a>" },
			{ "<a href=\"$groupUrl\">Href with variable</a>", "<a href=\"$groupUrl\">Href with variable</a>" },
			{ "<a href=\"$curriculumUrl\">Href with variable</a>", "<a href=\"$curriculumUrl\">Href with variable</a>" },
			{ "<a href=\"$courseUrlNotValid\">Href with variable</a>", "<a>Href with variable</a>" },
			{ "<a href=\"not$courseUrl\">Href with variable</a>", "<a>Href with variable</a>" },
			
			{ null, "" } // be tolerant	
        });
    }
    
    private String input;
    private String output;
    
    public XSSFilterParamTest(String input, String output) {
    	this.input = input;
    	this.output = output;
    }
    
    @Test
	public void filter() throws Exception {
		PolicyFactory policy = OpenOLATPolicy.POLICY_DEFINITION;
		String sanitized = policy.sanitize(input);
		Assert.assertEquals(output, sanitized);
	} 
}
