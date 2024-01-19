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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.FileUtils.Usage;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaLight;
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.MediaVersionMetadata;
import org.olat.modules.cemedia.model.MediaIdentityNames;
import org.olat.modules.cemedia.model.MediaImpl;
import org.olat.modules.cemedia.model.MediaUsage;
import org.olat.modules.cemedia.model.MediaUsageWithStatus;
import org.olat.modules.cemedia.model.MediaVersionImpl;
import org.olat.modules.cemedia.model.MediaVersionMetadataImpl;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MediaDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	@Autowired
	private VFSTranscodingService vfsTranscodingService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	/**
	 * 
	 * @param title
	 * @param description
	 * @param uuid
	 * @param type
	 * @param businessPath
	 * @param referenceId Some external id
	 * @param signature
	 * @param author
	 * @return
	 */
	public Media createMedia(String title, String description, String uuid, String altText, String type, String businessPath,
			String referenceId, int signature, Identity author) {
		MediaImpl media = new MediaImpl();
		media.setCreationDate(new Date());
		media.setCollectionDate(media.getCreationDate());
		media.setVersions(new ArrayList<>());
		media.setType(type);
		media.setReferenceId(referenceId);
		if(StringHelper.containsNonWhitespace(uuid)) {
			media.setUuid(uuid);
		} else {
			media.setUuid(UUID.randomUUID().toString());
		}
		media.setTitle(title);
		media.setDescription(description);
		media.setAltText(altText);
		media.setSignature(signature);
		media.setBusinessPath(businessPath);
		
		media.setAuthor(author);
		dbInstance.getCurrentEntityManager().persist(media);
		return media;
	}
	
	public Media createMediaAndVersion(String title, String description, String altText, String content, String type, String businessPath,
			String referenceId, int signature, Identity author) {
		Media media = createMedia(title, description, null, altText, type, businessPath, referenceId, signature, author);
		MediaWithVersion mediaWithVersion = createVersion(media, new Date(), null, content, null, null);
		return mediaWithVersion.media();
	}

	public MediaWithVersion createVersion(Media media, Date collectionDate, String uuid, String content, String storage, String rootFilename) {
		return createVersion(media, collectionDate, uuid, content, storage, rootFilename, null);
	}

	public MediaWithVersion createVersion(Media media, Date collectionDate, MediaVersionMetadata versionMetadata) {
		return createVersion(media, collectionDate, null, null, null, null, versionMetadata);
	}

	public MediaWithVersion createVersion(Media media, Date collectionDate, String uuid, String content, String storage,
										   String rootFilename, MediaVersionMetadata versionMetadata) {
		MediaVersionImpl version = new MediaVersionImpl();
		version.setCreationDate(new Date());
		version.setCollectionDate(collectionDate);
		version.setVersionName(Integer.toString(media.getVersions().size()));
		if(StringHelper.containsNonWhitespace(uuid)) {
			version.setVersionUuid(uuid);
		} else {
			version.setVersionUuid(UUID.randomUUID().toString());
		}
		version.setContent(content);
		version.setStoragePath(storage);
		version.setRootFilename(rootFilename);
		version.setVersionMetadata(versionMetadata);
		checksumAndMetadata(version);
		version.setMedia(media);
		dbInstance.getCurrentEntityManager().persist(version);
		media.getVersions().add(version);
		media = dbInstance.getCurrentEntityManager().merge(media);
		return new MediaWithVersion(media, version, null, media.getVersions().size());
	}
	
	public Media setVersion(Media media, Date collectionDate) {
		List<MediaVersion> versions = media.getVersions();
		if(versions == null || versions.isEmpty()) {
			return media;
		}
		MediaVersion currentVersion = media.getVersions().get(0);
		return addVersion(media, collectionDate, currentVersion.getContent(),
				currentVersion.getStoragePath(), currentVersion.getRootFilename());
	}
	
	public Media setVersionWithCopy(Media media, Date collectionDate) {
		List<MediaVersion> versions = media.getVersions();
		if(versions == null || versions.isEmpty()) {
			return media;
		}
		MediaVersion currentVersion = media.getVersions().get(0);
		File currentFile = fileStorage.getMediaRootFile(currentVersion);
		File mediaDir = fileStorage.generateMediaSubDirectory(media);
		File mediaFile = new File(mediaDir, currentVersion.getRootFilename());
		FileUtils.copyFileToFile(currentFile, mediaFile, false);
		String storagePath = fileStorage.getRelativePath(mediaDir);
		return addVersion(media, collectionDate, currentVersion.getContent(),
				storagePath, currentVersion.getRootFilename());
	}
	
	public Media restoreVersion(Media media, Date collectionDate, MediaVersion mediaVersion) {
		List<MediaVersion> versions = media.getVersions();
		if(versions == null || versions.isEmpty()) {
			return media;
		}
		return addVersion(media, collectionDate, mediaVersion.getContent(),
				mediaVersion.getStoragePath(), mediaVersion.getRootFilename());
	}
	
	public Media restoreVersionWithCopy(Media media, Date collectionDate, MediaVersion mediaVersion) {
		List<MediaVersion> versions = media.getVersions();
		if(versions == null || versions.isEmpty()) {
			return media;
		}
		MediaVersion currentVersion = media.getVersions().get(0);
		File currentFile = fileStorage.getMediaRootFile(currentVersion);
		File mediaDir = fileStorage.generateMediaSubDirectory(media);
		File mediaFile = new File(mediaDir, currentVersion.getRootFilename());
		FileUtils.copyFileToFile(currentFile, mediaFile, false);
		String storagePath = fileStorage.getRelativePath(mediaDir);
		return addVersion(media, collectionDate, mediaVersion.getContent(),
				storagePath, mediaVersion.getRootFilename());
	}
	
	public Media addVersion(Media media, Date collectionDate, String content, String storage, String rootFilename) {
		return addVersion(media, collectionDate, null, content, storage, rootFilename, null);
	}

	public Media addVersion(Media media, Date collectionDate, MediaVersionMetadata versionMetadata) {
		return addVersion(media, collectionDate, null, null, null, null, versionMetadata);
	}

	public Media addVersion(Media media, Date collectionDate, String uuid, String content, String storage,
							String rootFilename, MediaVersionMetadata versionMetadata) {
		List<MediaVersion> versions = media.getVersions();
		if(versions == null || versions.isEmpty()) {
			MediaWithVersion mediaWithVersion = createVersion(media, collectionDate, null, content, storage,
					rootFilename, versionMetadata);
			return mediaWithVersion.media();
		}
		
		MediaVersionImpl currentVersion = (MediaVersionImpl)media.getVersions().get(0);
		
		MediaVersionImpl newVersion = new MediaVersionImpl();
		newVersion.setCreationDate(currentVersion.getCreationDate());
		newVersion.setCollectionDate(currentVersion.getCollectionDate());
		newVersion.setVersionName(Integer.toString(media.getVersions().size()));
		newVersion.setVersionUuid(currentVersion.getVersionUuid());
		newVersion.setVersionChecksum(currentVersion.getVersionChecksum());
		newVersion.setContent(currentVersion.getContent());
		newVersion.setStoragePath(currentVersion.getStoragePath());
		newVersion.setRootFilename(currentVersion.getRootFilename());
		newVersion.setMetadata(currentVersion.getMetadata());
		newVersion.setVersionMetadata(currentVersion.getVersionMetadata());
		newVersion.setMedia(media);
		
		currentVersion.setCollectionDate(collectionDate);
		if(StringHelper.containsNonWhitespace(uuid)) {
			currentVersion.setVersionUuid(uuid);
		} else {
			currentVersion.setVersionUuid(UUID.randomUUID().toString());
		}
		currentVersion.setContent(content);
		currentVersion.setStoragePath(storage);
		currentVersion.setRootFilename(rootFilename);
		currentVersion.setVersionMetadata(versionMetadata);
		checksumAndMetadata(currentVersion);
		dbInstance.getCurrentEntityManager().persist(newVersion);
		dbInstance.getCurrentEntityManager().merge(currentVersion);
		media.getVersions().add(1, newVersion);
		return dbInstance.getCurrentEntityManager().merge(media);
	}
	
	public void checksumAndMetadata(MediaVersionImpl version) {
		if(StringHelper.containsNonWhitespace(version.getRootFilename())) {
			File rootFile = fileStorage.getMediaRootFile(version);
			if(rootFile != null && rootFile.exists()) {
				version.setVersionChecksum(FileUtils.checksumSha256(rootFile));
			}
			
			VFSMetadata metadata = fileStorage.getMediaRootItemMetadata(version);
			if(metadata != null) {
				version.setMetadata(metadata);
			}
		}
	}
	
	public List<MediaVersion> getVersions(Media media) {
		String query = """
			select mversion from mediaversion as mversion
			 left join fetch mversion.metadata as mdata
			 where mversion.media.key=:mediaKey
			 order by mversion.pos asc""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, MediaVersion.class)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
	}
	
	public MediaVersion loadVersionByKey(Long versionKey) {
		String query = """
			select mversion from mediaversion as mversion
			 left join fetch mversion.metadata as mdata
			 where mversion.key=:versionKey
			 order by mversion.pos asc""";
		List<MediaVersion> versions = dbInstance.getCurrentEntityManager()
				.createQuery(query, MediaVersion.class)
				.setParameter("versionKey", versionKey)
				.getResultList();
		return versions != null && !versions.isEmpty() ? versions.get(0) : null;
	}
	
	public Media update(Media media) {
		return dbInstance.getCurrentEntityManager().merge(media);
	}
	
	public MediaVersion update(MediaVersion mediaVersion) {
		return dbInstance.getCurrentEntityManager().merge(mediaVersion);
	}
	
	public Media loadByKey(Long key) {
		String sb = """
				select media from mmedia as media
				inner join fetch media.author as author
				where media.key=:mediaKey""";
		
		List<Media> medias = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Media.class)
				.setParameter("mediaKey", key)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return medias == null || medias.isEmpty() ? null : medias.get(0);
	}
	
	public Media loadByUuid(String uuid) {
		String sb = """
				select media from mmedia as media
				inner join fetch media.author as author
				where media.uuid=:uuid""";
		
		List<Media> medias = dbInstance.getCurrentEntityManager()
				.createQuery(sb, Media.class)
				.setParameter("uuid", uuid)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return medias == null || medias.isEmpty() ? null : medias.get(0);
	}
	
	public Media loadByMetadata(Long vfsMetadaKey) {
		String sb = """
				select media from mmedia as media
				where exists (select mversion.key from mediaversion mversion
				  where mversion.media.key=media.key and mversion.metadata.key=:metadataKey
				)""";
		
		List<Media> medias = dbInstance.getCurrentEntityManager()
				.createQuery(sb, Media.class)
				.setParameter("metadataKey", vfsMetadaKey)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return medias == null || medias.isEmpty() ? null : medias.get(0);
	}
	
	/**
	 * 
	 * @param identity The identity
	 * @param mediaKeys A list of medias primary keys
	 * @return A sub list of the specified media keys which are owned by the
	 *         specified identity and not used in a media part.
	 */
	public List<Long> filterOwnedDeletableMedias(IdentityRef identity, List<Long> mediaKeys) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select media.key")
		  .append(" from mmedia as media")
		  .append(" inner join media.author as author")
		  .append(" where author.key=:identityKey");
		boolean useInForKeys = mediaKeys.size() <= 255;
		if(useInForKeys) {
			sb.append(" and media.key in (:mediaKeys)");
		}
		sb.append(" and not exists(select mediaPart from cemediapart mediaPart")
		  .append("  where mediaPart.media.key=media.key")
		  .append(" )");
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey());
		if(useInForKeys) {
			query.setParameter("mediaKeys", mediaKeys);
		}
		List<Long> ownedKeys = query.getResultList();
		if(!useInForKeys) {
			ownedKeys.retainAll(mediaKeys);
		}
		return ownedKeys;
	}
	
	public List<Media> load(IdentityRef author) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select media from mmedia as media")
		  .append(" where media.author.key=:authorKey");
			
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Media.class)
				.setParameter("authorKey", author.getKey())
				.getResultList();
	}
	
	public boolean isUsed(MediaLight media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select part.key from cepagepart as part")
		  .append(" where part.media.key=:mediaKey");
		
		List<Long> pageKey = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("mediaKey", media.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return pageKey != null && !pageKey.isEmpty() && pageKey.get(0) != null;
	}
	
	public boolean isShared(IdentityRef identity, MediaLight media, Boolean editable) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select media.key from mmedia as media")
		  .and().append("media.key=:mediaKey and (media.author.key=:identityKey")
		  .append(" or exists (select shareRel.key from mediatogroup as shareRel")
		  .append("  inner join shareRel.group as uGroup")
		  .append("  inner join uGroup.members as uMember")
		  .append("  where shareRel.media.key=media.key and uMember.identity.key=:identityKey")
		  .append("   and shareRel.editable=:editable", editable != null)
		  .append("   and (shareRel.type").in(MediaToGroupRelationType.USER)
		  .append("    or shareRel.type").in(MediaToGroupRelationType.BUSINESS_GROUP)
		  .append("    or (shareRel.type").in(MediaToGroupRelationType.ORGANISATION).append(" and uMember.role").in(OrganisationRoles.author, OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator).append(")")
		  .append(" ))")
		  .append(" or exists (select shareReRel.key from mediatogroup as shareReRel")
		  .append("  inner join shareReRel.repositoryEntry as v")
		  .append("  inner join v.groups as relGroup")
		  .append("  inner join relGroup.group as vBaseGroup")
		  .append("  inner join vBaseGroup.members as vMembership")
		  .append("  where shareReRel.media.key=media.key and shareReRel.type").in(MediaToGroupRelationType.REPOSITORY_ENTRY)
		  .append("   and vMembership.identity.key=:identityKey and vMembership.role").in(GroupRoles.owner, OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator)
		  .append(" and shareReRel.editable=:editable", editable != null)
		  .append("))");
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("mediaKey", media.getKey())
			.setParameter("identityKey", identity.getKey());
		if(editable != null) {
			query.setParameter("editable", editable);
		}
			
		List<Long> editableMediaKeys = query
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return editableMediaKeys != null && !editableMediaKeys.isEmpty()
				&& editableMediaKeys.get(0) != null && editableMediaKeys.get(0).longValue() > 0;
	}
	
	public boolean isAdminOf(IdentityRef identity, MediaLight media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select media.key from mmedia as media")
		  .and().append("media.key=:mediaKey and (media.author.key<>:identityKey")
		  .append(" and exists (select shareReRel.key from mediatogroup as shareReRel")
		  .append("  inner join shareReRel.repositoryEntry as v")
		  .append("  inner join v.groups as relGroup")
		  .append("  inner join relGroup.group as vBaseGroup")
		  .append("  inner join vBaseGroup.members as vMembership")
		  .append("  where shareReRel.media.key=media.key and shareReRel.type").in(MediaToGroupRelationType.REPOSITORY_ENTRY)
		  .append("   and vMembership.identity.key=:identityKey and vMembership.role").in(OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator)
		  .append("))");
		
		List<Long> editableMediaKeys = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("mediaKey", media.getKey())
			.setParameter("identityKey", identity.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return editableMediaKeys != null && !editableMediaKeys.isEmpty()
				&& editableMediaKeys.get(0) != null && editableMediaKeys.get(0).longValue() > 0;
	}
	
	public List<MediaUsageWithStatus> getPortfolioUsages(IdentityRef author, IdentityRef identity, MediaLight media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select page.key, page.title, page.status, ")
		  .append("  binder.key, binder.title,")
		  .append("  media.key, mediaVersion.key, mediaVersion.versionName,")
		  .append("  identUser.firstName, identUser.lastName,")
		  .append("  (select count(pageMember.key) from bgroupmember as pageMember")
		  .append("   inner join bgroupmember as mediaMember on (pageMember.identity.key=mediaMember.identity.key)")
		  .append("   inner join bgroup as mGroup on (mediaMember.group.key=mGroup.key)")
		  .append("   inner join mediatogroup as groupToMedia on (groupToMedia.group.key=mGroup.key)")
		  .append("   where pageMember.group.key=page.baseGroup.key and groupToMedia.media.key=media.key")
		  .append("  ) as numOfLinkedGroups,")
		  .append("  (select count(pageMember.key) from bgroupmember as pageMember")
		  .append("   where pageMember.group.key=page.baseGroup.key and pageMember.identity.key=:authorKey")
		  .append("  ) as numOfPageAuthorOwners,")
		  .append("  (select count(pageMember.key) from bgroupmember as pageMember")
		  .append("   where pageMember.group.key=page.baseGroup.key and pageMember.identity.key=:identityKey")
		  .append("  ) as numOfPageAccess")
		  .append(" from cepage as page")
		  .append(" inner join page.body as pageBody")
		  .append(" inner join treat(pageBody.parts as cemediapart) mediaPart")
		  .append(" inner join mediaPart.media as media")
		  .append(" left join mediaPart.mediaVersion as mediaVersion")
		  .append(" left join mediaPart.identity as ident")
		  .append(" left join ident.user as identUser")
		  .append(" left join page.section as section")
		  .append(" left join section.binder as binder")
		  .append(" where media.key=:mediaKey and page.key not in (")
		  .append("   select pageRef.page.key from cepagereference pageRef")
		  .append(" )");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("mediaKey", media.getKey())
				.setParameter("authorKey", author.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		List<MediaUsageWithStatus> usage = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Long pageKey = (Long)object[0];
			String pageTitle = (String)object[1];
			String pageStatus = (String)object[2];
			
			Long binderKey = (Long)object[3];
			String binderTitle = (String)object[4];

			Long mediaKey = (Long)object[5];
			Long mediaVersionKey = (Long)object[6];
			String mediaVersionName = (String)object[7];
			
			String userFullName = toFullName((String)object[8], (String)object[9]);

			long linkedByUser = PersistenceHelper.extractPrimitiveLong(object, 10);
			long linkedToAuthor = PersistenceHelper.extractPrimitiveLong(object, 11);
			boolean revoked = linkedByUser == 0 && linkedToAuthor == 0;
			boolean pageAccess = PersistenceHelper.extractPrimitiveLong(object, 12) > 0;

			usage.add(new MediaUsageWithStatus(pageKey, pageTitle, pageStatus, binderKey, binderTitle,
					null, null, null, mediaKey, mediaVersionKey, mediaVersionName, userFullName,
					revoked, pageAccess));
		}
		return usage;
	}
	
	public List<MediaUsageWithStatus> getPageUsages(IdentityRef author, MediaLight media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select page.key, page.title, page.status, ")
		  .append("  v.key, ref.subIdent, v.displayname,")
		  .append("  media.key, mediaVersion.key, mediaVersion.versionName,")
		  .append("  identUser.firstName, identUser.lastName,")
		  .append("  (select count(mToGroup.key) from mediatogroup as mToGroup")
		  .append("   inner join mToGroup.group as baseGroup")
		  .append("   inner join repoentrytogroup as reToGroup on (baseGroup.key=reToGroup.group.key)")
		  .append("   where  mToGroup.media.key=media.key and reToGroup.entry.key=v.key")
		  .append("  ) as numOfLinkedGroups,")
		  .append("  (select count(pageRe.key) from repositoryentry as pageRe")
		  .append("   inner join pageRe.groups as pageRelGroup")
          .append("   inner join pageRelGroup.group as pageBaseGroup")
          .append("   inner join pageBaseGroup.members as pageMembership")
		  .append("   where pageRe.key=v.key and pageMembership.identity.key=:authorKey and pageMembership.role").in(GroupRoles.owner.name())
		  .append("  ) as numOfAuthorOwnership")
		  .append(" from cemediapart as mediaPart")
		  .append(" inner join cepagebody as pageBody on (pageBody.key=mediaPart.body.key)")
		  .append(" inner join cepage as page on (page.body.key=pageBody.key)")
		  .append(" inner join mediaPart.media as media")
		  .append(" left join mediaPart.mediaVersion as mediaVersion")
		  .append(" left join mediaPart.identity as ident")
		  .append(" left join ident.user as identUser")
		  .append(" inner join cepagereference ref on (ref.page.key=page.key)")
		  .append(" inner join ref.repositoryEntry as v")
		  .append(" where media.key=:mediaKey");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("mediaKey", media.getKey())
				.setParameter("authorKey", author.getKey())
				.getResultList();
		List<MediaUsageWithStatus> usage = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Long pageKey = (Long)object[0];
			String pageTitle = (String)object[1];
			String pageStatus = (String)object[2];

			Long repoKey = (Long)object[3];
			String subIdent = (String)object[4];
			String repoDisplayname = (String)object[5];

			Long mediaKey = (Long)object[6];
			Long mediaVersionKey = (Long)object[7];
			String mediaVersionName = (String)object[8];
			
			String userFullName = toFullName((String)object[9], (String)object[10]);

			long linkedByGroup = PersistenceHelper.extractPrimitiveLong(object, 11);
			long linkedToAuthor = PersistenceHelper.extractPrimitiveLong(object, 12);
			boolean revoked = linkedByGroup== 0 && linkedToAuthor == 0;

			usage.add(new MediaUsageWithStatus(pageKey, pageTitle, pageStatus, null, null,
					repoKey, subIdent, repoDisplayname, mediaKey, mediaVersionKey, mediaVersionName,
					userFullName, revoked, true));
		}
		return usage;
	}
	
	private String toFullName(String firstName, String lastName) {
		if(StringHelper.containsNonWhitespace(firstName) && StringHelper.containsNonWhitespace(lastName)) {
			MediaIdentityNames names = new MediaIdentityNames(firstName, lastName);
			return userManager.getUserDisplayName(names);
		}
		return null;
	}
	
	public List<MediaUsage> getUsages(MediaLight media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select page.key, page.title, page.status, ")
		  .append("  binder.key, binder.title,")
		  .append("  v.key, ref.subIdent, v.displayname,")
		  .append("  media.key, mediaVersion.key, mediaVersion.versionName")
		  .append(" from cepage as page")
		  .append(" inner join page.body as pageBody")
		  .append(" inner join treat(pageBody.parts as cemediapart) mediaPart")
		  .append(" inner join mediaPart.media as media")
		  .append(" left join mediaPart.mediaVersion as mediaVersion")
		  .append(" left join page.section as section")
		  .append(" left join section.binder as binder")
		  .append(" left join cepagereference ref on (ref.page.key=page.key)")
		  .append(" left join ref.repositoryEntry as v")
		  .append(" where media.key=:mediaKey");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
		List<MediaUsage> usage = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Long pageKey = (Long)object[0];
			String pageTitle = (String)object[1];
			String pageStatus = (String)object[2];
			
			Long binderKey = (Long)object[3];
			String binderTitle = (String)object[4];
			
			Long repoKey = (Long)object[5];
			String subIdent = (String)object[6];
			String repoDisplayname = (String)object[7];

			Long mediaKey = (Long)object[8];
			Long mediaVersionKey = (Long)object[9];
			String mediaVersionName = (String)object[10];
			
			usage.add(new MediaUsage(pageKey, pageTitle, pageStatus, binderKey, binderTitle,
					repoKey, subIdent, repoDisplayname,
					mediaKey, mediaVersionKey, mediaVersionName));
		}
		return usage;
	}
	
	public long countUsages(List<? extends MediaLight> medias) {
		if(medias == null || medias.isEmpty()) {
			return 0;
		}
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(mediaPart.key)")
		  .append(" from cepage as page")
		  .append(" inner join page.body as pageBody")
		  .append(" inner join treat(pageBody.parts as cemediapart) mediaPart")
		  .append(" inner join mediaPart.media as media")
		  .append(" where media.key in (:mediaKeyList)");
		
		List<Long> mediaKeys = medias.stream()
				.map(MediaLight::getKey)
				.toList();
		
		List<Number> count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("mediaKeyList", mediaKeys)
				.getResultList();
		return count != null && !count.isEmpty() ? count.get(0).longValue() : 0l;
	}
	
	public Usage getFileUsage(String path) {
		final String home = "/HomeSite/";
		final String mediaCenter = "/MediaCenter/";
		int index = path.indexOf(home);
		String identityKey = path;
		if(index >= 0) {
			identityKey = identityKey.substring(index + home.length());
		}
		int mediaIndex = identityKey.indexOf(mediaCenter);
		if(mediaIndex >= 0) {
			identityKey = identityKey.substring(0, mediaIndex);
		}
		if(StringHelper.isLong(identityKey)) {
			IdentityRef identity = new IdentityRefImpl(Long.valueOf(identityKey));
			return getFileUsage(identity);
		}
		return null;
	}
	
	public Usage getFileUsage(IdentityRef identity) {
		String query = """
			select count(metadataVersion.key), sum(metadataVersion.fileSize) from mmedia as media
			 inner join media.versions as mediaVersion
			 inner join mediaVersion.metadata as metadataVersion
			 where media.author.key=:identityKey
			 group by media.author.key""";
		
		List<Object[]> numOfFiles = dbInstance.getCurrentEntityManager()
			.createQuery(query, Object[].class)
			.setParameter("identityKey", identity.getKey())
			.getResultList();
		Usage usage = new Usage();
		if(numOfFiles != null && numOfFiles.size() == 1) {
			Object[] rawData = numOfFiles.get(0);
			usage.addNumOfFiles(PersistenceHelper.extractPrimitiveLong(rawData, 0));
			usage.addSize(PersistenceHelper.extractPrimitiveLong(rawData, 1));
		}
		return usage;
	}
	
	public int deleteMedia(Media media) {
		int count = 0;
		List<MediaVersion> versions = media.getVersions();
		if(versions != null) {
			for(MediaVersion version:versions) {
				deleteMedia(version);
				count++;
			}
		}
		dbInstance.getCurrentEntityManager().remove(media);
		count++;
		return count;
	}
	
	private void deleteMedia(MediaVersion mediaVersion) {
		if(mediaVersion == null) return;
		
		if(StringHelper.containsNonWhitespace(mediaVersion.getRootFilename())) {
			VFSContainer container = fileStorage.getMediaContainer(mediaVersion);
			VFSItem item = container.resolve(mediaVersion.getRootFilename());
			if(item instanceof VFSLeaf leaf) {
				leaf.delete();
				VFSMetadata metadata = mediaVersion.getMetadata();
				if (metadata != null && (metadata.isTranscoded() || metadata.isInTranscoding())) {
					vfsTranscodingService.deleteMasterFile(leaf);
				}
				vfsRepositoryService.deletePosterFile(leaf);
			}
		}

		MediaVersionMetadata mediaVersionMetadata = mediaVersion.getVersionMetadata();
		if (mediaVersionMetadata != null) {
			mediaVersion.setVersionMetadata(null);
			update(mediaVersion);
			dbInstance.getCurrentEntityManager().remove(mediaVersionMetadata);
		}
	}

	public MediaVersionMetadata createVersionMetadata() {
		MediaVersionMetadataImpl mediaVersionStreamingUrl = new MediaVersionMetadataImpl();
		mediaVersionStreamingUrl.setCreationDate(new Date());
		dbInstance.getCurrentEntityManager().persist(mediaVersionStreamingUrl);
		return mediaVersionStreamingUrl;
	}

	public MediaVersionMetadata clone(MediaVersionMetadata toClone) {
		MediaVersionMetadataImpl newInstance = new MediaVersionMetadataImpl();
		newInstance.setCreationDate(new Date());
		newInstance.setUrl(toClone.getUrl());
		newInstance.setFormat(toClone.getFormat());
		newInstance.setWidth(toClone.getWidth());
		newInstance.setHeight(toClone.getHeight());
		newInstance.setLength(toClone.getLength());
		dbInstance.getCurrentEntityManager().persist(newInstance);
		return newInstance;
	}

	public MediaVersionMetadata update(MediaVersionMetadata mediaVersionMetadata) {
		return dbInstance.getCurrentEntityManager().merge(mediaVersionMetadata);
	}

	public List<String> getUrlVideoPlatforms(IdentityRef authorRef) {
		String query = """
			select distinct mvm.format
			from mmedia as m
			inner join mediaversion as mv on (mv.media.key = m.key)
			inner join mediaversionmetadata as mvm on (mvm.key = mv.versionMetadata.key)
			where mv.pos = 0 and m.author.key = :authorKey
		""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, String.class)
				.setParameter("authorKey", authorRef.getKey())
				.getResultList();
	}
}
