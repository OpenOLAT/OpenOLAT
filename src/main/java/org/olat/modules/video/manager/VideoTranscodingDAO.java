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

import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.model.VideoTranscodingImpl;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * DAO implementation for manipulating VideoTranscoding objects
 * 
 * Initial date: 05.05.2016<br>
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
@Service("videoTranscodingDao")
public class VideoTranscodingDAO {

	@Autowired
	private DB dbInstance;

	/**
	 * Factory method to create and persist new video transcoding objects for a given video resource
	 * @param videoResource
	 * @param resolution
	 * @param format
	 * @return
	 */
	public VideoTranscoding createVideoTranscoding(OLATResource videoResource, int resolution, String format) {
		VideoTranscodingImpl videoTranscoding = new VideoTranscodingImpl();
		videoTranscoding.setCreationDate(new Date());
		videoTranscoding.setLastModified(new Date());
		videoTranscoding.setVideoResource(videoResource);
		videoTranscoding.setResolution(resolution);
		videoTranscoding.setFormat(format);
		videoTranscoding.setStatus(VideoTranscoding.TRANSCODING_STATUS_WAITING);
		dbInstance.getCurrentEntityManager().persist(videoTranscoding);
		return videoTranscoding;
	}
	
	public List<VideoTranscoding> getVideoTranscodings(OLATResource videoResource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select trans from videotranscoding as trans")
		  .append(" inner join fetch trans.videoResource as res")
		  .append(" where res.key=:resourceKey");
		TypedQuery<VideoTranscoding> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VideoTranscoding.class)
				.setParameter("resourceKey", videoResource.getKey());
		return query.getResultList();
	}

	public VideoTranscoding updateTranscoding(VideoTranscoding videoTranscoding) {
		((VideoTranscodingImpl)videoTranscoding).setLastModified(new Date());
		VideoTranscoding trans = dbInstance.getCurrentEntityManager().merge(videoTranscoding);
		//FIXME:SR: is that needed? flush did not work
		dbInstance.commit();
		return trans;
	}

	public int deleteVideoTranscodings(OLATResource videoResource) {
		String deleteQuery = "delete from videotranscoding where fk_resource_id=:resourceKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(deleteQuery).setParameter("resourceKey", videoResource.getKey())
				.executeUpdate();
	}

	
}
