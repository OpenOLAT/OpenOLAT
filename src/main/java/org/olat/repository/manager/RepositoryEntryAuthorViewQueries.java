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
package org.olat.repository.manager;

import java.util.Collections;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntryAuthorView;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Queries for the view "RepositoryEntryMyCourseView" dedicated to the "My course" feature.
 * The identity is a mandatory parameter.
 * 
 * 
 * Initial date: 12.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryAuthorViewQueries {
	
	private static final OLog log = Tracing.createLoggerFor(RepositoryEntryAuthorViewQueries.class);
	
	@Autowired
	private DB dbInstance;
	
	public int countViews(SearchAuthorRepositoryEntryViewParams params) {
		if(params.getIdentity() == null) {
			log.error("No identity defined for query");
			return 0;
		}
		
		TypedQuery<Number> query = createViewQuery(params, Number.class);
		Number count = query.getSingleResult();
		return count == null ? 0 : count.intValue();
	}
	
	public RepositoryEntryAuthorView loadView(IdentityRef identity, RepositoryEntryRef ref) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from repositoryentryauthor as v ")
		  .append(" inner join v.olatResource as res")
		  .append(" left join v.lifecycle as lifecycle")
		  .append(" where v.key=:repoEntryKey and v.identityKey=:identityKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryAuthorView.class)
				.setParameter("repoEntryKey", ref.getKey())
				.setParameter("identityKey", identity.getKey())
				.getSingleResult();
	}

	public List<RepositoryEntryAuthorView> searchViews(SearchAuthorRepositoryEntryViewParams params, int firstResult, int maxResults) {
		if(params.getIdentity() == null) {
			log.error("No identity defined for query");
			return Collections.emptyList();
		}

		TypedQuery<RepositoryEntryAuthorView> query = createViewQuery(params, RepositoryEntryAuthorView.class);
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}

	protected <T> TypedQuery<T> createViewQuery(SearchAuthorRepositoryEntryViewParams params,
			Class<T> type) {

		Identity identity = params.getIdentity();
		List<String> resourceTypes = params.getResourceTypes();

		boolean count = Number.class.equals(type);
		StringBuilder sb = new StringBuilder();
		if(count) {
			sb.append("select count(v.key) from repositoryentryauthor as v ")
			  .append(" inner join v.olatResource as res")
			  .append(" left join v.lifecycle as lifecycle");
		} else {
			sb.append("select v from repositoryentryauthor as v ")
			  .append(" inner join fetch v.olatResource as res")
			  .append(" left join fetch v.lifecycle as lifecycle");
		}

		sb.append(" where v.identityKey=:identityKey ");
		if(params.getRepoEntryKeys() != null && params.getRepoEntryKeys().size() > 0) {
			sb.append(" and v.key=:repoEntryKeys ");
		}

		if (params.isResourceTypesDefined()) {
			sb.append(" and res.resName in (:resourcetypes)");
		}
		if(params.getMarked() != null) {
			sb.append(" and v.markKey ").append(params.getMarked().booleanValue() ? " is not null " : " is null ");
		}
		
		String author = params.getAuthor();
		if (StringHelper.containsNonWhitespace(author)) { // fuzzy author search
			author = PersistenceHelper.makeFuzzyQueryString(author);

			sb.append(" and exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership, ")
			     .append(IdentityImpl.class.getName()).append(" as identity, ").append(UserImpl.class.getName()).append(" as user")
		         .append("    where rel.entry=v and rel.group=baseGroup and membership.group=baseGroup and membership.identity=identity and identity.user=user")
		         .append("      and membership.role='").append(GroupRoles.owner.name()).append("'")
		         .append("      and (");
			PersistenceHelper.appendFuzzyLike(sb, "user.userProperties['firstName']", "author", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "user.userProperties['lastName']", "author", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "identity.name", "author", dbInstance.getDbVendor());
			sb.append(" ))");
		}

		String displayname = params.getDisplayname();
		if (StringHelper.containsNonWhitespace(displayname)) {
			//displayName = '%' + displayName.replace('*', '%') + '%';
			//query.append(" and v.displayname like :displayname");
			displayname = PersistenceHelper.makeFuzzyQueryString(displayname);
			sb.append(" and ");
			PersistenceHelper.appendFuzzyLike(sb, "v.displayname", "displayname", dbInstance.getDbVendor());
		}
		
		String desc = params.getDescription();
		if (StringHelper.containsNonWhitespace(desc)) {
			//desc = '%' + desc.replace('*', '%') + '%';
			//query.append(" and v.description like :desc");
			desc = PersistenceHelper.makeFuzzyQueryString(desc);
			sb.append(" and ");
			PersistenceHelper.appendFuzzyLike(sb, "v.description", "desc", dbInstance.getDbVendor());
		}
		
		Long id = null;
		String refs = null;
		if(StringHelper.containsNonWhitespace(params.getIdAndRefs())) {
			refs = params.getIdAndRefs();
			if(StringHelper.isLong(refs)) {
				try {
					id = Long.parseLong(refs);
				} catch (NumberFormatException e) {
					//
				}
			}
			sb.append(" and (v.externalId=:ref or v.externalRef=:ref or v.softkey=:ref");
			if(id != null) {
				sb.append(" or v.key=:vKey)");
			}
			sb.append(")");	
		}
		
		TypedQuery<T> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), type);
		if(params.getRepoEntryKeys() != null && params.getRepoEntryKeys().size() > 0) {
			dbQuery.setParameter("repoEntryKeys", params.getRepoEntryKeys());
		}
		if (params.isResourceTypesDefined()) {
			dbQuery.setParameter("resourcetypes", resourceTypes);
		}
		if(id != null) {
			dbQuery.setParameter("vKey", id);
		}
		if(refs != null) {
			dbQuery.setParameter("ref", refs);
		}
		if (StringHelper.containsNonWhitespace(author)) { // fuzzy author search
			dbQuery.setParameter("author", author);
		}
		if (StringHelper.containsNonWhitespace(displayname)) {
			dbQuery.setParameter("displayname", displayname);
		}
		if (StringHelper.containsNonWhitespace(desc)) {
			dbQuery.setParameter("desc", desc);
		}

		dbQuery.setParameter("identityKey", identity.getKey());
		return dbQuery;
	}
}
