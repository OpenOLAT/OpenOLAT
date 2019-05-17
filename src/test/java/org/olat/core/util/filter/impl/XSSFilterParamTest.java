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
import org.olat.core.util.filter.Filter;

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
        	{ null, null },
        	{ "", "" },
	        { "hello", "hello" },
			{ "°+\"*ç%&/()=?`", "&deg;+&quot;*&ccedil;%&amp;/()=?`" },
			{ "Du &amp; ich", "Du &amp; ich" },
			{ "Du & ich", "Du &amp; ich" },
			{ "Du @ ich", "Du @ ich" },
			{ "1<2", "1&lt;2" },
			{ "2>1", "2&gt;1" },
			{ "&nbsp;","&nbsp;" },
			// test_balancing_tags
			{ "<b>hello", "<b>hello</b>" },
			{ "<b>hello", "<b>hello</b>" },
			{ "hello<b>", "hello" },
			{ "hello</b>", "hello" },
			{ "hello<b/>", "hello" },
			{ "<b><b><b>hello", "<b><b><b>hello</b></b></b>" },
			{ "</b><b>", "" },
			{ "<b><i>hello</b>", "<b><i>hello</i></b>" },
			{ "<b><i><em>hello</em></b>", "<b><i><em>hello</em></i></b>" },
			// test_end_slashes()
			{ "<img>", "<img />" },
			{ "<img/>", "<img />" },
			{ "<b/></b>", "" },
			// test_balancing_angle_brackets()
			{ "<img src=\"foo\"", "<img src=\"foo\" />" },
			{ "b>", "b&gt;" },
			{ "<img src=\"foo\"/", "<img src=\"foo\" />" },
			{ ">", "&gt;" },
			//FIXME: what to do? it should work if in another tag!
	//		{ "foo<b", "foo&lt;b" },
	//		{ "<span>foo<b</span>", "<span>foo<b</span>" },
	//		{ "b>foo", "b&gt;foo" },
	//		{ "><b", "&gt;&lt;b" },
	//		{ "><f", "&gt;&lt;f" },
			{ "b><", "b&gt;&lt;" },
			{ "><b>", "&gt;" },
			// test_attributes()
			{ "<img src=foo>", "<img src=\"foo\" />" },
			{ "<img asrc=foo>", "<img />" },
			{ "<span       title=\"bli\"  >&nbsp;</span>", "<span title=\"bli\">&nbsp;</span>" },
			{ "<img src=test test>", "<img src=\"test\" />" },
			{ "<img src=\"blibla\" alt=\"blubb\">", "<img alt=\"blubb\" src=\"blibla\" />" },
			//alt cannot contain < , title will allow it for jsMath
			{ "<img src=\"blibla\" alt=\"a>b\">", "<img src=\"blibla\" />" }, 
			// test_disallow_script_tags()
			{ "script", "script" },
			{ "<script>", "" },
	//		{ "<script", "&lt;script" },
			{ "<script", "" },
			{ "<script/>", "" },
			{ "</script>", "" },
			{ "<script woo=yay>", "" },
			{ "<script woo=\"yay\">", "" },
			{ "<script woo=\"yay>", "" },
			{ "<script woo=\"yay<b>", "" },
			{ "<script<script>>", "" },
			{ "<<script>script<script>>", "&lt;" },
			{ "<<script><script>>", "&lt;" },
			{ "<<script>script>>", "&lt;" },
			{ "<<script<script>>", "&lt;" },
			// test_protocols()
			{ "<a href=\"http://foo\">bar</a>", "<a href=\"http://foo\">bar</a>" },
			// we don't allow ftp. 
			//FIXME: is this ok? (strip link)
	//		{ "<a href=\"ftp://foo\">bar</a>", "<a href=\"#foo\">bar</a>" },
			{ "<a href=\"ftp://foo\">bar</a>", "bar" },
			{ "<a href=\"mailto:foo\">bar</a>",	"<a href=\"mailto:foo\">bar</a>" },
			{ "<a href=\"mailto:foo@frentix.com\">bar</a>",	"<a href=\"mailto:foo@frentix.com\">bar</a>" },
			{ "<a href=\"javascript:foo\">bar</a>", "bar" },
			{ "<a href=\"java script:foo\">bar</a>", "bar" },
			{ "<a href=\"java\tscript:foo\">bar</a>", "bar" },
			{ "<a href=\"java\nscript:foo\">bar</a>", "bar" },
			{ "<a href=\"java" + String.valueOf((char) 1) + "script:foo\">bar</a>", "bar" },
			{ "<a href=\"jscript:foo\">bar</a>", "bar" },
			{ "<a href=\"vbscript:foo\">bar</a>", "bar" },
			{ "<a href=\"view-source:foo\">bar</a>", "bar" },
			{ "<a href=\"view-source@foo\">bar</a>", "bar" },
			// test_link() {
			{ "<a href=\"blibla.html\" alt=\"blub\" target=\"_blank\">new window link</A>", "<a alt=\"blub\" href=\"blibla.html\" target=\"_blank\">new window link</a>" },
			// test_link_htmlEntities() {
			{ "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&amp;gen--496\">new window link</a>", "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&amp;gen--496\">new window link</a>" },
			{ "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&auml;gen--496\">new window link</a>", "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&auml;gen--496\">new window link</a>" },
			{ "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&aacute;gen--496\">new window link</a>", "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&aacute;gen--496\">new window link</a>" },
			
			//escape unkown entity
			{ "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&xss;gen--496\">new window link</a>", "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&amp;xss;gen--496\">new window link</a>" },
			//check if escaped result is allowed
			{ "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&amp;xss;gen--496\">new window link</a>", "<a href=\"http://www.schreinersicht.ch/artikel/Mehr_als_nur_S&amp;xss;gen--496\">new window link</a>" },
			// test_link_complexer(){
			{ "<a class=\"o_icon_link_extern\" target=\"_blank\" href=\"http://www.frentix.com\" onclick=\"javascript:alert('hallo');\" title=\"a good link\">a complicated link</a>",
					"<a class=\"o_icon_link_extern\" href=\"http://www.frentix.com\" target=\"_blank\" title=\"a good link\">a complicated link</a>" },
			// test_self_closing_tags() {
			{ "<img src=\"a\">", "<img src=\"a\" />" },
			{ "<img src=\"a\">foo</img>", "<img src=\"a\" />foo" },
			{ "</img>", "" },
			// test_comments()
			{ "<!-- a<b --->", "" },
			{ "<!-- a<b -->don't remove me<!-- hello world -->", "don't remove me" },
			{ "<!-- a<b  \n <!-- hello world \n -->", "" },
			{ "<!--comments1--> visible text <!--comments2-->", " visible text " },
			// test_tiny_paragraph()
			{ "<span>bliblablu</span>", "<span>bliblablu</span>" },
			{ "<p style=\"text-align: right;\">right orientation</p>", "<p style=\"text-align: right;\">right orientation</p>" },
			{ "<h1>Big font</h1>", "<h1>Big font</h1>" },
			{ "<h7>small font</h7>", "small font" },
			{ "<span style=\"font-family: wingdings;\">invalid font</span>", "<span style=\"font-family: wingdings;\">invalid font</span>" },
			{ "<span style=\"font-family: serif;\">invalid font</span>", "<span style=\"font-family: serif;\">invalid font</span>" },
			//FIXME:RH: to allow multiple fonts (as output from tiny)
			//committed as bug: http://code.google.com/p/owaspantisamy/issues/detail?id=49
	//		{ "<span style=\"font-family: serif, arial;\">preformated</span>", "<span style=\"font-family: courier new , courier;\">preformated</span>" },
			{ "<span class=\"schoen\">irgendwas</span>", "<span class=\"schoen\">irgendwas</span>" },
			// test_style_rgb(){
			{ "<p style=\"background-color: rgb(0%,0,0);\">background</p>", "<p>background</p>" },
			{ "<p style=\"background-color: rgba(100%,0,0);\">background</p>", "<p style=\"\">background</p>" },
			{ "<p style=\"background-color: rgb(100,50,50);\">background</p>", "<p style=\"background-color: rgb(100,50,50);\">background</p>" },
			// test_tiny_lists(){
			//lists (output without \n as policy has formatOutput = false		
			{ "<ul>\n<li>a list: adsf</li>\n<li>adsf</li>\n<li>adsfas</li>\n</ul>", "<ul>\n<li>a list: adsf</li>\n<li>adsf</li>\n<li>adsfas</li>\n</ul>" },
			{ "<ol style=\"font-size: 20pt;\">\n<li>numbered list</li>\n<li>adf</li>\n<li>asdfa</li>\n</ol>", "<ol style=\"font-size: 20.0pt;\">\n<li>numbered list</li>\n<li>adf</li>\n<li>asdfa</li>\n</ol>" },
			// test_tiny_tables()
			//tables
			{ "<table border=\"1\" style=\"width: 268px; height: 81px;\" class=\"table\">\n<caption>bliblablue</caption>\n<tbody>\n<tr>\n<td>\n<p>adsfadsf</p>\n</td>\n<td>asdf</td>\n</tr>\n<tr>\n<td>asf</td>\n<td>\n<p>asdf</p>\n</td>\n</tr>\n</tbody>\n</table>", "<table border=\"1\" class=\"table\" style=\"width: 268.0px;height: 81.0px;\">\n<caption>bliblablue</caption>\n<tbody>\n<tr>\n<td>\n<p>adsfadsf</p>\n</td>\n<td>asdf</td>\n</tr>\n<tr>\n<td>asf</td>\n<td>\n<p>asdf</p>\n</td>\n</tr>\n</tbody>\n</table>" },
			{ "<tr style=\"background-color: rgb(46, 147, 209);\">\n<td style=\"border: 1px solid rgb(240, 68, 14);\">asf</td>\n<td>\n<p>asdf</p>\n</td>\n</tr>","\nasf\n\n<p>asdf</p>\n" },
			// test_tiny_singleElements(){
			//sup/sub
			{ "<p><sup>super</sup>script <sub>sub</sub>script</p>", "<p><sup>super</sup>script <sub>sub</sub>script</p>" },
			// test_tiny_jsmath(){
	
			{ "<span title=\"a%20%3C%20b%20%3E%20c%20%3C%20/b%20%3E\">&nbsp;</span>","<span title=\"a%20%3C%20b%20%3E%20c%20%3C%20/b%20%3E\">&nbsp;</span>" },
			// should be saved with entities not with < etc...
	//		{ "<span title=\"a>b\">&nbsp;</span>", "<span title=\"a&gt;b\">&nbsp;</span>" },
			// test_font_awesome() {
			// for now i tags must have at least a space to not b removed
			{ "<i class=\"o_icon o_icon_dev\"> </i> ", "<i class=\"o_icon o_icon_dev\"> </i> " },
			// test_figure() {
			// for now i tags must have at least a space to not b removed
			{ "<figure class=\"image\"><img src=\"bla.png\" /><figcaption>gugs</figcaption></figure>", "<figure class=\"image\"><img src=\"bla.png\" /><figcaption>gugs</figcaption></figure>" },
			// test_big_tiny_output
			{ "<br>", "<br />" }
        });
    }

    private Filter vFilter = new OWASPAntiSamyXSSFilter(-1, true);
    
    private String input;
    private String output;
    
    public XSSFilterParamTest(String input, String output) {
    	this.input = input;
    	this.output = output;
    }
    
    @Test
	public void filter() {
		String filterRes = vFilter.filter(input);
		if (filterRes == output || filterRes.equals(output)){
			System.out.println("------------------------------------------------");
		} else {
			System.out.println("---------------- E R R O R ---------------------");
		}
		System.out.println("           Expected: " + output);
		System.out.println("************************************************\n\n");
		Assert.assertEquals(output, filterRes);
	}	
}
