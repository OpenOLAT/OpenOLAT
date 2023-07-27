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

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaLog.Action;
import org.olat.modules.cemedia.model.MediaLogImpl;
import org.olat.modules.cemedia.model.SearchMediaLogParameters;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * 
 * Initial date: 7 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaLogDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private MediaLogDAO mediaLogDao;
	
	@Test
	public void createMediaLog() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-1");
		Media media = mediaDao.createMediaAndVersion("Media log", "Media logged", "Alt-text", "Media content", "Forum", "[Media:0]", null, 10, id);
		dbInstance.commit();
		
		
		MediaLog mediaLog = mediaLogDao.createLog(Action.UPDATE, "tmp-id", media, id);
		dbInstance.commit();
		Assert.assertNotNull(mediaLog);
		Assert.assertNotNull(mediaLog.getKey());
		Assert.assertNotNull(mediaLog.getCreationDate());
		Assert.assertEquals(id, mediaLog.getIdentity());
		Assert.assertEquals("tmp-id", ((MediaLogImpl)mediaLog).getTempIdentifier());
		Assert.assertEquals(media, ((MediaLogImpl)mediaLog).getMedia());
	}
	
	@Test
	public void getMediaLog() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-1");
		Media media = mediaDao.createMediaAndVersion("Media log", "Media logged", "Alt-text", "Media content", "Forum", "[Media:0]", null, 10, id);
		dbInstance.commit();
		
		MediaLog mediaLog = mediaLogDao.createLog(Action.UPDATE, null, media, id);
		dbInstance.commit();
		
		SearchMediaLogParameters params = new SearchMediaLogParameters();
		List<MediaLog> logs = mediaLogDao.getLogs(media, params);
		assertThat(logs)
			.hasSize(1)
			.containsExactly(mediaLog);
	}
	
	@Test
	public void getLogs() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-3");
		Media media = mediaDao.createMediaAndVersion("Media log search", "Media logged", "Alt-text", "Media content", "Forum", "[Media:0]", null, 10, id);
		dbInstance.commit();
		
		String tempIdentifier = JunitTestHelper.random();
		MediaLog mediaLog = mediaLogDao.createLog(Action.UPDATE, tempIdentifier, media, id);
		dbInstance.commit();
		
		SearchMediaLogParameters params = new SearchMediaLogParameters();
		params.setActions(List.of(Action.UPDATE, Action.ASSIGNMENT));
		params.setTempIdentifier(tempIdentifier);
		params.setIdentityKeys(List.of(id.getKey()));
		params.setDateRange(new DateRange(DateUtils.addDays(new Date(), -3), DateUtils.addDays(new Date(), 3)));
		List<MediaLog> logs = mediaLogDao.getLogs(media, params);
		assertThat(logs)
			.hasSize(1)
			.containsExactly(mediaLog);
	}
	
	@Test
	public void getLogsNotFound() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-4");
		Media media = mediaDao.createMediaAndVersion("Media log not found", "Media logged", "Alt-text", "Media content", "Forum", "[Media:0]", null, 10, id);
		dbInstance.commit();
		
		String tempIdentifier = JunitTestHelper.random();
		MediaLog mediaLog = mediaLogDao.createLog(Action.UPDATE, tempIdentifier, media, id);
		dbInstance.commit();
		Assert.assertNotNull(mediaLog);
		
		SearchMediaLogParameters params = new SearchMediaLogParameters();
		params.setTempIdentifier("temp-identifier");
		List<MediaLog> logs = mediaLogDao.getLogs(media, params);
		assertThat(logs)
			.isEmpty();
	}

}
