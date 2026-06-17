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
package org.olat.modules.selectus;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 23 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum DocumentType {
	
	pdf,
	docx,
	xlsx,
	jpg;
	
	public static DocumentType[] types(String string) {
		String[] stringArray = string.split("[,]");
		DocumentType[] types = new DocumentType[stringArray.length];
		for(int i=stringArray.length; i-->0; ) {
			types[i] = valueOf(stringArray[i]);
		}
		return types;
	}
	
	public boolean contains(DocumentType[] types) {
		if(types == null || types.length == 0) return false;
		
		for(DocumentType type:types) {
			if(type == this) {
				return true;
			}
		}
		return false;
	}
	
	public static DocumentType valueOf(String filename, String mimeType) {
		DocumentType type = null;
		if(StringHelper.containsNonWhitespace(mimeType)) {
			switch(mimeType) {
				case "application/pdf":
					type = DocumentType.pdf; break;
				case "application/vnd.ms-excel":
					type = DocumentType.xlsx; break;
				case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
					type = DocumentType.xlsx; break;
				case "application/msword":
					type = DocumentType.docx; break;
				case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
					type = DocumentType.docx; break;
				case "image/jpeg":
					type = DocumentType.jpg; break;
				default:
					type = null;
					break;
			}
		}
		if(type == null && StringHelper.containsNonWhitespace(filename)) {
			String lFilename = filename.toLowerCase();
			if(lFilename.endsWith(".pdf")) {
				type = DocumentType.pdf;
			} else if(lFilename.endsWith(".doc") || lFilename.endsWith(".docx")) {
				type = DocumentType.docx;
			} else if(lFilename.endsWith(".xls") || lFilename.endsWith(".xlsx")) {
				type = DocumentType.xlsx;
			} else if (lFilename.endsWith(".jpg") || lFilename.endsWith(".jpeg")) {
				type = DocumentType.jpg;
			}
		}
		return type;
	}
	
	public static String mimeType(DocumentType type) {
		if(type == null) return "application/pdf";// default is PDF
		
		switch(type) {
			case pdf: return "application/pdf";
			case xlsx: return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
			case docx: return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
			case jpg: return "image/jpeg";
			default: return "application/pdf";
		}
	}
	
	public static String mimeType(String type) {
		if(type == null) return "application/pdf";// default is PDF
		
		switch(type) {
			case "pdf": return "application/pdf";
			case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
			case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
			case "jpg": return "image/jpeg";
			default: return "application/pdf";
		}
	}
	
	public static Set<String> toMimeTypes(List<DocumentType> types) {
		Set<String> mimeTypes = new HashSet<>();
		if(types == null || types.isEmpty()) {
			appendMimeTypes(DocumentType.pdf, mimeTypes);
		} else {
			for(DocumentType type:types) {
				appendMimeTypes(type, mimeTypes);
			}
		}
		return mimeTypes;
	}
	
	private static void appendMimeTypes(DocumentType type, Collection<String> mimeTypes) {
		switch(type) {
			case pdf:
				mimeTypes.add("application/pdf");
				break;
			case xlsx:
				mimeTypes.add("application/vnd.ms-excel");
				mimeTypes.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				break;
			case docx:
				mimeTypes.add("application/msword");
				mimeTypes.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
				break;
			case jpg:
				mimeTypes.add("image/jpeg");
				break;
		}
	}
	
	public static String toFlatString(List<DocumentType> types) {
		StringBuilder string = new StringBuilder();
		if(types == null || types.isEmpty()) {
			string.append(toString(DocumentType.pdf));
		} else {
			for(DocumentType type:types) {
				if(string.length() > 0) string.append(", ");
				string.append(toString(type));
			}
		}
		return string.toString();
	}
	
	public static String[] toString(List<DocumentType> types) {
		String[] strings;
		if(types == null || types.isEmpty()) {
			strings = new String[] { "PDF" };
		} else {
			strings = new String[types.size()];
			for(int i=0; i<types.size(); i++) {
				strings[i] = toString(types.get(i));
			}
		}
		return strings;
	}
	
	public static String toString(DocumentType type) {
		switch(type) {
			case pdf: return "PDF";
			case xlsx: return "XLS";
			case docx: return "DOC";
			case jpg: return "JPG";
			default: return null;
		}
	}
	
	public static boolean isOnlyPDFs(Map<DocumentEnum,List<DocumentType>> docTypes) {
		for(List<DocumentType> types:docTypes.values()) {
			if(types.contains(DocumentType.docx) || types.contains(DocumentType.xlsx) || types.contains(DocumentType.jpg)) {
				return false;
			}
		}
		return true;
	}
	
	public static DocumentType secureValueOf(String val) {
		for(DocumentType type:values()) {
			if(type.name().equalsIgnoreCase(val)) {
				return type;
			}
		}
		return DocumentType.pdf;
	}
}
