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
package org.olat.course.nodes.livestream.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.time.Duration;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.livestream.Launch;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LaunchDAOTest extends OlatTestCase {
	
	@Autowired
	private LaunchDAO sut;
	@Autowired
	private DB dbInstance;
	
	@Test
	public void shouldCreateLaunch() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = JunitTestHelper.random();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("ls");
		Date launchDate = new Date();
		dbInstance.commitAndCloseSession();
		
		Launch launch = sut.create(entry, subIdent, identity, launchDate);
		dbInstance.commitAndCloseSession();
		
		assertThat(launch.getCreationDate()).isNotNull();
		assertThat(launch.getLaunchDate()).isCloseTo(launchDate, Duration.ofMillis(1000).toMillis());
		assertThat(launch.getCourseEntry()).isEqualTo(entry);
		assertThat(launch.getSubIdent()).isEqualTo(subIdent);
		assertThat(launch.getIdentity()).isEqualTo(identity);
	}
	
	@Test
	public void shouldGetLaunchers() {
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityOther = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		
		Date before = new GregorianCalendar(2010, 2, 8).getTime();
		Date from = new GregorianCalendar(2010, 2, 9).getTime();
		Date inside = new GregorianCalendar(2010, 2, 10).getTime();
		Date to = new GregorianCalendar(2010, 2, 11).getTime();
		Date after = new GregorianCalendar(2010, 2, 12).getTime();
		sut.create(courseEntry, subIdent, identity1, inside);
		sut.create(courseEntry, subIdent, identity1, inside);
		sut.create(courseEntry, subIdent, identity1, inside);
		sut.create(courseEntry, subIdent, identity2, inside);
		// These log entries should have all wrong parameters. So userKeyOther should not be a viewer.
		sut.create(courseEntryOther, subIdent, identityOther, inside);
		sut.create(courseEntry, random(), identityOther, inside);
		sut.create(courseEntry, subIdent, identityOther, before);
		sut.create(courseEntry, subIdent, identityOther, after);
		dbInstance.commitAndCloseSession();
		
		Long viewers = sut.getLaunchers(courseEntry, subIdent, from, to);
		
		assertThat(viewers).isEqualTo(2);
	}

}
