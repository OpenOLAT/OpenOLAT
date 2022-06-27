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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.core.gui.control.generic.ajax.autocompletion;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.CustomJSComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.render.velocity.VelocityRenderDecorator;

/**
 * 
 * Description:<br>
 * The AutoCompleterController provides an input field with a live-AJAX feed
 * from the database. While typing, after entering a configurable amount of
 * characters, the system performs a server side search and shows a list of
 * search results the user can choose from.
 * <p>
 * This controller uses typeahead javascript library to implement the feature
 * <p>
 * Fires: an EntriesChosenEvent which contain the chosen entry/entries as
 * strings
 * <P>
 * Initial Date: 06.10.2006 <br>
 * 
 * @author Felix Jost, FLorian Gnägi
 */
public class AutoCompleterController extends BasicController {
	private static final String COMMAND_SELECT = "select";
	private static final String JSNAME_INPUTFIELD = "o_autocomplete_input";

	static final String AUTOCOMPLETER_NO_RESULT = "AUTOCOMPLETER_NO_RESULT";

	private VelocityContainer myContent;
	private Mapper mapper;
	private final ListProvider gprovider;
	private final String noResults;
	private boolean emptyAsReset;

	/**
	 * Constructor to create an auto completer controller
	 * 
	 * @param ureq
	 *            The user request object
	 * @param wControl
	 *            The window control object
	 * @param provider
	 *            The provider that can be called to return the search-results
	 *            for a given search query
	 * @param noResults
	 *            The translated value to display when no results are found,
	 *            e.g. "no matches found" or "-no users found-". When a NULL
	 *            value is provided, the controller will use a generic message.
	 * @param showDisplayKey
	 *            true: show the key for each record; false: don't show the key,
	 *            only the value
	 * @param inputWidth
	 *            The input field width in characters
	 * @param minChars
	 *            The minimum number of characters the user has to enter to
	 *            perform a search
	 * @param label
	 */
	public AutoCompleterController(UserRequest ureq, WindowControl wControl, ListProvider provider, String noresults,
			final boolean showDisplayKey, int inputWidth, int minChars, String label) {
		super(ureq, wControl);
		this.gprovider = provider;
		this.noResults = (noresults == null ? translate("autocomplete.noresults") : noresults);
		this.myContent = createVelocityContainer("autocomplete");
		
		// Configure displaying parameters
		if (label != null) {
			myContent.contextPut("autocompleter_label", label);
		}
		
		myContent.put("typeahead", new CustomJSComponent("typeahead", new String[] {
				"js/jquery/typeahead/typeahead.bundle.min.js"
		}));
		
		myContent.contextPut("showDisplayKey", Boolean.valueOf(showDisplayKey));
		myContent.contextPut("inputWidth", Integer.valueOf(inputWidth));
		myContent.contextPut("minChars", Integer.valueOf(minChars));
		myContent.contextPut("inputValue", "");
		int limit = provider == null || provider.getMaxEntries() <= 0 ? 5 : provider.getMaxEntries();
		myContent.contextPut("limit", Integer.valueOf(limit));
		// Create a mapper for the server responses for a given input
		mapper = new AutoCompleterMapper(noResults, showDisplayKey, gprovider);
			
		// Add mapper URL to JS data store in velocity
		String fetchUri = registerMapper(ureq, mapper);
		myContent.contextPut("mapuri", fetchUri + "/autocomplete.json");
		putInitialPanel(myContent);
	}

	public boolean isEmptyAsReset() {
		return emptyAsReset;
	}

	public void setEmptyAsReset(boolean emptyAsReset) {
		this.emptyAsReset = emptyAsReset;
	}
	
	/**
	 * Set a message that is displayed as a grayed-out message in the input field
	 * when the field is empty.
	 * 
	 * @param placeholder
	 */
	public void setPlaceholderMessage(String placeholder) {
		myContent.contextPut("placeholder", placeholder);
	}
	
	/**
	 * Delete the current value in the input field and replace it with an empty
	 * string
	 */
	public void resetAutocompleter() {
		myContent.contextPut("inputValue", "");
	}

	/**
	 * This dispatches component events...
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == myContent) {
			if (COMMAND_SELECT.equals(event.getCommand())) {
				List<String> selectedEntries = new ArrayList<>(); // init empty result list
				String key = ureq.getParameter(AutoCompleterMapper.PARAM_KEY);
				if (key == null) {
					// Fallback to submitted input field: the input field does not contain
					// the key but the search value itself
					String searchValue = ureq.getParameter(VelocityRenderDecorator.getId(JSNAME_INPUTFIELD, myContent));
					if (searchValue == null) {
						// log error because something went wrong in the code and send empty list as result
						logError("Auto complete JS code must always send 'key' or the autocomplete parameter!", null);						
						getWindowControl().setError(translate("autocomplete.error"));
						return;
					} else if (searchValue.equals("")) {
						if(!isEmptyAsReset()) {
							getWindowControl().setWarning(translate("autocomplete.not.enough.chars"));
						}
						fireEvent(ureq, new EmptyChosenEvent());
						return;
					} else if (searchValue.equals("") || searchValue.length() < 3) {
						getWindowControl().setWarning(translate("autocomplete.not.enough.chars"));
						return;
					}
					// Create temporary receiver and perform search for given value. 
					AutoCompleterListReceiver receiver = new AutoCompleterListReceiver("-", false);
					gprovider.getResult(searchValue, receiver);
					JSONArray result = receiver.getResult();
					// Use key from first result
					if (result.length() > 0) {
						try {
							JSONObject object = result.getJSONObject(0);
							key = object.getString(AutoCompleterMapper.PARAM_KEY);
						} catch (JSONException e) {
							logError("Error while getting json object from list receiver", e);						
							key = "";
						}
					} else {
						key = "";
					}
				}
				// Proceed with a key, empty or valid key
				key = key.trim();
				if (!key.equals("") && !key.equals(AUTOCOMPLETER_NO_RESULT)) {
					// Normal case, add entry
					selectedEntries.add(key);
				} else if (key.equals(AUTOCOMPLETER_NO_RESULT)) {
					return;
				}
				fireEvent(ureq, new EntriesChosenEvent(selectedEntries));					
			}
		}
	}
}