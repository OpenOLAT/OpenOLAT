package org.olat.ims.qti21.manager;

import java.util.Date;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.UserTestSession;
import org.olat.ims.qti21.model.UserTestSessionImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TestSessionDAO {
	
	@Autowired
	private DB dbInstance;
	

	public UserTestSession createTestSession(RepositoryEntry testEntry, RepositoryEntry courseEntry, Identity identity) {
		UserTestSessionImpl testSession = new UserTestSessionImpl();
		Date now = new Date();
		testSession.setCreationDate(now);
		testSession.setLastModified(now);
		testSession.setTestEntry(testEntry);
		testSession.setCourseEntry(courseEntry);
		testSession.setAuthorMode(false);
		testSession.setExploded(false);
		testSession.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(testSession);
		return testSession;
	}

}
