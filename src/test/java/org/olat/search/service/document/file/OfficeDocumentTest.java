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

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.document.file.FileDocument.FileContent;
import org.olat.test.VFSJavaIOFile;

/**
 * Test the low memory text extractor for OpenXML (Microsoft Office XML)
 * documents.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OfficeDocumentTest {
	
	@Test
	public void testWordOpenXMLDocument() throws IOException, DocumentException, DocumentAccessException, URISyntaxException {
		URL docUrl = OfficeDocumentTest.class.getResource("Test_word_indexing.docx");
		Assert.assertNotNull(docUrl);
		
		VFSLeaf doc = new VFSJavaIOFile(new File(docUrl.toURI()));
		WordOOXMLDocument document = new WordOOXMLDocument();
		FileContent content =	document.readContent(doc);
		Assert.assertNotNull(content);
		String body = content.getContent();
		Assert.assertTrue(body.contains("Document compatibility test"));
		Assert.assertTrue(body.contains("They prefer to start writing a document at home in desktop or laptop computer"));
	}
	
	@Test
	public void testWordDocument() throws IOException, DocumentException, DocumentAccessException, URISyntaxException {
		URL docUrl = OfficeDocumentTest.class.getResource("Test_word_indexing.doc");
		Assert.assertNotNull(docUrl);
		
		VFSLeaf doc = new VFSJavaIOFile(new File(docUrl.toURI()));
		WordDocument document = new WordDocument();
		FileContent content =	document.readContent(doc);
		Assert.assertNotNull(content);
		String body = content.getContent();
		Assert.assertTrue(body.contains("Lorem ipsum dolor sit amet"));
	}
	
	@Test
	public void testExcelOpenXMLDocument() throws IOException, DocumentException, DocumentAccessException, URISyntaxException {
		URL docUrl = OfficeDocumentTest.class.getResource("Test_excel_indexing.xlsx");
		Assert.assertNotNull(docUrl);

		VFSLeaf doc = new VFSJavaIOFile(new File(docUrl.toURI()));
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

		VFSLeaf doc = new VFSJavaIOFile(new File(docUrl.toURI()));
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

		VFSLeaf doc = new VFSJavaIOFile(new File(docUrl.toURI()));
		PowerPointOOXMLDocument document = new PowerPointOOXMLDocument();
		FileContent content =	document.readContent(doc);
		Assert.assertNotNull(content);
		String body = content.getContent();
		Assert.assertTrue(body.contains("Here is some text"));
	}
	
	@Test
	public void testPowerPointDocument() throws IOException, DocumentException, DocumentAccessException, URISyntaxException {
		URL docUrl = OfficeDocumentTest.class.getResource("Test_ppt_indexing.ppt");
		Assert.assertNotNull(docUrl);

		VFSLeaf doc = new VFSJavaIOFile(new File(docUrl.toURI()));
		PowerPointDocument document = new PowerPointDocument();
		FileContent content =	document.readContent(doc);
		Assert.assertNotNull(content);
		String body = content.getContent();
		Assert.assertTrue(body.contains("Sample Powerpoint Slide"));
	}
}