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

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaLight;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaLog.Action;
import org.olat.modules.cemedia.model.MediaLogImpl;
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
	
	public MediaLog createLog(Action action, Media media, Identity identity) {
		MediaLogImpl mediaLog = new MediaLogImpl();
		mediaLog.setCreationDate(new Date());
		mediaLog.setAction(action);
		mediaLog.setMedia(media);
		mediaLog.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(mediaLog);
		return mediaLog;
	}
	
	public List<MediaLog> getLogs(MediaLight media) {
		String query = """
			select mlog from medialog as mlog
			 inner join fetch mlog.identity as ident
			 where mlog.media.key=:mediaKey
			 order by mlog.creationDate desc
			""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, MediaLog.class)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
	}
	
	public int deleteLogs(Media media) {
		String query = "delete from medialog mlog where mlog.media.key=:mediaKey";
		return dbInstance.getCurrentEntityManager()
			.createQuery(query)
			.setParameter("mediaKey", media.getKey())
			.executeUpdate();
	}

}
