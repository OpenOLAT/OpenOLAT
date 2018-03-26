package org.olat.user.ui.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.id.Identity;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 21 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityListDataSource implements FlexiTableDataSourceDelegate<UserPropertiesRow> {
	
	private final List<UserPropertiesRow> userRows;
	
	public IdentityListDataSource(List<Identity> identities, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		userRows = new ArrayList<>(identities.size());
		for(Identity identity:identities) {
			userRows.add(new UserPropertiesRow(identity, userPropertyHandlers, locale));
		}
	}

	@Override
	public int getRowCount() {
		return userRows.size();
	}

	@Override
	public List<UserPropertiesRow> reload(List<UserPropertiesRow> rows) {
		return Collections.emptyList();
	}

	@Override
	public ResultInfos<UserPropertiesRow> getRows(String query, List<FlexiTableFilter> filters,
			List<String> condQueries, int firstResult, int maxResults, SortKey... orderBy) {
		return new DefaultResultInfos<>(userRows.size(), -1, userRows);
	}
}
