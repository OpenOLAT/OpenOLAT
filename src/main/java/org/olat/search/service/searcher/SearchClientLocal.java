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
package org.olat.search.service.searcher;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.lucene.queryparser.classic.ParseException;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.search.QueryException;
import org.olat.search.SearchResults;
import org.olat.search.ServiceNotAvailableException;
import org.olat.search.service.SearchServiceFactory;

/**
 * 
 * Description:<br>
 * OO-109: bypass the JMS server in no-cluster environment
 * 
 * <P>
 * Initial Date:  6 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchClientLocal implements SearchClient {
	
	private DB dbInstance;
	
	/**
	 * [used by Spring]
	 * @param dbInstance
	 */
	public void setDbInstance(DB dbInstance) {
		this.dbInstance = dbInstance;
	}

	@Override
	public SearchResults doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles, Locale locale,
			int firstResult, int maxReturns, boolean doHighlighting)
	throws ServiceNotAvailableException, ParseException, QueryException {
		dbInstance.commitAndCloseSession();
		return SearchServiceFactory.getService().doSearch(queryString, condQueries, identity, roles, locale, firstResult, maxReturns, doHighlighting);
	}

	@Override
	public List<Long> doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles, Locale locale,
			int firstResult, int maxResults, SortKey... orderBy)
	throws ServiceNotAvailableException, ParseException, QueryException {
		dbInstance.commitAndCloseSession();
		return SearchServiceFactory.getService().doSearch(queryString, condQueries, identity, roles, locale, firstResult, maxResults, orderBy);
	}

	@Override
	public Set<String> spellCheck(String query) throws ServiceNotAvailableException {
		dbInstance.commitAndCloseSession();
		return SearchServiceFactory.getService().spellCheck(query);
	}
}
