/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
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
public class SessionDAO {
	
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
	
	public UserTestSession update(UserTestSession testSession) {
		return dbInstance.getCurrentEntityManager().merge(testSession);
	}

}
