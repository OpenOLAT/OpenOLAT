package org.olat.basesecurity.manager;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationOrderedTreeCacheTest extends OlatTestCase {
	
	@Autowired
	private OrganisationOrderedTreeCache organisationOrderedTreeCache;
	
	@Test
	public void load() {
		List<OrganisationWithParents> organisations = organisationOrderedTreeCache.getOrderedOrganisationsWithParents();
		Assert.assertFalse(organisations.isEmpty());
	}

}
