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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.video.VideoTranscoding;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTranscodingDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VideoTranscodingDAO videoTranscodingDao;
	
	@Test
	public void createVideoTranscoding() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		// pending transcoding
		VideoTranscoding vTranscoding = videoTranscodingDao.createVideoTranscoding(resource, 1080, "mp4");
		Assert.assertNotNull(vTranscoding);
		dbInstance.commitAndCloseSession();

		// done transcoding
		VideoTranscoding vTranscoding2 = videoTranscodingDao.createVideoTranscoding(resource, 720, "mp4");
		Assert.assertNotNull(vTranscoding2);
		vTranscoding2.setStatus(VideoTranscoding.TRANSCODING_STATUS_DONE);
		vTranscoding2.setTranscoder(VideoTranscoding.TRANSCODER_LOCAL);
		vTranscoding2 = videoTranscodingDao.updateTranscoding(vTranscoding2);
		Assert.assertNotNull(vTranscoding2);
		Assert.assertTrue(vTranscoding2.getStatus() == 100);
		dbInstance.commitAndCloseSession();

		// check for transcodings of resource
		List<VideoTranscoding> vTranscodingList = videoTranscodingDao.getVideoTranscodings(resource);
		Assert.assertNotNull(vTranscodingList);
		Assert.assertEquals(2, vTranscodingList.size());
		Assert.assertEquals(vTranscoding, vTranscodingList.get(0));
		Assert.assertEquals(vTranscoding2, vTranscodingList.get(1));

		// check for overall pending transcodings
		List<VideoTranscoding> vTranscodingList2 = videoTranscodingDao.getVideoTranscodingsPendingAndInProgress();
		Assert.assertNotNull(vTranscodingList2);
		Assert.assertTrue(vTranscodingList2.size() >= 1);
		Assert.assertTrue(vTranscodingList2.contains(vTranscoding));
	}
	
	@Test
	public void getVideoTranscodingByKey() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		VideoTranscoding vTranscoding = videoTranscodingDao.createVideoTranscoding(resource, 1080, "mp4");
		dbInstance.commitAndCloseSession();
		
		VideoTranscoding reloadedTranscoding = videoTranscodingDao.getVideoTranscoding(vTranscoding.getKey());
		Assert.assertNotNull(reloadedTranscoding);
		Assert.assertEquals(vTranscoding, reloadedTranscoding);
	}
	
	@Test
	public void deleteVideoTranscoding() {
//		void deleteVideoTranscoding(VideoTranscoding videoTranscoding) {
			OLATResource resource = JunitTestHelper.createRandomResource();
			// prepare transcodings
			VideoTranscoding vTranscoding1 = videoTranscodingDao.createVideoTranscoding(resource, 1080, "mp4");
			videoTranscodingDao.createVideoTranscoding(resource, 720, "mp4");
			videoTranscodingDao.createVideoTranscoding(resource, 720, "mp4");
			videoTranscodingDao.createVideoTranscoding(resource, 720, "mp4");
			dbInstance.commitAndCloseSession();
			// delete single transcoding
			videoTranscodingDao.deleteVideoTranscoding(vTranscoding1);
			dbInstance.commitAndCloseSession();
			List<VideoTranscoding> results = videoTranscodingDao.getVideoTranscodings(resource);
			Assert.assertEquals(3,results.size());
			// delete all transcodings of resource
			videoTranscodingDao.deleteVideoTranscodings(resource);
			dbInstance.commitAndCloseSession();
			results = videoTranscodingDao.getVideoTranscodings(resource);
			Assert.assertEquals(0,results.size());
	}
}
