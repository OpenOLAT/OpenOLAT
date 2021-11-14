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
package org.olat.course.nodes.document.ui;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.olat.core.commons.modules.bc.FolderLicenseHandler;
import org.olat.core.commons.modules.bc.meta.MetaInfoFormController;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.course.nodes.document.DocumentSource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.handlers.WebDocumentHandler;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Jul 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentDisplayController extends BasicController {
	
	public static final Event EVENT_PREVIEW = new Event("preview-document");
	public static final Event EVENT_SELECT_DOCUMENT = new Event("select-document");
	public static final Event EVENT_EDIT_METADATA = new Event("edit-metadata");
	public static final Event EVENT_COPY_TO_COURSE = new Event("copy-to-course");
	public static final Event EVENT_COPY_TO_REPOSITORY = new Event("copy-to-repository");

	private VelocityContainer mainVC;
	
	private Dropdown editDropdown;
	private Link previewLink;
	private Link editMetadataLink;
	private Link editDocumentLink;
	private Link changeDocumentLink;
	private Link copyToCourseLink;
	private Link copyToRepositoryLink;
	private Link toggleMetadataLink;
	
	private DocumentSource documentSource;
	private Boolean metadataToggle = Boolean.FALSE;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private FolderLicenseHandler folderLicenseHandler;
	@Autowired
	private RepositoryEntryLicenseHandler repositoryEntryLicenseHandler;
	@Autowired
	private DocEditorService docEditorService;

	public DocumentDisplayController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MetaInfoFormController.class, getLocale(), getTranslator()));
		
		mainVC = createVelocityContainer("display");
		putInitialPanel(mainVC);
	}
	
	public void setDocumentSource(UserRequest ureq, DocumentSource documentSource) {
		this.documentSource = documentSource;
		
		mainVC.clear();
		
		if (documentSource.getVfsLeaf() != null) {
			DocumentWrapper wrapper = new DocumentWrapper();
			createDocumentWrapper(ureq, wrapper);
			mainVC.contextPut("item", wrapper);
			
			previewLink = LinkFactory.createLink("config.preview", mainVC, this);
			previewLink.setIconLeftCSS("o_icon o_icon_preview");
			
			toggleMetadataLink = LinkFactory.createLink("metadata.toggle", mainVC, this);
			toggleMetadataUI();
			
			initCommands(ureq);
		}
	}

	private void createDocumentWrapper(UserRequest ureq, DocumentWrapper wrapper) {
		VFSLeaf vfsLeaf = documentSource.getVfsLeaf();
		VFSMetadata vfsMetadata = vfsLeaf.getMetaInfo();
		if (vfsMetadata != null) {
			wrapper.setMetaInfo(vfsMetadata);
			String localizedLastModified = DateFormat.getDateInstance(DateFormat.FULL, getLocale()).format(vfsMetadata.getLastModified());
			wrapper.setLocalizedLastModified(localizedLastModified);
			
			Date publicationDate = calculateDateFromPublicationDateArray(vfsMetadata.getPublicationDate());
			if (publicationDate != null) {
				String localizedPublicationDate = DateFormat.getDateInstance(DateFormat.FULL, getLocale()).format(publicationDate);
				wrapper.setLocalizedPublicationDate(localizedPublicationDate);
			}
		}

		String iconCssClass = CSSHelper.createFiletypeIconCssClassFor(vfsLeaf.getName());
		wrapper.setIconCssClass(iconCssClass);
		
		boolean thumbnailAvailable = vfsRepositoryService.isThumbnailAvailable(vfsLeaf, vfsMetadata);
		wrapper.setThumbnailAvailable(thumbnailAvailable);
		if (thumbnailAvailable) {
			VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(vfsLeaf, 150, 150, false);
			VFSMediaMapper thumbnailMapper = new VFSMediaMapper(thumbnail);
			String thumbnailUrl = registerCacheableMapper(ureq, null, thumbnailMapper);
			wrapper.setThumbnailUrl(thumbnailUrl);
		}
		
		long size = vfsLeaf.getSize();
		String fileSize = Formatter.formatBytes(size);
		wrapper.setFileSize(fileSize);
		
		if (documentSource.getEntry() != null) {
			wrapRepositoryEntry(wrapper, documentSource.getEntry());
		} else {
			wrapCourseFolderDoc(wrapper);
		}
	}
	
	void wrapRepositoryEntry(DocumentWrapper wrapper, RepositoryEntry entry) {
		String displayName = entry.getDisplayname();
		wrapper.setDisplayName(displayName);
		
		String description = entry.getDescription();
		if (StringHelper.containsNonWhitespace(description)) {
			wrapper.setDescription(description);
		}
		
		if (licenseModule.isEnabled(repositoryEntryLicenseHandler)) {
			OLATResource res = entry.getOlatResource();
			ResourceLicense license = licenseService.loadLicense(res);
			if (license != null) {
				LicenseType licenseType = license.getLicenseType();
				String licenseName = licenseService.isFreetext(licenseType)
						? license.getFreetext()
						: LicenseUIFactory.translate(licenseType, getLocale());
				wrapper.setLicenseName(licenseName);
				
				String licensor = license.getLicensor();
				if (StringHelper.containsNonWhitespace(licensor)) {
					wrapper.setLicensor(licensor);
				}
			}
		}
		
		String storageLocation = translate("storage.repo.entry");
		wrapper.setStorageLocation(storageLocation);
	}

	private void wrapCourseFolderDoc(DocumentWrapper wrapper) {
		VFSMetadata vfsMetadata = documentSource.getVfsLeaf().getMetaInfo();
		if (vfsMetadata != null) {
			String displayName = StringHelper.containsNonWhitespace(vfsMetadata.getTitle())
					? vfsMetadata.getTitle()
					: vfsMetadata.getFilename();
			wrapper.setDisplayName(displayName);
			
			String comment = vfsMetadata.getComment();
			if (StringHelper.containsNonWhitespace(comment)) {
				wrapper.setDescription(comment);
			}
			
			if (licenseModule.isEnabled(folderLicenseHandler)) {
				LicenseType licenseType = vfsMetadata.getLicenseType();
				if (licenseType != null) {
					String licenseName = licenseService.isFreetext(licenseType)
							? vfsMetadata.getLicenseText()
							: LicenseUIFactory.translate(licenseType, getLocale());
					wrapper.setLicenseName(licenseName);
					
					String licensor = vfsMetadata.getLicensor();
					if (StringHelper.containsNonWhitespace(licensor)) {
						wrapper.setLicensor(licensor);
					}
				}
			}
		}
		
		String storageLocation = translate("storage.course.folder");
		wrapper.setStorageLocation(storageLocation);
	}
	
	private Date calculateDateFromPublicationDateArray(String[] pubDateArray) {
		if(pubDateArray == null || pubDateArray.length == 0) return null;
		try {
			Calendar cal = Calendar.getInstance();
			cal.clear();
			if(pubDateArray.length > 0 && pubDateArray[0] != null) {
				cal.set(Calendar.YEAR, Integer.parseInt(pubDateArray[0]));
			}
			if(pubDateArray.length > 1 && pubDateArray[1] != null) {
				cal.set(Calendar.MONTH, Integer.parseInt(pubDateArray[1]));
			}
			if(pubDateArray.length > 2 && pubDateArray[2] != null) {
				cal.set(Calendar.DATE, Integer.parseInt(pubDateArray[2]));
			}
			return cal.getTime();
		} catch (NumberFormatException e) {
			// can happen
			return null;
		}
	}

	private void toggleMetadataUI() {
		mainVC.contextPut("metadataToggle", metadataToggle);
		if (metadataToggle.booleanValue()) {
			toggleMetadataLink.setCustomDisplayText(translate("metadata.toggle.hide"));
			toggleMetadataLink.setIconLeftCSS("o_icon o_icon_move_up");
		} else {
			toggleMetadataLink.setCustomDisplayText(translate("metadata.toggle.show"));
			toggleMetadataLink.setIconLeftCSS("o_icon o_icon_move_down");
		}
	}
	
	private void initCommands(UserRequest ureq) {
		editDropdown = new Dropdown("config.edit", "config.edit", true, getTranslator());
		editDropdown.setButton(true);
		editDropdown.setEmbbeded(true);
		editDropdown.setOrientation(DropdownOrientation.right);
		
		boolean isNotEntry = documentSource.getEntry() == null;
		if (isNotEntry) {
			editMetadataLink = LinkFactory.createLink("config.metadata", mainVC, this);
			editDropdown.addComponent(editMetadataLink);
		}
		
		String extension = FileUtils.getFileSuffix(documentSource.getVfsLeaf().getName());
		if (docEditorService.hasEditor(getIdentity(), ureq.getUserSession().getRoles(), extension, Mode.EDIT, true, true)) {
			editDocumentLink = LinkFactory.createLink("config.edit.document", mainVC, this);
			editDocumentLink.setNewWindow(true, true);
			editDropdown.addComponent(editDocumentLink);
		}
		
		changeDocumentLink = LinkFactory.createLink("config.change.document", mainVC, this);
		editDropdown.addComponent(changeDocumentLink);
		
		if (isNotEntry) {
			if (isCopyToRepositorySupported()) {
				copyToRepositoryLink = LinkFactory.createLink("config.copy.to.repository", mainVC, this);
				editDropdown.addComponent(copyToRepositoryLink);
			}
		} else {
			copyToCourseLink = LinkFactory.createLink("config.copy.to.course", mainVC, this);
			editDropdown.addComponent(copyToCourseLink);
		}
		
		mainVC.put("config.edit", editDropdown);
	}

	private boolean isCopyToRepositorySupported() {
		VFSLeaf vfsLeaf = documentSource.getVfsLeaf();
		if (vfsLeaf instanceof LocalFileImpl) {
			File file = ((LocalFileImpl)vfsLeaf).getBasefile();
			String filename = vfsLeaf.getName();
			for (String type : repositoryHandlerFactory.getSupportedTypes()) {
				RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(type);
				// Only documents but not other resources
				if (handler instanceof WebDocumentHandler) {
					ResourceEvaluation eval = handler.acceptImport(file, filename);
					if (eval != null && eval.isValid()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == previewLink) {
			fireEvent(ureq, EVENT_PREVIEW);
		} else if (source == changeDocumentLink) {
			fireEvent(ureq, EVENT_SELECT_DOCUMENT);
		} else if (source == editMetadataLink) {
			fireEvent(ureq, EVENT_EDIT_METADATA);
		} else if (source == copyToRepositoryLink) {
			fireEvent(ureq, EVENT_COPY_TO_REPOSITORY);
		} else if (source == copyToCourseLink) {
			fireEvent(ureq, EVENT_COPY_TO_COURSE);
		} else if (source == editDocumentLink) {
			doEditDocument(ureq);
		} else if (source == toggleMetadataLink) {
			metadataToggle = Boolean.valueOf(!metadataToggle.booleanValue());
			toggleMetadataUI();
		}
	}

	private void doEditDocument(UserRequest ureq) {
		VFSLeaf vfsLeaf = documentSource.getVfsLeaf();
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withMode(Mode.EDIT)
				.build(vfsLeaf);
		String url = docEditorService.prepareDocumentUrl(ureq.getUserSession(), configs);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
	}

	public static final class DocumentWrapper {
		
		private String displayName;
		private String iconCssClass;
		private boolean thumbnailAvailable;
		private String thumbnailUrl;
		private String description;
		private String fileSize;
		private String licenseName;
		private String licensor;
		private String storageLocation;
		private VFSMetadata metaInfo;
		private String localizedLastModified;
		private String localizedPublicationDate;
		
		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getIconCssClass() {
			return iconCssClass;
		}

		public void setIconCssClass(String iconCssClass) {
			this.iconCssClass = iconCssClass;
		}

		public boolean isThumbnailAvailable() {
			return thumbnailAvailable;
		}

		public void setThumbnailAvailable(boolean thumbnailAvailable) {
			this.thumbnailAvailable = thumbnailAvailable;
		}

		public String getThumbnailUrl() {
			return thumbnailUrl;
		}

		public void setThumbnailUrl(String thumbnailUrl) {
			this.thumbnailUrl = thumbnailUrl;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getFileSize() {
			return fileSize;
		}

		public void setFileSize(String fileSize) {
			this.fileSize = fileSize;
		}

		public String getLicenseName() {
			return licenseName;
		}

		public void setLicenseName(String licenseName) {
			this.licenseName = licenseName;
		}

		public String getLicensor() {
			return licensor;
		}

		public void setLicensor(String licensor) {
			this.licensor = licensor;
		}

		public String getStorageLocation() {
			return storageLocation;
		}

		public void setStorageLocation(String storageLocation) {
			this.storageLocation = storageLocation;
		}

		public VFSMetadata getMetaInfo() {
			return metaInfo;
		}

		public void setMetaInfo(VFSMetadata metaInfo) {
			this.metaInfo = metaInfo;
		}

		public String getLocalizedLastModified() {
			return localizedLastModified;
		}

		public void setLocalizedLastModified(String localizedLastModified) {
			this.localizedLastModified = localizedLastModified;
		}

		public String getLocalizedPublicationDate() {
			return localizedPublicationDate;
		}

		public void setLocalizedPublicationDate(String localizedPublicationDate) {
			this.localizedPublicationDate = localizedPublicationDate;
		}
		
	}

}
