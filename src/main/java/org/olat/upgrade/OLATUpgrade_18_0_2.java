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
package org.olat.upgrade;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.olat.commons.info.InfoMessage;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.PageStatus;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.cemedia.model.MediaImpl;
import org.olat.modules.cemedia.model.MediaVersionImpl;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.manager.PortfolioServiceImpl;
import org.olat.upgrade.model.UpgradeMedia;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_18_0_2 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_18_0_2.class);

	private static final int BATCH_SIZE = 1000;

	private static final String VERSION = "OLAT_18.0.2";
	private static final String INIT_INFO_MESSAGES_SCHEDULER_UPDATE = "INIT INFO MESSAGES MISSING PUBLISHED DATES";
	
	private static final String MIGRATE_MEDIA_CATEGORIES = "MIGRATE MEDIA CATEGORIES";
	private static final String MIGRATE_MEDIA_CONTENT = "MIGRATE MEDIA CONTENT";
	private static final String MIGRATE_MEDIA_UUID = "MIGRATE MEDIA UUID";
	private static final String MIGRATE_MEDIA_MISSING_CHECKSUM = "MIGRATE MEDIA MISSING CHECKSUM";
	private static final String MIGRATE_MEDIA_VERSION_METADATA = "MIGRATE MEDIA VERSION METADATA";
	private static final String MIGRATE_PUBLISHED_PAGE = "MIGRATE PUBISHED PAGE";
	
	@Autowired
	private DB dbInstance;
 	@Autowired
	private MediaDAO mediaDao;
 	@Autowired
 	private PageService pageService;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	@Autowired
	private PortfolioService portfolioService;


	public OLATUpgrade_18_0_2() {
		super();
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}

		boolean allOk = true;
		allOk &= initInfoMessageSchedulerUpdate(upgradeManager, uhd);
		allOk &= initMigrateMediaUuid(upgradeManager, uhd);
		allOk &= initMigrateMediaCategoriesToTags(upgradeManager, uhd);
		allOk &= initMigrateMediaContent(upgradeManager, uhd);
		allOk &= initMigrateMediaMissingChecksum(upgradeManager, uhd);
		allOk &= initMigrateMediaMissingMetadata(upgradeManager, uhd);
		allOk &= initMigratePublishedPage(upgradeManager, uhd);
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_18_0_2 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_18_0_2 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean initInfoMessageSchedulerUpdate(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(INIT_INFO_MESSAGES_SCHEDULER_UPDATE)) {
			try {
				log.info("Start updating info messages with missing published dates");

				int counter = 0;
				List<InfoMessage> infoMessages;
				do {
					infoMessages = getInfoMessages(counter, BATCH_SIZE);
					for (InfoMessage infoMessage : infoMessages) {
						//set initial value to true, because there was no scheduler option before
						infoMessage.setPublished(true);
						//set initial value to creationDate, because there was no scheduler option before
						infoMessage.setPublishDate(infoMessage.getCreationDate());
					}
					counter += infoMessages.size();
					log.info(Tracing.M_AUDIT, "Updated info messages: {} total processed ({})", infoMessages.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (infoMessages.size() == BATCH_SIZE);

				log.info("Update for infoMessages with missing published dates finished.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(INIT_INFO_MESSAGES_SCHEDULER_UPDATE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
	private List<InfoMessage> getInfoMessages(int firstResult, int maxResults) {
		String query = "select msg from infomessage as msg where msg.publishDate is null order by msg.key";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, InfoMessage.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults).getResultList();
	}
	

	private boolean initMigrateMediaUuid(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_MEDIA_UUID)) {
			try {
				log.info("Start generate media UUIDs.");
				
				int counter = 0;
				List<Media> medias;
				do {
					medias = getMediaWithoutUUID(counter, BATCH_SIZE);
					for (Media media : medias) {
						if(!StringHelper.containsNonWhitespace(media.getUuid())) {
							((MediaImpl)media).setUuid(UUID.randomUUID().toString());
							mediaService.updateMedia(media);
						}
					}
					counter += medias.size();
					log.info(Tracing.M_AUDIT, "UUIDs media generated: {} total processed ({})", medias.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (medias.size() == BATCH_SIZE);

				dbInstance.commitAndCloseSession();

				log.info("Media UUIDs generation finished.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(MIGRATE_MEDIA_UUID, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private List<Media> getMediaWithoutUUID(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select media from mmedia as media")
		  .append(" where media.uuid is null")
		  .append(" order by media.key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Media.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private boolean initMigrateMediaCategoriesToTags(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_MEDIA_CATEGORIES)) {
			try {
				log.info("Start migration of media categories to tags.");
				
				int counter = 0;
				List<Media> medias;
				do {
					medias = getMediaWithCategories(counter, BATCH_SIZE);
					for (Media media : medias) {
						List<String> categories = getCategories(media);
						mediaService.updateTags(null, media, categories);
					}
					counter += medias.size();
					log.info(Tracing.M_AUDIT, "Updated media categories: {} total processed ({})", medias.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (medias.size() == BATCH_SIZE);

				dbInstance.commitAndCloseSession();

				log.info("Media categories migration finished.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(MIGRATE_MEDIA_CATEGORIES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	public List<String> getCategories(Media media) {
		StringBuilder sb = new StringBuilder();
		sb.append("select category.name from pfcategoryrelation as rel")
		  .append(" inner join rel.category as category")
		  .append(" where rel.resId=:resId and rel.resName=:resName");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), String.class)
			.setParameter("resName", "Media")
			.setParameter("resId", media.getKey())
			.getResultList();
	}
	
	private List<Media> getMediaWithCategories(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select media from mmedia as media")
		  .append(" inner join fetch media.author as author")
		  .append(" where exists (select rel.key from pfcategoryrelation as rel")
		  .append("  where rel.resId=media.key and rel.resName='Media'")
		  .append(" ) order by media.key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Media.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private boolean initMigrateMediaContent(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_MEDIA_CONTENT)) {
			try {
				log.info("Start migration of media content to version.");
				
				int counter = 0;
				List<UpgradeMedia> upgradeMedias;
				do {
					upgradeMedias = getMedia(counter, BATCH_SIZE);
					for (UpgradeMedia upgradeMedia : upgradeMedias) {
						Media media = mediaDao.loadByKey(upgradeMedia.getKey());
						List<MediaVersion> versions = media.getVersions();
						if(versions == null || versions.isEmpty()) {
							mediaDao.createVersion(media, upgradeMedia.getCollectionDate(), null,
									upgradeMedia.getContent(), upgradeMedia.getStoragePath(), upgradeMedia.getRootFilename());
							dbInstance.commit();
						}
					}
					counter += upgradeMedias.size();
					log.info(Tracing.M_AUDIT, "Updated media content: {} total processed ({})", upgradeMedias.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (upgradeMedias.size() == BATCH_SIZE);

				dbInstance.commitAndCloseSession();
				log.info("Media content migration finished.");
				
				// Both are not independent
				log.info("Start migration of page parts to media version.");
				int counterPart = 0;
				List<PagePart> parts;
				do {
					parts = getMediaParts(counterPart, BATCH_SIZE);
					for (PagePart part : parts) {
						if(part instanceof MediaPart mediaPart) {
							Media media = mediaPart.getMedia();
							MediaVersion mediaVersion = mediaPart.getMediaVersion();
							List<MediaVersion> versions = media.getVersions();
							if(versions != null && !versions.isEmpty() && mediaVersion == null) {
								mediaPart.setMediaVersion(versions.get(0));
								dbInstance.getCurrentEntityManager().merge(mediaPart);
								dbInstance.commit();
							}
						}
					}
					counterPart += parts.size();
					log.info(Tracing.M_AUDIT, "Updated page parts: {} total processed ({})", parts.size(), counterPart);
					dbInstance.commitAndCloseSession();
				} while (parts.size() == BATCH_SIZE);

				dbInstance.commitAndCloseSession();

				log.info("Page parts migration finished.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(MIGRATE_MEDIA_CONTENT, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<UpgradeMedia> getMedia(int firstResult, int maxResults) {
		String query = "select media from upgrademedia as media order by media.key";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, UpgradeMedia.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}

	private List<PagePart> getMediaParts(int firstResult, int maxResults) {
		String query = "select part from cepagepart as part order by part.key";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, PagePart.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private boolean initMigrateMediaMissingChecksum(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_MEDIA_MISSING_CHECKSUM)) {
			try {
				log.info("Start calculating missing versions checksum.");
				
				int counter = 0;
				List<MediaVersionImpl> mediaVersions;
				do {
					mediaVersions = getMediaVersionWithoutChecksum(counter, BATCH_SIZE);
					for (MediaVersionImpl mediaVersion : mediaVersions) {
						mediaDao.checksumAndMetadata(mediaVersion);
						mediaDao.update(mediaVersion);
					}
					counter += mediaVersions.size();
					log.info(Tracing.M_AUDIT, "Updated media version checksum: {} total processed ({})", mediaVersions.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (mediaVersions.size() == BATCH_SIZE);

				dbInstance.commitAndCloseSession();

				log.info("Calculating missing versions checksum finished.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(MIGRATE_MEDIA_MISSING_CHECKSUM, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean initMigrateMediaMissingMetadata(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_MEDIA_VERSION_METADATA)) {
			try {
				log.info("Start linking metadata to version.");
				
				int counter = 0;
				List<MediaVersionImpl> mediaVersions;
				do {
					mediaVersions = getMediaVersionWithoutMetadata(counter, BATCH_SIZE);
					for (MediaVersionImpl mediaVersion : mediaVersions) {
						VFSMetadata metadata = fileStorage.getMediaRootItemMetadata(mediaVersion);
						if(mediaVersion.getMetadata() == null && metadata != null) {
							mediaVersion.setMetadata(metadata);
							mediaService.updateMediaVersion(mediaVersion);
						}	
					}
					counter += mediaVersions.size();
					log.info(Tracing.M_AUDIT, "Updated media version metadata: {} total processed ({})", mediaVersions.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (mediaVersions.size() == BATCH_SIZE);

				dbInstance.commitAndCloseSession();

				log.info("End linking metadata to version.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(MIGRATE_MEDIA_VERSION_METADATA, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<MediaVersionImpl> getMediaVersionWithoutChecksum(int firstResult, int maxResults) {
		String query = """
			select mversion from mediaversion as mversion
			 where mversion.rootFilename is not null and mversion.storagePath is not null and mversion.versionChecksum is null
			 order by mversion.key asc
			""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, MediaVersionImpl.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private List<MediaVersionImpl> getMediaVersionWithoutMetadata(int firstResult, int maxResults) {
		String query = """
			select mversion from mediaversion as mversion
			 where mversion.rootFilename is not null and mversion.storagePath is not null and mversion.metadata.key is null
			 order by mversion.key asc
			""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, MediaVersionImpl.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private boolean initMigratePublishedPage(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_PUBLISHED_PAGE)) {
			try {
				log.info("Start versioning of published pages.");
				
				int counter = 0;
				List<Page> publishedPages;
				do {
					publishedPages = getPublishedPage(counter, BATCH_SIZE);
					for (Page publishedPage : publishedPages) {
						versionedPublishedPage(publishedPage);
					}
					counter += publishedPages.size();
					log.info(Tracing.M_AUDIT, "Versioning of pages: {} total processed ({})", publishedPages.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (publishedPages.size() == BATCH_SIZE);

				dbInstance.commitAndCloseSession();

				log.info("End versioning of published pages.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(MIGRATE_PUBLISHED_PAGE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void versionedPublishedPage(Page page) {
		try {
			page = pageService.getFullPageByKey(page.getKey());
			((PortfolioServiceImpl)portfolioService).versionedMedias(page);
			dbInstance.commitAndCloseSession();
		} catch (Exception e) {
			log.error("Error versioning medias of page: {}", page.getKey(), e);
			dbInstance.rollbackAndCloseSession();
		}
	}
	
	private List<Page> getPublishedPage(int firstResult, int maxResults) {
		QueryBuilder query = new QueryBuilder();
		query.append("select page from cepage as page")
			 .append(" where page.status").in(PageStatus.published, PageStatus.closed)
			 .append(" order by page.key asc");
		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Page.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
}
