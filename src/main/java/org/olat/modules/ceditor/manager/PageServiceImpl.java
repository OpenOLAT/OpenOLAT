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
package org.olat.modules.ceditor.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.image.ImageOutputOptions;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfOutputOptions;
import org.olat.core.commons.services.pdf.PdfOutputOptions.MediaType;
import org.olat.core.commons.services.pdf.PdfOutputOptions.PageRange;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.manager.AsyncFileSizeUpdateEvent;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.ceditor.Assignment;
import org.olat.modules.ceditor.Category;
import org.olat.modules.ceditor.ContentAuditLog;
import org.olat.modules.ceditor.ContentAuditLog.Action;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.ContentRoles;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageBody;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.PageReference;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.model.jpa.ContainerPart;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.model.jpa.PageImpl;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.cemedia.manager.MediaLogDAO;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.manager.PageUserInfosDAO;
import org.olat.modules.portfolio.ui.PageRunController;
import org.olat.modules.portfolio.ui.PageSettings;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceLinkLocations;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.manager.TaxonomyCompetenceDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class PageServiceImpl implements PageService, RepositoryEntryDataDeletable {

	private static final Logger log = Tracing.createLoggerFor(PageServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private PdfService pdfService;
	@Autowired
	private CategoryDAO categoryDao;
	@Autowired
	private MediaLogDAO mediaLogDao;
	@Autowired
	private ImageService imageService;
	@Autowired
	private AssignmentDAO assignmentDao;
	@Autowired
	private PageReferenceDAO pageReferenceDao;
	@Autowired
	private PageUserInfosDAO pageUserInfosDao;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDAO;
	@Autowired
	private ContentAuditLogDAO contentAuditLogDao;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private TaxonomyCompetenceDAO taxonomyCompetenceDAO;
	@Autowired
	private PageToTaxonomyCompetenceDAO pageToTaxonomyCompetenceDao;
	
	@Override
	public Page getPageByKey(Long key) {
		return pageDao.loadByKey(key);
	}
	
	@Override
	public Page getFullPageByKey(Long key) {
		return pageDao.loadPageByKey(key);
	}

	@Override
	public Page updatePage(Page page) {
		return pageDao.updatePage(page);
	}
	
	@Override
	public PageReference addReference(Page page, RepositoryEntry repositoryEntry, String subIdent) {
		return pageReferenceDao.createReference(page, repositoryEntry, subIdent);
	}
	
	@Override
	public boolean hasReference(Page page) {
		return pageReferenceDao.hasReference(page);
	}

	@Override
	public boolean hasReference(Page page, RepositoryEntry repositoryEntry, String subIdent) {
		return pageReferenceDao.hasReference(page, repositoryEntry, subIdent);
	}
	
	@Override
	public boolean deleteRepositoryEntryData(RepositoryEntry re) {
		pageReferenceDao.deleteReference(re);
		return true;
	}

	@Override
	public int deleteReference(RepositoryEntry repositoryEntry, String subIdent) {
		return pageReferenceDao.deleteReference(repositoryEntry, subIdent);	
	}

	@Override
	public Page copyPage(Identity owner, Page page) {
		String imagePath = page.getImagePath();
		Page copy = pageDao.createAndPersist(page.getTitle(), page.getSummary(), imagePath, page.getImageAlignment(), page.isEditable(), null, null);
		if(owner != null) {
			groupDao.addMembershipTwoWay(copy.getBaseGroup(), owner, ContentRoles.owner.name());
		}
		
		// Copy the parts but let the media untouched
		PageBody copyBody = copy.getBody();
		List<PagePart> parts = page.getBody().getParts();
		Map<String,String> mapKeys = new HashMap<>();
		for(PagePart part:parts) {
			PagePart newPart = part.copy();
			copyBody = pageDao.persistPart(copyBody, newPart);
			mapKeys.put(part.getKey().toString(), newPart.getKey().toString());
		}

		remapContainers(copyBody, mapKeys);
		dbInstance.commit();
		return copy;
	}

	@Override
	public Page importPage(Identity owner, Page page, ZipFile storage) {
		String imagePath = page.getImagePath();
		Page copy = pageDao.createAndPersist(page.getTitle(), page.getSummary(), imagePath, page.getImageAlignment(), page.isEditable(), null, null);
		if(owner != null) {
			groupDao.addMembershipTwoWay(copy.getBaseGroup(), owner, ContentRoles.owner.name());
		}
		
		// Copy the parts but let the media untouched
		PageBody copyBody = copy.getBody();
		List<PagePart> parts = page.getBody().getParts();
		Map<String,String> mapKeys = new HashMap<>();
		for(PagePart part:parts) {
			PagePart newPart = part.copy();
			if(newPart instanceof MediaPart mediaPart && mediaPart.getMedia() != null) {
				MediaWithVersion importedMedia = importMedia(mediaPart.getMedia(), mediaPart.getMediaVersion(), owner, storage);
				mediaPart.setMedia(importedMedia.media());
				mediaPart.setMediaVersion(importedMedia.version());
				mediaPart.setIdentity(owner);
			}
			copyBody = pageDao.persistPart(copyBody, newPart);
			mapKeys.put(part.getKey().toString(), newPart.getKey().toString());
		}
		
		remapContainers(copyBody, mapKeys);
		return copy;
	}
	
	private MediaWithVersion importMedia(Media media, MediaVersion mediaVersion, Identity owner, ZipFile storage) {
		String mediaUuid = media.getUuid();
		if(StringHelper.containsNonWhitespace(mediaUuid)) {
			Media existingMedia = mediaDao.loadByUuid(mediaUuid);
			if(existingMedia != null) {
				List<MediaVersion> versions = existingMedia.getVersions();
				if(versions != null) {
					if(mediaVersion != null) {
						for(MediaVersion version:versions) {
							if(Objects.equals(mediaVersion.getVersionUuid(), version.getVersionUuid())) {
								return new MediaWithVersion(existingMedia, version, null, 1l);
							}
						}
						
						MediaWithVersion importedVersion = importMediaVersion(existingMedia, mediaVersion, owner, storage);
						if(importedVersion != null) {
							return new MediaWithVersion(importedVersion.media(), importedVersion.version(), null, 1l);
						}
					}
					
					if(!versions.isEmpty()) {
						return new MediaWithVersion(existingMedia, versions.get(0), null, 1l);
					}
				}
				return new MediaWithVersion(existingMedia, null, null, 0l);
			}
		}

		Media importedMedia = mediaDao.createMedia(media.getTitle(), media.getDescription(), mediaUuid, media.getAltText(),
				media.getType(), media.getBusinessPath(), null, 0, owner);
		MediaVersion importedVersionMedia = null;
		if(mediaVersion != null) {
			MediaWithVersion importedVersion = importMediaVersion(importedMedia, mediaVersion, owner, storage);
			if(importedVersion != null) {
				importedMedia = importedVersion.media();
				importedVersionMedia = importedVersion.version();
			}
		}
		if(importedVersionMedia == null && !importedMedia.getVersions().isEmpty()) {
			importedVersionMedia = importedMedia.getVersions().get(0);
		}
		return new MediaWithVersion(importedMedia, importedVersionMedia, null, importedMedia.getVersions().size());
	}
	

	private MediaWithVersion importMediaVersion(Media importedMedia, MediaVersion mediaVersion, Identity owner, ZipFile storage) {
		String content = mediaVersion.getContent();
		String mediaZipPath = mediaVersion.getStoragePath();
		
		MediaWithVersion importedVersion = null;
		if(StringHelper.containsNonWhitespace(mediaZipPath)) {
			File mediaDir = fileStorage.generateMediaSubDirectory(importedMedia);
			String storagePath = fileStorage.getRelativePath(mediaDir);
			for(Enumeration<? extends ZipEntry> entries=storage.entries(); entries.hasMoreElements(); ) {
				ZipEntry entry=entries.nextElement();
				String entryPath = entry.getName();
				if(entryPath.startsWith(mediaZipPath)) {
					File mediaFile = unzip(mediaZipPath, entry, storage, mediaDir);
					if(mediaFile != null) {
						importedVersion = mediaDao.createVersion(importedMedia, mediaVersion.getCollectionDate(), 
								mediaVersion.getVersionUuid(), mediaFile.getName(), storagePath, mediaFile.getName());
						mediaLogDao.createLog(MediaLog.Action.IMPORTED, null, importedMedia, owner);
						importedMedia = importedVersion.media();
					}
				}
			}
			importedMedia = mediaDao.update(importedMedia);
		} else if(StringHelper.containsNonWhitespace(content)) {
			importedVersion = mediaDao.createVersion(importedMedia, mediaVersion.getCollectionDate(),
					mediaVersion.getVersionUuid(), content, null, null);
			mediaLogDao.createLog(MediaLog.Action.IMPORTED, null, importedMedia, owner);
			importedMedia = importedVersion.media();
		}
		if(importedVersion != null) {
			return new MediaWithVersion(importedMedia, importedVersion.version(), null, 0l);
		}
		return null;
	}
	
	/**
	 * The method doesn't unzip hidden files 
	 * 
	 * @param mediaZipPath The path
	 * @param entry A zip entry
	 * @param storage The zip file
	 * @param mediaDir The folder to store the file
	 * @return The file or null
	 */
	private File unzip(String mediaZipPath, ZipEntry entry, ZipFile storage, File mediaDir) {
		try(InputStream in=storage.getInputStream(entry)) {
			String entryPath = entry.getName();
			String fileName = entryPath.replace(mediaZipPath, "");
			File mediaFile = new File(mediaDir, fileName);
			if(mediaFile.isHidden() || mediaFile.getName().startsWith(".")) {
				return null;
			} else {
				FileUtils.copyToFile(in, mediaFile, "");
			}
			return mediaFile;
		} catch(IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	private void remapContainers(PageBody body, Map<String,String> mapKeys) {
		List<PagePart> newParts = body.getParts();
		for(PagePart newPart:newParts) {
			if(newPart instanceof ContainerPart container) {
				ContainerSettings settings = container.getContainerSettings();
				settings.remapElementIds(mapKeys);
				container.setLayoutOptions(ContentEditorXStream.toXml(settings));
			}
		}
	}

	@Override
	public void deletePage(Long pageKey) {
		Page reloadedPage = pageDao.loadByKey(pageKey);
		if(reloadedPage != null) {
			pageReferenceDao.deleteReferences(reloadedPage);
			pageDao.deletePage(reloadedPage);
			pageUserInfosDao.delete(reloadedPage);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <U extends PagePart> U updatePart(U part) {
		U mergedPart = (U)pageDao.merge(part);
		if(mergedPart instanceof MediaPart mediaPart) {
			// Prevent lazy loading issues
			Media media = mediaPart.getMedia();
			if(media != null) {
				media.getMetadataXml();
			}
		}
		return mergedPart;
	}
	
	@Override
	public File getPosterImage(Page page) {
		String imagePath = page.getImagePath();
		if(StringHelper.containsNonWhitespace(imagePath)) {
			File bcroot = fileStorage.getRootDirectory();
			return new File(bcroot, imagePath);
		}
		return null;
	}

	@Override
	public String addPosterImageForPage(File file, String filename) {
		File dir = fileStorage.generatePageSubDirectory();
		File destinationFile = new File(dir, filename);
		String renamedFile = FileUtils.rename(destinationFile);
		if(renamedFile != null) {
			destinationFile = new File(dir, renamedFile);
		}
		FileUtils.copyFileToFile(file, destinationFile, false);
		return fileStorage.getRelativePath(destinationFile);
	}

	@Override
	public void removePosterImage(Page page) {
		String imagePath = page.getImagePath();
		if(StringHelper.containsNonWhitespace(imagePath)) {
			File bcroot = fileStorage.getRootDirectory();
			File file = new File(bcroot, imagePath);
			FileUtils.deleteFile(file);
		}
	}

	@Override
	public <U extends PagePart> U appendNewPagePart(Page page, U part) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(body, part);
		return part;
	}

	@Override
	public <U extends PagePart> U appendNewPagePartAt(Page page, U part, int index) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(body, part, index);
		return part;
	}

	@Override
	public void removePagePart(Page page, PagePart part) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		PagePart reloadedPart = pageDao.loadPart(part);
		pageDao.removePart(body, reloadedPart);
	}

	@Override
	public void moveUpPagePart(Page page, PagePart part) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.moveUpPart(body, part);
	}

	@Override
	public void moveDownPagePart(Page page, PagePart part) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.moveDownPart(body, part);
	}

	@Override
	public void movePagePart(Page page, PagePart partToMove, PagePart sibling, boolean after) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.movePart(body, partToMove, sibling, after);
	}

	@Override
	public Page removePage(Page page) {
		// will take care of the assignments
		return pageDao.removePage(page);
	}	

	@Override
	public List<PagePart> getPageParts(Page page) {
		return pageDao.getParts(page.getBody());
	}

	@Override
	public Integer getNumOfFilesInPage(Long pageKey) {
		return pageDao.getNumOfFiles(pageKey);
	}

	@Override
	public Long getUsageKbOfPage(Long pageKey) {
		double usageInBytes = pageDao.getUsage(pageKey);
		return usageInBytes > 0.0 ? Math.round(usageInBytes / 1024.0) : 0l;
	}

	@Override
	public Assignment getAssignment(PageBody body) {
		return assignmentDao.loadAssignment(body);
	}

	@Override
	public void createLog(Page page, Identity doer) {
		contentAuditLogDao.create(Action.CREATE, page, doer);
	}

	@Override
	public void updateLog(Page page, Identity doer) {
		contentAuditLogDao.create(Action.UPDATE, page, doer);
	}
	
	@Override
	public ContentAuditLog lastChange(Page page) {
		return contentAuditLogDao.lastChange(page);
	}

	@Override
	public List<TaxonomyCompetence> getRelatedCompetences(Page page, boolean fetchTaxonomies) {
		return pageToTaxonomyCompetenceDao.getCompetencesToPage(page, fetchTaxonomies);
	}
	
	@Override
	public Page getPageToCompetence(TaxonomyCompetence competence) {
		return pageToTaxonomyCompetenceDao.getPageToCompetence(competence);
	}
	
	@Override
	public void linkCompetence(Page page, TaxonomyCompetence competence) {
		pageToTaxonomyCompetenceDao.createRelation(page, competence);
		
	}
	
	@Override
	public void unlinkCompetence(Page page, TaxonomyCompetence competence) {
		pageToTaxonomyCompetenceDao.deleteRelation(page, competence);
	}
	
	@Override
	public void linkCompetences(Page page, Identity identity, Set<? extends TaxonomyLevelRef> taxonomyLevels) {
		List<TaxonomyCompetence> relatedCompetences = getRelatedCompetences(page, true);
		List<TaxonomyLevel> relatedCompetenceLevels = relatedCompetences.stream().map(TaxonomyCompetence::getTaxonomyLevel).collect(Collectors.toList());
		
		List<Long> newTaxonomyLevelKeys = taxonomyLevels.stream()
				.map(TaxonomyLevelRef::getKey)
				.collect(Collectors.toList());
		
		List<TaxonomyLevel> newTaxonomyLevels = taxonomyLevelDAO.loadLevelsByKeys(newTaxonomyLevelKeys);
		
		// Remove old competences
		for (TaxonomyCompetence competence : relatedCompetences) {
			if (!newTaxonomyLevels.contains(competence.getTaxonomyLevel())) {
				unlinkCompetence(page, competence);
			}
		}
		
		// Create new competences
		for (TaxonomyLevel newLevel : newTaxonomyLevels) {
			if (!relatedCompetenceLevels.contains(newLevel)) {
				TaxonomyCompetence competence = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, newLevel, identity, null, TaxonomyCompetenceLinkLocations.PORTFOLIO);
				linkCompetence(page, competence);
			}
		}		
	}
	
	@Override
	public Map<TaxonomyLevel, Long> getCompetencesAndUsage(List<Page> pages) {
		return pageToTaxonomyCompetenceDao.getCompetencesAndUsage(pages);
	}
	
	@Override
	public Map<Category, Long> getCategoriesAndUsage(List<Page> pages) {
		return categoryDao.getCategoriesAndUsage(pages);
	}

	@Override
	public void generatePreviewAsync(final Page page, final PageSettings pageSettings, final Identity identity, final WindowControl wControl) {
		if(!pdfModule.isEnabled()) return;
		taskExecutorManager.execute(() -> 
			generatePagePreview(page, pageSettings, identity, wControl));
	}
	
	private Page generatePagePreview(final Page page, PageSettings pageSettings, Identity identity, WindowControl wControl) {
		Page mergedPage = page;
		
		try {
			VFSLeaf imageLeaf = null;
			VFSContainer dir = null;
			VFSMetadata metadata = getPageByKey(page.getKey()).getPreviewMetadata();
			if(metadata == null) {
				dir = fileStorage.generatePreviewSubDirectory();
				String imageName =  VFSManager.rename(dir, "Page.jpg");
				imageLeaf = dir.createChildLeaf(imageName);
			} else {
				VFSItem item = vfsRepositoryService.getItemFor(metadata);
				if(item instanceof VFSLeaf leaf) {
					imageLeaf = leaf;
				}
				dir = VFSManager.olatRootContainer("/" + metadata.getRelativePath());
			}
			
			if(imageLeaf != null && dir != null) {
				String pdfName =  VFSManager.rename(dir, "Page.pdf");
				VFSLeaf pdfLeaf = dir.createChildLeaf(pdfName);
				generatePdfPreview(pdfLeaf, page, pageSettings, identity,  wControl);
				
				if(pdfLeaf.exists() && pdfLeaf.getSize() > 0l) {
					vfsRepositoryService.resetThumbnails(imageLeaf);
					
					ImageOutputOptions options = ImageOutputOptions.valueOf(144, true);
					Size size = imageService.thumbnailPDF(pdfLeaf, imageLeaf, 2480, 3504, false, options);
					if(size != null && metadata == null) {
						metadata = imageLeaf.getMetaInfo();
						((PageImpl)page).setPreviewPath(imageLeaf.getRelPath());
						page.setPreviewMetadata(metadata);
						mergedPage = pageDao.updatePage(page);
						dbInstance.commitAndCloseSession();
					} else if(metadata != null) {
						if(metadata.getCannotGenerateThumbnails() != null) {
							metadata.setCannotGenerateThumbnails(null);
							metadata = vfsRepositoryService.updateMetadata(metadata);
						}
						AsyncFileSizeUpdateEvent event = new AsyncFileSizeUpdateEvent(metadata.getRelativePath(), metadata.getFilename());
						CoordinatorManager.getInstance().getCoordinator().getEventBus()
							.fireEventToListenersOf(event, OresHelper.createOLATResourceableType("UpdateFileSizeAsync"));
					}
				}
				pdfLeaf.deleteSilently();
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return mergedPage;
	}
	
	private void generatePdfPreview(final VFSLeaf pdfLeaf, final Page page, final PageSettings pageSettings,
			final Identity identity, final WindowControl wControl) {
		try(OutputStream out = pdfLeaf.getOutputStream(false)) {
			ControllerCreator creator = (uureq, wwControl) -> {
				ChiefController chiefCtrl = wwControl.getWindowBackOffice().getChiefController();
				chiefCtrl.addBodyCssClass("o_print_a4_144");
				chiefCtrl.addCurrentCustomCSSToView(CustomCSS.printA4And144dpi());
				return new PageRunController(uureq, wwControl, null,
						BinderSecurityCallbackFactory.getReadOnlyCallback(), page, pageSettings, false);
			};
			PdfOutputOptions outputOptions = PdfOutputOptions.valueOf(MediaType.screen, Integer.valueOf(0), new PageRange(1, 1));
			pdfService.convert(identity, creator, wControl, outputOptions, out);
			out.flush();
		} catch(IOException e) {
			log.error("", e);
		}
	}
}
