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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.htmlheader.jscss.CustomJSFormItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Description:<br>
 * The AutoCompleterController provides an input field with a live-AJAX feed
 * from the database. While typing, after entering a configurable amount of
 * characters, the system performs a server side search and shows a list of
 * search results the user can choose from.
 * <p>
 * This controller uses the typeahead javascript library to implement the feature
 * <p>
 * Fires: an EntriesChosenEvent which contain the chosen entry/entries as
 * strings
 * <P>
 * Initial Date: 06.10.2006 <br>
 * 
 * @author Felix Jost, FLorian Gnägi
 */
public class FlexiAutoCompleterController extends FormBasicController {

	protected static final String COMMAND_SELECT = "select";
	protected static final String COMMAND_CHANGE = "change";
	protected static final String JSNAME_INPUTFIELD = "o_autocomplete_input";
	protected static final String JSNAME_DATASTORE = "autocompleterDatastore";
	protected static final String JSNAME_COMBOBOX = "autocompleterCombobox";
	protected static final String AUTOCOMPLETER_NO_RESULT = "AUTOCOMPLETER_NO_RESULT";

	private Mapper mapper;
	private ListProvider gprovider;
	private boolean allowNewValues;
	private boolean formElement;

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
	public FlexiAutoCompleterController(UserRequest ureq, WindowControl wControl, ListProvider provider, String noresults,
			final boolean showDisplayKey, int inputWidth, int minChars, String label) {
		super(ureq, wControl, "autocomplete");
		this.gprovider = provider;
		this.allowNewValues = false;
		setupAutoCompleter(ureq, flc, noresults, showDisplayKey, inputWidth, minChars, label);
		setFormElement(true);
	}
	
	public FlexiAutoCompleterController(UserRequest ureq, WindowControl wControl, ListProvider provider, String noresults,
			final boolean showDisplayKey, final boolean allowNewValues, int inputWidth, int minChars, String label, Form externalMainForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "autocomplete", externalMainForm);
		this.gprovider = provider;
		this.allowNewValues = allowNewValues;
		setupAutoCompleter(ureq, flc, noresults, showDisplayKey, inputWidth, minChars, label);
		setFormElement(true);
	}
	
	protected FlexiAutoCompleterController(UserRequest ureq, WindowControl wControl, int layout, String customLayoutPageName, Form externalMainForm) {
		super(ureq, wControl, layout, customLayoutPageName, externalMainForm);
	}
	
	protected FlexiAutoCompleterController(UserRequest ureq, WindowControl wControl, String customLayoutPageName) {
		super(ureq, wControl, customLayoutPageName);
	}
	
	protected void setListProvider(ListProvider provider) {
		this.gprovider = provider;
	}
	
	protected void setAllowNewValues(boolean allowNewValues) {
		this.allowNewValues = allowNewValues;
	}
	
	public boolean isFormElement() {
		return formElement;
	}

	public void setFormElement(boolean formElement) {
		this.formElement = formElement;
		if(formElement) {
			flc.contextPut("formElementClass", "b_form_element");
		} else {
			flc.contextPut("formElementClass", "");
		}
	}

	protected void setupAutoCompleter(UserRequest ureq, FormLayoutContainer layoutCont, String noresults, boolean showDisplayKey, int inputWidth, int minChars, String label) {
		String noResults = (noresults == null ? translate("autocomplete.noresults") : noresults);
		// Configure displaying parameters
		
		layoutCont.add("typeahead", new CustomJSFormItem("typeahead", new String[] {
				"js/jquery/typeahead/typeahead.bundle.min.js"
		}));
	
		if (label != null) {
			layoutCont.contextPut("autocompleter_label", label);
		}
		layoutCont.contextPut("showDisplayKey", Boolean.valueOf(showDisplayKey));
		layoutCont.contextPut("inputWidth", Integer.valueOf(inputWidth));
		layoutCont.contextPut("minChars", Integer.valueOf(minChars));
		layoutCont.contextPut("flexi", Boolean.TRUE);
		layoutCont.contextPut("inputValue", "");
		int limit = gprovider == null || gprovider.getMaxEntries() <= 0 ? 5 : gprovider.getMaxEntries();
		layoutCont.contextPut("limit", Integer.valueOf(limit));
		layoutCont.getComponent().addListener(this);

		// Create a mapper for the server responses for a given input
		mapper = new AutoCompleterMapper(noResults, showDisplayKey, gprovider);
		
		// Add mapper URL to JS data store in velocity
		String fetchUri = registerMapper(ureq, mapper);
		layoutCont.contextPut("mapuri", fetchUri + "/autocomplete.json");
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//
	}

	/**
	 * This dispatches component events...
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == flc.getComponent()) {
			String value = getSearchValue(ureq);
			flc.contextPut("inputValue", value);			
			if (event.getCommand().equals(COMMAND_SELECT)) {
				doSelect(ureq);
			} else if (event.getCommand().equals(COMMAND_CHANGE)) {
				if(allowNewValues) {
					fireEvent(ureq, new NewValueChosenEvent(value));	
				} else {
					super.event(ureq, source, event);
				}				
			}
		} else {
			super.event(ureq, source, event);
		}
	}
	
	protected void doSelect(UserRequest ureq) {
		List<String> selectedEntries = new ArrayList<>(); // init empty result list
		String key = ureq.getParameter(AutoCompleterMapper.PARAM_KEY);
		if (key == null) {
			// Fallback to submitted input field: the input field does not contain
			// the key but the search value itself
			String searchValue = getSearchValue(ureq);
			if (searchValue == null) {
				// log error because something went wrong in the code and send empty list as result
				logError("Auto complete JS code must always send 'key' or the autocomplete parameter!", null);						
				getWindowControl().setError(translate("autocomplete.error"));
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
		doFireSelection(ureq, selectedEntries);
	}
	
	protected void doFireSelection(UserRequest ureq, List<String> selectedEntries) {
		fireEvent(ureq, new EntriesChosenEvent(selectedEntries));
	}
	
	protected String getSearchValue(UserRequest ureq) {
		String searchValue = ureq.getParameter(flc.getId(JSNAME_INPUTFIELD));
		return searchValue;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//
	}
}