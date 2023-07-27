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

import java.util.Date;
import java.util.List;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaLight;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaLog.Action;
import org.olat.modules.cemedia.model.MediaLogImpl;
import org.olat.modules.cemedia.model.SearchMediaLogParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 7 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MediaLogDAO {
	
	@Autowired
	private DB dbInstance;
	
	public MediaLog createLog(Action action, String tempIdentifier, Media media, Identity identity) {
		MediaLogImpl mediaLog = new MediaLogImpl();
		mediaLog.setCreationDate(new Date());
		mediaLog.setAction(action);
		mediaLog.setTempIdentifier(tempIdentifier);
		mediaLog.setMedia(media);
		mediaLog.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(mediaLog);
		return mediaLog;
	}
	
	public List<Identity> getDoers(MediaLight media) {
		String query = """
				select distinct ident from medialog as mlog
				inner join mlog.identity as ident
				inner join fetch ident.user as identUser
				where mlog.media.key=:mediaKey""";

		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Identity.class)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
	}
	
	public List<MediaLog> getLogs(MediaLight media, SearchMediaLogParameters params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select mlog from medialog as mlog")
		  .append(" inner join fetch mlog.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .and().append("mlog.media.key=:mediaKey");
		if(params.getDateRange() != null) {
			sb.and().append("mlog.creationDate>=:from and mlog.creationDate<=:to");
		}
		if(params.getIdentityKeys() != null && !params.getIdentityKeys().isEmpty()) {
			sb.and().append("mlog.identity.key in (:identityKeys)");
		}
		if(params.getActions() != null && !params.getActions().isEmpty()) {
			sb.and().append("mlog.action in (:actions)");
		}
		if(params.getTempIdentifier() != null) {
			sb.and().append("mlog.tempIdentifier in (:tempIdentifier)");
		}
		
		sb.append(" order by mlog.creationDate desc");

		TypedQuery<MediaLog> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), MediaLog.class)
				.setParameter("mediaKey", media.getKey());
		if(params.getDateRange() != null) {
			query.setParameter("from", params.getDateRange().getFrom(), TemporalType.TIMESTAMP);
			query.setParameter("to", params.getDateRange().getTo(), TemporalType.TIMESTAMP);
		}
		if(params.getIdentityKeys() != null && !params.getIdentityKeys().isEmpty()) {
			query.setParameter("identityKeys", params.getIdentityKeys());
		}
		if(params.getActions() != null && !params.getActions().isEmpty()) {
			query.setParameter("actions", params.getActions());
		}
		if(params.getTempIdentifier() != null) {
			query.setParameter("tempIdentifier", params.getTempIdentifier());
		}
		return query.getResultList();
	}
	
	public int deleteLogs(Media media) {
		String query = "delete from medialog mlog where mlog.media.key=:mediaKey";
		return dbInstance.getCurrentEntityManager()
			.createQuery(query)
			.setParameter("mediaKey", media.getKey())
			.executeUpdate();
	}

}
