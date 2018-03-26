package org.olat.basesecurity;

import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 20 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface IdentityPowerSearchQueries {
	
	public int countIdentitiesByPowerSearch(SearchIdentityParams params);
	
	public List<Identity> getIdentitiesByPowerSearch(SearchIdentityParams params, int firstResult, int maxResults);
	
	public List<UserPropertiesRow> getIdentitiesByPowerSearch(SearchIdentityParams params,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale, int firstResult, int maxResults);

}
