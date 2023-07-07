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

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.cemedia.model.SearchMediaParameters;
import org.olat.modules.cemedia.model.SearchMediaParameters.Scope;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private MediaService mediaService;
	
	@Test
	public void searchWithScopeSharedWithMe() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-16");
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-17");

		Media media = mediaDao.createMedia("Media 7", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		mediaService.addRelation(media, false, user);
		
		dbInstance.commit();
		
		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setIdentity(user);
		parameters.setScope(Scope.SHARED_WITH_ME);

		List<MediaWithVersion> sharedMedias = mediaDao.searchBy(parameters);
		assertThat(sharedMedias)
			.hasSizeGreaterThanOrEqualTo(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsAnyOf(media);
	}
	
	@Test
	public void searchWithScopeSharedByMe() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-18");
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-19");

		Media sharedMedia = mediaDao.createMedia("Media shared", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		mediaService.addRelation(sharedMedia, false, user);
		Media privateMedia = mediaDao.createMedia("Media private", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setIdentity(author);
		parameters.setScope(Scope.SHARED_BY_ME);

		List<MediaWithVersion> sharedMedias = mediaDao.searchBy(parameters);
		assertThat(sharedMedias)
			.hasSizeGreaterThanOrEqualTo(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsAnyOf(sharedMedia)
			.doesNotContain(privateMedia);
	}
	
	@Test
	public void searchWithScopeSharedWithEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-20");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);

		Media sharedMedia = mediaDao.createMedia("Media shared with repo", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		mediaService.addRelation(sharedMedia, false, entry);
		Media privateMedia = mediaDao.createMedia("Media private", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setRepositoryEntry(entry);
		parameters.setScope(Scope.SHARED_WITH_ENTRY);

		List<MediaWithVersion> sharedMedias = mediaDao.searchBy(parameters);
		assertThat(sharedMedias)
			.hasSizeGreaterThanOrEqualTo(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsAnyOf(sharedMedia)
			.doesNotContain(privateMedia);
	}
	

}
