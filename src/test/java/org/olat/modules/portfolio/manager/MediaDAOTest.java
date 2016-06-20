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
package org.olat.modules.portfolio.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.Media;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaDAO mediaDao;
	
	@Test
	public void createMedia() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-1");
		Media media = mediaDao.createMedia("Media", "Media description", "Media content", "Forum", "[Media:0]", 10, id);
		dbInstance.commit();
		
		Assert.assertNotNull(media);
		Assert.assertNotNull(media.getKey());
		Assert.assertNotNull(media.getCreationDate());
		Assert.assertNotNull(media.getCollectionDate());
		Assert.assertEquals(id, media.getAuthor());
		
		Media reloadedMedia = mediaDao.loadByKey(media.getKey());
		Assert.assertNotNull(reloadedMedia);
		Assert.assertEquals(media, reloadedMedia);
		Assert.assertEquals(id, reloadedMedia.getAuthor());
		Assert.assertEquals("Media", reloadedMedia.getTitle());
		Assert.assertEquals("Media description", reloadedMedia.getDescription());
		Assert.assertEquals("[Media:0]", reloadedMedia.getBusinessPath());
		Assert.assertEquals(id, reloadedMedia.getAuthor());
	}

}
