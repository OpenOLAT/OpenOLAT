/**
jk * <a href="http://www.openolat.org">
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
package org.olat.core.gui.control.generic.ajax.autocompletion;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.JSONMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AutoCompleterMapper implements Mapper {
	private final static OLog log = Tracing.createLoggerFor(AutoCompleterMapper.class);
	
	private static final String CONTENT_TYPE_APPLICATION_X_JSON = "application/x-json";
	private static final String CONTENT_TYPE_TEXT_JAVASCRIPT = "text/javascript";
	private static final String RESPONSE_ENCODING = "utf-8";
	private static final String PARAM_CALLBACK = "callback";
	private static final String PARAM_QUERY = "term";
	protected static final String PARAM_KEY = "key";
	
	private final String noResults;
	private final boolean showDisplayKey;
	private final ListProvider gprovider;
	
	public AutoCompleterMapper(String noResults, boolean showDisplayKey, ListProvider gprovider) {
		this.noResults = noResults;
		this.showDisplayKey = showDisplayKey;
		this.gprovider = gprovider;
	}

	@Override
	@SuppressWarnings({ "synthetic-access" })			
	public MediaResource handle(String relPath, HttpServletRequest request) {
		// Prepare resulting media resource
		StringBuilder response = new StringBuilder();

		// Prepare result for ExtJS ScriptTagProxy call-back
		boolean scriptTag = false;
		String cb = request.getParameter(PARAM_CALLBACK);
		if (cb != null) {
		    scriptTag = true;
		}
		if (scriptTag) {
		    response.append(cb + "(");
		}
		// Read query and generate JSON result
		String lastN = request.getParameter(PARAM_QUERY);
		AutoCompleterListReceiver receiver = new AutoCompleterListReceiver(noResults, showDisplayKey);
		gprovider.getResult(lastN, receiver);

		JSONArray result = receiver.getResult(); 
		return new JSONMediaResource(result, "UTF-8");
	}
}
