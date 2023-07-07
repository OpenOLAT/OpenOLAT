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
package org.olat.modules.cemedia.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaToGroupRelation;
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;
import org.olat.modules.cemedia.model.MediaShare;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaRelationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private MediaRelationDAO mediaRelationDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	
	
	@Test
	public void createRelation() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("ce-media-1");
		Media media = mediaDao.createMedia("Media", "Shared Media", "Alt-text", "Media content", "Forum", "[Media:0]", null, 10, id);
		dbInstance.commit();
		
		Group group = groupDao.createGroup();
		MediaToGroupRelation relation = mediaRelationDao.createRelation(MediaToGroupRelationType.USER, true, media, group, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(relation);
		Assert.assertNotNull(relation.getKey());
		Assert.assertNotNull(relation.getCreationDate());
	}
	
	@Test
	public void getRelationByMediaAndType() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("ce-media-2");
		Media media = mediaDao.createMedia("Media", "Shared Media", "Alt-text", "Media content", "Forum", "[Media:0]", null, 10, id);
		dbInstance.commit();
		
		Group group = groupDao.createGroup();
		MediaToGroupRelation relation = mediaRelationDao.createRelation(MediaToGroupRelationType.USER, true, media, group, null);
		dbInstance.commitAndCloseSession();
		
		MediaToGroupRelation editableRelation = mediaRelationDao.getRelation(media, MediaToGroupRelationType.USER, true);
		Assert.assertEquals(relation, editableRelation);
	}
	
	@Test
	public void getRelationNotFound() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("ce-media-3");
		Media media = mediaDao.createMedia("Media", "Shared Media", "Alt-text", "Media content", "Forum", "[Media:0]", null, 10, id);
		dbInstance.commit();
		
		Group group = groupDao.createGroup();
		MediaToGroupRelation relation = mediaRelationDao.createRelation(MediaToGroupRelationType.USER, true, media, group, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);
		
		MediaToGroupRelation notEditableRelation = mediaRelationDao.getRelation(media, MediaToGroupRelationType.USER, false);
		Assert.assertNull(notEditableRelation);
	}
	
	@Test
	public void getRelationByMediaTypeAndGroup() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("ce-media-4");
		Media media = mediaDao.createMedia("Media", "Shared Media", "Alt-text", "Media content", "Forum", "[Media:0]", null, 10, id);
		dbInstance.commit();
		
		Group group = groupDao.createGroup();
		MediaToGroupRelation relation = mediaRelationDao.createRelation(MediaToGroupRelationType.USER, false, media, group, null);
		dbInstance.commitAndCloseSession();
		
		MediaToGroupRelation editableRelation = mediaRelationDao.getRelation(media, MediaToGroupRelationType.USER, false, group);
		Assert.assertEquals(relation, editableRelation);
	}
	
	@Test
	public void getRelations() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("ce-media-5");
		Media media = mediaDao.createMedia("Media", "Shared Media", "Alt-text", "Media content", "Forum", "[Media:0]", null, 10, id);
		dbInstance.commit();
		
		Group group = groupDao.createGroup();
		MediaToGroupRelation editableRelation = mediaRelationDao.createRelation(MediaToGroupRelationType.USER, true, media, group, null);
		MediaToGroupRelation readOnlyRelation = mediaRelationDao.createRelation(MediaToGroupRelationType.USER, false, media, group, null);
		dbInstance.commitAndCloseSession();

		List<MediaToGroupRelation> relations = mediaRelationDao.getRelations(media, MediaToGroupRelationType.USER, group);
		assertThat(relations)
			.hasSize(2)
			.containsExactlyInAnyOrder(editableRelation, readOnlyRelation);
	}
	
	@Test
	public void getUserRelations() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("ce-media-6");
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("ce-media-6");
		Media media = mediaDao.createMedia("Media", "Shared Media", "Alt-text", "Media content", "Forum", "[Media:0]", null, 10, id);
		dbInstance.commit();
		
		MediaToGroupRelation relation = mediaService.addRelation(media, false, user);
		dbInstance.commitAndCloseSession();

		List<MediaShare> relations = mediaRelationDao.getUserRelations(media);
		Assert.assertNotNull(relations);
		Assert.assertEquals(1, relations.size());
		
		MediaShare share = relations.get(0);
		Assert.assertEquals(relation, share.getRelation());
		Assert.assertEquals(user, share.getUser());
		Assert.assertNull(share.getBusinessGroup());
		Assert.assertNull(share.getRepositoryEntry());
		Assert.assertNull(share.getOrganisation());
	}
	
	@Test
	public void getOrganisationRelations() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("ce-media-7");
		Media media = mediaDao.createMedia("Media 7", "Shared Media", "Alt-text", "Media content", "Forum", "[Media:0]", null, 10, id);
		dbInstance.commit();
		
		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		MediaToGroupRelation relation = mediaService.addRelation(media, false, defaultOrganisation);
		dbInstance.commitAndCloseSession();

		List<MediaShare> relations = mediaRelationDao.getOrganisationRelations(media);
		Assert.assertNotNull(relations);
		Assert.assertEquals(1, relations.size());
		
		MediaShare share = relations.get(0);
		Assert.assertEquals(relation, share.getRelation());
		Assert.assertNull(share.getUser());
		Assert.assertNull(share.getBusinessGroup());
		Assert.assertNull(share.getRepositoryEntry());
		Assert.assertEquals(defaultOrganisation, share.getOrganisation());
	}
	
	@Test
	public void getRepositoryRelations() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("ce-media-7");
		Media media = mediaDao.createMedia("Media 7", "Shared Media", "Alt-text", "Media content", "Forum", "[Media:0]", null, 10, id);
		dbInstance.commit();
		
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(id);
		MediaToGroupRelation relation = mediaService.addRelation(media, false, entry);
		dbInstance.commitAndCloseSession();

		List<MediaShare> relations = mediaRelationDao.getRepositoryEntryRelations(media);
		Assert.assertNotNull(relations);
		Assert.assertEquals(1, relations.size());
		
		MediaShare share = relations.get(0);
		Assert.assertEquals(relation, share.getRelation());
		Assert.assertNull(share.getUser());
		Assert.assertNull(share.getBusinessGroup());
		Assert.assertEquals(entry, share.getRepositoryEntry());
		Assert.assertNull(share.getOrganisation());
	}
	
	@Test
	public void getBusinessGroupRelations() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("ce-media-7");
		Media media = mediaDao.createMedia("Media 7", "Shared Media", "Alt-text", "Media content", "Forum", "[Media:0]", null, 10, id);
		
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(id, "Shared media group", "Shared media group", BusinessGroup.BUSINESS_TYPE,
				0, 5, false, false, true, false, false);
		businessGroupRelationDao.addRole(id, businessGroup, GroupRoles.participant.name());
		
		MediaToGroupRelation relation = mediaService.addRelation(media, false, businessGroup);
		dbInstance.commitAndCloseSession();

		List<MediaShare> relations = mediaRelationDao.getBusinesGroupRelations(media);
		Assert.assertNotNull(relations);
		Assert.assertEquals(1, relations.size());
		
		MediaShare share = relations.get(0);
		Assert.assertEquals(relation, share.getRelation());
		Assert.assertNull(share.getUser());
		Assert.assertEquals(businessGroup, share.getBusinessGroup());
		Assert.assertNull(share.getOrganisation());
	}

}
