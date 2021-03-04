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
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.image.Size;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.VideoMetadataSearchParams;
import org.olat.modules.video.model.VideoMetaImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * DAO implementation for manipulating VideoMetadata objects
 * 
 * Initial date: January 2017<br>
 * 
 * @author fkiefer, fabian.kiefer@frentix.com, http://www.frentix.com
 *
 */
@Service("videoMetadataDao")
public class VideoMetadataDAO {

	@Autowired
	private DB dbInstance;
	@Autowired 
	private VideoManager videoManager;

	
	/**
	 * Gets the video meta data. 
	 *
	 * @param videoResource the OLATResource
	 * @return the videometadata or null
	 */
	VideoMetaImpl getVideoMetadata(OLATResource videoResource) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select meta from videometadata as meta")
		  .append(" inner join fetch meta.videoResource as vResource")
		  .append(" where vResource.key=:resourceKey");
		List<VideoMetaImpl> metadata = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(),VideoMetaImpl.class)
				.setParameter("resourceKey", videoResource.getKey())
				.getResultList();
		return metadata.isEmpty() ? null :  metadata.get(0);
	}
	
	VideoMeta updateVideoMetadata(VideoMeta videoMetadata) {
		((VideoMetaImpl)videoMetadata).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(videoMetadata);
	}
	
	List<VideoMeta> getVideoMetadata(VideoMetadataSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meta from videometadata as meta");
		sb.append(" inner join fetch meta.videoResource as vResource");
		if (searchParams.getUrlNull() != null) {
			sb.and().append("meta.url is ").append("not ", !searchParams.getUrlNull().booleanValue()).append("null");
		}
		if (searchParams.getMinHeight() != null) {
			sb.and().append("meta.height >= :minHeight");
		}
		
		TypedQuery<VideoMeta> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(),VideoMeta.class);
		if (searchParams.getMinHeight() != null) {
			query.setParameter("minHeight", searchParams.getMinHeight());
		}
		
		return query.getResultList();
	}
	
	/** 
	 * Gets the all video repo entries by type.
	 *
	 * @param typename
	 * @return all video repo entries
	 */
	List<RepositoryEntry> getAllVideoRepoEntries (String typename) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" v ")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" where ores.resName = :type");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("type",typename)
				.getResultList();
	}
	
	/**
	 * Delete all video metadata objects for a given video resource
	 *
	 * @param videoResource
	 * @return the number of entities updated or deleted
	 */
	int deleteVideoMetadata(OLATResource videoResource) {
		String deleteQuery = "delete from videometadata where fk_resource_id=:resourceKey";
		return dbInstance.getCurrentEntityManager().createQuery(deleteQuery)
				.setParameter("resourceKey", videoResource.getKey()).executeUpdate();
	}
	
	/**
	 * Creates and persists the video metadata.
	 *
	 * @param videoResource
	 * @param size
	 * @param filename
	 * @return metadata
	 */
	VideoMetaImpl createVideoMetadata(RepositoryEntry repoEntry, long size, String url, VideoFormat format) {
		VideoMetaImpl videometa = new VideoMetaImpl();
		OLATResource videoResource = repoEntry.getOlatResource();
		videometa.setVideoResource(videoResource);
		videometa.setVideoFormat(format);
		videometa.setUrl(url);
		videometa.setCreationDate(new Date());
		videometa.setLastModified(videometa.getCreationDate());		
		Size resolution = videoManager.getVideoResolutionFromOLATResource(videoResource);
		videometa.setHeight(resolution.getHeight());
		videometa.setWidth(resolution.getWidth());
		videometa.setSize(size);
		videometa.setLength(repoEntry.getExpenditureOfWork());
		dbInstance.getCurrentEntityManager().persist(videometa);
		return videometa;
	}
	
	VideoMetaImpl copyVideoMetadata(RepositoryEntry repoEntry, VideoMeta sourceMeta) {
		VideoMetaImpl videometa = new VideoMetaImpl();
		videometa.setVideoResource(repoEntry.getOlatResource());
		videometa.setVideoFormat(sourceMeta.getVideoFormat());
		videometa.setUrl(sourceMeta.getUrl());
		videometa.setCreationDate(new Date());
		videometa.setLastModified(videometa.getCreationDate());		
		videometa.setHeight(sourceMeta.getHeight());
		videometa.setWidth(sourceMeta.getWidth());
		videometa.setSize(sourceMeta.getSize());
		videometa.setLength(sourceMeta.getLength());
		dbInstance.getCurrentEntityManager().persist(videometa);
		return videometa;
	}

}
