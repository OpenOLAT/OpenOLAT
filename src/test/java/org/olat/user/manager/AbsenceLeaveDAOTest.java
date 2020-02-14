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
package org.olat.user.manager;

import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.AbsenceLeave;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceLeaveDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AbsenceLeaveDAO absenceLeaveDao;
	
	@Test
	public void createAbsenceLeave_minimal() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-1");
		AbsenceLeave absenceLeave = absenceLeaveDao.createAbsenceLeave(id, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(absenceLeave);
		Assert.assertNotNull(absenceLeave.getCreationDate());
		Assert.assertNotNull(absenceLeave.getLastModified());
		Assert.assertEquals(id, absenceLeave.getIdentity());
	}

	@Test
	public void createAndLoadAbsenceLeave() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-1");
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(UUID.randomUUID().toString(), 25l);
		AbsenceLeave absenceLeave = absenceLeaveDao.createAbsenceLeave(id, new Date(), new Date(), ores, "hello-world");
		dbInstance.commitAndCloseSession();
		
		AbsenceLeave reloadedAbsenceLeave = absenceLeaveDao.loadAbsenceLeaveByKey(absenceLeave.getKey());
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(reloadedAbsenceLeave);
		Assert.assertNotNull(reloadedAbsenceLeave.getCreationDate());
		Assert.assertNotNull(reloadedAbsenceLeave.getLastModified());
		Assert.assertNotNull(reloadedAbsenceLeave.getAbsentFrom());
		Assert.assertNotNull(reloadedAbsenceLeave.getAbsentTo());
		Assert.assertEquals(id, reloadedAbsenceLeave.getIdentity());
		Assert.assertEquals(ores.getResourceableTypeName(), reloadedAbsenceLeave.getResName());
		Assert.assertEquals(Long.valueOf(25l), reloadedAbsenceLeave.getResId());
		Assert.assertEquals("hello-world", reloadedAbsenceLeave.getSubIdent());
	}
}
