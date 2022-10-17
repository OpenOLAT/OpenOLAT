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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.model.TranscodingCount;
import org.olat.modules.video.model.VideoTranscodingImpl;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * DAO implementation for manipulating VideoTranscoding objects
 * 
 * Initial date: 05.05.2016<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
@Service("videoTranscodingDao")
public class VideoTranscodingDAO {

	@Autowired
	private DB dbInstance;

	/**
	 * Factory method to create and persist new video transcoding objects for a
	 * given video resource
	 * 
	 * @param videoResource
	 * @param resolution
	 * @param format
	 * @return
	 */
	VideoTranscoding createVideoTranscoding(OLATResource videoResource, int resolution, String format) {
		VideoTranscodingImpl videoTranscoding = new VideoTranscodingImpl();
		videoTranscoding.setCreationDate(new Date());
		videoTranscoding.setLastModified(videoTranscoding.getCreationDate());
		videoTranscoding.setVideoResource(videoResource);
		videoTranscoding.setResolution(resolution);
		videoTranscoding.setFormat(format);
		videoTranscoding.setStatus(VideoTranscoding.TRANSCODING_STATUS_WAITING);
		dbInstance.getCurrentEntityManager().persist(videoTranscoding);
		return videoTranscoding;
	}

	/**
	 * Merge updated video transcoding, persist on DB
	 * 
	 * @param videoTranscoding
	 * @return Updated transcoding object
	 */
	VideoTranscoding updateTranscoding(VideoTranscoding videoTranscoding) {
		((VideoTranscodingImpl) videoTranscoding).setLastModified(new Date());
		VideoTranscoding trans = dbInstance.getCurrentEntityManager().merge(videoTranscoding);
		return trans;
	}

	/**
	 * Delete all video transcoding objects for a given video resource
	 * 
	 * @param videoResource
	 * @return
	 */
	int deleteVideoTranscodings(OLATResource videoResource) {
		String deleteQuery = "delete from videotranscoding where fk_resource_id=:resourceKey";
		return dbInstance.getCurrentEntityManager().createQuery(deleteQuery)
				.setParameter("resourceKey", videoResource.getKey()).executeUpdate();
	}

	/**
	 * Delete a specific video transcoding version
	 * 
	 * @param videoTranscoding
	 */
	void deleteVideoTranscoding(VideoTranscoding videoTranscoding) {
		try {
			videoTranscoding = dbInstance.getCurrentEntityManager().getReference(VideoTranscodingImpl.class, videoTranscoding.getKey());
			dbInstance.getCurrentEntityManager().remove(videoTranscoding);
		} catch (EntityNotFoundException e) {
			// already deleted
		}
	}

	/**
	 * Get all video transcodings for a specific video resource, sorted by
	 * resolution, highes resolution first
	 * 
	 * @param videoResource
	 * @return
	 */
	List<VideoTranscoding> getVideoTranscodings(OLATResource videoResource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select trans from videotranscoding as trans")
			.append(" inner join fetch trans.videoResource as res")
			.append(" where res.key=:resourceKey")
			.append(" order by trans.resolution desc");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VideoTranscoding.class)
				.setParameter("resourceKey", videoResource.getKey())
				.getResultList();
	}
	
	/**
	 * Gets all video transcodings.
	 *
	 * @return all video transcodings
	 */
	List<VideoTranscoding> getAllVideoTranscodings() {
		StringBuilder sb = new StringBuilder();
		sb.append("select trans from videotranscoding as trans")
			.append(" order by trans.resolution desc");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VideoTranscoding.class)
				.getResultList();
	}
	
	public VideoTranscoding getVideoTranscoding(Long key) {
		String query = "select trans from videotranscoding as trans where trans.key=:transcodingKey";
		List<VideoTranscoding> transcoding = dbInstance.getCurrentEntityManager()
				.createQuery(query, VideoTranscoding.class)
				.setParameter("transcodingKey", key)
				.getResultList();
		return transcoding == null || transcoding.isEmpty() ? null : transcoding.get(0);
	}
	
	/**
	 * Gets all transcodings of one video resolution.
	 *
	 * @param resolution
	 * @return all videos of one resolution
	 */
	List<VideoTranscoding> getOneVideoResolution (int resolution) {
		StringBuilder sb = new StringBuilder();
		sb.append("select trans from videotranscoding as trans")
			.append(" where trans.resolution=:resolution")
			.append(" order by trans.lastModified desc");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VideoTranscoding.class)
				.setParameter("resolution", resolution)
				.getResultList();
	}
	
	/**
	 * Gets the all video transcodings count.
	 *
	 * @return the all video transcodings count
	 */
	List<TranscodingCount> getAllVideoTranscodingsCount() {
		StringBuilder sb = new StringBuilder();
		//[0] count of a distinct [1] resolution
		sb.append("select count(trans.key), trans.resolution from videotranscoding as trans")
		  .append(" group by trans.resolution");
		
		List<Object[]> rawData = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.getResultList();
		
		List<TranscodingCount> allTranscodings = new ArrayList<>();
		for (Object[] data : rawData) {
			Long count = (Long) data[0];
			Integer resolution = (Integer) data[1];
			allTranscodings.add(new TranscodingCount(count, resolution));
		}
		return allTranscodings;
	}
	
	/**
	 * Gets the all video transcodings count FAILS.
	 *
	 * @return FAILS count
	 */
	List<TranscodingCount> getAllVideoTranscodingsCountFails(int errorcode) {
		StringBuilder sb = new StringBuilder();
		//[0] count of a distinct [1] resolution
		sb.append("select count(trans.key), trans.resolution from videotranscoding as trans")
			.append(" where trans.status <= :errorcode")
			.append(" group by trans.resolution");
		
		List<Object[]> rawData = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("errorcode", errorcode)
			.getResultList();
		
		List<TranscodingCount> allTranscodings = new ArrayList<>();
		for (Object[] data : rawData) {
			Long count = (Long) data[0];
			Integer resolution = (Integer) data[1];
			allTranscodings.add(new TranscodingCount(count, resolution));
		}
		return allTranscodings;
	}
	
	/**
	 * Gets the all video successful transcodings count.
	 *
	 * @return Success count
	 */
	List<TranscodingCount> getAllVideoTranscodingsCountSuccess(int errorcode) {
		StringBuilder sb = new StringBuilder();
		//[0] count of a distinct [1] resolution
		sb.append("select count(trans.key), trans.resolution from videotranscoding as trans")
			.append(" where trans.status > :errorcode")
			.append(" group by trans.resolution");
		
		List<Object[]> rawData = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("errorcode", errorcode)
			.getResultList();
		
		List<TranscodingCount> allTranscodings = new ArrayList<>();
		for (Object[] data : rawData) {
			Long count = (Long) data[0];
			Integer resolution = (Integer) data[1];
			allTranscodings.add(new TranscodingCount(count, resolution));
		}
		return allTranscodings;
	}

	/**
	 * Get all video transcodings which are waiting for transcoding or are
	 * currently in transcoding in FIFO ordering
	 * 
	 * @return
	 */
	List<VideoTranscoding> getVideoTranscodingsPendingAndInProgress() {
		StringBuilder sb = new StringBuilder();
		sb.append("select trans from videotranscoding as trans")
		  .append(" inner join fetch trans.videoResource as res")
		  .append(" where trans.status != 100 and trans.status > -2")//without error codes
		  .append(" order by trans.creationDate asc, trans.id asc");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VideoTranscoding.class)
				.getResultList();
	}
	
	/**
	 * Gets the failed video transcodings.
	 * currently error codes start at -2 until -4.
	 *
	 * @return the failed video transcodings
	 */
	List<VideoTranscoding> getFailedVideoTranscodings() {
			StringBuilder sb = new StringBuilder();
				sb.append("select trans from videotranscoding as trans")
				.append(" inner join fetch trans.videoResource as res")
				.append(" where trans.status <= -2")//error codes
				.append(" order by trans.creationDate asc, trans.id asc");
			return dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), VideoTranscoding.class)
					.getResultList();
	}
	
	/**
	 * Update transcoding status so TranscodingJob can find the resource.
	 *
	 * @param videoTranscoding
	 * @return updated videoTranscoding
	 */
	VideoTranscoding updateTranscodingStatus (VideoTranscoding videoTranscoding) {
		((VideoTranscodingImpl) videoTranscoding).setLastModified(new Date());
		videoTranscoding.setStatus(-1);
		VideoTranscoding trans = dbInstance.getCurrentEntityManager().merge(videoTranscoding);
		return trans;
	}


}
