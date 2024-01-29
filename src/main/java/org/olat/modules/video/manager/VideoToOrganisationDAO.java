/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.video.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.video.VideoToOrganisation;
import org.olat.modules.video.model.VideoToOrganisationImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 29 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VideoToOrganisationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public VideoToOrganisation createVideoToOrganisation(RepositoryEntry entry, Organisation organisation) {
		VideoToOrganisationImpl rel = new VideoToOrganisationImpl();
		rel.setCreationDate(new Date());
		rel.setRepositoryEntry(entry);
		rel.setOrganisation(organisation);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	public List<VideoToOrganisation> getVideoToOrganisation(RepositoryEntryRef entry) {
		String query = """
				select rel from videotoorganisation as rel
				inner join fetch organisation as org
				where rel.repositoryEntry.key=:repositoryEntryKey
				""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, VideoToOrganisation.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.getResultList();
	}
	
	public List<Organisation> getOrganisations(RepositoryEntryRef entry) {
		String query = """
				select org from videotoorganisation as rel
				inner join organisation as org
				where rel.repositoryEntry.key=:repositoryEntryKey
				""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, Organisation.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.getResultList();
	}
	
	public void deleteVideoToOrganisation(VideoToOrganisation videoToOrganisation) {
		dbInstance.getCurrentEntityManager().remove(videoToOrganisation);;
	}

}
