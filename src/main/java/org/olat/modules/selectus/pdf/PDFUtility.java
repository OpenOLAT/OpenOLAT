/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.pdf;

import java.io.File;
import java.nio.file.Files;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 29 sept. 2017<br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PDFUtility {

	private static final Logger log = Tracing.createLoggerFor(PDFUtility.class);

	public static boolean isEncrypted(File file) {
		boolean isEncrypted = false;
		try {
			// Convert the binary bytes to String. Caution, it can result in loss of data.
			// But for our purposes, we are simply interested in the String portion of the
			// binary pdf data. So we should be fine.
			String pdfContent = new String(Files.readAllBytes(file.toPath()));
			int lastTrailerIndex = pdfContent.lastIndexOf("trailer");
			if (lastTrailerIndex >= 0 && lastTrailerIndex < pdfContent.length()) {
				isEncrypted = checkEncrypt(pdfContent, lastTrailerIndex);
			} else {
				// try last stream
				int lastEndStreamIndex = pdfContent.lastIndexOf("endstream");
				if (lastEndStreamIndex >= 0 && lastEndStreamIndex < pdfContent.length()) {
					isEncrypted = checkEncrypt(pdfContent, lastEndStreamIndex);
					if (!isEncrypted) {
						// try the stream before the last stream
						lastEndStreamIndex = pdfContent.lastIndexOf("endstream", lastEndStreamIndex - 10);
						if (lastEndStreamIndex >= 0 && lastEndStreamIndex < pdfContent.length()) {
							isEncrypted = checkEncryptWithinStream(pdfContent, lastEndStreamIndex);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Cannot say if a pdf is encrypted", e);
		}
		return isEncrypted;
	}

	private static boolean checkEncrypt(String pdfContent, int lastIndex) {
		String newString = pdfContent.substring(lastIndex, pdfContent.length());
		int firstEOFIndex = newString.indexOf("%%EOF");
		String trailer = newString.substring(0, firstEOFIndex);
		return trailer.contains("/Encrypt");
	}
	
	private static boolean checkEncryptWithinStream(String pdfContent, int lastIndex) {
		String newString = pdfContent.substring(lastIndex, pdfContent.length());
		int firstEOFIndex = newString.indexOf("endstream", 12);
		String trailer = newString.substring(0, firstEOFIndex);
		return trailer.contains("/Encrypt");
	}
}
