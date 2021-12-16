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

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 04.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenXMLDocumentTest {

	@Test
	public void convertHtmlCode() {
		String html = "Year 0: -$2'000";

		OpenXMLDocument document = new OpenXMLDocument();
		document.appendHtmlText(html, false);
		
		OpenXMLUtils.writeTo(document.getDocument(), System.out, true);
		Assert.assertNotNull(document.getDocument());
	}

	@Test
	public void writeDoc() throws Exception {
		File file = File.createTempFile("worddoc", "_min.docx");
		try(FileOutputStream fileOut = new FileOutputStream(file);
			ZipOutputStream out = new ZipOutputStream(fileOut)) {
			
			OpenXMLDocument document = new OpenXMLDocument();
			String html = "<table style='height: 80px;' width='446'><tbody><tr><td>1-1</td><td colspan='2' rowspan='2'>1-21-32-32-2</td><td>1-4</td></tr><tr><td>2-1</td><td>2-4</td></tr><tr><td>3-1</td><td>3-2</td><td colspan='2'>3-33-4</td></tr></tbody></table>";
			document.appendHtmlText(html, false);
	
			OpenXMLDocumentWriter writer = new OpenXMLDocumentWriter();
			writer.createDocument(out, document);
			
			out.flush();
			fileOut.flush();
		} catch(Exception e) {
			throw e;
		}
		
		Assert.assertTrue(file.exists());
		Assert.assertTrue(file.length() > 4096);
		file.delete();
	}
}
