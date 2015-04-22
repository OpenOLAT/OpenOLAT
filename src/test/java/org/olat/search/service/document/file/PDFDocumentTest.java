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
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.test.OlatTestCase;
import org.olat.test.VFSJavaIOFile;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PDFDocumentTest extends OlatTestCase {
	
	@Test
	public void testPDFDocument()
	throws DocumentException, DocumentAccessException, URISyntaxException, IOException {
		URL pdfUrl = PDFDocumentTest.class.getResource("Test_pdf_indexing.pdf");
		Assert.assertNotNull(pdfUrl);

		VFSLeaf doc = new VFSJavaIOFile("Test_1.pdf", new File(pdfUrl.toURI()));
		String temp = System.getProperty("java.io.tmpdir");
		PdfDocument document = new PdfDocument(temp, false);
		FileContent content =	document.readContent(doc);
		Assert.assertNotNull(content);
		Assert.assertEquals("Test pdf indexing", content.getTitle());
		String body = content.getContent();
		Assert.assertEquals("Un petit texte en français", body.trim());
	}
	
	@Test
	public void testPDFDocumentCaching()
	throws DocumentException, DocumentAccessException, URISyntaxException, IOException {
		URL pdfUrl = PDFDocumentTest.class.getResource("Test_pdf_indexing.pdf");
		Assert.assertNotNull(pdfUrl);

		VFSLeaf doc = new VFSJavaIOFile(UUID.randomUUID().toString() + ".pdf", new File(pdfUrl.toURI()));
		String temp = System.getProperty("java.io.tmpdir");
		PdfDocument document = new PdfDocument(temp, false);
		
		//index the pdf
		FileContent contentIndexed =	document.readContent(doc);
		Assert.assertNotNull(contentIndexed);
		Assert.assertEquals("Test pdf indexing", contentIndexed.getTitle());
		String bodyIndexed = contentIndexed.getContent();
		Assert.assertEquals("Un petit texte en français", bodyIndexed.trim());
		
		//take from the cache
		FileContent contentCached =	document.readContent(doc);
		Assert.assertNotNull(contentCached);
		Assert.assertEquals("Test pdf indexing", contentCached.getTitle());
		String cachedBody = contentCached.getContent();
		Assert.assertEquals("Un petit texte en français", cachedBody.trim());
	}
}