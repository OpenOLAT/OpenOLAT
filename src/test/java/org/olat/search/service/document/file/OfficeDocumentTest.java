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
package org.olat.search.service.document.file;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.test.OlatTestCase;

/**
 * Test the low memory text extractor for OpenXML (Microsoft Office XML)
 * documents.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OfficeDocumentTest extends OlatTestCase {
	
	@Test
	public void testWordOpenXMLDocument() throws IOException, DocumentException, DocumentAccessException, URISyntaxException {
		URL docUrl = OfficeDocumentTest.class.getResource("Test_word_indexing.docx");
		Assert.assertNotNull(docUrl);
		
		VFSLeaf doc = new LocalFileImpl(new File(docUrl.toURI()));
		WordOOXMLDocument document = new WordOOXMLDocument();
		FileContent content =	document.readContent(doc);
		Assert.assertNotNull(content);
		String body = content.getContent();
		Assert.assertTrue(body.contains("Document compatibility test"));
		Assert.assertTrue(body.contains("They prefer to start writing a document at home in desktop or laptop computer"));
	}
	
	@Test
	public void testWordOOXMLDocumentComparator() {
		List<String> docs = new ArrayList<>();
		docs.add("word/document.xml");
		docs.add("word/header1.xml");
		docs.add("word/footer3.xml");
		docs.add("word/footer.xml");
		docs.add("word/footer14.xml");
		docs.add("word/header4.xml");
		docs.add("word/header25.xml");
		
		Collections.sort(docs, new WordOOXMLDocument.WordDocumentComparator());
		
		Assert.assertEquals("word/header1.xml", docs.get(0));
		Assert.assertEquals("word/header4.xml", docs.get(1));
		Assert.assertEquals("word/header25.xml", docs.get(2));
		Assert.assertEquals("word/document.xml", docs.get(3));
		Assert.assertEquals("word/footer.xml", docs.get(4));
		Assert.assertEquals("word/footer3.xml", docs.get(5));
		Assert.assertEquals("word/footer14.xml", docs.get(6));
	}
	
	@Test
	public void testWordDocument() throws IOException, DocumentException, DocumentAccessException, URISyntaxException {
		URL docUrl = OfficeDocumentTest.class.getResource("Test_word_indexing.doc");
		Assert.assertNotNull(docUrl);
		
		VFSLeaf doc = new LocalFileImpl(new File(docUrl.toURI()));
		WordDocument document = new WordDocument();
		FileContent content =	document.readContent(doc);
		Assert.assertNotNull(content);
		String body = content.getContent();
		Assert.assertTrue(body.contains("Lorem ipsum dolor sit amet"));//content
		Assert.assertTrue(body.contains("Rue (domicile)"));//footer
	}
	
	@Test
	public void testExcelOpenXMLDocument() throws IOException, DocumentException, DocumentAccessException, URISyntaxException {
		URL docUrl = OfficeDocumentTest.class.getResource("Test_excel_indexing.xlsx");
		Assert.assertNotNull(docUrl);

		VFSLeaf doc = new LocalFileImpl(new File(docUrl.toURI()));
		ExcelOOXMLDocument document = new ExcelOOXMLDocument();
		FileContent content =	document.readContent(doc);
		Assert.assertNotNull(content);
		String body = content.getContent();
		Assert.assertTrue(body.contains("Numbers and their Squares"));
		Assert.assertTrue(body.contains("225"));
	}
	
	@Test
	public void testExcelDocument() throws IOException, DocumentException, DocumentAccessException, URISyntaxException {
		URL docUrl = OfficeDocumentTest.class.getResource("Test_excel_indexing.xls");
		Assert.assertNotNull(docUrl);

		VFSLeaf doc = new LocalFileImpl(new File(docUrl.toURI()));
		ExcelDocument document = new ExcelDocument();
		FileContent content =	document.readContent(doc);
		Assert.assertNotNull(content);
		String body = content.getContent();
		Assert.assertTrue(body.contains("Nachname"));
		Assert.assertTrue(body.contains("olat4you"));
	}
	
	@Test
	public void testPowerPointOpenXMLDocument() throws IOException, DocumentException, DocumentAccessException, URISyntaxException {
		URL docUrl = OfficeDocumentTest.class.getResource("Test_ppt_indexing.pptx");
		Assert.assertNotNull(docUrl);

		VFSLeaf doc = new LocalFileImpl(new File(docUrl.toURI()));
		PowerPointOOXMLDocument document = new PowerPointOOXMLDocument();
		FileContent content =	document.readContent(doc);
		Assert.assertNotNull(content);
		String body = content.getContent();
		Assert.assertTrue(body.contains("Here is some text"));
	}
	
	@Test
	public void testPowerPointOOXMLDocumentComparator() {
		List<String> docs = new ArrayList<>();
		docs.add("word/dru.xml");
		docs.add("ppt/slides/slide9.xml");
		docs.add("ppt/slides/slide6.xml");
		docs.add("ppt/slides/slide25.xml");
		docs.add("ppt/slides/slide.xml");
		docs.add("ppt/slides/slide12.xml");
		docs.add("ppt/slides/slide3.xml");
		
		Collections.sort(docs, new PowerPointOOXMLDocument.PowerPointDocumentComparator());
		
		Assert.assertEquals("ppt/slides/slide.xml", docs.get(0));
		Assert.assertEquals("ppt/slides/slide3.xml", docs.get(1));
		Assert.assertEquals("ppt/slides/slide6.xml", docs.get(2));
		Assert.assertEquals("ppt/slides/slide9.xml", docs.get(3));
		Assert.assertEquals("ppt/slides/slide12.xml", docs.get(4));
		Assert.assertEquals("ppt/slides/slide25.xml", docs.get(5));
		Assert.assertEquals("word/dru.xml", docs.get(6));
	}
	
	@Test
	public void testPowerPointDocument() throws IOException, DocumentException, DocumentAccessException, URISyntaxException {
		URL docUrl = OfficeDocumentTest.class.getResource("Test_ppt_indexing.ppt");
		Assert.assertNotNull(docUrl);

		VFSLeaf doc = new LocalFileImpl(new File(docUrl.toURI()));
		PowerPointDocument document = new PowerPointDocument();
		FileContent content =	document.readContent(doc);
		Assert.assertNotNull(content);
		String body = content.getContent();
		Assert.assertTrue(body.contains("Sample Powerpoint Slide"));
	}
}