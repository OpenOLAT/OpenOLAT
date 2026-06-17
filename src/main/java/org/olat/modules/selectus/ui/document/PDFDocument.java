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
package org.olat.modules.selectus.ui.document;

import java.io.InputStream;

import org.olat.core.util.FileUtils;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  28 sept. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PDFDocument {
	
	private final Type type;
	private final String name;
	private InputStream summaryStream;
	private InputStream documentStream;
	
	
	public PDFDocument(Type type, String name, InputStream summaryStream) {
		this(type, name, summaryStream, null);
	}
	
	public PDFDocument(Type type, String name, InputStream summaryStream, InputStream documentStream) {
		this.type = type;
		this.name = name;
		this.summaryStream = summaryStream;
		this.documentStream = documentStream;
	}
	
	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public InputStream getSummaryStream() {
		return summaryStream;
	}

	public InputStream getDocumentStream() {
		return documentStream;
	}
	
	public void close() {
		FileUtils.closeSafely(summaryStream);
		FileUtils.closeSafely(documentStream);
		summaryStream = null;
		documentStream = null;
	}

	public enum Type {
		INTRODUCTION,
		DOCUMENT
	}
}
