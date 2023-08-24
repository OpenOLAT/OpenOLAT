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
package org.olat.modules.project.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactRef;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectRef;
import org.olat.modules.project.ProjTag;
import org.olat.modules.project.ProjTagSearchParams;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.model.ProjTagImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 14 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class ProjTagDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ProjTag create(ProjProject project, ProjArtefact artefact, Tag tag) {
		ProjTagImpl projTag = new ProjTagImpl();
		projTag.setCreationDate(new Date());
		projTag.setProject(project);
		projTag.setArtefact(artefact);
		projTag.setTag(tag);
		dbInstance.getCurrentEntityManager().persist(projTag);
		return projTag;
	}

	public void delete(ProjTag projTag) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from projtag projTag");
		sb.and().append("projTag.key = :key");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("key", projTag.getKey())
				.executeUpdate();
	}
	
	public void delete(ProjArtefact artefact) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from projtag projTag");
		sb.and().append("projTag.artefact.key = :artefactKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("artefactKey", artefact.getKey())
				.executeUpdate();
	}
	
	public List<TagInfo> loadProjectTagInfos(ProjProjectRef project, ProjArtefactRef selectionArtefact) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.core.commons.services.tag.model.TagInfoImpl(");
		sb.append("       tag.key");
		sb.append("     , min(tag.creationDate)");
		sb.append("     , min(tag.displayName)");
		sb.append("     , count(projTag.artefact.key)");
		if (selectionArtefact != null) {
			sb.append("     , sum(case when (projTag.artefact.key=").append(selectionArtefact.getKey()).append(") then 1 else 0 end) as selected");
		} else {
			sb.append(" , cast(0 as long) as selected");
		}
		sb.append(")");
		sb.append("  from projtag projTag");
		sb.append("       inner join projTag.tag tag");
		sb.and().append("projTag.artefact.status = '").append(ProjectStatus.active.name()).append("'");
		sb.and().append("projTag.project.key = :projectKey");
		sb.groupBy().append("tag.key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TagInfo.class)
				.setParameter("projectKey", project.getKey())
				.getResultList();
	}
	
	public List<ProjTag> loadTags(ProjTagSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select projTag");
		sb.append("  from projtag projTag");
		sb.append("       inner join fetch projTag.tag tag");
		sb.append("       left join fetch projTag.artefact artefact");
		
		appendQuery(sb, searchParams);
		
		TypedQuery<ProjTag> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjTag.class);
		addParameters(query, searchParams);
		
		return query.getResultList();
	}

	private void appendQuery(QueryBuilder sb, ProjTagSearchParams searchParams) {
		if (searchParams.getProjectKey() != null) {
			sb.and().append("projTag.project.key = :projectKey");
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			sb.and().append("projTag.artefact.key in :artefactKeys");
		}
		if (searchParams.getArtefactTypes() != null && !searchParams.getArtefactTypes().isEmpty()) {
			sb.and().append("artefact.type in :types");
		}
		if (searchParams.getArtefactStatus() != null && !searchParams.getArtefactStatus().isEmpty()) {
			sb.and().append("artefact.status in :status");
		}
	}

	private void addParameters(TypedQuery<?> query, ProjTagSearchParams searchParams) {
		if (searchParams.getProjectKey() != null) {
			query.setParameter("projectKey", searchParams.getProjectKey());
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			query.setParameter("artefactKeys", searchParams.getArtefactKeys());
		}
		if (searchParams.getArtefactTypes() != null && !searchParams.getArtefactTypes().isEmpty()) {
			query.setParameter("types", searchParams.getArtefactTypes());
		}
		if (searchParams.getArtefactStatus() != null && !searchParams.getArtefactStatus().isEmpty()) {
			query.setParameter("status", searchParams.getArtefactStatus());
		}
	}

}
