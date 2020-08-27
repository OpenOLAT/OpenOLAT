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
package org.olat.core.gui.components.textboxlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.media.JSONMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.translator.Translator;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;

/**
 * Initial Date: 23.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public abstract class TextBoxListComponent extends FormBaseComponentImpl {

	// if changed, do so also in multiselect.js!
	public static final String MORE_RESULTS_INDICATOR = ".....";
	public static final String INPUT_SUFFIX = "textboxlistinput";

	private String inputHint;

	/*
	 * holds the initial items (keyString is the caption! valueString is the
	 * value)
	 */
	private Map<String, String> initialItems;

	/*
	 * holds the current Set of items
	 */
	private Map<String, String> currentItems;

	/*
	 * if set to true, multiselect.js will allow new values (apart from the ones
	 * in the autocompletion set)
	 */
	private boolean allowNewValues = true;

	private boolean allowDuplicates = false;

	private static final Logger logger = Tracing.createLoggerFor(TextBoxListComponent.class);

	/*
	 * the autoCompletion map. Key-String in the map is the "caption",
	 * Value-String is the "value"
	 */
	private Map<String, String> autoCompletionValues;

	private ResultMapProvider provider;
	private MapperKey mapperKey;

	/*
	 * the number of maxResults shown in the auto-completion list
	 */
	private int maxResults;

	/**
	 * 
	 * @param name
	 * @param inputHint
	 *            i18n key for an input hint, displayed when no autocompletion
	 *            result are shown and the pointer is in the input field
	 * @param initialItems
	 *            set the already existing items. Map is "Key, Value" where
	 *            value could be null. so returned value is same as key. If you
	 *            don't want to set any intial items, just pass null or an empty
	 *            map
	 * 
	 */
	public TextBoxListComponent(String name, String inputHint, Map<String, String> initialItems, Translator translator) {
		super(name, translator);
		this.inputHint = inputHint;
		this.initialItems = initialItems;

		// check for null values
		if (this.initialItems == null) {
			this.initialItems = new HashMap<>();
		}

		// copy the initialItems into the "currentItems" map
		this.currentItems = new HashMap<>();
		
		for (Entry<String, String> initialMapEntry : this.initialItems.entrySet()) {
			currentItems.put(initialMapEntry.getKey(), initialMapEntry.getValue());
		}
	}

	/**
	 * @see org.olat.core.gui.components.Component#doDispatchRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String inputId = "textboxlistinput" + getFormDispatchId();
		String cmd = ureq.getParameter(inputId);
		if(cmd == null){
			return;
		}
		setCmd(ureq, cmd);
	}
	
	public void setCmd(UserRequest ureq, String cmd) {
		if(cmd == null) {
			return;
		}
		// empty string is ok = empty text box
		
		String[] splitted = cmd.split(",");
		List<String> cleanedItemValues = new ArrayList<>();
		for (String item : splitted) {
			if (!StringUtils.isBlank(item))
				cleanedItemValues.add(item.trim());
		}
		if (!isAllowDuplicates())
			removeDuplicates(cleanedItemValues);

		// update our current items
		currentItems = new HashMap<>();
		String caption = "";
		for (String itemValue : cleanedItemValues) {
			caption = getCaptionForKnownValue(itemValue);
			if ("".equals(caption)) {
				currentItems.put(itemValue, itemValue);
			} else {
				currentItems.put(caption, itemValue);
			}
		}

		if (logger.isDebugEnabled())
			logger.debug("doDispatchRequest --> firing textBoxListEvent with current items: " + cleanedItemValues);
		fireEvent(ureq, new TextBoxListEvent(cleanedItemValues));	
	}

	/**
	 * 
	 * @param itemValue
	 * @return
	 */
	private String getCaptionForKnownValue(String itemValue) {
		String caption = getInitialItemCaptionByValue(itemValue);
		if ("".equals(caption))
			caption = getAutoCompletionItemCaptionByValue(itemValue);

		return caption;
	}

	/**
	 * 
	 * @return
	 */
	private String getInitialItemCaptionByValue(String itemValue) {
		String initialItemCaption = "";
		for (Entry<String, String> initialItemEntry : this.initialItems.entrySet()) {
			if (initialItemEntry.getValue().equals(itemValue))
				initialItemCaption = initialItemEntry.getKey();
		}
		return initialItemCaption;
	}

	/**
	 * 
	 * @param itemValue
	 * @return
	 */
	private String getAutoCompletionItemCaptionByValue(String itemValue) {
		String autoCompletionItemCaption = "";
		Map<String,String> content = getAutoCompleteContent();
		if (content == null) {
			return autoCompletionItemCaption;
		}
		for (Entry<String, String> autoCompletionItemEntry : content.entrySet()) {
			if (autoCompletionItemEntry.getValue().equals(itemValue)) {
				autoCompletionItemCaption = autoCompletionItemEntry.getKey();
			}
		}
		return autoCompletionItemCaption;
	}

	/**
	 * 
	 * @param arlList
	 */
	private static void removeDuplicates(List<String> arlList) {
		HashSet<String> h = new HashSet<>(arlList);
		arlList.clear();
		arlList.addAll(h);
	}

	/**
	 * returns the input-hint <br />
	 * (the text that is displayed within the input-field on rendering)
	 * 
	 * @return
	 */
	public String getInputHint() {
		return inputHint;
	}

	/**
	 * @return Returns the provider.
	 */
	public ResultMapProvider getProvider() {
		return provider;
	}

	/**
	 * returns the set of initial items
	 * 
	 * @return
	 */
	public Map<String, String> getInitialItems() {
		return initialItems;
	}

	/**
	 * returns the current Set of items in the textBoxList<br />
	 * (aka the current "bits"). The returned map contains the captions (as key)
	 * and the values
	 * 
	 * @return the current Items/"bits" of the TextBoxListComponent
	 */
	public Map<String, String> getCurrentItems() {
		return currentItems;
	}

	/**
	 * returns the current List of item-values (without the captions)
	 * 
	 * @return
	 */
	public List<String> getCurrentItemValues() {
		return new ArrayList<>(currentItems.values());
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		jsa.addRequiredStaticJsFile("js/jquery/tagsinput/bootstrap-tagsinput.min.js");
		if (provider != null) {
			jsa.addRequiredStaticJsFile("js/jquery/typeahead/typeahead.bundle.min.js");
			setMapper(ureq);
		}
	}

	/**
	 * registers a OpenOLAT Mapper for this textBoxListComponent
	 * 
	 * @param ureq
	 */
	private void setMapper(UserRequest ureq) {
		Mapper mapper = new Mapper() {
			@Override
			public MediaResource handle(String relPath, HttpServletRequest request) {
				String lastInput = request.getParameter("term");
				if (lastInput != null && lastInput.length() > 2) {
					Map<String, String> autoCContLoc = new HashMap<>();
					provider.getAutoCompleteContent(lastInput, autoCContLoc);
					setAutoCompleteContent(autoCContLoc);
				}
				JSONArray jsonResult = getAutoCompleteJSON();
				return new JSONMediaResource(jsonResult, "UTF-8");
			}
		};

		mapperKey = CoreSpringFactory.getImpl(MapperService.class).register(ureq.getUserSession(), mapper);
	}

	/**
	 * @param allowNewValues
	 *            if set to false, no values outside the autocompletion-result
	 *            are allowed to be entered. default is true
	 */
	public void setAllowNewValues(boolean allowNewValues) {
		this.allowNewValues = allowNewValues;
	}

	/**
	 * @return Returns true if its allowed to enter new values
	 */
	public boolean isAllowNewValues() {
		return allowNewValues;
	}

	/**
	 * @return Returns the allowDuplicates.
	 */
	public boolean isAllowDuplicates() {
		return allowDuplicates;
	}

	/**
	 * @param allowDuplicates
	 *            if set to false (default) duplicates will be filtered
	 *            automatically
	 */
	public void setAllowDuplicates(boolean allowDuplicates) {
		this.allowDuplicates = allowDuplicates;
	}

	/**
	 * @param autoCompletionValues
	 *            set a Map to use for autocompletion. Key in the map is the
	 *            "caption"
	 */
	public void setAutoCompleteContent(Map<String, String> autoCompletionValues) {
		this.autoCompletionValues = autoCompletionValues;
	}

	/**
	 * set a Set of auto-completion values. ( caption will be equal to value,
	 * use setAutoCompleteContent(Map<String, String> autoCompleteContent) if
	 * you want to set custom values and captions )
	 * 
	 * @param autoCompletionValues
	 *            the Set of autoCompletionValues to use in this
	 *            TextBoxListComponent
	 */
	public void setAutoCompleteContent(Set<String> autoCompletionValues) {
		Map<String, String> map = new HashMap<>(autoCompletionValues.size());
		for (String string : autoCompletionValues) {
			map.put(string, string);
		}
		setAutoCompleteContent(map);
	}

	/**
	 * @return Returns the autoCompletionValues as Map, where the Key-String is
	 *         the caption, the Value-String the value of the
	 *         auto-Completion-item
	 */
	public Map<String, String> getAutoCompleteContent() {
		return autoCompletionValues;
	}

	/**
	 * returns the AutoCompletionContent as JSON String.<br />
	 * it will contain the captions and values
	 * 
	 * @return the autoCompletionContent as JSON
	 */
	protected JSONArray getAutoCompleteJSON() {
		JSONArray array = new JSONArray();
		try {
			Map<String, String> autoCont = getAutoCompleteContent();
			if (autoCont != null) {
				for (String item : autoCont.keySet()) {
					array.put(StringHelper.escapeHtml(autoCont.get(item)));
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		return array;
	}

	public void setMapperProvider(ResultMapProvider provider) {
		this.provider = provider;
	}

	/**
	 * @return Returns the mapperUri.
	 */
	public String getMapperUri() {
		return mapperKey.getUrl();
	}

	/**
	 * @return Return the maximal number of results shown by the auto-completion
	 *         list
	 */
	public int getMaxResults() {
		return maxResults;
	}

	/**
	 * set the maximal number of results that should be shown by the
	 * auto-completion list
	 * 
	 * @param maxResults
	 */
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	/**
	 * returns a the current items as comma-separated list.<br />
	 * 
	 * @return An HTML escaped list of item
	 */
	protected String getItemsAsString() {
		Map<String, String> content = getCurrentItems();
		
		if (content != null && content.size() != 0) {
			//antisamy + escaping to prevent issue with the javascript code
			OWASPAntiSamyXSSFilter filter = new OWASPAntiSamyXSSFilter();
			List<String> filtered = new ArrayList<>();
			for(String item:content.keySet()) {
				String antiItem = filter.filter(item);
				if(StringHelper.containsNonWhitespace(antiItem)) {
					filtered.add(antiItem);
				}
			}
			return StringUtils.join(filtered, ", ");
		} else
			return "";
	}

}
