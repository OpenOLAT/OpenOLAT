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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.OrganisationRef;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.modules.video.model.SearchVideoInCollectionParams;
import org.olat.modules.video.model.SearchVideoInCollectionParams.OrderBy;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 29 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VideoCollectionQuery {

	private static final Logger log = Tracing.createLoggerFor(VideoCollectionQuery.class);
	
	@Autowired
	private DB dbInstance;
	
	public int countVideos(SearchVideoInCollectionParams params) {
		if(params.getIdentity() == null) {
			log.error("No identity defined for query");
			return 0;
		}
		
		TypedQuery<Number> query = createMyViewQuery(params, Number.class);
		if(query == null) {
			return 0;
		}
		Number count = query
				.setFlushMode(FlushModeType.COMMIT)
				.getSingleResult();
		return count == null ? 0 : count.intValue();
	}
	
	public List<RepositoryEntry> searchVideos(SearchVideoInCollectionParams params, int firstResult, int maxResults) {
		if(params.getIdentity() == null) {
			log.error("No identity defined for query");
			return Collections.emptyList();
		}

		TypedQuery<RepositoryEntry> query = createMyViewQuery(params, RepositoryEntry.class);
		if(query == null) {
			return new ArrayList<>();
		}
		query.setFlushMode(FlushModeType.COMMIT)
		     .setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		return query.getResultList();
	}
	
	private final <T> TypedQuery<T> createMyViewQuery(SearchVideoInCollectionParams params, Class<T> type) {

		boolean count = Number.class.equals(type);
		boolean oracle = "oracle".equals(dbInstance.getDbVendor());
		QueryBuilder sb = new QueryBuilder(2048);
		
		if(count) {
			sb.append("select count(v.key) ")
			  .append(" from repositoryentry as v")
			  .append(" inner join v.olatResource as res")
			  .append(" left join v.lifecycle as lifecycle ");
		} else {
			sb.append("select v");
			sb.append(" from repositoryentry as v")
			  .append(" inner join ").append(oracle ? "" : "fetch").append(" v.olatResource as res")
			  .append(" inner join fetch v.statistics as stats");
		}
		if(params.hasOrganisations()) {
			sb.append(" inner join videotoorganisation as videoToOrganisation on (videoToOrganisation.repositoryEntry.key=v.key)");
		}
		
		sb.and().append("v.videoCollection=true")
		  .and().append("res.resName=:resourceType")
		  .and().append("v.status").in(RepositoryEntryStatusEnum.published);
		if(params.hasOrganisations()) {
			sb.and().append("videoToOrganisation.organisation.key in (:organisationKeys)");
		}	

		String text = params.getText();
		if (StringHelper.containsNonWhitespace(text)) {
			text = PersistenceHelper.makeFuzzyQueryString(text);
			sb.append(" and (");
			PersistenceHelper.appendFuzzyLike(sb, "v.displayname", "displaytext", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.description", "displaytext", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.objectives", "displaytext", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.authors", "displaytext", dbInstance.getDbVendor());
			sb.append(")");
		}
		
		appendOrderBy(params, sb);
		
		TypedQuery<T> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), type)
				.setParameter("resourceType", VideoFileResource.TYPE_NAME);
		
		if(params.hasOrganisations()) {
			List<Long> organisationKeys = params.getOrganisations().stream()
					.map(OrganisationRef::getKey)
					.collect(Collectors.toList());
			dbQuery.setParameter("organisationKeys", organisationKeys);
		}
		
		if(StringHelper.containsNonWhitespace(text)) {
			dbQuery.setParameter("displaytext", text);
		}
		
		return dbQuery;
	}
	
	private void appendOrderBy(SearchVideoInCollectionParams params, QueryBuilder sb) {
		OrderBy orderBy = params.getOrderBy();
		boolean asc = params.isOrderByAsc();
		
		if(orderBy != null) {
			switch(orderBy) {
				case automatic://! the sorting is reverse
					if(asc) {
						sb.append(" order by recentLaunch desc nulls last, lifecycle.validFrom desc nulls last, marks desc nulls last, lower(v.displayname) asc, v.key asc ");
					} else {
						sb.append(" order by recentLaunch asc nulls last, lifecycle.validFrom asc nulls last, marks asc nulls last, lower(v.displayname) desc, v.key desc ");
					}
					break;

				case title:
					//life cycle always sorted from the newer to the older.
					if(asc) {
						sb.append(" order by lower(v.displayname) asc, lifecycle.validFrom desc nulls last, lower(v.externalRef) asc nulls last, v.key asc");
					} else {
						sb.append(" order by lower(v.displayname) desc, lifecycle.validFrom desc nulls last, lower(v.externalRef) desc nulls last, v.key desc");
					}
					break;
				case author:
					sb.append(" order by lower(v.authors)");
					appendAsc(sb, asc).append(" nulls last, v.key asc");
					break;

				case creationDate:
					sb.append(" order by v.creationDate ");
					appendAsc(sb, asc).append(", lower(v.displayname) asc, v.key asc");
					break;
				case launchCounter:
					sb.append(" order by v.statistics.launchCounter ");
					if(asc) {
						sb.append(" asc nulls first");
					} else {
						sb.append(" desc nulls last");
					}
					sb.append(", lower(v.displayname) asc, v.key asc");
					break;
				case key:
					sb.append(" order by v.key");
					appendAsc(sb, asc);
					break;
				default:
					if(asc) {
						sb.append(" order by lower(v.displayname) asc, lifecycle.validFrom desc nulls last, lower(v.externalRef) asc nulls last, v.key asc");
					} else {
						sb.append(" order by lower(v.displayname) desc, lifecycle.validFrom desc nulls last, lower(v.externalRef) desc nulls last, v.key asc");
					}
					break;
			}
		}
	}
	
	private final QueryBuilder appendAsc(QueryBuilder sb, boolean asc) {
		if(asc) {
			sb.append(" asc");
		} else {
			sb.append(" desc");
		}
		return sb;
	}
}
