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

import java.io.IOException;
import java.io.InputStream;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Description:<br>
 * Detect the suffix with double check for office document with the magic bytes
 * 
 * <P>
 * Initial Date:  1 sept. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class FileTypeDetector {

	private static final OLog log = Tracing.createLoggerFor(FileDocumentFactory.class);
	
	private static final String ZIP = "PK\003\004";
	
	
	public static String getSuffix(VFSLeaf leaf) throws DocumentNotImplementedException {
		String fileName = leaf.getName();
		int dotpos = fileName.lastIndexOf('.');
		if (dotpos < 0 || dotpos == fileName.length() - 1) {
			if (log.isDebug()) log.debug("I cannot detect the document suffix (marked with '.').");
			throw new DocumentNotImplementedException("I cannot detect the document suffix (marked with '.') for " + fileName);
		}
		String suffix = fileName.substring(dotpos+1).toLowerCase();
		if("doc".equals(suffix) && checkMagicBytes(leaf, ZIP)) {
			return "docx";
		} else if("xls".equals(suffix) && checkMagicBytes(leaf, ZIP)) {
			return "xlsx";
		} else if("ppt".equals(suffix) && checkMagicBytes(leaf, ZIP)) {
			return "pptx";
		}
		return suffix;
	}
	
	public static boolean checkMagicBytes(VFSLeaf leaf, String reference) {
		try(InputStream in = leaf.getInputStream()) {
			byte[] buffer = new byte[50];
			int n = in.read(buffer);
			if (n > 0) {
				boolean allOk = true;
				byte[] ref = reference.getBytes();
				for(int i=0; i<ref.length; i++) {
					allOk &= (ref[i] == buffer[i]);
				}
				return allOk; 
			}
		} catch (IOException e) {
			log.warn("", e);
		}
		return false;
	}
}
