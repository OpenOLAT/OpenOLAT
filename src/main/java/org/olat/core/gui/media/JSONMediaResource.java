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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.core.gui.media;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.json.JSONArray;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * A JSON MediaResource. Represents a String holding JSON-data
 * 
 * <P>
 * Initial Date: 31.08.2011 <br>
 * 
 * @author mkuendig
 */
public class JSONMediaResource extends DefaultMediaResource {
	private static final String ENCODING_DEFAULT = "iso-8859-1";

	private String encoding = "";
	private JSONArray json;

	public JSONMediaResource(JSONArray json, String encoding) {
		this.json = json;
		this.encoding = encoding;
		this.setContentType("application/json; charset=" + encoding);
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getInputStream()
	 */
	public InputStream getInputStream() {
		ByteArrayInputStream bis = null;
		try {
			bis = new ByteArrayInputStream(json.toString().getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			try {
				bis = new ByteArrayInputStream(json.toString().getBytes(ENCODING_DEFAULT));
			} catch (UnsupportedEncodingException ec) {
				throw new AssertException(encoding + " encoding not supported??");
				// iso-8859-1 must be supported on the platform
			}
		}
		return new BufferedInputStream(bis);
		// nputStream sis = new
		// ByteArrayInputStream(json.toString().getBytes());
		// return sis;
	}

}
