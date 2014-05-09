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
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntryAuthorView;
import org.olat.repository.RepositoryService;
import org.olat.repository.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.ui.PriceMethod;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.olat.search.QueryException;
import org.olat.search.ServiceNotAvailableException;
import org.olat.search.service.searcher.SearchClient;
import org.olat.search.service.searcher.SearchClientLocal;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEntryDataSource implements FlexiTableDataSourceDelegate<AuthoringEntryRow> {
	
	private static final OLog log = Tracing.createLoggerFor(AuthoringEntryDataSource.class);

	private final SearchAuthorRepositoryEntryViewParams searchParams;
	

	private final ACService acService;
	private final SearchClient searchClient;
	private final RepositoryService repositoryService;
	private final AuthoringEntryDataSourceUIFactory uifactory;
	
	private Integer count;
	
	public AuthoringEntryDataSource(SearchAuthorRepositoryEntryViewParams searchParams,
			AuthoringEntryDataSourceUIFactory uifactory) {
		this.searchParams = searchParams;
		this.uifactory = uifactory;
		
		acService = CoreSpringFactory.getImpl(ACService.class);
		searchClient = CoreSpringFactory.getImpl(SearchClientLocal.class);
		repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
	}
	
	public void resetCount() {
		count = null;
	}

	@Override
	public int getRowCount() {
		if(count == null) {
			count = repositoryService.countAuthorView(searchParams);
		}
		return count.intValue();
	}

	@Override
	public List<AuthoringEntryRow> reload(List<AuthoringEntryRow> rows) {
		return Collections.emptyList();
	}

	@Override
	public final ResultInfos<AuthoringEntryRow> getRows(String query, List<String> condQueries,
			int firstResult, int maxResults, SortKey... orderBy) {

		if(StringHelper.containsNonWhitespace(query)) {
			try {
				List<Long> fullTextResults = searchClient.doSearch(query, null, searchParams.getIdentity(), searchParams.getRoles(), 0, 100);
				searchParams.setRepoEntryKeys(fullTextResults);
			} catch (ServiceNotAvailableException | ParseException | QueryException e) {
				log.error("", e);
			}
		}
		
		List<RepositoryEntryAuthorView> views = repositoryService.searchAuthorView(searchParams, firstResult, maxResults);
		List<AuthoringEntryRow> rows = processViewModel(views);
		ResultInfos<AuthoringEntryRow> results = new DefaultResultInfos<AuthoringEntryRow>(firstResult + rows.size(), -1, rows);
		if(firstResult == 0 && views.size() < maxResults) {
			count = new Integer(views.size() );
		}
		return results;
	}

	private List<AuthoringEntryRow> processViewModel(List<RepositoryEntryAuthorView> repoEntries) {
		List<OLATResource> resourcesWithAC = new ArrayList<>(repoEntries.size());
		for(RepositoryEntryAuthorView entry:repoEntries) {
			if(entry.isValidOfferAvailable()) {
				resourcesWithAC.add(entry.getOlatResource());
			}
		}
		List<OLATResourceAccess> resourcesWithOffer = acService.filterResourceWithAC(resourcesWithAC);
		
		List<AuthoringEntryRow> items = new ArrayList<AuthoringEntryRow>();
		for(RepositoryEntryAuthorView entry:repoEntries) {
			String fullname = "";
			AuthoringEntryRow row = new AuthoringEntryRow(entry, fullname);
			//bookmark
			row.setMarked(entry.isMarked());

			List<PriceMethod> types = new ArrayList<PriceMethod>();
			if (entry.isMembersOnly()) {
				// members only always show lock icon
				types.add(new PriceMethod("", "o_ac_membersonly_icon"));
			} else {
				// collect access control method icons
				OLATResource resource = entry.getOlatResource();
				for(OLATResourceAccess resourceAccess:resourcesWithOffer) {
					if(resource.getKey().equals(resourceAccess.getResource().getKey())) {
						for(PriceMethodBundle bundle:resourceAccess.getMethods()) {
							String type = (bundle.getMethod().getMethodCssClass() + "_icon").intern();
							String price = bundle.getPrice() == null || bundle.getPrice().isEmpty() ? "" : PriceFormat.fullFormat(bundle.getPrice());
							types.add(new PriceMethod(price, type));
						}
					}
				}
			}
			
			if(!types.isEmpty()) {
				row.setAccessTypes(types);
			}
			
			uifactory.forgeMarkLink(row);
			
			items.add(row);
		}
		return items;
	}
}
