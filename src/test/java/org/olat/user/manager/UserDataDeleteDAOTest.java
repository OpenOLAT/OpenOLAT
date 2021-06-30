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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.olat.user.UserDataDelete;
import org.olat.user.UserDataDeleteManager;
import org.olat.user.model.UserData;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserDataDeleteDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserDataDeleteDAO userDataDeleteDao;
	@Autowired
	private UserDataDeleteManager userDataDeleteManager;
	
	
	@Test
	public void createUserDataDelete() {
		List<UserData> userDatas = new ArrayList<>();
		userDatas.add(new UserData(9l, "del_9"));
		String userDataXml = userDataDeleteManager.toXML(userDatas);
		UserDataDelete userDataDelete = userDataDeleteDao.create(userDataXml, "3625");
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(userDataDelete);
		Assert.assertNotNull(userDataDelete.getCreationDate());
		Assert.assertNotNull(userDataDelete.getLastModified());
		Assert.assertNotNull(userDataDelete.getUserData());
		List<UserData> persistedDatas = userDataDeleteManager.fromXML(userDataDelete.getUserData());
		Assert.assertNotNull(persistedDatas);
		Assert.assertEquals(1, persistedDatas.size());
		Assert.assertEquals("3625", userDataDelete.getResourceIds());
	}
	
	@Test
	public void getWithoutResourceIds() {
		List<UserData> userDatas = new ArrayList<>();
		userDatas.add(new UserData(10l, "del_10"));
		String userDataXml = userDataDeleteManager.toXML(userDatas);
		UserDataDelete userDataDelete = userDataDeleteDao.create(userDataXml, "all");
		dbInstance.commitAndCloseSession();
		
		List<UserDataDelete> deletes = userDataDeleteDao.getWithoutResourceIds();
		Assert.assertNotNull(deletes);
		Assert.assertTrue(deletes.contains(userDataDelete));
	}
	
	@Test
	public void getWithResourceIds() {
		List<UserData> userDatas1 = List.of(new UserData(11l, "del_11"));
		String userDataXml1 = userDataDeleteManager.toXML(userDatas1);
		UserDataDelete userDataDeleteNo = userDataDeleteDao.create(userDataXml1, "noresources");
		
		List<UserData> userDatas2 = List.of(new UserData(12l, "del_12"));
		String userDataXml2 = userDataDeleteManager.toXML(userDatas2);
		UserDataDelete userDataDeleteNum = userDataDeleteDao.create(userDataXml2, "234723493279");
		
		dbInstance.commitAndCloseSession();
		
		List<UserDataDelete> deletes = userDataDeleteDao.getWithResourceIds();
		Assert.assertNotNull(deletes);
		Assert.assertTrue(deletes.contains(userDataDeleteNo));
		Assert.assertTrue(deletes.contains(userDataDeleteNum));
	}

}
