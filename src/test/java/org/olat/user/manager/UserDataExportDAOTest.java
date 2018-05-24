package org.olat.user.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserDataExport;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserDataExportDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserDataExportDAO userDataExportDao;
	
	@Test
	public void createExport() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("exp-1");
		Collection<String> exporters = Collections.singletonList("test");
		UserDataExport data = userDataExportDao.createExport(identity, exporters, UserDataExport.ExportStatus.requested);
		dbInstance.commit();
		
		Assert.assertNotNull(data);
		Assert.assertNotNull(data.getKey());
		Assert.assertNotNull(data.getCreationDate());
		Assert.assertNotNull(data.getLastModified());
		Assert.assertNotNull(data.getDirectory());
		Assert.assertEquals(identity, data.getIdentity());
		Assert.assertEquals(1, data.getExportIds().size());
		Assert.assertEquals("test", data.getExportIds().iterator().next());
		Assert.assertEquals(UserDataExport.ExportStatus.requested, data.getStatus());
	}
	
	@Test
	public void loadByKey() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("exp-1");
		Collection<String> exporters = Collections.singletonList("test");
		UserDataExport data = userDataExportDao.createExport(identity, exporters, UserDataExport.ExportStatus.requested);
		dbInstance.commitAndCloseSession();
		
		UserDataExport reloadedData = userDataExportDao.loadByKey(data.getKey());
		
		Assert.assertNotNull(reloadedData);
		Assert.assertEquals(data, reloadedData);
		Assert.assertNotNull(reloadedData.getCreationDate());
		Assert.assertNotNull(reloadedData.getLastModified());
		Assert.assertEquals(data.getDirectory(), reloadedData.getDirectory());
		Assert.assertEquals(identity, reloadedData.getIdentity());
		Assert.assertEquals(UserDataExport.ExportStatus.requested, reloadedData.getStatus());
	}
	
	@Test
	public void getUserDataExport() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("exp-1");
		Collection<String> exporters = Collections.singletonList("test");
		UserDataExport requestedData = userDataExportDao.createExport(identity, exporters, UserDataExport.ExportStatus.requested);
		UserDataExport readyData = userDataExportDao.createExport(identity, exporters, UserDataExport.ExportStatus.ready);
		dbInstance.commitAndCloseSession();
		
		List<UserDataExport.ExportStatus> runningStatus = new ArrayList<>();
		runningStatus.add(UserDataExport.ExportStatus.requested);
		runningStatus.add(UserDataExport.ExportStatus.processing);
		List<UserDataExport> runningExports = userDataExportDao.getUserDataExport(identity, runningStatus);
		Assert.assertNotNull(runningExports);
		Assert.assertEquals(1, runningExports.size());
		Assert.assertEquals(requestedData, runningExports.get(0));
		Assert.assertFalse(runningExports.contains(readyData));
	}
	


}
