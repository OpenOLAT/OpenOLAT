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

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperRegistry;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * component to use the TextBoxList from 
 * http://www.interiders.com/2008/02/18/protomultiselect-02/
 * a bugfixed-version (the one used in OLAT) stays here:
 * http://github.com/thewebfellas/protomultiselect
 * 
 * <P>
 * Initial Date: 23.07.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class TextBoxListComponent extends FormBaseComponentImpl {

	public static final String MORE_RESULTS_INDICATOR = "....."; // if changed, do so also in multiselect.js!
	private static final ComponentRenderer RENDERER = new TextBoxListRenderer(false);
	private String inputHint;
	private Map<String, String> initialItems;
	private boolean allowNewValues = true;
	private boolean allowDuplicates = false;
	private Map<String, String> autoCompleteContent;
	ResultMapProvider provider;
	private String mapperUri;
	private boolean noFormSubmit;
	private int maxResults;

	private TextBoxListComponent(String name, Translator translator) {
		super(name, translator);
	}

	/**
	 * 
	 * @param name
	 * @param inputHint i18n key for an input hint, displayed when no autocompletion result are shown and the pointer is in the input field
	 * @param initialItems set the already existing items. Map is "Key, Value" where value could be null. so returned value is same as key.
	 * 
	 */
	public TextBoxListComponent(String name, String inputHint, Map<String, String> initialItems, Translator translator) {
		this(name, translator);
		this.inputHint = inputHint;
		this.initialItems = initialItems;
	}

	/**
	 * @see org.olat.core.gui.components.Component#doDispatchRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String inputId = "textboxlistinput" + getFormDispatchId();
		String cmd = ureq.getParameter(inputId);
		if (StringHelper.containsNonWhitespace(cmd)){
			List<String> items = StringHelper.getParts(cmd, ",");
			List<String> allCleaned = new ArrayList<String>();
			List<String> newOnly = new ArrayList<String>();
			for (String item : items) {
				String cleanedItem = item; 
				if (item.startsWith("[") && item.endsWith("]")){
					cleanedItem = item.substring(1, item.length()-1);
					newOnly.add(cleanedItem);
				} 
				allCleaned.add(cleanedItem); 
			}
			if (!isAllowDuplicates()) removeDuplicates(allCleaned);			
			fireEvent(ureq, new TextBoxListEvent(allCleaned, newOnly));			
		}		
	}
	
	public static void removeDuplicates(List<String> arlList) {
		HashSet<String> h = new HashSet<String>(arlList);
		arlList.clear();
		arlList.addAll(h);
	}
	

	/**
	 * @see org.olat.core.gui.components.Component#getHTMLRendererSingleton()
	 */
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	public String getInputHint() {
		return inputHint;
	}
	/**
	 * @return Returns the provider.
	 */
	public ResultMapProvider getProvider() {
		return provider;
	}

	public Map<String, String> getInitialItems() {
		return initialItems;
	}

	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		jsa.addRequiredJsFile(TextBoxListComponent.class, "js/multiselect.js");
		if (this.provider != null) setMapper(ureq);
	}

	/**
	 * @param allowNewValues if set to false, no values outside the autocompletion-result are allowed to be entered. 
	 * default is true
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
	 * @param allowDuplicates if set to false (default) duplicates will be filtered automatically
	 */
	public void setAllowDuplicates(boolean allowDuplicates) {
		this.allowDuplicates = allowDuplicates;
	}

	/**
	 * @param autoCompleteContent set a Map to use for autocompletion
	 * the integer is displayed in the list and elements get ordered by its value 
	 */
	public void setAutoCompleteContent(Map<String, String> autoCompleteContent) {
		this.autoCompleteContent = autoCompleteContent;
	}

	/**
	 * set a list which gets converted to a map with <caption, value> pairs
	 * @param tagL
	 */
	public void setAutoCompleteContent(List<String> tagL) {
		Map<String, String> map = new HashMap<String, String>(tagL.size());
		for (String string : tagL) {
			map.put(string, string);
		}
		setAutoCompleteContent(map);
	}
	
	/**
	 * @return Returns the autoCompleteContent.
	 */
	public Map<String, String> getAutoCompleteContent() {
		return autoCompleteContent;
	}
	
	// list contains String (caption), string (value to submit)
	protected String getAutoCompleteJSON() {
		String res = "[]";
		try {
			JSONArray cssAdd = new JSONArray();
			Map<String, String> autoCont = getAutoCompleteContent();
			if (autoCont != null && !autoCont.isEmpty()) {
				for (String item : autoCont.keySet()) {
					JSONObject array = new JSONObject();
					array.put("caption", item);
					array.put("value", autoCont.get(item));
					cssAdd.put(array);
				}
				res = cssAdd.toString();
			} 
		} catch (JSONException e) {
			throw new OLATRuntimeException("could not convert the autocompletion-map to json", e);
		}
		return res;
	}
	
	public void setMapperProvider(ResultMapProvider provider){
		this.provider = provider;		
	}
	
	private void setMapper(UserRequest ureq){
		Mapper mapper = new Mapper() {
			@SuppressWarnings("unused")
			public MediaResource handle(String relPath, HttpServletRequest request) {
				String lastInput = request.getParameter("keyword");
				if (lastInput.length() > 2){
					Map<String, String> autoCContLoc = new HashMap<String, String>();
					provider.getAutoCompleteContent(lastInput, autoCContLoc);
					setAutoCompleteContent(autoCContLoc);
				}
				String jsonRes = getAutoCompleteJSON();
				StringMediaResource smr = new StringMediaResource();
				smr.setContentType("application/x-json;charset=utf-8");
				smr.setEncoding("utf-8");
				smr.setData(jsonRes);
				return smr;
			}
		};
		
		MapperRegistry mr = MapperRegistry.getInstanceFor(ureq.getUserSession());
		String fetchUri = mr.register(mapper);
		this.mapperUri = fetchUri + "/";
	}
	
	

	/**
	 * @return Returns the mapperUri.
	 */
	public String getMapperUri() {
		return mapperUri;
	}
	
	public void setNoFormSubmit(boolean noFormSubmit) {
		this.noFormSubmit = noFormSubmit;		
	}
	
	/**
	 * @return Return the maximal number of results showned by the box
	 */
	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	/**
	 * @return Returns the noFormSubmit.
	 */
	public boolean isNoFormSubmit() {
		return noFormSubmit;
	}

	public String getReadOnlyContent() {
		Map<String, String> content = getInitialItems();
		if (content != null && content.size()!=0) {
			String res = "";
			for (String item : content.keySet()) {
				res = res + ", " + content.get(item);
			}
			return res.substring(2);
		} else return "";
	}
	
}
