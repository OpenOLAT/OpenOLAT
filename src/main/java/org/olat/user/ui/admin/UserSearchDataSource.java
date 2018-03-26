package org.olat.user.ui.admin;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.IdentityPowerSearchQueries;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 21 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSearchDataSource implements FlexiTableDataSourceDelegate<UserPropertiesRow> {
	
	private final SearchIdentityParams searchParams;
	private final IdentityPowerSearchQueries searchQuery;

	private final Locale locale;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public UserSearchDataSource(SearchIdentityParams searchParams, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		this.locale = locale;
		this.searchParams = searchParams;
		this.userPropertyHandlers = userPropertyHandlers;
		searchQuery = CoreSpringFactory.getImpl(IdentityPowerSearchQueries.class);
	}

	@Override
	public int getRowCount() {
		return searchQuery.countIdentitiesByPowerSearch(searchParams);
	}

	@Override
	public List<UserPropertiesRow> reload(List<UserPropertiesRow> rows) {
		return Collections.emptyList();
	}

	@Override
	public ResultInfos<UserPropertiesRow> getRows(String query, List<FlexiTableFilter> filters,
			List<String> condQueries, int firstResult, int maxResults, SortKey... orderBy) {
		List<UserPropertiesRow> rows = searchQuery.getIdentitiesByPowerSearch(searchParams, userPropertyHandlers, locale, firstResult, maxResults);
		return new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
	}
}
