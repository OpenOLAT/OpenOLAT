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
package org.olat.core.gui.control.generic.ajax.autocompletion;

import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;

/**
 * 
 * Description:<br>
 * The AutoCompleterListReceiver implementes a list receiver that generates JSON
 * output. The class is only used in the AutoCompleterController
 * 
 * <P>
 * Initial Date: 25.11.2010 <br>
 * 
 * @author gnaegi
 */
public class AutoCompleterListReceiver implements ListReceiver {
	
	private static final Logger log = Tracing.createLoggerFor(AutoCompleterListReceiver.class);
	
	private static final String VALUE = "value";
	private static final String CSS_CLASS = "cssClass";
	private static final String CSS_CLASS_EMPTY = "";
	private static final String DISPLAY_KEY = "displayKey";
	private static final String DISPLAY_KEY_NO_RESULTS = "-";
	
	private final JSONArray list = new JSONArray();
	private final String noresults;
	private final boolean showDisplayKey;

	/**
	 * Constructor
	 * 
	 * @param noResults Text to use when no results are found
	 * @param showDisplayKey true: add displayKey in result; false: don't add
	 *          displayKey in results (e.g. to protect privacy)
	 */
	public AutoCompleterListReceiver(String noresults, boolean showDisplayKey) {
		this.noresults = noresults;
		this.showDisplayKey = showDisplayKey;
	}
	
	@Override
	public void addEntry(String key, String displayText) {
		addEntry(key, key, displayText, null);
	}

	/**
	 * @return the result as a JSONArray object
	 */
	public JSONArray getResult() {
		if (list.length() == 0) {
			addEntry(AutoCompleterController.AUTOCOMPLETER_NO_RESULT, DISPLAY_KEY_NO_RESULTS, noresults, CSSHelper.CSS_CLASS_ERROR);
		}
		return list;
	}

	@Override	
	public void addEntry(String key, String displayKey, String displayText, String iconCssClass) {
		if (key == null) {
			throw new AssertException("Can not add entry with displayText::" + displayText + " with a NULL key!");
		}
		if (log.isDebugEnabled()) {
			log.debug("Add entry with key::" + key+ ", displayKey::" + displayKey + ", displayText::" + displayText + ", iconCssClass::" + iconCssClass);
		}
		try {
			JSONObject object = new JSONObject();
			// add key
			object.put("key", key);
			// add displayable key, use key as fallback 
			if (showDisplayKey) {				
				if (displayKey == null) {
					object.put(DISPLAY_KEY, key);
				} else {
					object.put(DISPLAY_KEY, displayKey);
				}
			}
			// add value to be displayed
			object.put(VALUE, displayText);
			// add optional css class
			if (iconCssClass == null) {
				object.put(CSS_CLASS, CSS_CLASS_EMPTY);								
			} else {
				object.put(CSS_CLASS, iconCssClass);				
			}
			// JSCON object finished
			list.put(object);

		} catch (JSONException e) {
			// do nothing, only log error to logfile
			log.error("Could not add entry with key::" + key+ ", displayKey::" + displayKey + ", displayText::" + displayText + ", iconCssClass::" + iconCssClass, e);
		}

	}
}