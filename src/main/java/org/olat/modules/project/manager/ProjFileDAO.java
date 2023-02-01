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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjFileSearchParams;
import org.olat.modules.project.model.ProjFileImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 12 Dez 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class ProjFileDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ProjFile create(ProjArtefact artefact, VFSMetadata metadata) {
		ProjFileImpl file = new ProjFileImpl();
		file.setCreationDate(new Date());
		file.setLastModified(file.getCreationDate());
		file.setVfsMetadata(metadata);
		file.setArtefact(artefact);
		dbInstance.getCurrentEntityManager().persist(file);
		return file;
	}
	
	public ProjFile save(ProjFile file) {
		if (file instanceof ProjFileImpl) {
			ProjFileImpl impl = (ProjFileImpl)file;
			impl.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(file);
		}
		return file;
	}

	public long loadFilesCount(ProjFileSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(*)");
		sb.append("  from projfile file");
		sb.append("       inner join file.artefact artefact");
		sb.append("       inner join file.vfsMetadata metadata");
		QueryParams queryParams = new QueryParams();
		appendQuery(sb, searchParams, queryParams);
		appendOrderBy(searchParams, sb);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		addParameters(query, searchParams, queryParams);
		
		return query.getSingleResult().longValue();
	}

	public List<ProjFile> loadFiles(ProjFileSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select file");
		sb.append("  from projfile file");
		sb.append("       inner join fetch file.artefact artefact");
		sb.append("       inner join fetch artefact.project project");
		sb.append("       inner join fetch file.vfsMetadata metadata");
		QueryParams queryParams = new QueryParams();
		appendQuery(sb, searchParams, queryParams);
		appendOrderBy(searchParams, sb);
		
		TypedQuery<ProjFile> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjFile.class);
		addParameters(query, searchParams, queryParams);
		if (searchParams.getNumLastModified() != null) {
			query.setMaxResults(searchParams.getNumLastModified().intValue());
		}
		
		return query.getResultList();
	}

	private void appendQuery(QueryBuilder sb, ProjFileSearchParams searchParams, QueryParams queryParams) {
		if (searchParams.getProject() != null) {
			sb.and().append("file.artefact.project.key = :projectKey");
		}
		if (searchParams.getFileKeys() != null && !searchParams.getFileKeys().isEmpty()) {
			sb.and().append("file.key in :fileKeys");
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			sb.and().append("artefact.key in :artefactKeys");
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			sb.and().append("artefact.status in :status");
		}
		if (searchParams.getCreatorKeys() != null && !searchParams.getCreatorKeys().isEmpty()) {
			sb.and().append("metadata.fileInitializedBy.key in :creatorKeys");
		}
		if (searchParams.getSuffixes() != null && !searchParams.getSuffixes().isEmpty()) {
			Map<String, String> paramToSuffix = new HashMap<>(searchParams.getSuffixes().size());
			queryParams.setParamToSuffix(paramToSuffix);
			
			sb.and().append(" (");
			boolean or = false;
			for (int i = 0; i < searchParams.getSuffixes().size(); i++) {
				if (or) {
					sb.append(" or ");
				}
				or = true;
				
				String suffix = searchParams.getSuffixes().get(i);
				suffix = PersistenceHelper.makeStartFuzzyQueryString(suffix);
				
				String param = "suffix_" + i;
				PersistenceHelper.appendFuzzyLike(sb, "metadata.filename", param, dbInstance.getDbVendor());
				paramToSuffix.put(param, suffix);
			}
			sb.append(")");	
		}
		if (searchParams.getCreatedAfter() != null) {
			sb.and().append("metadata.creationDate >= :createdAfter");
		}
	}

	private void appendOrderBy(ProjFileSearchParams params, QueryBuilder sb) {
		if (params.getNumLastModified() != null) {
			sb.orderBy().append("metadata.fileLastModified").appendAsc(false);
			sb.orderBy().append("metadata.key").appendAsc(false);
		}
	}

	private void addParameters(TypedQuery<?> query, ProjFileSearchParams searchParams, QueryParams queryParams) {
		if (searchParams.getProject() != null) {
			query.setParameter("projectKey", searchParams.getProject().getKey());
		}
		if (searchParams.getFileKeys() != null && !searchParams.getFileKeys().isEmpty()) {
			query.setParameter("fileKeys", searchParams.getFileKeys());
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			query.setParameter("artefactKeys", searchParams.getArtefactKeys());
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			query.setParameter("status", searchParams.getStatus());
		}
		if (searchParams.getCreatorKeys() != null && !searchParams.getCreatorKeys().isEmpty()) {
			query.setParameter("creatorKeys", searchParams.getCreatorKeys());
		}
		Map<String,String> paramToSuffix = queryParams.getParamToSuffix();
		if (paramToSuffix != null && !paramToSuffix.isEmpty()) {
			paramToSuffix.entrySet().stream().forEach(entrySet -> query.setParameter(entrySet.getKey(), entrySet.getValue()));
		}
		if (searchParams.getCreatedAfter() != null) {
			query.setParameter("createdAfter", searchParams.getCreatedAfter());
		}
	}
	
	private final static class QueryParams {
		
		private Map<String, String> paramToSuffix;

		public Map<String, String> getParamToSuffix() {
			return paramToSuffix;
		}

		public void setParamToSuffix(Map<String, String> paramToSuffix) {
			this.paramToSuffix = paramToSuffix;
		}
		
	}

}
