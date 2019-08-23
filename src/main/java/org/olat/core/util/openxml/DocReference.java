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
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * Initial date: 04.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DocReference {

	private final String id;
	private final String filename;
	private final URL url;
	private final OpenXMLSize emuSize;

	public DocReference(String id, String filename, OpenXMLSize emuSize, File file) {
		this.id = id;
		this.url = fileToURL(file);
		this.emuSize = emuSize;
		this.filename = filename;
	}

	public DocReference(String id, String filename, OpenXMLSize emuSize, URL url) {
		this.id = id;
		this.url = url;
		this.emuSize = emuSize;
		this.filename = filename;
	}

	private URL fileToURL(File file) {
		URL tmp_url = null;
		try {
			tmp_url  = file.toURI().toURL();
		} catch (MalformedURLException e) {
			//
		}
		return tmp_url;
	}

	public String getId() {
		return id;
	}

	public String getFilename() {
		return filename;
	}

	public URL getUrl() {
		return url;
	}

	public OpenXMLSize getEmuSize() {
		return emuSize;
	}
}
