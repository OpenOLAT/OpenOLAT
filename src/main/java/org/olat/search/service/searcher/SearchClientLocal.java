package org.olat.search.service.searcher;

import java.util.List;
import java.util.Set;

import org.apache.lucene.queryParser.ParseException;
import org.olat.core.commons.services.search.QueryException;
import org.olat.core.commons.services.search.SearchResults;
import org.olat.core.commons.services.search.ServiceNotAvailableException;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.search.service.SearchServiceFactory;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchClientLocal implements SearchClient {

	@Override
	public SearchResults doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles, int firstResult, int maxReturns, boolean doHighlighting)
	throws ServiceNotAvailableException, ParseException, QueryException {
		return SearchServiceFactory.getService().doSearch(queryString, condQueries, identity, roles, firstResult, maxReturns, doHighlighting);
	}

	@Override
	public Set<String> spellCheck(String query) throws ServiceNotAvailableException {
		return SearchServiceFactory.getService().spellCheck(query);
	}
}
