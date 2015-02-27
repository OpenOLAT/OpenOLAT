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

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;

/**
 * Description:<br>
 * This test case tests the simle html tags filter
 * 
 * <P>
 * Initial Date:  14.07.2009 <br>
 * @author gnaegi
 */
@RunWith(JUnit4.class)
public class ConditionalHtmlCommentsFilterTest {

	protected Filter filter;

	@Before
	public void setUp() {
		filter = FilterFactory.getConditionalHtmlCommentsFilter();
	}
	@After
	public void tearDown() {
		filter = null;
	}

	private void t(String input, String result) {
		Assert.assertEquals(result, filter.filter(input));
	}
	@Test
	public void testPlainText() {
		t(null, null);
		t("", "");
		t("hello world", "hello world");
		t("hello \n \t \r world", "hello \n \t \r world");
		t("1+2=3", "1+2=3");
	}
	@Test
	public void testSimpleTags() {
		t("<b>hello</b> world", "<b>hello</b> world");
		t("<b>h<i>ell</i>o</b> world", "<b>h<i>ell</i>o</b> world");
		t("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">", "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		t("<a ref='#bla' \n title='gugus'>hello</b> world", "<a ref='#bla' \n title='gugus'>hello</b> world");
	}
	@Test
	public void testTagsWithAttributes() {
		t("<font color='red'>endif</font> world", "<font color='red'>endif</font> world");
		t("<font color=\"red\">endif</font> world", "<font color=\"red\">endif</font> world");
		t("<a href=\"#top\" color='=>top'>go up</a>", "<a href=\"#top\" color='=>top'>go up</a>");
		t("<a href=\"#top\" title=\"a > b < c <=x\">blu blu</a>", "<a href=\"#top\" title=\"a > b < c <=x\">blu blu</a>");
		t("<a href=\"#top\" title=\"a > b < c <=x\">blu <font color='red'>hello</font> world blu</a>", "<a href=\"#top\" title=\"a > b < c <=x\">blu <font color='red'>hello</font> world blu</a>");
	}
	@Test
	public void testComments() {
		t("<!-- hello world -->blu bli bla endif<!-- fertig -->", "<!-- hello world -->blu bli bla endif<!-- fertig -->");
		t("<!--[achtung]-->blu bli bla endif<!--[fertig]-->", "<!--[achtung]-->blu bli bla endif<!--[fertig]-->");
	}
	@Test
	public void testConditionalComments() {
		t("<span class=\"o_qti_item_mattext\"><!--[if gte mso 9]><xml> <o:DocumentProperties> <o:Template>Normal.dotm</o:Template> <o:Revision>0</o:Revision> <o:TotalTime>0</o:TotalTime> <o:Pages>1</o:Pages> <o:Words>2</o:Words> <o:Characters>13</o:Characters> <o:Company>-</o:Company> <o:Lines>1</o:Lines> <o:Paragraphs>1</o:Paragraphs> <o:CharactersWithSpaces>15</o:CharactersWithSpaces> <o:Version>12.0</o:Version> </o:DocumentProperties> <o:OfficeDocumentSettings> <o:AllowPNG /> </o:OfficeDocumentSettings> </xml><![endif]--><!--[if gte mso 9]><xml> <w:WordDocument> <w:Zoom>0</w:Zoom> <w:TrackMoves>false</w:TrackMoves> <w:TrackFormatting /> <w:PunctuationKerning /> <w:DrawingGridHorizontalSpacing>18 pt</w:DrawingGridHorizontalSpacing> <w:DrawingGridVerticalSpacing>18 pt</w:DrawingGridVerticalSpacing> <w:DisplayHorizontalDrawingGridEvery>0</w:DisplayHorizontalDrawingGridEvery> <w:DisplayVerticalDrawingGridEvery>0</w:DisplayVerticalDrawingGridEvery> <w:ValidateAgainstSchemas /> <w:SaveIfXMLInvalid>false</w:SaveIfXMLInvalid> <w:IgnoreMixedContent>false</w:IgnoreMixedContent> <w:AlwaysShowPlaceholderText>false</w:AlwaysShowPlaceholderText> <w:Compatibility> <w:BreakWrappedTables /> <w:DontGrowAutofit /> <w:DontAutofitConstrainedTables /> <w:DontVertAlignInTxbx /> </w:Compatibility> </w:WordDocument> </xml><![endif]--><!--[if gte mso 9]><xml> <w:LatentStyles DefLockedState=\"false\" LatentStyleCount=\"276\"> </w:LatentStyles> </xml><![endif]--> <!--[if gte mso 10]><mce:style><! /* Style Definitions */table.MsoNormalTable {mso-style-name:\"Table Normal\"; mso-tstyle-rowband-size:0; mso-tstyle-colband-size:0; mso-style-noshow:yes; mso-style-parent:\"\"; mso-padding-alt:0cm 5.4pt 0cm 5.4pt; mso-para-margin:0cm; mso-para-margin-bottom:.0001pt; mso-pagination:widow-orphan; font-size:12.0pt; font-family:\"Times New Roman\"; mso-ascii-font-family:Cambria; mso-ascii-theme-font:minor-latin; mso-fareast-font-family:\"Times New Roman\"; mso-fareast-theme-font:minor-fareast; mso-hansi-font-family:Cambria; mso-hansi-theme-font:minor-latin;} --><!--[endif]--> <!--StartFragment--><strong><span>Temperature is</span></strong> <!--EndFragment--></span>", 
			"<span class=\"o_qti_item_mattext\">  <!--StartFragment--><strong><span>Temperature is</span></strong> <!--EndFragment--></span>");
		t("<span class=\"o_qti_item_mattext\"><!--[if gte mso 9]><xml> <o:DocumentProperties> <o:Template>Normal.dotm</o:Template> <o:Revision>0</o:Revision> <o:TotalTime>0</o:TotalTime> <o:Pages>1</o:Pages> <o:Words>2</o:Words> <o:Characters>13</o:Characters> <o:Company>-</o:Company> <o:Lines>1</o:Lines> <o:Paragraphs>1</o:Paragraphs> <o:CharactersWithSpaces>15</o:CharactersWithSpaces> <o:Version>12.0</o:Version> </o:DocumentProperties> <o:OfficeDocumentSettings> <o:AllowPNG /> </o:OfficeDocumentSettings> </xml><![endif]-->1<!--[if gte mso 9]><xml> <w:WordDocument> <w:Zoom>0</w:Zoom> <w:TrackMoves>false</w:TrackMoves> <w:TrackFormatting /> <w:PunctuationKerning /> <w:DrawingGridHorizontalSpacing>18 pt</w:DrawingGridHorizontalSpacing> <w:DrawingGridVerticalSpacing>18 pt</w:DrawingGridVerticalSpacing> <w:DisplayHorizontalDrawingGridEvery>0</w:DisplayHorizontalDrawingGridEvery> <w:DisplayVerticalDrawingGridEvery>0</w:DisplayVerticalDrawingGridEvery> <w:ValidateAgainstSchemas /> <w:SaveIfXMLInvalid>false</w:SaveIfXMLInvalid> <w:IgnoreMixedContent>false</w:IgnoreMixedContent> <w:AlwaysShowPlaceholderText>false</w:AlwaysShowPlaceholderText> <w:Compatibility> <w:BreakWrappedTables /> <w:DontGrowAutofit /> <w:DontAutofitConstrainedTables /> <w:DontVertAlignInTxbx /> </w:Compatibility> </w:WordDocument> </xml><![endif]-->2<!--[if gte mso 9]><xml> <w:LatentStyles DefLockedState=\"false\" LatentStyleCount=\"276\"> </w:LatentStyles> </xml><![endif]-->3 <!--[if gte mso 10]><mce:style><! /* Style Definitions */table.MsoNormalTable {mso-style-name:\"Table Normal\"; mso-tstyle-rowband-size:0; mso-tstyle-colband-size:0; mso-style-noshow:yes; mso-style-parent:\"\"; mso-padding-alt:0cm 5.4pt 0cm 5.4pt; mso-para-margin:0cm; mso-para-margin-bottom:.0001pt; mso-pagination:widow-orphan; font-size:12.0pt; font-family:\"Times New Roman\"; mso-ascii-font-family:Cambria; mso-ascii-theme-font:minor-latin; mso-fareast-font-family:\"Times New Roman\"; mso-fareast-theme-font:minor-fareast; mso-hansi-font-family:Cambria; mso-hansi-theme-font:minor-latin;} --><!--[endif]-->4 <!--StartFragment--><strong><span>Temperature is</span></strong> <!--EndFragment--></span>", 
		"<span class=\"o_qti_item_mattext\">123 4 <!--StartFragment--><strong><span>Temperature is</span></strong> <!--EndFragment--></span>");
		t("<!--achtung fertig los--><span class=\"o_qti_item_mattext\"><!--[if gte mso 9]><xml> <o:DocumentProperties> <o:Template>Normal.dotm</o:Template> <o:Revision>0</o:Revision> <o:TotalTime>0</o:TotalTime> <o:Pages>1</o:Pages> <o:Words>2</o:Words> <o:Characters>13</o:Characters> <o:Company>-</o:Company> <o:Lines>1</o:Lines> <o:Paragraphs>1</o:Paragraphs> <o:CharactersWithSpaces>15</o:CharactersWithSpaces> <o:Version>12.0</o:Version> </o:DocumentProperties> <o:OfficeDocumentSettings> <o:AllowPNG /> </o:OfficeDocumentSettings> </xml><![endif]--><!--[if gte mso 9]><xml> <w:WordDocument> <w:Zoom>0</w:Zoom> <w:TrackMoves>false</w:TrackMoves> <w:TrackFormatting /> <w:PunctuationKerning /> <w:DrawingGridHorizontalSpacing>18 pt</w:DrawingGridHorizontalSpacing> <w:DrawingGridVerticalSpacing>18 pt</w:DrawingGridVerticalSpacing> <w:DisplayHorizontalDrawingGridEvery>0</w:DisplayHorizontalDrawingGridEvery> <w:DisplayVerticalDrawingGridEvery>0</w:DisplayVerticalDrawingGridEvery> <w:ValidateAgainstSchemas /> <w:SaveIfXMLInvalid>false</w:SaveIfXMLInvalid> <w:IgnoreMixedContent>false</w:IgnoreMixedContent> <w:AlwaysShowPlaceholderText>false</w:AlwaysShowPlaceholderText> <w:Compatibility> <w:BreakWrappedTables /> <w:DontGrowAutofit /> <w:DontAutofitConstrainedTables /> <w:DontVertAlignInTxbx /> </w:Compatibility> </w:WordDocument> </xml><![endif]--><!--[if gte mso 9]><xml> <w:LatentStyles DefLockedState=\"false\" LatentStyleCount=\"276\"> </w:LatentStyles> </xml><![endif]--> <!--[if gte mso 10]><mce:style><! /* Style Definitions */table.MsoNormalTable {mso-style-name:\"Table Normal\"; mso-tstyle-rowband-size:0; mso-tstyle-colband-size:0; mso-style-noshow:yes; mso-style-parent:\"\"; mso-padding-alt:0cm 5.4pt 0cm 5.4pt; mso-para-margin:0cm; mso-para-margin-bottom:.0001pt; mso-pagination:widow-orphan; font-size:12.0pt; font-family:\"Times New Roman\"; mso-ascii-font-family:Cambria; mso-ascii-theme-font:minor-latin; mso-fareast-font-family:\"Times New Roman\"; mso-fareast-theme-font:minor-fareast; mso-hansi-font-family:Cambria; mso-hansi-theme-font:minor-latin;} --><!--[endif]--> <!--StartFragment--><strong><span>Temperature is</span></strong> <!--EndFragment--></span>", 
		"<!--achtung fertig los--><span class=\"o_qti_item_mattext\">  <!--StartFragment--><strong><span>Temperature is</span></strong> <!--EndFragment--></span>");
	}

}
