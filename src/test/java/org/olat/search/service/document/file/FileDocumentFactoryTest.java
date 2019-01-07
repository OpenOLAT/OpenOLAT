/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.search.service.document.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.lucene.document.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class FileDocumentFactoryTest extends OlatTestCase {
	
	private static final OLog log = Tracing.createLoggerFor(FileDocumentFactoryTest.class);

	// variables for test fixture
	
	@Autowired
	private FileDocumentFactory fileDocumentFactory;
	private String rootPath;
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	public void setup()throws Exception {
		//clear database from errors
		rootPath = "/search_junit_test_folder";
	}
	
	@Test
	public void testIsFileSupported() {
		Assert.assertTrue("html must be supported", fileDocumentFactory.isFileSupported("test.html"));
		Assert.assertTrue("htm must be supported", fileDocumentFactory.isFileSupported("test.htm"));
		Assert.assertTrue("HTML must be supported", fileDocumentFactory.isFileSupported("test.HTML"));
		Assert.assertTrue("HTM must be supported", fileDocumentFactory.isFileSupported("test.HTM"));
		Assert.assertTrue("HTM must be supported", fileDocumentFactory.isFileSupported("test.xhtml"));
		Assert.assertTrue("HTM must be supported", fileDocumentFactory.isFileSupported("test.XHTML"));

		Assert.assertTrue("pdf must be supported", fileDocumentFactory.isFileSupported("test.pdf"));
		Assert.assertTrue("PDF must be supported", fileDocumentFactory.isFileSupported("test.PDF"));

		Assert.assertTrue("DOC must be supported", fileDocumentFactory.isFileSupported(getVFSFile("test2.DOC")));
		Assert.assertTrue("doc must be supported", fileDocumentFactory.isFileSupported(getVFSFile("test.doc")));

		Assert.assertTrue("TXT must be supported", fileDocumentFactory.isFileSupported("test.TXT"));
		Assert.assertTrue("txt must be supported", fileDocumentFactory.isFileSupported("test.txt"));
		Assert.assertTrue("txt must be supported", fileDocumentFactory.isFileSupported("test.readme"));
		Assert.assertTrue("txt must be supported", fileDocumentFactory.isFileSupported("test.README"));
		Assert.assertTrue("txt must be supported", fileDocumentFactory.isFileSupported("test.csv"));
		Assert.assertTrue("txt must be supported", fileDocumentFactory.isFileSupported("test.CSV"));
		Assert.assertTrue("XML must be supported", fileDocumentFactory.isFileSupported("test.XML"));
		Assert.assertTrue("xml must be supported", fileDocumentFactory.isFileSupported("test.xml"));
		
		//this is excluded
		Assert.assertFalse("xml must be supported", fileDocumentFactory.isFileSupported("imsmanifest.xml"));
	}
	
	@Test
	public void testIsFileSupported_realfile() throws IOException, DocumentException, DocumentAccessException, URISyntaxException {

		Assert.assertTrue("html must be supported", fileDocumentFactory.isFileSupported(getVFSFile("test.html")));
		Assert.assertTrue("html must be supported", fileDocumentFactory.isFileSupported(getVFSFile("test.doc")));
		Assert.assertTrue("html must be supported", fileDocumentFactory.isFileSupported(getVFSFile("test2.DOC")));

	}
	
	private VFSLeaf getVFSFile(String filename) {
		try {
			URL url = FileDocumentFactoryTest.class.getResource(filename);
			File file = new File(url.toURI());
			return new LocalFileImpl(file);
		} catch (URISyntaxException e) {
			log.error("", e);
			return null;
		}
	}

	@Test
	public void testCreateHtmlDocument() {
		String filePath = "SearchTestFolder";
		String htmlFileName = "test.html";
		String htmlText = "<html><head><meta name=\"generator\" content=\"olat-tinymce-1\"><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body>"
		                  + "<H1>Test HTML Seite fuer JUnit Test</H1>"
		                  + "Dies ist<br />der Test&nbsp;Text"
		                  + "</body></html>"; // Text = 'Dies ist der Test Text'
		String text = "Test HTML Seite fuer JUnit Test Dies ist der Test\u00A0Text"; // must include '\u00A0' !!! 19.5.2010/cg
		// Create a test HTML File  
		VFSContainer rootFolder = VFSManager.olatRootContainer(rootPath , null);
		NamedContainerImpl namedFolder = new NamedContainerImpl(filePath, rootFolder);
		VFSLeaf leaf = (VFSLeaf)namedFolder.resolve(htmlFileName);
		if (leaf != null) {
			leaf.delete();
		}
	  	leaf = namedFolder.createChildLeaf(htmlFileName);
	  	FileUtils.save(leaf.getOutputStream(false), htmlText, "utf-8");
		try {
			
			SearchResourceContext resourceContext = new SearchResourceContext();
			resourceContext.setBusinessControlFor(OresHelper.createOLATResourceableType("FileDocumentFactoryTest"));
			resourceContext.setFilePath(filePath + "/" + leaf.getName());
			Document htmlDocument = fileDocumentFactory.createDocument(resourceContext, leaf);
			// 1. Check content
			String content = htmlDocument.get(OlatDocument.CONTENT_FIELD_NAME);
			assertEquals("Wrong HTML content=" + content.trim() + " , must be =" + text.trim(), text.trim(), content.trim());
      // 2. Check resourceUrl
			String resourceUrl = htmlDocument.get(OlatDocument.RESOURCEURL_FIELD_NAME);
			assertEquals("Wrong ResourceUrl", "[FileDocumentFactoryTest:0][path=" + filePath + "/" + htmlFileName + "]", resourceUrl); 
      // 3. Check File-Type
			String fileType = htmlDocument.get(OlatDocument.FILETYPE_FIELD_NAME);
			assertEquals("Wrong file-type", "type.file.html", fileType); 
			
		} catch (IOException e) {
			fail("IOException=" + e.getMessage());
		} catch (DocumentAccessException e) {
			fail("DocumentAccessException=" + e.getMessage());
		}
	}

}
