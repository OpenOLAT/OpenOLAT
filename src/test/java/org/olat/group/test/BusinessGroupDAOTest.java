package org.olat.group.test;

import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupDAOTest extends OlatTestCase {
	
	private OLog log = Tracing.createLoggerFor(BusinessGroupDAOTest.class);
	
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private DB dbInstance;
	
	@After
	public void tearDown() throws Exception {
		try {
			DBFactory.getInstance().commitAndCloseSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
			throw e;
		}
	}
	
	@Test
	public void should_service_present() {
		Assert.assertNotNull(businessGroupDao);
	}
	
	@Test
	public void createBusinessGroup() {
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdao", "gdao-desc", BusinessGroup.TYPE_LEARNINGROUP, -1, -1, false, false);
		Assert.assertNotNull(group);
		
		dbInstance.commit();
	}
	
	@Test
	public void loadBusinessGroups() {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(); 
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, false, false, null, 0, -1);
		Assert.assertNotNull(groups);
		

		dbInstance.commit();
	}

}
