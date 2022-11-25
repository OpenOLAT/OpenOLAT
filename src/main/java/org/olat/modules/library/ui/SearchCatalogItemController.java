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
package org.olat.modules.library.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.model.CatalogItem;
import org.olat.modules.library.ui.comparator.PublicationDateComparator;
import org.olat.modules.library.ui.comparator.TitleComparator;
import org.olat.modules.library.ui.event.OpenFolderEvent;
import org.olat.search.SearchResults;
import org.olat.search.model.ResultDocument;
import org.olat.search.service.searcher.SearchClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * This is a controller to show the results of the search. From a list of
 * ResultDocument, it creates a list OlatRootFileImpl and then of CatalogItem
 * which the standard CatalogController can use.<br>
 * Events fired:
 * <ul>
 *   <li>OpenFolderEvent</li>
 * </ul>
 * <P>
 * Initial Date:  4 sept. 2009 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchCatalogItemController extends BasicController {
	
	private final CatalogController catalogController;
	private final VelocityContainer mainVC;
	private final Link backLink;
	private final Link orderByTitleLink;
	private final Link orderByRelevanceLink;
	private final Link orderByPublicationDateLink;
	
	private final String queryString;
	
	@Autowired
	private SearchClient searchClient;
	@Autowired
	private LibraryManager libraryManager;
	
	public SearchCatalogItemController(UserRequest ureq, WindowControl control, String queryString,
			String mapperBaseURL, String thumbnailMapperBaseURL, OLATResourceable libraryOres) {
		super(ureq, control);
		this.queryString = queryString;

		mainVC = createVelocityContainer("searchCatalogItems");
		mainVC.contextPut("thumbnailHelper", new ThumbnailHelper(getTranslator(), thumbnailMapperBaseURL));

		backLink = LinkFactory.createCustomLink("backLinkLT", "back", "back", Link.LINK_BACK, mainVC, this);
		mainVC.put("backLink", backLink);
		orderByRelevanceLink = LinkFactory.createButton("order.relevance", mainVC, this);
		orderByRelevanceLink.setIconRightCSS("o_icon o_icon-fw o_icon_sort_amount_desc");
		mainVC.put("orderByRelevanceLink", orderByRelevanceLink);
		orderByTitleLink = LinkFactory.createButton("order.title", mainVC, this);
		mainVC.put("orderByTitleLink", orderByTitleLink);
		orderByPublicationDateLink = LinkFactory.createButton("order.publication", mainVC, this);
		mainVC.put("orderByPublicationDateLink", orderByPublicationDateLink);

		String title = getTranslator().translate("search.results.title", queryString);
		catalogController = new CatalogController(ureq, getWindowControl(), mapperBaseURL, thumbnailMapperBaseURL, true, false, title, libraryOres);
		listenTo(catalogController);
		mainVC.put("results", catalogController.getInitialComponent());

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if(orderByRelevanceLink == source) {
			resetIcons();
			doSearch(ureq);
			orderByRelevanceLink.setIconRightCSS("o_icon o_icon-fw o_icon_sort_amount_desc");
		} else if(orderByPublicationDateLink == source) {
			doSortByPublicationDate();
		} else if(orderByTitleLink == source) {
			doSortByTitle();
		}
	}
	
	protected void doSortByPublicationDate() {
		resetIcons();
		boolean asc = toggleAscDesc(orderByPublicationDateLink);
		if(asc) {
			catalogController.sort(new PublicationDateComparator(getLocale()));
		} else {
			catalogController.sort(new PublicationDateComparator(getLocale()).reversed());
		}
	}
	
	protected void doSortByTitle() {
		resetIcons();
		boolean asc = toggleAscDesc(orderByTitleLink);
		if(asc) {
			catalogController.sort(new TitleComparator(getLocale()));
		} else {
			catalogController.sort(new TitleComparator(getLocale()).reversed());
		}
	}
	
	protected void doSearch(UserRequest ureq) {
		try {
			if(!StringHelper.containsNonWhitespace(queryString)) {
				return;
			}
			List<String> condQueries = Collections.<String>singletonList("parentcontexttype:\"LibrarySite\"");
			SearchResults searchResults = searchClient.doSearch(queryString, condQueries, getIdentity(), ureq.getUserSession().getRoles(), getLocale(), 0, 50, false);
			List<ResultDocument> documents = searchResults.getList();
			List<CatalogItem> items = new ArrayList<>(documents.size());
			for(ResultDocument doc:documents) {
				String url = doc.getResourceUrl();
				if(url.startsWith(LibraryManager.URL_PREFIX)) {
					CatalogItem item = libraryManager.getCatalogItemsByUrl(url, getIdentity());
					if(item != null
							&& !item.getFilename().equals(".noFolderIndexing")
							&& !item.getFilename().equals(".DS_Store")) {
						items.add(item);
					}
				}
			}

			catalogController.display(items);
			if(items.isEmpty()) {
				mainVC.contextPut("empty", getTranslator().translate("search.results.empty"));
			}
		} catch (Exception e) {
			logError("Unexpected error while searching in library", e);
		}
	}
	
	private boolean toggleAscDesc(Link link) {
		if(link.getUserObject() instanceof Boolean) {
			Boolean sort = (Boolean)link.getUserObject();
			boolean asc = !sort;
			if(asc) {
				link.setIconRightCSS("o_icon o_icon-fw o_icon_sort_amount_asc");
			} else {
				link.setIconRightCSS("o_icon o_icon-fw o_icon_sort_amount_desc");
			}
			link.setUserObject(Boolean.valueOf(asc));
			return asc;
		}
		link.setIconRightCSS("o_icon o_icon-fw o_icon_sort_amount_asc");
		link.setUserObject(Boolean.TRUE);
		return true;
	}
	
	private void resetIcons() {
		orderByRelevanceLink.setIconRightCSS("");
		orderByTitleLink.setIconRightCSS("");
		orderByPublicationDateLink.setIconRightCSS("");
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == catalogController) {
			if(event instanceof OpenFolderEvent) {
				fireEvent(ureq, event);
			}
		}
	}
}
