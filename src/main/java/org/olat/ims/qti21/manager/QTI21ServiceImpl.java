package org.olat.ims.qti21.manager;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.UserTestSession;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ServiceImpl implements QTI21Service {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TestSessionDAO testSessionDAO;
	

	@Override
	public UserTestSession createTestSession(RepositoryEntry testEntry, RepositoryEntry courseEntry, Identity identity) {
		return testSessionDAO.createTestSession(testEntry, courseEntry, identity);
	}
	
	
	

}
