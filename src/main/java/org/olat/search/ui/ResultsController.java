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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.search.SearchResults;
import org.olat.search.SearchServiceUIFactory;
import org.olat.search.model.ResultDocument;

/**
 * Description:<br>
 * Controller which show the list of results, with paging.
 * <P>
 * Events:
 * <ul>
 * 	<li>SearchEvent</li>
 * </ul>
 * 
 * Initial Date:  3 dec. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class ResultsController extends FormBasicController {
	private FormLink previousLink, nextLink; 
	private FormLink highlightLink, dishighlightLink;
	
	private int currentPage;
	public static final int RESULT_PER_PAGE = 10;
	private boolean highlight = true;
	private SearchResults searchResults;
	
	private final List<ResultDocument> documents = new ArrayList<>();
	private final List<ResultController> resultsCtrl = new ArrayList<>();
	
	public ResultsController(UserRequest ureq, WindowControl wControl, Form mainForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "results", mainForm);
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		previousLink = uifactory.addFormLink("previous.page", formLayout);
		nextLink = uifactory.addFormLink("next.page", formLayout);
		
		highlightLink = uifactory.addFormLink("highlight.page", "enable.highlighting", "enable.highlighting", formLayout, Link.LINK);
		dishighlightLink = uifactory.addFormLink("dishighlight.page", "disable.highlighting", "disable.highlighting", formLayout, Link.LINK);
		flc.contextPut("highlight", true);
		reset();
	}
	
	public int getCurrentPage() {
		return currentPage;
	}

	public SearchResults getSearchResults() {
		return searchResults;
	}
	
	public void setSearchResults(UserRequest ureq, SearchResults results) {
		reset();
		searchResults = results;
		if(searchResults == null) {
			searchResults = SearchResults.EMPTY_SEARCH_RESULTS;
		}
		documents.addAll(searchResults.getList());
		setSearchResults(ureq, 0);
	}

	private void setSearchResults(UserRequest ureq, int page) {
		currentPage = page;
		updateUI(ureq);
	}
	
	public void nextSearchResults(UserRequest ureq, SearchResults results) {
		searchResults = results;
		if(searchResults == null) {
			searchResults = SearchResults.EMPTY_SEARCH_RESULTS;
		}
		
		//the last result set can be empty
		if(!searchResults.getList().isEmpty()) {
			currentPage++;
	
			int pos = currentPage * RESULT_PER_PAGE;
			for (int i = 0 ; (i < RESULT_PER_PAGE) && (i < searchResults.getList().size() ); i++) {
				ResultDocument document = searchResults.getList().get(i);
				if(documents.size() > pos + i) {
					documents.set(pos + i, document);
				} else {
					documents.add(document);
				}
			}
		}
		updateUI(ureq);
	}
	
	private void updateUI(UserRequest ureq) {
		removeResultsController();
		
		int start = currentPage * RESULT_PER_PAGE;
		
		SearchServiceUIFactory searchUIFactory = (SearchServiceUIFactory)CoreSpringFactory.getBean(SearchServiceUIFactory.class);
		int count = 0;
		for (int i = start; (count < RESULT_PER_PAGE) && (i < documents.size() ); i++) {
			ResultDocument document = documents.get(i);
			ResultController ctrl = searchUIFactory.createController(ureq, getWindowControl(), mainForm, document);
			ctrl.setHighlight(highlight);
			listenTo(ctrl);
			flc.add("result_" + (++count), ctrl.getInitialFormItem());
			resultsCtrl.add(ctrl);
		}
		
		flc.contextPut("numOfPages", getMaxPage() + 1);
		flc.contextPut("numOfResults", getNumOfResults());
		flc.contextPut("results", resultsCtrl);
		flc.contextPut("hasResult", searchResults != null);
		flc.contextPut("emptyResult", documents.isEmpty());
		flc.contextPut("searchResults", searchResults);
		flc.contextPut("currentPage", currentPage + 1);
		
		previousLink.setEnabled(currentPage != 0);
		nextLink.setEnabled(currentPage != getMaxPage());
		
		String [] args = {Integer.toString(getStartResult()), Integer.toString(getEndResult()), Integer.toString(getNumOfResults())};
		flc.contextPut("resultTitle", getTranslator().translate("search.result.title",args));
	}
	
	public void reload(UserRequest ureq) {
		updateUI(ureq);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source == highlightLink) {
			highlight = true;
			flc.contextPut("highlight", highlight);
			reload(ureq);
		} else if (source == dishighlightLink) {
			highlight = false;
			flc.contextPut("highlight", highlight);
			reload(ureq);
		} else if (source == previousLink) {
			setSearchResults(ureq, Math.max(0, --currentPage));
		} else if (source == nextLink) {
			if(documents.size() <= (currentPage + 1) * RESULT_PER_PAGE) {
				SearchEvent e = new SearchEvent(getLastLucenePosition() + 1, RESULT_PER_PAGE);
				fireEvent(ureq, e);
			} else {
				setSearchResults(ureq, Math.min(getMaxPage(), ++currentPage));
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof SearchEvent) {
			if(resultsCtrl.contains(source)) {
				fireEvent(ureq, event);
			}
		}
	}
	
	private void reset() {
		flc.contextPut("numOfResults", 0);
		flc.contextPut("hasResult", Boolean.FALSE);
		flc.contextPut("emptyResult", Boolean.TRUE);
		
		documents.clear();
		removeResultsController();
	}
	
	private void removeResultsController() {
		if(resultsCtrl != null && !resultsCtrl.isEmpty()) {
			for(int i=0; i<resultsCtrl.size(); i++) {
				flc.remove("result_" + (i+1));
				removeAsListenerAndDispose(resultsCtrl.get(i));
			}
			resultsCtrl.clear();
			flc.contextPut("results", resultsCtrl);
		}
	}
	
	public int getStartResult() {
		return currentPage * RESULT_PER_PAGE + 1;
	}
	
	public int getEndResult() {
		if ( (currentPage * RESULT_PER_PAGE + RESULT_PER_PAGE) > documents.size() ) {
			return documents.size();
		} else {		
			return getStartResult() + RESULT_PER_PAGE - 1;
		}
	}
	
	/**
	 * @return Number of pages for current result-list.
	 */
	public int getMaxPage() {
		int numOfResults = getNumOfResults();
		int maxPage = numOfResults / RESULT_PER_PAGE;
		if ((numOfResults) % RESULT_PER_PAGE == 0) {
			maxPage--;
		}
		return maxPage;
	}
	
	public int getNumOfResults() {
		if(searchResults.getList().size() < RESULT_PER_PAGE) {
			//last result set, all documents are loaded
			return documents.size();
		}
		return searchResults.getTotalDocs() - getLastLucenePosition() + documents.size() - 1;
	}
	
	private int getLastLucenePosition() {
		if(documents.isEmpty()) return 0;
		return documents.get(documents.size() - 1).getLucenePosition();
	}

	public FormItem getFormItem() {
		return this.flc;
	}
}
