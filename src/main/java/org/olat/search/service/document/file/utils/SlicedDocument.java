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
package org.olat.search.service.document.file.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Utility class to help reorder the slides/sheets/fotters/headers of XML 
 * documents which are sliced in different numbered XML files
 * 
 * <P>
 * Initial Date:  5 nov. 2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SlicedDocument {

	private List<StringBuilder> headers = new ArrayList<StringBuilder>();
	private List<StringBuilder> documents = new ArrayList<StringBuilder>();
	private List<StringBuilder> footers = new ArrayList<StringBuilder>();
	
	private int size;

	public void setHeader(int index, StringBuilder doc) {
		ensureSize(headers, index);
		headers.set(index, doc);
		size += doc.length();
	}
	
	public void setContent(int index, StringBuilder doc) {
		ensureSize(documents, index);
		documents.set(index, doc);
		size += doc.length();
	}
	
	public void setFooter(int index, StringBuilder doc) {
		ensureSize(footers, index);
		footers.set(index, doc);
		size += doc.length();
	}
	
	private final void ensureSize(List<StringBuilder> list, int index) {
		if(list.size() <= index) {
			for(int i=list.size(); i< (index+20); i++) {
				list.add(null);
			}
		}
	}
	
	private final List<StringBuilder> toStringAndClear(StringBuilder content, List<StringBuilder> list) {
		if(list != null && !list.isEmpty()) {
			for(StringBuilder document:list) {
				if(document != null) {
					content.append(document).append('\n');
				}
			}
			list.clear();
		}
		return null;
	}

	public String toStringAndClear() {
		StringBuilder content = new StringBuilder(size + 100);
		headers = toStringAndClear(content, headers);
		documents = toStringAndClear(content, documents);
		footers = toStringAndClear(content, footers);
		return content.toString();
	}
}
