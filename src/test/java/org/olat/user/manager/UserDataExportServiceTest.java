package org.olat.user.manager;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserDataExport;
import org.olat.user.UserDataExportService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 23 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserDataExportServiceTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserDataExportDAO userDataExportDao;
	@Autowired
	private UserDataExportService exportService;
	
	@Test
	public void exportUserData() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("exp-1");
		Collection<String> exportIds = Collections.singletonList("efficiency.statements");
		UserDataExport data = userDataExportDao.createExport(identity, exportIds, UserDataExport.ExportStatus.requested);
		dbInstance.commitAndCloseSession();
		
		// trigger the method manually
		exportService.exportData(data.getKey());
	}

}
