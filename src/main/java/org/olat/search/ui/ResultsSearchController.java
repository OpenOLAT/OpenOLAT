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

package org.olat.search.ui;

import static org.olat.search.ui.ResultsController.RESULT_PER_PAGE;

import java.util.List;
import java.util.Properties;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.search.SearchResults;

/**
 * Description:<br>
 * With toggle simple search &lt;-&gt; extended search
 * <P>
 * Initial Date:  3 dec. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class ResultsSearchController extends SearchInputController {

	private boolean extendedSearch;
	private FormLink simpleSearchLink;
	private FormLink extendedSearchLink;
	private SingleSelection contextSelection;
	
	private ResultsController resultCtlr;
	private AdvancedSearchInputController advancedSearchController;
	
	public ResultsSearchController(UserRequest ureq, WindowControl wControl, String resourceUrl) {
		super(ureq, wControl, resourceUrl, "searchInput");
	}
	
	@Override
	public void setResourceContextEnable(boolean resourceContextEnable) {
		if(contextSelection.isVisible() != resourceContextEnable) {
			contextSelection.setVisible(resourceContextEnable);
		}
		advancedSearchController.setResourceContextEnable(resourceContextEnable);
		super.setResourceContextEnable(resourceContextEnable);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		extendedSearchLink = uifactory.addFormLink("switch.advanced.search", formLayout);
		extendedSearchLink.setElementCssClass("o_search_link_extended");
		simpleSearchLink = uifactory.addFormLink("switch.simple.search", formLayout);
		simpleSearchLink.setElementCssClass("o_search_link_simple");

		FormLayoutContainer searchLayout = FormLayoutContainer.createHorizontalFormLayout("search_form", getTranslator());
		formLayout.add(searchLayout);
		super.initForm(searchLayout, listener, ureq);

		FormLayoutContainer extSearchLayout = FormLayoutContainer.createVerticalFormLayout("ext_search_form", getTranslator());
		formLayout.add(extSearchLayout);
		advancedSearchController = new AdvancedSearchInputController(ureq, getWindowControl(), mainForm);
		listenTo(advancedSearchController);
		extSearchLayout.add("adv_search", advancedSearchController.getFormItem());
		
		contextSelection = uifactory.addRadiosHorizontal("context", "form.search.label.context", formLayout, new String[0], new String[0]);

		resultCtlr = new ResultsController(ureq, getWindowControl(), mainForm);
		listenTo(resultCtlr);
		formLayout.add("resultList", resultCtlr.getFormItem());
	}
	
	@Override
	protected void setContext(ContextTokens context) {
		super.setContext(context);
		contextSelection.setKeysAndValues(context.getKeys(), context.getValues(), null);
		if (!context.isEmpty()) {
			String selectedContext = context.getKeyAt(context.getSize() - 1);
			Properties props = getPersistedSearch();
			if(props != null && props.containsKey("ctxt")) {
				selectedContext = props.getProperty("ctxt");
			}
			contextSelection.select(selectedContext, true);
		}
		advancedSearchController.setContextKeysAndValues(context.getKeys(), context.getValues());
		
		String extended = getPersistedSearch().getProperty("ext", "false");
		if("true".equals(extended)) {
			extendedSearch = true;
			advancedSearchController.setSearchString(getSearchString());
			advancedSearchController.load();
			flc.contextPut("advancedSearchFlag", extendedSearch);
		}
	}

	@Override
	public void formOK(UserRequest ureq) {
		doSearch(ureq);
	}
	
	@Override
	protected void formNOK(UserRequest ureq) {
		doSearch(ureq);
	}

	@Override
	public void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == searchButton) {
			doSearch(ureq);
		} else if (didYouMeanLinks != null && didYouMeanLinks.contains(source)) {
			String didYouMeanWord = (String)source.getUserObject();
			searchInput.setValue(didYouMeanWord);
			advancedSearchController.setSearchString(didYouMeanWord);
			
			String key = null;
			List<String> condQueries = null;
			if(extendedSearch) {
				key = advancedSearchController.getContext();
				condQueries = advancedSearchController.getQueryStrings();
			} else if(contextSelection.isOneSelected()) {
				key = contextSelection.getSelectedKey();
			}
			
			hideDidYouMeanWords();
			SearchResults results = doSearch(ureq, didYouMeanWord, condQueries, getParentContext(), getDocumentType(), key, 0, RESULT_PER_PAGE, false);
			resultCtlr.setSearchResults(ureq, results);
			persistSearch();
		} else if (source == extendedSearchLink) {
			extendedSearch = true;
			advancedSearchController.setSearchString(getSearchString());
			advancedSearchController.load();
			flc.contextPut("advancedSearchFlag", extendedSearch);
		} else if (source == simpleSearchLink) {
			extendedSearch = false;
			advancedSearchController.unload();
			setSearchString(advancedSearchController.getSearchString());
			flc.contextPut("advancedSearchFlag", extendedSearch);
		} else if (source == advancedSearchController.getSearchButton()) {
			doSearch(ureq);
		}
	}
	
	@Override
	protected void doSearch(UserRequest ureq) {
		doSearch(ureq, 0);
	}
	
	private void doSearch(UserRequest ureq, int firstResult) {
		SearchResults results;
		if(extendedSearch) {
			String query = advancedSearchController.getSearchString();
			List<String> condQueries = advancedSearchController.getQueryStrings();
			String key = advancedSearchController.getContext();
			if(advancedSearchController.isDocumentTypesSelected()) {
				//if document types are selected, these queries overwrite the conditional query for document type
				//set in this controller
				results = doSearch(ureq, query, condQueries, getParentContext(), null, key, firstResult, RESULT_PER_PAGE, true);
			} else {
				results = doSearch(ureq, query, condQueries, getParentContext(), getDocumentType(), key, firstResult, RESULT_PER_PAGE, true);
			}
		} else {
			String searchString = getSearchString();
			if(StringHelper.containsNonWhitespace(searchString)) {
				String key = null;
				if(contextSelection.isOneSelected()) {
					key = contextSelection.getSelectedKey();
				}
				results = doSearch(ureq, searchString, null, getParentContext(), getDocumentType(), key, firstResult, RESULT_PER_PAGE, true);
			} else {
				results = SearchResults.EMPTY_SEARCH_RESULTS;
			}
		}
		
		if(firstResult == 0) {
			resultCtlr.setSearchResults(ureq, results);
		} else {
			resultCtlr.nextSearchResults(ureq, results);
		}
		
		persistSearch();
	}

	@Override
	protected void getSearchProperties(Properties props) {
		super.getSearchProperties(props);
		if(contextSelection.isOneSelected() && contextSelection.getSelectedKey() != null) {
			props.put("ctxt", contextSelection.getSelectedKey());
		} else {
			props.remove("ctxt");
		}
		props.put("ext", extendedSearch ? "true" : "false");
		advancedSearchController.getSearchProperties(props);
		
		int currentPage = resultCtlr.getCurrentPage();
		if(currentPage >= 0) {
			props.put("c_page", Integer.toString(currentPage));
		} else {
			props.remove("c_page");
		}
	}

	@Override
	protected void setSearchProperties(Properties props) {
		super.setSearchProperties(props);
		//set context after
		advancedSearchController.setSearchProperties(props);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == resultCtlr) {
			if(SearchEvent.NEW_SEARCH_EVENT.equals(event.getCommand())) {
				SearchEvent e = (SearchEvent)event;
				doSearch(ureq, e.getFirstResult());
			} else {
				fireEvent(ureq, event);
			}
		} else {
			super.event(ureq, source, event);
		}
	}
}
