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
package org.olat.modules.video.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.VideoMeta;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 nov. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoMetaXStreamTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(VideoMetaXStreamTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VideoMetadataDAO videoMetadataDao;
	
	@Test
	public void writeRead_meta() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		//create metadata entries
		VideoMeta meta = videoMetadataDao.createVideoMetadata(entry, 1500, null, VideoFormat.mp4);
		Assert.assertNotNull(meta);
		dbInstance.commitAndCloseSession();

		byte[] content = null;
		try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			VideoMetaXStream.toXml(out, meta);
			content = out.toByteArray();
		} catch(IOException e) {
			log.error("", e);
		}
		
		Assert.assertNotNull(content);
		
		try(ByteArrayInputStream in = new ByteArrayInputStream(content)) {
			VideoMeta reloadedMeta = VideoMetaXStream.fromXml(in);
			Assert.assertNotNull(reloadedMeta);
		} catch(IOException e) {
			log.error("", e);
			Assert.fail();
		}
	}
}
