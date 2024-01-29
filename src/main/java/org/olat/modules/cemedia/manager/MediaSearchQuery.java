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

import static org.olat.core.commons.persistence.PersistenceHelper.appendFuzzyLike;
import static org.olat.core.commons.persistence.PersistenceHelper.makeFuzzyQueryString;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.util.StringHelper;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.cemedia.model.SearchMediaParameters;
import org.olat.modules.cemedia.model.SearchMediaParameters.Access;
import org.olat.modules.cemedia.model.SearchMediaParameters.Scope;
import org.olat.modules.cemedia.model.SearchMediaParameters.UsedIn;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MediaSearchQuery {
	
	@Autowired
	private DB dbInstance;

	public List<MediaWithVersion> searchBy(SearchMediaParameters parameters) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select media, mversion, metadata,")
		  .append(" (select count(cVersion.key) from mediaversion as cVersion")
		  .append("  where cVersion.media.key=media.key")
		  .append(" ) as numOfVersions")
		  .append(" from mmedia as media")
		  .append(" left join fetch media.author as author")
		  .append(" left join fetch mediaversion as mversion on (mversion.media.key=media.key and mversion.pos=0)")
		  .append(" left join fetch mversion.metadata as metadata")
		  .append(" left join fetch mversion.versionMetadata as mvmetadata");
		
		Long identityKey = null;
		Long repositoryEntryKey = null;
		
		if(parameters.getScope() == Scope.SHARED_WITH_ENTRY) {
			repositoryEntryKey = parameters.getRepositoryEntry().getKey();
			sb.and().append(" exists (select baseEntryRel.key from mediatogroup as baseEntryRel")
			  .append("  where baseEntryRel.media.key=media.key and baseEntryRel.repositoryEntry.key=:entryKey")
			  .append(")");
		} else if(parameters.getIdentity() != null) {
			identityKey = parameters.getIdentity().getKey();
			if(parameters.getScope() == Scope.MY || parameters.getScope() == null) {
				sb.and().append("author.key=:identityKey");
			} else {
				sb.and().append("(");
				appendSharedAll(sb, parameters);
				sb.append(")");
			}
		}
		
		if(parameters.getUsedIn() != null && !parameters.getUsedIn().isEmpty()) {
			appendUsedIn(sb, parameters);
		}
		
		if(parameters.getSharedWith() != null && !parameters.getSharedWith().isEmpty()) {
			sb.and().append(" exists (select shareWithRel.key from mediatogroup as shareWithRel")
			  .append("  where shareWithRel.media.key=media.key and shareWithRel.type in (:sharedWith)")
			  .append(")");
		}

		String searchString = parameters.getSearchString();
		if(StringHelper.containsNonWhitespace(searchString)) {
			searchString = makeFuzzyQueryString(searchString);
			sb.and().append("(");
			appendFuzzyLike(sb, "media.title", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "media.description", "searchString", dbInstance.getDbVendor());
			sb.append(")");
		}
		
		List<Long> tagKeys = parameters.getTags();
		if(tagKeys != null && !tagKeys.isEmpty()) {
			sb.and()
			  .append(" exists (select rel.key from mediatag as rel")
			  .append("  where rel.tag.key in (:tagKeys) and rel.media.key=media.key")
			  .append(" )");
		}
		
		List<String> types = parameters.getTypes();
		if(types != null && !types.isEmpty()) {
			sb.and().append("media.type in (:types)");
		}
		
		String checksum = parameters.getChecksum();
		if(StringHelper.containsNonWhitespace(checksum)) {
			sb.and().append("mversion.versionChecksum=:checksum");
		}
		
		List<TaxonomyLevelRef> taxonomyLevels = parameters.getTaxonomyLevelsRefs();
		if(taxonomyLevels != null && !taxonomyLevels.isEmpty()) {
			sb.and()
			  .append("exists (select taxRel.key from mediatotaxonomylevel as taxRel")
			  .append("  inner join taxRel.taxonomyLevel as level")
			  .append("  where level.key in (:levelKeys) and taxRel.media.key=media.key")
			  .append(" )");
		}

		String source = parameters.getSource();
		if (StringHelper.containsNonWhitespace(source)) {
			source = PersistenceHelper.makeFuzzyQueryString(source);
			sb.and();
			PersistenceHelper.appendFuzzyLike(sb, "media.source", "source", dbInstance.getDbVendor());
		}

		List<String> platforms = parameters.getPlatforms();
		if (platforms != null && !platforms.isEmpty()) {
			sb
					.and()
					.append("(")
					.append("(mvmetadata is not null and mvmetadata.format in (:platforms))");
			sb.append(")");
		}

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(identityKey != null) {
			query.setParameter("identityKey", identityKey);
		}
		if(StringHelper.containsNonWhitespace(searchString)) {
			query.setParameter("searchString", searchString.toLowerCase());
		}
		if(tagKeys != null && !tagKeys.isEmpty()) {
			query.setParameter("tagKeys", tagKeys);
		}
		if(types != null && !types.isEmpty()) {
			query.setParameter("types", types);
		}
		if(StringHelper.containsNonWhitespace(checksum)) {
			query.setParameter("checksum", checksum);
		}
		if(taxonomyLevels != null && !taxonomyLevels.isEmpty()) {
			List<Long> levelKeys = taxonomyLevels.stream()
					.map(TaxonomyLevelRef::getKey).toList();
			query.setParameter("levelKeys", levelKeys);
		}
		if(repositoryEntryKey != null) {
			query.setParameter("entryKey", repositoryEntryKey);
		}
		if(parameters.getSharedWith() != null && !parameters.getSharedWith().isEmpty()) {
			query.setParameter("sharedWith", parameters.getSharedWith());
		}
		if (StringHelper.containsNonWhitespace(source)) {
			query.setParameter("source", source);
		}
		if(platforms != null && !platforms.isEmpty()) {
			query.setParameter("platforms", platforms);
		}
		
		List<Object[]> objects = query.getResultList();
		List<MediaWithVersion> medias = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Media media = (Media)object[0];
			MediaVersion mediaVersion = (MediaVersion)object[1];
			VFSMetadata metadata = (VFSMetadata)object[2];
			long numOfVersions = PersistenceHelper.extractPrimitiveLong(object, 3);
			medias.add(new MediaWithVersion(media, mediaVersion, metadata, numOfVersions));
		}
		return medias;
	}
	
	private void appendSharedAll(QueryBuilder sb, SearchMediaParameters parameters) {
		if(parameters.getScope() == Scope.SHARED_WITH_ME || parameters.getScope() == Scope.ALL) {
			if(parameters.getScope() == Scope.SHARED_WITH_ME || parameters.getAccess() == Access.INDIRECT) {
				sb.append("author.key<>:identityKey and");
			} else {
				sb.append("author.key=:identityKey or");
			}

			sb.append(" (");
			
			if(parameters.getAccess() == null || parameters.getAccess() == Access.DIRECT) {
				sb.append(" exists (select shareRel.key from mediatogroup as shareRel")
				  .append("  inner join shareRel.group as uGroup")
				  .append("  inner join uGroup.members as uMember")
				  .append("  where shareRel.media.key=media.key and uMember.identity.key=:identityKey")
				  .append("  and (shareRel.type").in(MediaToGroupRelationType.USER)
				  .append("   or shareRel.type").in(MediaToGroupRelationType.BUSINESS_GROUP)
				  .append("   or (shareRel.type").in(MediaToGroupRelationType.ORGANISATION).append(" and uMember.role").in(OrganisationRoles.author, OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator).append(")")
				  .append(")) or ");
			}

			Object[] roles;
			if(parameters.getAccess() == Access.INDIRECT) {
				roles = new OrganisationRoles[]{ OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator };
			} else {
				roles = new GroupRoles[]{ GroupRoles.owner };
			}
			sb.append(" exists (select shareReRel.key from mediatogroup as shareReRel")
			  .append("  inner join shareReRel.repositoryEntry as v")
			  .append("  inner join v.groups as relGroup")
			  .append("  inner join relGroup.group as vBaseGroup")
	          .append("  inner join vBaseGroup.members as vMembership")
			  .append("  where shareReRel.media.key=media.key and shareReRel.type").in(MediaToGroupRelationType.REPOSITORY_ENTRY)
			  .append("    and vMembership.identity.key=:identityKey and vMembership.role").in(roles)
			  .append(")");

			sb.append(")");
		} else if(parameters.getScope() == Scope.SHARED_BY_ME) {
			sb.append("author.key=:identityKey and");
			sb.append(" exists (select shareRel.key from mediatogroup as shareRel")
			  .append("  inner join shareRel.group as uGroup")
			  .append("  where shareRel.media.key=media.key")
			  .append(")");
		}
	}
	
	private void appendUsedIn(QueryBuilder sb, SearchMediaParameters parameters) {
		if(parameters.getUsedIn().contains(UsedIn.PAGE) && parameters.getUsedIn().contains(UsedIn.PORTFOLIO) && parameters.getUsedIn().contains(UsedIn.NOT_USED)) {
			// mean all -> no filter
		} else if(parameters.getUsedIn().contains(UsedIn.PAGE) && parameters.getUsedIn().contains(UsedIn.PORTFOLIO)) {
			sb.and().append(" exists (select mediaPart.key from cemediapart mediaPart")
			  .append("  where mediaPart.media.key=media.key")
			  .append(" )");
		} else if(parameters.getUsedIn().contains(UsedIn.PAGE)) {
			sb.and().append(" exists (select 1 from cemediapart as pageRefMediaPart")
			  .append(" inner join cepagebody as pageRefBody on (pageRefBody.key=pageRefMediaPart.body.key)")
			  .append(" inner join cepage as pageRef on (pageRef.body.key=pageRefBody.key)")
			  .append(" inner join cepagereference ref on (ref.page.key=pageRef.key)")
			  .append(" where pageRefMediaPart.media.key=media.key")
			  .append(")");
		} else if(parameters.getUsedIn().contains(UsedIn.PORTFOLIO)) {
			sb.and().append(" exists (select 1 from cemediapart as portfolioRefMediaPart")
			  .append(" inner join cepagebody as portfolioRefBody on (portfolioRefBody.key=portfolioRefMediaPart.body.key)")
			  .append(" inner join cepage as pagePortfolioRef on (pagePortfolioRef.body.key=portfolioRefBody.key)")
			  .append(" where portfolioRefMediaPart.media.key=media.key and pagePortfolioRef.key not in (")
			  .append("   select pageRef.page.key from cepagereference pageRef where pageRef.page.key=pagePortfolioRef.key")
			  .append(" ))");
		} else if(parameters.getUsedIn().contains(UsedIn.NOT_USED)) {
			sb.and().append(" not exists(select mediaPart.key from cemediapart mediaPart")
			  .append("  where mediaPart.media.key=media.key")
			  .append(" )");
		}
	}
}
