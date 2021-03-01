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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.media.JSONMediaResource;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;

/**
 * Description:<br>
 * component to use the TextBoxList from
 * http://www.interiders.com/2008/02/18/protomultiselect-02/ a bugfixed-version
 * (the one used in OLAT) stays here:
 * http://github.com/thewebfellas/protomultiselect
 * 
 * note: march 2012, strentini merged some bugfixes from
 * https://github.com/garrytan/protomultiselect as of march 2012, this is
 * intended to be used always within a flexiform.
 * 
 * <P>
 * Initial Date: 23.07.2010 <br>
 * 
 * 
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public abstract class TextBoxListComponent extends FormBaseComponentImpl implements Disposable {

	// if changed, do so also in multiselect.js!
	public static final String MORE_RESULTS_INDICATOR = ".....";
	public static final String INPUT_SUFFIX = "textboxlistinput";

	private String inputHint;
	private String icon = "o_icon_tags";

	/**
	 * Holds the initial items (keyString is the caption! valueString is the
	 * value)
	 */
	private List<TextBoxItem> initialItems;

	/**
	 * Holds the current Set of items
	 */
	private List<TextBoxItem> currentItems;
	
	/**
	 * The autoCompletion map. Key-String in the map is the "caption",
	 * Value-String is the "value"
	 */
	private List<TextBoxItem> autoCompletionValues;

	/*
	 * if set to true, multiselect.js will allow new values (apart from the ones
	 * in the autocompletion set)
	 */
	private boolean allowNewValues = true;

	private boolean allowDuplicates = false;

	private static final Logger logger = Tracing.createLoggerFor(TextBoxListComponent.class);

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
	public TextBoxListComponent(String name, String inputHint, List<TextBoxItem> initialItems, Translator translator) {
		super(name, translator);
		this.inputHint = inputHint;
		this.initialItems = initialItems;

		// check for null values
		if (this.initialItems == null) {
			this.initialItems = new ArrayList<>();
		}
		// copy the initialItems into the "currentItems" map
		currentItems = new ArrayList<>(this.initialItems);
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String inputId = "textboxlistinput".concat(getFormDispatchId());
		String cmd = ureq.getParameter(inputId);
		if(cmd == null){
			return;
		}
		setCmd(ureq, cmd);
	}
	
	public void clearItems() {
		currentItems.clear();
	}
	
	public void setCmd(UserRequest ureq, String cmd) {
		if(!StringHelper.containsNonWhitespace(cmd)) {
			return;
		}
		
		List<TextBoxItem> updatedItems = new ArrayList<>();
		if(cmd.startsWith("[")) {
			JSONArray array = new JSONArray(cmd);
			int numOfItems = array.length();
			for(int i=0; i<numOfItems; i++) {
				JSONObject obj = array.optJSONObject(i);
				if(obj != null) {
					String itemLabel = obj.optString("label");
					String itemValue = obj.optString("value");
					TextBoxItem item = getByValue(itemValue);
					if (item == null) {
						updatedItems.add(new TextBoxItemImpl(itemLabel, itemValue, null, true));
					} else {
						updatedItems.add(item);
					}
				}
			}
		} else {
			String[] splitted = cmd.split("[,]");
			List<String> cleanedItemValues = new ArrayList<>();
			for (String item : splitted) {
				if (!StringUtils.isBlank(item)) {
					cleanedItemValues.add(item.trim());
				}
			}
			for (String itemValue : cleanedItemValues) {
				TextBoxItem item = getByValue(itemValue);
				if (item == null) {
					updatedItems.add(new TextBoxItemImpl(itemValue, itemValue, null, true));
				} else {
					updatedItems.add(item);
				}
			}
		}
		
		if (!isAllowDuplicates()) {
			removeDuplicates(updatedItems);
		}
		currentItems = updatedItems;
		fireEvent(ureq, new TextBoxListEvent(updatedItems));	
	}
	
	private void removeDuplicates(List<TextBoxItem> items) {
		Set<String> values = new HashSet<>();
		for(Iterator<TextBoxItem> itemIt=items.iterator(); itemIt.hasNext(); ) {
			TextBoxItem item = itemIt.next();
			if(values.contains(item.getValue())) {
				itemIt.remove();
			} else {
				values.add(item.getValue());
			}
		}
	}
	
	private TextBoxItem getByValue(String value) {
		if(!StringHelper.containsNonWhitespace(value)) return null;
		
		TextBoxItem item = getByValue(initialItems, value);
		if(item == null) {
			item = getByValue(autoCompletionValues, value);
		}
		if(item == null) {
			item = getByValue(currentItems, value);
		}
		return item;
	}
	
	private final TextBoxItem getByValue(List<TextBoxItem> items, String value) {
		if(items == null) return null;
		
		for(TextBoxItem item:items) {
			if(value.equals(item.getValue())) {
				return item;
			}
		}
		return null;
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
	public List<TextBoxItem> getInitialItems() {
		return initialItems;
	}

	/**
	 * returns the current Set of items in the textBoxList<br />
	 * (aka the current "bits"). The returned map contains the captions (as key)
	 * and the values
	 * 
	 * @return the current Items/"bits" of the TextBoxListComponent
	 */
	public List<TextBoxItem> getCurrentItems() {
		return currentItems;
	}
	
	public void setCurrentItems(List<TextBoxItem> currentItems) {
		this.currentItems = currentItems;
		setDirty(true);
	}

	/**
	 * returns the current List of item-values (without the captions)
	 * 
	 * @return
	 */
	public List<String> getCurrentItemValues() {
		return currentItems.stream()
				.map(TextBoxItem::getValue)
				.collect(Collectors.toList());
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		jsa.addRequiredStaticJsFile("js/tagify/tagify.js");
		String userAgent = ureq.getUserSession().getSessionInfo().getUserAgent();
		if(userAgent != null && userAgent.contains("Trident/")) {
			jsa.addRequiredStaticJsFile("js/tagify/tagify.polyfills.min.js");
		}
	}

	/**
	 * registers a OpenOLAT Mapper for this textBoxListComponent
	 * 
	 * @param ureq
	 */
	protected void setMapper(UserRequest ureq) {
		Mapper mapper = (relPath, request) -> {
			JSONArray array = new JSONArray();
			String lastInput = request.getParameter("term");
			if (lastInput != null && lastInput.length() > 1) {
				Map<String, String> autoCContLoc = new HashMap<>();
				provider.getAutoCompleteContent(lastInput, autoCContLoc);
				try {
					for (Map.Entry<String, String> entry : autoCContLoc.entrySet()) {
						JSONObject item = new JSONObject();
						String value = StringHelper.escapeHtml(entry.getKey());
						String label = StringHelper.escapeHtml(entry.getValue());
						item.put("value", value);
						item.put("label", label);
						item.put("searchBy", label);
						array.put(item);
					}
				} catch (Exception e) {
					logger.error("", e);
				}
			}
			return new JSONMediaResource(array, "UTF-8");
		};
		
		MapperService mapperService = CoreSpringFactory.getImpl(MapperService.class);
		if(mapperKey != null) {
			mapperService.cleanUp(List.of(mapperKey));
		}
		mapperKey = mapperService.register(ureq.getUserSession(), mapper);
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
	 * @return Returns the autoCompletionValues as Map, where the Key-String is
	 *         the caption, the Value-String the value of the
	 *         auto-Completion-item
	 */
	public List<TextBoxItem> getAutoCompleteContent() {
		return autoCompletionValues;
	}

	/**
	 * @param autoCompletionValues
	 *            set a Map to use for autocompletion. Key in the map is the
	 *            "caption"
	 */
	public void setAutoCompleteContent(List<TextBoxItem> autoCompletionValues) {
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
		List<TextBoxItem> map = new ArrayList<>(autoCompletionValues.size());
		for (String string : autoCompletionValues) {
			map.add(new TextBoxItemImpl(string, string, null, true));
		}
		setAutoCompleteContent(map);
	}

	public void setMapperProvider(ResultMapProvider provider, UserRequest ureq) {
		this.provider = provider;
		setMapper(ureq);
	}
	
	public boolean hasMapper() {
		return mapperKey != null;
	}

	/**
	 * @return Returns the mapperUri.
	 */
	public String getMapperUri() {
		return mapperKey == null ? null : mapperKey.getUrl();
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
		List<TextBoxItem> content = getCurrentItems();
		if (content != null && !content.isEmpty()) {
			//antisamy + escaping to prevent issue with the javascript code
			OWASPAntiSamyXSSFilter filter = new OWASPAntiSamyXSSFilter();
			List<String> filtered = new ArrayList<>();
			for(TextBoxItem item:content) {
				String antiItem = filter.filter(item.getValue());
				if(StringHelper.containsNonWhitespace(antiItem)) {
					filtered.add(antiItem);
				}
			}
			return StringUtils.join(filtered, ", ");
		}
		return "";
	}
	
	public String getIcon() {
		return this.icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Override
	public void dispose() {
		if (mapperKey != null) {
			CoreSpringFactory.getImpl(MapperService.class).cleanUp(List.of(mapperKey));
			mapperKey = null;
		}		
	}
}
