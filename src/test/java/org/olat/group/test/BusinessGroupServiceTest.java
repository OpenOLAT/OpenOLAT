package org.olat.group.test;

import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupServiceTest extends OlatTestCase {
	
	private OLog log = Tracing.createLoggerFor(BusinessGroupServiceTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupService groupService;
	@Autowired
	private OLATResourceManager olatResourceManager;
	
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
		Assert.assertNotNull(groupService);
		Assert.assertNotNull(olatResourceManager);
	}
	
	@Test
	public void createBusinessGroup() {
		BusinessGroup group = groupService.createBusinessGroup(null, "gdao", "gdao-desc", BusinessGroup.TYPE_LEARNINGROUP, -1, -1, false, false, null);
		Assert.assertNotNull(group);
	}
	
	@Test
	public void createBusinessGroupWithResource() {
		String resourceType = UUID.randomUUID().toString();
		OLATResource resource =  olatResourceManager.createOLATResourceInstance(new TestResource(resourceType));
		olatResourceManager.saveOLATResource(resource);
		BusinessGroup group = groupService.createBusinessGroup(null, "gdao", "gdao-desc", BusinessGroup.TYPE_LEARNINGROUP, -1, -1, false, false, resource);
		
		//commit the group
		dbInstance.commit();
		Assert.assertNotNull(group);
	}
	
	@Test
	public void loadBusinessGroups() {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(); 
		List<BusinessGroup> groups = groupService.findBusinessGroups(params, null, false, false, null, 0, 5);
		Assert.assertNotNull(groups);
	}
	
	private static class TestResource implements OLATResourceable {
		private String type;
		
		public TestResource(String type) {
			this.type = type;
		}

		@Override
		public String getResourceableTypeName() {
			return type;
		}

		@Override
		public Long getResourceableId() {
			return 1l;
		}
	}
}
