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
package org.olat.core.commons.services.export.manager;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.export.model.CurriculumReportBlocParameters;
import org.olat.core.commons.services.export.model.ExportMetadataImpl;
import org.olat.core.commons.services.export.model.SearchExportMetadataParameters;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ExportMetadataDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ExportMetadata createMetadata(String title, String description,
			String filename, ArchiveType type, Date expirationDate, boolean onlyAdministrators,
			RepositoryEntry entry, OLATResource resource, String resSubPath, Identity creator, PersistentTask task) {
		ExportMetadataImpl metadata = new ExportMetadataImpl();
		metadata.setCreationDate(new Date());
		metadata.setLastModified(metadata.getCreationDate());
		metadata.setTitle(title);
		if(description != null && description.length() >= 4000) {
			description = FilterFactory.getHtmlTagsFilter().filter(description);
			description = Formatter.truncate(description, 3950);
		}
		metadata.setDescription(description);
		metadata.setFilename(filename);
		metadata.setArchiveType(type);
		metadata.setExpirationDate(expirationDate);
		metadata.setOnlyAdministrators(onlyAdministrators);
		metadata.setCreator(creator);
		metadata.setTask(task);
		metadata.setEntry(entry);
		metadata.setResource(resource);
		metadata.setSubIdent(resSubPath);
		metadata.setOrganisations(new HashSet<>());
		metadata.setCurriculums(new HashSet<>());
		metadata.setCurriculumElements(new HashSet<>());
		dbInstance.getCurrentEntityManager().persist(metadata);
		return metadata;
	}
	
	public ExportMetadata getMetadataByTask(PersistentTask task) {
		String query = """
				select exp from exportmetadata exp
				inner join fetch exp.task as task
				left join fetch exp.entry as v
				left join fetch v.olatResource as res
				left join fetch exp.creator creator
				left join fetch creator.user creatorUsr
				left join fetch exp.metadata vfsMetadata
				where task.key=:taskKey""";
		List<ExportMetadata> metadata = dbInstance.getCurrentEntityManager()
				.createQuery(query, ExportMetadata.class)
				.setParameter("taskKey", task.getKey())
				.getResultList();
		return metadata.isEmpty() ? null : metadata.get(0);
	}
	
	public ExportMetadata getMetadataByKey(Long key) {
		String query = """
				select exp from exportmetadata exp
				left join fetch exp.task as task
				left join fetch exp.entry as v
				left join fetch v.olatResource as res
				left join fetch exp.creator creator
				left join fetch creator.user creatorUsr
				left join fetch exp.metadata vfsMetadata
				where exp.key=:metadataKey""";
		List<ExportMetadata> metadata = dbInstance.getCurrentEntityManager()
				.createQuery(query, ExportMetadata.class)
				.setParameter("metadataKey", key)
				.getResultList();
		return metadata.isEmpty() ? null : metadata.get(0);
	}
	
	public List<ExportMetadata> expiredExports(Date date) {
		String query = """
				select exp from exportmetadata exp
				left join fetch exp.task as task
				left join fetch exp.entry as v
				left join fetch v.olatResource as res
				left join fetch exp.creator creator
				left join fetch creator.user creatorUsr
				left join fetch exp.metadata vfsMetadata
				where exp.expirationDate<:date""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, ExportMetadata.class)
				.setParameter("date", date, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public ExportMetadata updateMetadata(ExportMetadata metadata) {
		((ExportMetadataImpl)metadata).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(metadata);
	}
	
	public boolean metadataInUse(VFSMetadata metadata) {
		String query = """
				select exp.key from exportmetadata exp
				inner join exp.metadata vfsMetadata
				where vfsMetadata.key=:metadataKey""";
		
		List<Long> used = dbInstance.getCurrentEntityManager().createQuery(query, Long.class)
				.setParameter("metadataKey", metadata.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return used != null && !used.isEmpty() && used.get(0) != null && used.get(0).longValue() > 0l;
	}

	public void deleteMetadata(ExportMetadata metadata) {
		ExportMetadata metadataRef = dbInstance.getCurrentEntityManager()
				.getReference(ExportMetadataImpl.class, metadata.getKey());
		dbInstance.getCurrentEntityManager().remove(metadataRef);
	}
	
	public List<ExportMetadata> searchMetadatas(SearchExportMetadataParameters params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select exp from exportmetadata exp")
		  .append(" left join fetch exp.task as task")
		  .append(" left join fetch exp.entry as v")
		  .append(" left join fetch v.olatResource as res")
		  .append(" left join fetch exp.creator creator")
		  .append(" left join fetch creator.user creatorUsr")
		  .append(" left join fetch exp.metadata vfsMetadata")
		  .and().append("exp.archiveType").in(params.getArchiveTypesArray());
		
		if(params.hasRepositoryEntries()) {
			sb.and().append("v.key in :entryKeys");
		}
		if(StringHelper.containsNonWhitespace(params.getResSubPath())) {
			sb.and().append("exp.subIdent=:resSubPath");
		}
		if(params.isOngoingExport()) {
			sb.and().append("task.statusStr").in(TaskStatus.newTask, TaskStatus.inWork);
		}
		if(params.getOnlyAdministrators() != null) {
			sb.and().append("exp.onlyAdministrators=:onlyAdministrators");
		}
		
		if(params.getHasRepositoryEntryAdministrator() != null || params.getHasRepositoryEntryAuthor() != null) {
			sb.and().append("(exists (select relpart from repoentrytogroup as relpart, bgroupmember as repoOwner")
		      .append("   where relpart.entry.key=v.key and repoOwner.group.key=relpart.group.key")
		      .append("   and repoOwner.role='").append(GroupRoles.owner.name()).append("'")
		      .append("   and repoOwner.identity.key=:identityKey")
		      .append(" )");
			if(params.getHasRepositoryEntryAdministrator() != null) {
				sb.append(" or exp.creator.key=:identityKey");
				sb.append(")");
			} else if(params.getHasRepositoryEntryAuthor() != null) {
				sb.append(")");
				if(params.getOnlyAdministrators() == null) {
					sb.and().append("exp.onlyAdministrators=false");
				}
			}
		}
		
		if(params.getOrganisationRoles() != null && !params.getOrganisationRoles().isEmpty()
				&& params.getOrganisationIdentity() != null) {
			sb.and().append("(exists (select relOrgMember.key from exportmetadatatoorg as relToOrg")
			  .append("   inner join relToOrg.organisation relOrg")
			  .append("   inner join relOrg.group relOrgGroup")
			  .append("   inner join relOrgGroup.members relOrgMember")
			  .append("   where relToOrg.metadata.key=exp.key")
			  .append("   and relOrgMember.role ").in(params.getOrganisationRoles().toArray())
		      .append("   and relOrgMember.identity.key=:rolesIdentityKey")
			  .append(" )")
			  .append(" or exp.creator.key=:rolesIdentityKey")
			  .append(")");
		}
		
		if(params.getReportSubParameters() != null) {
			CurriculumReportBlocParameters subParams = params.getReportSubParameters();
			sb.and().append("(");
			
			boolean need = false;
			if(subParams.exclusiveCurriculums() != null && !subParams.exclusiveCurriculums().isEmpty()) {
				sb.append("(not exists (select relToNotCur.key from exportmetadatatocurriculum as relToNotCur")
				  .append("   where relToNotCur.metadata.key=exp.key and relToNotCur.curriculum.key not in (:exclusiveCurriculumKeys)")
				  .append(" )");
				sb.append(" and exists (select relToCur.key from exportmetadatatocurriculum as relToCur")
				  .append("   where relToCur.metadata.key=exp.key and relToCur.curriculum.key in (:exclusiveCurriculumKeys)")
				  .append(" ))");
				need = true;
			}
			
			if(subParams.exclusiveCurriculumElements() != null && !subParams.exclusiveCurriculumElements().isEmpty()) {
				if(need) {
					sb.append(" and ");
				}
				sb.append("(not exists (select relToNotCurEl.key from exportmetadatatocurriculumelement as relToNotCurEl")
				  .append("   where relToNotCurEl.metadata.key=exp.key and relToNotCurEl.curriculumElement.key not in (:exclusiveCurriculumElementKeys)")
				  .append(" )");
				sb.append(" and exists (select relToCurEl.key from exportmetadatatocurriculumelement as relToCurEl")
				  .append("   where relToCurEl.metadata.key=exp.key and relToCurEl.curriculumElement.key in (:exclusiveCurriculumElementKeys)")
				  .append(" ))");
				need = true;
			}
			
			if(subParams.identity() != null) {
				if(need) {
					sb.append(" or ");
				}
				sb.append("(exp.creator.key=:curriculumIdentityKey")
				  .append(" and not exists (select relToNotCurEl.key from exportmetadatatocurriculumelement as relToNotCurEl")
				  .append("   where relToNotCurEl.metadata.key=exp.key")
				  .append(" )")
				  .append(" and not exists (select relToNotCur.key from exportmetadatatocurriculum as relToNotCur")
				  .append("   where relToNotCur.metadata.key=exp.key")
				  .append(" ))");
			}
			
			sb.append(")");
		}

		if (params.getCreator() != null) {
			sb.and().append("creator.key=:creatorKey");
		}

		TypedQuery<ExportMetadata> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ExportMetadata.class);
		if(params.hasRepositoryEntries()) {
			List<Long> entryKeys = params.getRepositoryEntries().stream()
					.map(RepositoryEntryRef::getKey)
					.collect(Collectors.toList());
			query.setParameter("entryKeys", entryKeys);
		}
		if(StringHelper.containsNonWhitespace(params.getResSubPath())) {
			query.setParameter("resSubPath", params.getResSubPath());
		}
		if(params.getOnlyAdministrators() != null) {
			query.setParameter("onlyAdministrators", params.getOnlyAdministrators());
		}
		if(params.getHasRepositoryEntryAdministrator() != null ) {
			query.setParameter("identityKey", params.getHasRepositoryEntryAdministrator().getKey());
		} else if(params.getHasRepositoryEntryAuthor() != null) {
			query.setParameter("identityKey", params.getHasRepositoryEntryAuthor().getKey());
		}
		
		if(params.getOrganisationRoles() != null && !params.getOrganisationRoles().isEmpty()
				&& params.getOrganisationIdentity() != null) {
			query.setParameter("rolesIdentityKey", params.getOrganisationIdentity().getKey());
		}
		
		if(params.getReportSubParameters() != null) {
			CurriculumReportBlocParameters subParams = params.getReportSubParameters();
			if(subParams.exclusiveCurriculums() != null && !subParams.exclusiveCurriculums().isEmpty()) {
				List<Long> exclusiveCurriculumKeys = subParams.exclusiveCurriculums().stream()
						.map(Curriculum::getKey)
						.toList();
				query.setParameter("exclusiveCurriculumKeys", exclusiveCurriculumKeys);
			}
			if(subParams.exclusiveCurriculumElements() != null && !subParams.exclusiveCurriculumElements().isEmpty()) {
				List<Long> exclusiveCurriculumKeys = subParams.exclusiveCurriculumElements().stream()
						.map(CurriculumElement::getKey)
						.toList();
				query.setParameter("exclusiveCurriculumElementKeys", exclusiveCurriculumKeys);
			}
			if(subParams.identity() != null) {
				query.setParameter("curriculumIdentityKey", subParams.identity().getKey());
			}
		}
		
		if (params.getCreator() != null) {
			query.setParameter("creatorKey", params.getCreator().getKey());
		}

		return query.getResultList();
	}
}
