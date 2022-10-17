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

import jakarta.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.JSONMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.StringHelper;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AutoCompleterMapper implements Mapper {
	private static final String PARAM_QUERY = "term";
	protected static final String PARAM_KEY = "key";
	protected static final String PARAM_VALUE = "val";
	
	private final String noResults;
	private final boolean showDisplayKey;
	private final ListProvider gprovider;
	
	public AutoCompleterMapper(String noResults, boolean showDisplayKey, ListProvider gprovider) {
		this.noResults = noResults;
		this.showDisplayKey = showDisplayKey;
		this.gprovider = gprovider;
	}

	@Override		
	public MediaResource handle(String relPath, HttpServletRequest request) {
		// Read query and generate JSON result
		String lastN = request.getParameter(PARAM_QUERY);
		JSONArray result;
		if(StringHelper.containsNonWhitespace(lastN)) {
			AutoCompleterListReceiver receiver = new AutoCompleterListReceiver(noResults, showDisplayKey);
			gprovider.getResult(lastN, receiver);
			result = receiver.getResult(); 
		} else {
			result = new JSONArray();
		}
		return new JSONMediaResource(result, "UTF-8");
	}
}