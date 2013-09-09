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
package org.olat.core.util.openxml;

import org.junit.Test;

/**
 * 
 * Initial date: 04.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenXMLDocumentTest {
	/*
	@Test
	public void convertHtmlCode() {
		String html = "<p>Ceci est un <strong>test</strong> de <span style='text-decoration: underline;'>format</span>&nbsp;</p><p>sans toutefois <em>une</em> table:</p>";		

		OpenXMLDocument document = new OpenXMLDocument();
		document.appendHtmlText(html);
		
		OpenXMLUtils.writeTo(document.getDocument(), System.out, true);
	}*/
	
	@Test
	public void converLatexCode() {
		String latex = "x^2 \\epsilon";		

		OpenXMLDocument document = new OpenXMLDocument();
		document.convertLaTeX(latex);
		
		
	}
	
	//@Test
	public void convert2Paragraph() {
		String test = "<p>Hello</p><p>World</p>";
		
		OpenXMLDocument document = new OpenXMLDocument();
		document.appendHtmlText(test);
		
		OpenXMLUtils.writeTo(document.getDocument(), System.out, true);
		
	}
	
	//@Test
	public void convertHtmlCode() {
		String html = "<p>Ceci est un <strong>test</strong> de <span style='text-decoration: underline;'>format</span>&nbsp;</p><p>sans toutefois <em>une</em> table:</p>";		

		OpenXMLDocument document = new OpenXMLDocument();
		document.appendHtmlText(html);
		
		OpenXMLUtils.writeTo(document.getDocument(), System.out, true);
	}
	
	@Test
	public void convertTableCode() {
		String html = "<p>Ceci est une table</p><table style='height: 75px;' width='356'><tbody><tr><td>1 - 1</td><td><p>1 - 2</p></td><td>&nbsp;1-3 avec un plus de text</td></tr><tr><td>2 - 1</td><td>Avec beaucoup de text</td><td>2 - 3</td></tr><tr><td>3 - 1 mais avec du text et encore et encore</td><td>3 - 2</td><td>3 - 3</td></tr></tbody></table><p>&nbsp;</p>";
		
		OpenXMLDocument document = new OpenXMLDocument();
		document.appendHtmlText(html);
		
		OpenXMLUtils.writeTo(document.getDocument(), System.out, true);
	}
	
	/*@Test
	public void writeDoc() throws Exception {
		FileOutputStream fileOut = new FileOutputStream(new File("/HotCoffee/tmp/test_1_min.docx"));
		ZipOutputStream out = new ZipOutputStream(fileOut);
		
		OpenXMLDocument document = new OpenXMLDocument();
		Element text = document.createTextEl("Hello word!");
		Element run = document.createRunEl(Collections.singletonList(text));
		Element paragraph = document.createParagraphEl(null, Collections.singletonList(run));
		document.getBodyElement().appendChild(paragraph);
		
		//add break page
		Element breakEl = document.createPageBreakEl();
		document.getBodyElement().appendChild(breakEl);
		
		//add an image
		URL imageUrl = UserMgmtTest.class.getResource("portrait.jpg");
		assertNotNull(imageUrl);
		File image = new File(imageUrl.toURI());
		Element imgEl = document.createImageEl(image);
		Element imgRun = document.createRunEl(Collections.singletonList(imgEl));
		Element imgParagraph = document.createParagraphEl(null, Collections.singletonList(imgRun));
		document.getBodyElement().appendChild(imgParagraph);
		
		
		Element break2El = document.createPageBreakEl();
		document.getBodyElement().appendChild(break2El);
		
		Element text2 = document.createTextEl("Miko rule the world!");
		Element run2 = document.createRunEl(Collections.singletonList(text2));
		Element paragraph2 = document.createParagraphEl(null, Collections.singletonList(run2));
		document.getBodyElement().appendChild(paragraph2);

		OpenXMLDocumentWriter writer = new OpenXMLDocumentWriter();
		writer.createDocument(out, document);
		
		out.flush();
		fileOut.flush();
		IOUtils.closeQuietly(out);
		IOUtils.closeQuietly(fileOut);
	}*/
	
	
	
	
	

}
