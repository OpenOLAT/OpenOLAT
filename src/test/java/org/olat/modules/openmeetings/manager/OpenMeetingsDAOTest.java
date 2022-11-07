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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.manager;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.modules.openmeetings.model.OpenMeetingsRoom;
import org.olat.modules.openmeetings.model.OpenMeetingsRoomReference;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenMeetingsDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OpenMeetingsDAO openMeetingsDAO;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	
	@Test
	public void createReference() {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(UUID.randomUUID().toString(), 1l);
		OpenMeetingsRoom room = new OpenMeetingsRoom();
		room.setRoomId(123l);
		OpenMeetingsRoomReference ref = openMeetingsDAO.createReference(null, ores, "hello", room);
		
		Assert.assertNotNull(ref);
		Assert.assertNotNull(ref.getKey());
		Assert.assertNotNull(ref.getCreationDate());
		Assert.assertNotNull(ref.getLastModified());
		
		Assert.assertEquals(ores.getResourceableTypeName(), ref.getResourceTypeName());
		Assert.assertEquals(ores.getResourceableId(), ref.getResourceTypeId());
		Assert.assertEquals(123l, ref.getRoomId());
	}
	
	@Test
	public void getReferences() {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(UUID.randomUUID().toString(), 1l);
		OpenMeetingsRoom room = new OpenMeetingsRoom();
		room.setRoomId(123l);
		OpenMeetingsRoomReference ref = openMeetingsDAO.createReference(null, ores, "hello", room);
		dbInstance.commitAndCloseSession();

		List<OpenMeetingsRoomReference> refs = openMeetingsDAO.getReferences();
		Assert.assertNotNull(refs);
		Assert.assertFalse(refs.isEmpty());
		Assert.assertTrue(refs.contains(ref));
	}
	
	@Test
	public void createAndGetReference() {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(UUID.randomUUID().toString(), 1l);
		OpenMeetingsRoom room = new OpenMeetingsRoom();
		room.setRoomId(123l);
		OpenMeetingsRoomReference ref = openMeetingsDAO.createReference(null, ores, "world", room);
		Assert.assertNotNull(ref);
		dbInstance.commitAndCloseSession();
		
		OpenMeetingsRoomReference loadedRef = openMeetingsDAO.getReference(null, ores, "world");
		
		Assert.assertNotNull(loadedRef);
		Assert.assertNotNull(loadedRef.getKey());
		Assert.assertNotNull(loadedRef.getCreationDate());
		Assert.assertNotNull(loadedRef.getLastModified());
		
		Assert.assertEquals(ores.getResourceableTypeName(), loadedRef.getResourceTypeName());
		Assert.assertEquals(ores.getResourceableId(), loadedRef.getResourceTypeId());
		Assert.assertEquals(123l, loadedRef.getRoomId());
	}
	
	@Test
	public void createAndGetReferenceInGroup() {
		BusinessGroup group = businessGroupDao.createAndPersist(null, "OpenMeeting", "Open meeting desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		
		OpenMeetingsRoom room = new OpenMeetingsRoom();
		room.setRoomId(123l);
		OpenMeetingsRoomReference ref = openMeetingsDAO.createReference(group, null, null, room);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(ref);
		
		OpenMeetingsRoomReference loadedRef = openMeetingsDAO.getReference(group, null, null);
		
		Assert.assertNotNull(loadedRef);
		Assert.assertNotNull(loadedRef.getKey());
		Assert.assertNotNull(loadedRef.getCreationDate());
		Assert.assertNotNull(loadedRef.getLastModified());
		
		Assert.assertEquals(group, loadedRef.getGroup());
	}
}
