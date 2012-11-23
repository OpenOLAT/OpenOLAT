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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.restapi.support;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MultipartReader {

	private static final OLog log = Tracing.createLoggerFor(MultipartReader.class);

	private String filename;
	private String contentType;
	private File file;
	private Map<String, String> fields = new HashMap<String, String>();

	public MultipartReader(HttpServletRequest request) {
		long uploadLimit = 500000l;
		apache(request, uploadLimit);
	}

	private final void apache(HttpServletRequest request, long uploadLimit) {
		ServletFileUpload uploadParser = new ServletFileUpload();
		uploadParser.setSizeMax((uploadLimit * 1024l) + 512000l);
		// Parse the request
		try {
			FileItemIterator iter = uploadParser.getItemIterator(request);
			while (iter.hasNext()) {
				FileItemStream item = iter.next();
				String itemName = item.getFieldName();
				InputStream itemStream = item.openStream();
				if (item.isFormField()) {
					String value = Streams.asString(itemStream, "UTF-8");
					fields.put(itemName, value);
				} else {
					// File item, store it to temp location
					filename = item.getName();
					contentType = item.getContentType();
					
					if(filename != null) {
						filename = UUID.randomUUID().toString().replace("-", "") + "_" + filename;
					} else {
						filename = "upload-" + UUID.randomUUID().toString().replace("-", "");
					}
					file = new File(System.getProperty("java.io.tmpdir"), filename);
					try {
						save(itemStream, file);
					} catch (Exception e) {
						log.error("", e);
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public String getFilename() {
		return filename;
	}

	public String getContentType() {
		return contentType;
	}
	
	public String getText() {
		return fields.get("text");
	}

	public String getValue(String key) {
		String value = fields.get(key);
		return value;
	}
	
	public String getValue(String key, String defaultValue) {
		String value = fields.get(key);
		if(StringHelper.containsNonWhitespace(key)) {
			return value;
		}
		return defaultValue;
	}

	public Long getLongValue(String key) {
		String value = fields.get(key);
		if (value == null) { return null; }
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public Integer getIntegerValue(String key) {
		String value = fields.get(key);
		if (value == null) { return null; }
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public File getFile() {
		return file;
	}

	private void save(InputStream source, File targetFile)
	throws IOException {
		InputStream in = new BufferedInputStream(source);
		OutputStream out = new FileOutputStream(targetFile);

		byte[] buffer = new byte[4096];

		int c;
		while ((c = in.read(buffer, 0, buffer.length)) != -1) {
			out.write(buffer, 0, c);
		}

		out.flush();
		out.close();
		in.close();
	}

	public void close() {
		if (file != null) {
			file.delete();
		}
		fields.clear();
	}
	
	public static void closeQuietly(MultipartReader reader) {
		if(reader != null) {
			try {
				reader.close();
			} catch (Exception e) {
				//quietly
			}
		}
	}
}