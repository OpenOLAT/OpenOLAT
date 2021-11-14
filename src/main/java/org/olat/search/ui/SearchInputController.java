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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.iq.AssessmentEvent;
import org.olat.course.nodes.iq.AssessmentInstance;
import org.olat.search.QueryException;
import org.olat.search.SearchResults;
import org.olat.search.SearchServiceUIFactory;
import org.olat.search.SearchServiceUIFactory.DisplayOption;
import org.olat.search.ServiceNotAvailableException;
import org.olat.search.model.AbstractOlatDocument;
import org.olat.search.model.ResultDocument;
import org.olat.search.service.QuickSearchEvent;
import org.olat.search.service.searcher.SearchClient;

/**
 * Description:<br>
 * Controller with a simple input for the full text search. The display option
 * select how the input is shown: only a button, button with text, input field and
 * button.
 * <P>
 * Initial Date:  3 dec. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class SearchInputController extends FormBasicController implements GenericEventListener {
	
	private static final Logger log = Tracing.createLoggerFor(SearchInputController.class);
	
	private static final String FUZZY_SEARCH = "~0.7";
	private static final String CMD_DID_YOU_MEAN_LINK = "didYouMeanLink-";
	private static final String SEARCH_STORE_KEY = "search-store-key";
	
	private String parentContext;
	private String documentType;
	private String resourceUrl;
	private boolean resourceContextEnable = true;
	
	private DisplayOption displayOption; 
	
	protected FormLink searchButton;
	protected TextElement searchInput;
	private ResultsSearchController resultCtlr;
	private CloseableModalController searchDialogBox;

	protected List<FormLink> didYouMeanLinks;
	
	private Map<String,Properties> prefs;
	private SearchClient searchClient;
	
	public SearchInputController(UserRequest ureq, WindowControl wControl, String resourceUrl, DisplayOption displayOption) {
		super(ureq, wControl, LAYOUT_HORIZONTAL);
		this.resourceUrl = resourceUrl;
		this.displayOption = displayOption;
		setSearchStore(ureq);
		initForm(ureq);
		loadPersistedSearch();
		loadContext();
	}
	
	public SearchInputController(UserRequest ureq, WindowControl wControl, String resourceUrl, String customPage) {
		super(ureq, wControl, customPage);
		this.displayOption = DisplayOption.STANDARD_TEXT;
		this.resourceUrl = resourceUrl;
		setSearchStore(ureq);
		initForm(ureq);
		loadPersistedSearch();
		loadContext();
	}
	
	public SearchInputController(UserRequest ureq, WindowControl wControl, String resourceUrl, DisplayOption displayOption, Form mainForm) {
		super(ureq, wControl, LAYOUT_HORIZONTAL, null, mainForm);
		this.displayOption = displayOption;
		this.resourceUrl = resourceUrl;
		setSearchStore(ureq);
		initForm(ureq);
		loadPersistedSearch();
		loadContext();
	}

	public String getParentContext() {
		return parentContext;
	}

	public void setParentContext(String parentContext) {
		this.parentContext = parentContext;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getResourceUrl() {
		return resourceUrl;
	}
	
	public void setResourceUrl(String resourceUrl) {
		this.resourceUrl = resourceUrl;
	}
	
	public boolean isResourceContextEnable() {
		return resourceContextEnable;
	}

	public void setResourceContextEnable(boolean resourceContextEnable) {
		this.resourceContextEnable = resourceContextEnable;
	}
	
	private EventBus singleUserEventCenter;
	private static final OLATResourceable ass = OresHelper.createOLATResourceableType(AssessmentEvent.class);
	
	public void setAssessmentListener(UserRequest ureq) {
		singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
		singleUserEventCenter.registerFor(this, getIdentity(), ass);
	}

	@Override
	public void event(Event event) {
		if (event instanceof AssessmentEvent) {
			AssessmentEvent ae = (AssessmentEvent)event;
			if(ae.getEventType().equals(AssessmentEvent.TYPE.STARTED)) {
				flc.setVisible(false);
			} else if(ae.getEventType().equals(AssessmentEvent.TYPE.STOPPED)) {
				OLATResourceable a = OresHelper.createOLATResourceableType(AssessmentInstance.class);
				if (singleUserEventCenter.getListeningIdentityCntFor(a)<1) {
					flc.setVisible(true);
				}
			} 
		}
	}

	public String getSearchString() {
		return searchInput.getValue();
	}
	
	public void setSearchString(String searchString) {
		if (StringHelper.containsNonWhitespace(searchString)) {
			if(searchInput != null) {
				searchInput.setValue(searchString);
			}
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchClient = (SearchClient)CoreSpringFactory.getBean("searchClient");

		FormItemContainer searchLayout = formLayout;
		
		if (displayOption.equals(DisplayOption.STANDARD) || displayOption.equals(DisplayOption.STANDARD_TEXT)) {
			searchLayout = FormLayoutContainer.createInputGroupLayout("searchWrapper", getTranslator(), null, null);
			formLayout.add(searchLayout);
			searchInput = uifactory.addTextElement("search_input", "search.title", 255, "", searchLayout);
			searchInput.setLabel(null, null);
			searchInput.setPlaceholderKey("search", null);
			searchInput.setFocus(true);
		}
		
		if (displayOption.equals(DisplayOption.STANDARD) || displayOption.equals(DisplayOption.BUTTON)) {
			searchButton = uifactory.addFormLink("rightAddOn", "", "", searchLayout, Link.NONTRANSLATED);
			searchButton.setIconLeftCSS("o_icon o_icon-fw o_icon_search o_icon-lg");
			String searchLabel = getTranslator().translate("search");
			searchButton.setLinkTitle(searchLabel);
		} else if (displayOption.equals(DisplayOption.BUTTON_WITH_LABEL)) {
			searchButton = uifactory.addFormLink("rightAddOn", searchLayout, Link.BUTTON_SMALL);
		} else if (displayOption.equals(DisplayOption.STANDARD_TEXT)) {
			String searchLabel = getTranslator().translate("search");
			searchButton = uifactory.addFormLink("rightAddOn", searchLabel, "", searchLayout, Link.NONTRANSLATED + Link.BUTTON_SMALL);
			searchButton.setIconLeftCSS("o_icon o_icon-fw o_icon_search o_icon-lg");
		}
		searchButton.setCustomEnabledLinkCSS("o_search");
		searchButton.setEnabled(true);
	}
	
	private void loadContext() {
		if(resourceUrl != null) {
			ContextTokens context = getContextTokens(resourceUrl);
			setContext(context);
		}
	}
	
	protected void setContext(ContextTokens context) {
		if(!context.isEmpty()) {
			String scope = context.getValueAt(context.getSize() - 1);
			String tooltip = getTranslator().translate("form.search.label.tooltip", new String[]{scope});
			searchButton.getComponent().setTitle(tooltip);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setSearchStore(UserRequest ureq) {
		prefs = (Map<String,Properties>)ureq.getUserSession().getEntry(SEARCH_STORE_KEY);
		if(prefs == null) {
			prefs = new HashMap<>();
			ureq.getUserSession().putEntry(SEARCH_STORE_KEY, prefs);
		}
	}
	
	@Override
	public void formOK(UserRequest ureq) {
		fireEvent(ureq, QuickSearchEvent.QUICKSEARCH_EVENT);
		doSearch(ureq);
	}
	
	@Override
	protected void formNOK(UserRequest ureq) {
		doSearch(ureq);
	}

	@Override
	public void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == searchButton) {
			fireEvent(ureq, QuickSearchEvent.QUICKSEARCH_EVENT);
			doSearch(ureq);
		} else if (didYouMeanLinks != null && didYouMeanLinks.contains(source)) {
			String didYouMeanWord = (String)source.getUserObject();
			searchInput.setValue(didYouMeanWord);
			doSearch(ureq, didYouMeanWord, null, parentContext, documentType, resourceUrl, 0, RESULT_PER_PAGE, false);
		}
	}
	
	protected void doSearch(UserRequest ureq) {
		if (resultCtlr != null) return;
		
		String oldSearchString = null;
		Properties props = getPersistedSearch();
		if(props != null) {
			oldSearchString = props.getProperty("s");
		}
		
		persistSearch();
		
		if (DisplayOption.BUTTON.equals(displayOption) || DisplayOption.BUTTON_WITH_LABEL.equals(displayOption)) {
			//no search, only popup
			createResultsSearchController(ureq);
			popupResultsSearchController();
			if(resultCtlr.getPersistedSearch() != null && !resultCtlr.getPersistedSearch().isEmpty()) {
				resultCtlr.doSearch(ureq);
			}
		} else {
			String searchString = getSearchString();
			if(StringHelper.containsNonWhitespace(searchString)) {
				if(oldSearchString != null && !oldSearchString.equals(searchString)) {
					resetSearch();
				}

				createResultsSearchController(ureq);
				resultCtlr.setSearchString(searchString);
				popupResultsSearchController();
				resultCtlr.doSearch(ureq);
			}
		}
	}
	
	protected Properties getPersistedSearch() {
		if(getResourceUrl() != null) {
			String uri = getResourceUrl();
			Properties props = prefs.get(uri);
			if(props == null) {
				props = new Properties();
				prefs.put(uri, props);
			}
			return props;
		}
		//not possible but i don't want to trigger a red screen for this if i'm wrong
		return new Properties();
	}
	
	protected void resetSearch() {
		if(getResourceUrl() != null) {
			String uri = getResourceUrl();
			Properties props = prefs.get(uri);
			if(props != null) {
				prefs.remove(uri);
			}
		}
	}
	
	protected final void persistSearch() {
		if(getResourceUrl() != null) {
			String uri = getResourceUrl();
			Properties props = prefs.get(uri);
			if(props == null) {
				props = new Properties();
			}
			getSearchProperties(props);
			
			if(props.isEmpty()) {
				prefs.remove(uri);
			} else {
				prefs.put(uri, props);
			}
		}
	}
	
	protected void loadPersistedSearch() {
		if(getResourceUrl() != null) {
			String uri = getResourceUrl();
			Properties props = prefs.get(uri);
			if(props != null) {
				setSearchProperties(props);
			}
		}
	}
	
	private void createResultsSearchController(UserRequest ureq) {
		resultCtlr = new ResultsSearchController(ureq, getWindowControl(), getResourceUrl());
		resultCtlr.setDocumentType(getDocumentType());
		resultCtlr.setParentContext(getParentContext());
		resultCtlr.setResourceContextEnable(isResourceContextEnable());
		listenTo(resultCtlr);
	}
	
	protected void getSearchProperties(Properties props) {
		if (displayOption.equals(DisplayOption.STANDARD) || displayOption.equals(DisplayOption.STANDARD_TEXT)) {
			String searchString = getSearchString();
			props.setProperty("s", searchString == null ? "" : searchString);
		}
	}
	
	protected void setSearchProperties(Properties props) {
		if (displayOption.equals(DisplayOption.STANDARD) || displayOption.equals(DisplayOption.STANDARD_TEXT)) {
			String searchString = props.getProperty("s");
			if(StringHelper.containsNonWhitespace(searchString)) {
				setSearchString(searchString);
			} else {
				setSearchString("");
			}
		}
	}
	
	private void popupResultsSearchController() {
		String title = translate("search.title");
		String close = translate("close");
		searchDialogBox = new CloseableModalController(getWindowControl(), close, resultCtlr.getInitialComponent(), true, title);
		searchDialogBox.activate();
		listenTo(searchDialogBox);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == resultCtlr) {
			if (event instanceof SearchEvent) {
				SearchEvent goEvent = (SearchEvent)event;
				ResultDocument doc = goEvent.getDocument();
				gotoSearchResult(ureq, doc);
			} else if (event == Event.DONE_EVENT) {
				setSearchString(resultCtlr.getSearchString());
			}
		} else if (source == searchDialogBox) {
			cleanUp();
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	public void closeSearchDialogBox() {
		if(searchDialogBox != null) {
			searchDialogBox.deactivate();
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(searchDialogBox);
		removeAsListenerAndDispose(resultCtlr);
		searchDialogBox = null;
		resultCtlr = null;
	}
	
	/**
	 * 
	 * @param ureq
	 * @param command
	 */
	public void gotoSearchResult(UserRequest ureq, ResultDocument document) {
		try {
			// attach the launcher data
			closeSearchDialogBox();
			String url = document.getResourceUrl();
			if(!StringHelper.containsNonWhitespace(url)) {
				//no url, no document
				getWindowControl().setWarning(getTranslator().translate("error.resource.could.not.found"));
			} else {
				BusinessControl bc = BusinessControlFactory.getInstance().createFromString(url);
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
				NewControllerFactory.getInstance().launch(ureq, bwControl);
			}
		} catch (Exception ex) {
			log.debug("Document not found");
			getWindowControl().setWarning(getTranslator().translate("error.resource.could.not.found"));
		}		
	}
	
	protected SearchResults doSearch(UserRequest ureq, String searchString, List<String> condSearchStrings, String parentCtxt, String docType, String rsrcUrl,
			int firstResult, int maxReturns, boolean doSpellCheck) {
		
		String query = null;
		List<String> condQueries = null;
		try {
			if(doSpellCheck) {
				//remove first old "did you mean words"
				hideDidYouMeanWords();
			}

			query = getQueryString(searchString, false);
			condQueries = getCondQueryStrings(condSearchStrings, parentCtxt, docType, rsrcUrl);
			SearchResults searchResults = searchClient.doSearch(query, condQueries,
					getIdentity(), ureq.getUserSession().getRoles(), getLocale(), firstResult, maxReturns, true);

			if(searchResults != null && searchResults.getException() instanceof ParseException) {
				getWindowControl().setWarning(translate("invalid.search.query"));
			} else if(searchResults == null || searchResults.getException() != null) {
				getWindowControl().setWarning(translate("search.service.unexpected.error"));
			} else if (firstResult == 0 && searchResults.size() == 0 && StringHelper.containsNonWhitespace(query) && !query.endsWith(FUZZY_SEARCH)) {
				// result-list was empty => first try to find word via spell-checker
				if (doSpellCheck) {
					Set<String> didYouMeansWords = searchClient.spellCheck(searchString);
					if (didYouMeansWords != null && !didYouMeansWords.isEmpty()) {
						setDidYouMeanWords(didYouMeansWords);
					} else {
						searchResults = doFuzzySearch(ureq, searchString, null, parentCtxt, docType, rsrcUrl, firstResult, maxReturns);
					}
				} else {
					searchResults = doFuzzySearch(ureq, searchString, null, parentCtxt, docType, rsrcUrl, firstResult, maxReturns);
				}
			}
			
			if(firstResult == 0 && searchResults != null && searchResults.getException() == null && searchResults.getList().isEmpty()) {
				showInfo("found.no.result.try.fuzzy.search");
			}
			return searchResults;
		} catch (ParseException e) {
			if(log.isDebugEnabled()) log.debug("Query cannot be parsed: " + query);
			getWindowControl().setWarning(translate("invalid.search.query"));
		} catch (QueryException e) {
			getWindowControl().setWarning(translate("invalid.search.query.with.wildcard"));
		} catch(ServiceNotAvailableException e) {
			getWindowControl().setWarning(translate("search.service.not.available"));
		} catch (Exception e) {
			log.error("Unexpected exception while searching", e);
			getWindowControl().setWarning(translate("search.service.unexpected.error"));
		}
		return SearchResults.EMPTY_SEARCH_RESULTS;
	}
	
	protected SearchResults doFuzzySearch(UserRequest ureq, String searchString, List<String> condSearchStrings, String parentCtxt, String docType, String rsrcUrl,
			int firstResult, int maxReturns) throws QueryException, ParseException, ServiceNotAvailableException  {
		hideDidYouMeanWords();
		String query = getQueryString(searchString, true);
		List<String> condQueries = getCondQueryStrings(condSearchStrings, parentCtxt, docType, rsrcUrl);
		return searchClient.doSearch(query, condQueries,
				getIdentity(), ureq.getUserSession().getRoles(), getLocale(), firstResult, maxReturns, true);
	}
	
	public Set<String> getDidYouMeanWords() {
		if (didYouMeanLinks != null && !didYouMeanLinks.isEmpty()) {
			Set<String> didYouMeanWords = new HashSet<>();
			for(FormLink link:didYouMeanLinks) {
				String word = (String)link.getUserObject();
				didYouMeanWords.add(word);
			}
			return didYouMeanWords;
		}
		return Collections.emptySet();
	}
	
	/**
	 * Unregister existing did-you-mean-links from content and add new links.
	 * @param didYouMeansWords  List of 'did you mean' words
	 */
	public void setDidYouMeanWords(Set<String> didYouMeansWords) {
		// unregister existing did-you-mean links
		hideDidYouMeanWords();
		
		didYouMeanLinks = new ArrayList<>(didYouMeansWords.size());
		int wordNumber = 0;
		for (String word : didYouMeansWords) {
			FormLink l = uifactory.addFormLink(CMD_DID_YOU_MEAN_LINK + wordNumber++, word, null, flc, Link.NONTRANSLATED);
			l.setUserObject(word);
			didYouMeanLinks.add(l);
		}
		flc.contextPut("didYouMeanLinks", didYouMeanLinks);
		flc.contextPut("hasDidYouMean", Boolean.TRUE);
	}
	
	protected void hideDidYouMeanWords() {
		// unregister existing did-you-mean links
		if (didYouMeanLinks != null) {
			for (int i = 0; i < didYouMeanLinks.size(); i++) {
				flc.remove(CMD_DID_YOU_MEAN_LINK + i);
			}
			didYouMeanLinks = null;
		}
		flc.contextPut("didYouMeanLinks", didYouMeanLinks);
		flc.contextPut("hasDidYouMean", Boolean.FALSE);
	}
	
	private String getQueryString(String searchString, boolean fuzzy) {
		StringBuilder query = new StringBuilder(searchString);
		if(fuzzy) {
			query.append(FUZZY_SEARCH);
		}
		return query.toString();
	}
	
	private List<String> getCondQueryStrings(List<String> condSearchStrings, String parentCtxt, String docType, String rsrcUrl) {
		List<String> queries = new ArrayList<>();
		if(condSearchStrings != null && !condSearchStrings.isEmpty()) {
			queries.addAll(condSearchStrings);
		}
		
		if (StringHelper.containsNonWhitespace(parentCtxt)) {
			appendAnd(queries, AbstractOlatDocument.PARENT_CONTEXT_TYPE_FIELD_NAME, ":\"", parentCtxt, "\"");
		}
		if (StringHelper.containsNonWhitespace(docType)) {
			appendAnd(queries, "(", AbstractOlatDocument.DOCUMENTTYPE_FIELD_NAME, ":(", docType, "))");
		}
		if (StringHelper.containsNonWhitespace(rsrcUrl)) {
			appendAnd(queries, AbstractOlatDocument.RESOURCEURL_FIELD_NAME, ":", escapeResourceUrl(rsrcUrl), "*");
		}
		return queries;
	}
	
	private void appendAnd(List<String> queries, String... strings) {
		StringBuilder query = new StringBuilder();
		for(String string:strings) {
			query.append(string);
		}
		
		if(query.length() > 0) {
			queries.add(query.toString());
		}
	}
	
	/**
	 * Remove the ROOT keyword, duplicate entry in the business path
	 * and escape the keywords used by lucene.
	 * @param url
	 * @return
	 */
	protected String escapeResourceUrl(String url) {
		List<String> tokens = getResourceUrlTokenized(url);
		StringBuilder sb = new StringBuilder();
		for(String token:tokens) {
			sb.append("\\[").append(token.replace(":", "\\:")).append("\\]");
		}
		return sb.toString();
	}
	
	protected List<String> getResourceUrlTokenized(String url) {
		if (url.startsWith("ROOT")) {
			url = url.substring(4, url.length());
		}
		List<String> tokens = new ArrayList<>();
		for(StringTokenizer tokenizer = new StringTokenizer(url, "[]"); tokenizer.hasMoreTokens(); ) {
			String token = tokenizer.nextToken();
			if(!tokens.contains(token)) {
				tokens.add(token);
			}
		}
		return tokens;
	}
	
	protected ContextTokens getContextTokens(String resourceURL) {
		SearchServiceUIFactory searchUIFactory = (SearchServiceUIFactory)CoreSpringFactory.getBean(SearchServiceUIFactory.class);
		List<String> tokens = getResourceUrlTokenized(resourceURL);
		String[] keys = new String[tokens.size() + 1];
		String[] values = new String[tokens.size() + 1];
		keys[0] = "";
		values[0] = translate("search.context.all");
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<tokens.size(); i++) {
			String token = tokens.get(i);
			keys[i+1] = sb.append('[').append(token).append(']').toString();
			values[i+1] = searchUIFactory.getBusinessPathLabel(token, tokens, getLocale());
		}
		return new ContextTokens(keys, values);
	}
	
	public FormItem getFormItem() {
		return flc;
	}
	
	public class ContextTokens {
		private final String[] keys;
		private final String[] values;
		
		public ContextTokens(String[] keys, String[] values) {
			this.keys = keys == null ? new String[0] : keys;
			this.values = values == null ? new String[0] : values;
		}

		public String[] getKeys() {
			return keys;
		}

		public String[] getValues() {
			return values;
		}
		
		public boolean isEmpty() {
			return values.length == 0;
		}
		
		public int getSize() {
			return values.length;
		}
		
		public String getKeyAt(int index) {
			if(keys != null && index < keys.length && index >= 0) {
				return keys[index];
			}
			return "";
		}
		
		public String getValueAt(int index) {
			if(values != null && index < values.length && index >= 0) {
				return values[index];
			}
			return "";
		}
	}
}
