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
package org.olat.core.gui.media;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

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
	private static final Logger log = Tracing.createLoggerFor(JSONMediaResource.class);
	
	private String encoding = "";
	private JSONArray jsonArray;
	private JSONObject jsonObject;

	public JSONMediaResource(JSONArray jsonArray, String encoding) {
		this.jsonArray = jsonArray;
		this.encoding = encoding;
		setContentType("application/json; charset=" + encoding);
	}
	
	public JSONMediaResource(JSONObject jsonObject, String encoding) {
		this.jsonObject = jsonObject;
		this.encoding = encoding;
		setContentType("application/json; charset=" + encoding);
	}

	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		super.prepare(hres);
		try {
			hres.setCharacterEncoding(encoding);
		} catch (Exception e) {
			log.warn("", e);
		}

		try {
			if(jsonObject != null) {
				jsonObject.write(hres.getWriter());
			} else if(jsonArray != null) {
				jsonArray.write(hres.getWriter());
			}
		} catch (JSONException | IOException e) {
			log.error("", e);
		}
	}
}
