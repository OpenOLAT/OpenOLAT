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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;

/**
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MultipartReader {

	private static final Logger log = Tracing.createLoggerFor(MultipartReader.class);

	private String filename;
	private String contentType;
	private File file;
	private Map<String, String> fields = new HashMap<>();

	public MultipartReader(HttpServletRequest request) {
		servlet31(request);
	}
	
	private final void servlet31(HttpServletRequest request) {
		try {
			for(Part part:request.getParts()) {
				if(part.getContentType() != null && (StringHelper.containsNonWhitespace(part.getSubmittedFileName()) || !part.getContentType().startsWith("text/plain"))) {
					contentType = part.getContentType();
					filename = part.getSubmittedFileName();
					if(filename != null && !filename.contains("..")) {
						filename = UUID.randomUUID().toString().replace("-", "") + "_" + filename;
					} else {
						filename = "upload-" + UUID.randomUUID().toString().replace("-", "");
					}
					file = new File(WebappHelper.getTmpDir(), filename);
					part.write(file.getAbsolutePath());
					file = new File(WebappHelper.getTmpDir(), filename);
				} else {
					String value = IOUtils.toString(part.getInputStream(), request.getCharacterEncoding());
					fields.put(part.getName(), value);
				}
				
				try {
					part.delete();
				} catch (Exception e) {
					//we try (tomcat doesn't send exception but undertow)
				}
			}
		} catch (IOException | ServletException e) {
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
		return fields.get(key);
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

	public void close() {
		if (file != null) {
			try {
				Files.deleteIfExists(file.toPath());
			} catch (IOException e) {
				log.error("", e);
			}
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