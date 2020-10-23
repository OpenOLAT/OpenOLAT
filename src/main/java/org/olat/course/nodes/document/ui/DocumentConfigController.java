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
import java.util.function.Function;

import org.olat.core.commons.controllers.filechooser.FileChoosenEvent;
import org.olat.core.commons.controllers.filechooser.FileChooserController;
import org.olat.core.commons.controllers.filechooser.FileChooserUIFactory;
import org.olat.core.commons.modules.bc.FolderLicenseHandler;
import org.olat.core.commons.modules.bc.meta.MetaInfoFormController;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocTemplate;
import org.olat.core.commons.services.doceditor.DocTemplates;
import org.olat.core.commons.services.doceditor.ui.CreateDocumentController;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.DocumentCourseNode;
import org.olat.course.nodes.document.DocumentSecurityCallback;
import org.olat.course.nodes.document.DocumentSource;
import org.olat.course.nodes.document.ui.DocumentSelectionController.CreateEvent;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.DocFileResource;
import org.olat.fileresource.types.ImageFileResource;
import org.olat.fileresource.types.PdfFileResource;
import org.olat.fileresource.types.PowerpointFileResource;
import org.olat.fileresource.types.SoundFileResource;
import org.olat.fileresource.types.XlsFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.handlers.VideoHandler;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 Jul 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentConfigController extends BasicController {
	
	private static final DocumentSecurityCallback PREVIEW_SEC_CALLBACK = new PreviewSecurityCallback();
	private static final String[] FILE_TYPES = new String[] {
			DocFileResource.TYPE_NAME,
			XlsFileResource.TYPE_NAME,
			PowerpointFileResource.TYPE_NAME,
			PdfFileResource.TYPE_NAME,
			ImageFileResource.TYPE_NAME,
			SoundFileResource.TYPE_NAME
	};

	private VelocityContainer mainVC;

	private final BreadcrumbPanel stackPanel;
	private CloseableModalController cmc;
	private DocumentDisplayController documentDisplayCtrl;
	private DocumentRightsController documentRightsCtrl;
	private DocumentSelectionController selectionCtrl;
	private FileChooserController fileChooserCtr;
	private ReferencableEntriesSearchController repoSearchCtrl;
	private CreateDocumentController createCtrl;
	private DialogBoxController copyToCourseCtrl;
	private CopyToRepositoryController copyToRepositoryCtrl;
	private MetaInfoFormController metadataCtrl;
	private Controller previewCtrl;
	
	private final DocumentCourseNode courseNode;
	private final VFSContainer courseFolderCont;
	private DocumentSource documentSource;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private FolderLicenseHandler folderLicenseHandler;
	@Autowired
	private RepositoryEntryLicenseHandler repositoryEntryLicenseHandler;
	@Autowired
	private VideoHandler videoHandler;

	public DocumentConfigController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, DocumentCourseNode courseNode,
			VFSContainer courseFolderCont) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.courseNode = courseNode;
		this.courseFolderCont = courseFolderCont;
		
		mainVC = createVelocityContainer("config");
		
		documentDisplayCtrl = new DocumentDisplayController(ureq, getWindowControl());
		listenTo(documentDisplayCtrl);
		mainVC.put("display", documentDisplayCtrl.getInitialComponent());
		
		documentRightsCtrl = new DocumentRightsController(ureq, wControl, courseNode);
		listenTo(documentRightsCtrl);
		mainVC.put("rights", documentRightsCtrl.getInitialComponent());
		
		selectionCtrl = new DocumentSelectionController(ureq, getWindowControl(), VFSManager.getQuotaLeftKB(courseFolderCont));
		listenTo(selectionCtrl);
		mainVC.put("selection", selectionCtrl.getInitialComponent());
		
		updateUI(ureq);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == documentDisplayCtrl) {
			if (DocumentDisplayController.EVENT_PREVIEW == event) {
				doPreview(ureq);
			} else if (DocumentDisplayController.EVENT_SELECT_DOCUMENT == event) {
				doChangeDocument(ureq);
			} else if (DocumentDisplayController.EVENT_EDIT_METADATA == event) {
				doEditMetadata(ureq);
			} else if (DocumentDisplayController.EVENT_COPY_TO_REPOSITORY == event) {
				doCopyToRepository(ureq);
			} else if (DocumentDisplayController.EVENT_COPY_TO_COURSE == event) {
				doAskCopyToCourse(ureq, documentSource.getEntry());
			}
		} else if (source == documentRightsCtrl) {
			fireEvent(ureq, event);
		} else if (source == selectionCtrl) {
			if (DocumentSelectionController.EVENT_SELECT_COURSE == event) {
				deactivateCmc();
				cleanUp();
				doSelectFromCourseFolder(ureq);
			} else if (DocumentSelectionController.EVENT_SELECT_REPOSITORY == event) {
				deactivateCmc();
				cleanUp();
				doSelectRepositoryEntry(ureq);
			} else if (DocumentSelectionController.EVENT_UPLOADED == event) {
				if (acceptFile(selectionCtrl.getUploadedFile(), selectionCtrl.getUploadedFileName())) {
					String fileName = selectionCtrl.moveUploadFileTo(courseFolderCont);
					VFSItem vfsItem = courseFolderCont.resolve(fileName);
					if (vfsItem instanceof VFSLeaf) {
						documentSource = new DocumentSource((VFSLeaf)vfsItem);
						doSetDocumentFromCourseFolder(ureq, fileName);
					}
					updateUI(ureq);
					deactivateCmc();
					cleanUp();
				}
				
			} else if (event instanceof CreateEvent) {
				CreateEvent ce = (CreateEvent)event;
				DocTemplate docTemplate = ce.getDocTemplate();
				deactivateCmc();
				cleanUp();
				doCreateDocument(ureq, docTemplate);
			}
		} else if (source instanceof FileChooserController) {
			if (event instanceof FileChoosenEvent) {
				FileChoosenEvent fce = (FileChoosenEvent)event;
				VFSItem vfsItem = fce.getSelectedItem();
				if (vfsItem instanceof LocalFileImpl) {
					LocalFileImpl localFileImpl = (LocalFileImpl)vfsItem;
					if (acceptFile(localFileImpl.getBasefile(), localFileImpl.getName())) {
						String selectedRelativeItemPath = FileChooserUIFactory.getSelectedRelativeItemPath(fce, courseFolderCont, null);
						doSetDocumentFromCourseFolder(ureq, selectedRelativeItemPath);
						updateUI(ureq);
					}
				} else {
					showError("error.file.corrupt");
				}
			}
			cleanUp();
		} else if (repoSearchCtrl == source) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry entry = repoSearchCtrl.getSelectedEntry();
				deactivateCmc();
				cleanUp();
				doAskCopyToCourse(ureq, entry);
			} else {
				deactivateCmc();
				cleanUp();
			}
		} else if (source == copyToRepositoryCtrl) {
			if (event == Event.DONE_EVENT) {
				RepositoryEntry newEntry = copyToRepositoryCtrl.getEntry();
				if (newEntry != null) {
					doSetDocumentFromRepository(ureq, newEntry);
				} else {
					showError("error.copy.to.repository");
				}
			}
			deactivateCmc();
			cleanUp();
			updateUI(ureq);
		} else if (source == copyToCourseCtrl) {
			RepositoryEntry copyEntry = (RepositoryEntry)copyToCourseCtrl.getUserObject();
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doCopyToCourse(ureq, copyEntry);
			} else {
				doSetDocumentFromRepository(ureq, copyEntry);
			}
			cleanUp();
			updateUI(ureq);
		} else if (source == metadataCtrl) {
			if (event == FormEvent.DONE_EVENT) {
				VFSMetadata meta = metadataCtrl.getMetaInfo();
				String fileName = metadataCtrl.getFilename();
				doUpdateMetadata(meta, fileName);
				updateUI(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == createCtrl) {
			if (event == Event.DONE_EVENT) {
				VFSLeaf createdLeaf = createCtrl.getCreatedLeaf();
				documentSource = new DocumentSource(createdLeaf);
				String selectedRelativeItemPath = VFSManager.getRelativeItemPath(createdLeaf, courseFolderCont, null);
				doSetDocumentFromCourseFolder(ureq, selectedRelativeItemPath);
				updateUI(ureq);
			}
			deactivateCmc();
			cleanUp();
		} else if (source == cmc) {
			deactivateCmc();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void deactivateCmc() {
		if (cmc != null) {
			cmc.deactivate();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(copyToRepositoryCtrl);
		removeAsListenerAndDispose(copyToCourseCtrl);
		removeAsListenerAndDispose(repoSearchCtrl);
		removeAsListenerAndDispose(fileChooserCtr);
		removeAsListenerAndDispose(metadataCtrl);
		removeAsListenerAndDispose(createCtrl);
		removeAsListenerAndDispose(cmc);
		copyToRepositoryCtrl = null;
		copyToCourseCtrl = null;
		repoSearchCtrl = null;
		fileChooserCtr = null;
		metadataCtrl = null;
		createCtrl = null;
		cmc = null;
	}

	private void updateUI(UserRequest ureq) {
		documentSource = courseNode.getDocumentSource(courseFolderCont);
		
		boolean documentAvailable = documentSource.getVfsLeaf() != null;
		selectionCtrl.getInitialComponent().setVisible(!documentAvailable);
		
		documentDisplayCtrl.setDocumentSource(ureq ,documentSource);
		
		documentRightsCtrl.setVfsLeaf(ureq, documentSource.getVfsLeaf());
		documentRightsCtrl.getInitialComponent().setVisible(documentAvailable);
	}
	
	private void doPreview(UserRequest ureq) {
		removeAsListenerAndDispose(previewCtrl);
		previewCtrl = new DocumentRunController(ureq, getWindowControl(), courseNode, PREVIEW_SEC_CALLBACK, courseFolderCont, "o_cnd_preview");
		listenTo(previewCtrl);
		stackPanel.pushController(translate("config.preview"), previewCtrl);
	}
	
	private void doChangeDocument(UserRequest ureq) {
		selectionCtrl = new DocumentSelectionController(ureq, getWindowControl(), VFSManager.getQuotaLeftKB(courseFolderCont));
		listenTo(selectionCtrl);
	
		cmc = new CloseableModalController(getWindowControl(), "close", selectionCtrl.getInitialComponent(), true,
				translate("config.change.title"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSelectFromCourseFolder(UserRequest ureq) {
		VFSItemFilter filter = new VFSSystemItemFilter();
		fileChooserCtr = FileChooserUIFactory.createFileChooserController(ureq, getWindowControl(), courseFolderCont, filter, true);
		fileChooserCtr.setShowTitle(true);
		listenTo(fileChooserCtr);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), fileChooserCtr.getInitialComponent(),
				true, translate("config.select.title"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSetDocumentFromCourseFolder(UserRequest ureq, String selectedRelativeItemPath) {
		courseNode.setDocumentFromCourseFolder(selectedRelativeItemPath);
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void doSelectRepositoryEntry(UserRequest ureq) {
		repoSearchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq, FILE_TYPES, translate("config.select.entry"));
		listenTo(repoSearchCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), repoSearchCtrl.getInitialComponent(),
				true, translate("config.select.title"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private boolean acceptFile(File file, String fileName) {
		if(StringHelper.containsNonWhitespace(fileName)) {
			String lowercase = fileName.toLowerCase().trim();
			if (lowercase.endsWith(".html") || lowercase.endsWith(".htm")) {
				showWarning("error.single.page");
				return false;
			}
		}
		if (videoHandler.acceptImport(file, fileName).isValid()) {
			showWarning("error.video");
			return false;
		}
		return true;
	}
	
	private void doCreateDocument(UserRequest ureq, DocTemplate docTemplate) {
		DocTemplates templates = DocTemplates.builder(getLocale()).addFileType(docTemplate).build();
		createCtrl = new CreateDocumentController(ureq, getWindowControl(), courseFolderCont, templates, getConfigProvider());
		listenTo(createCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), createCtrl.getInitialComponent(),
				true, translate("config.create.title"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private Function<VFSLeaf, DocEditorConfigs> getConfigProvider() {
		return vfsLeaf -> {
			return DocEditorConfigs.builder().withMode(DocEditor.Mode.EDIT).build(vfsLeaf);
		};
	}
	
	private void doCopyToRepository(UserRequest ureq) {
		VFSLeaf vfsLeaf = documentSource.getVfsLeaf();
		if (vfsLeaf instanceof LocalFileImpl) {
			LocalFileImpl localFileImpl = (LocalFileImpl) vfsLeaf;
			
			copyToRepositoryCtrl = new CopyToRepositoryController(ureq, getWindowControl(), localFileImpl);
			listenTo(copyToRepositoryCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), copyToRepositoryCtrl.getInitialComponent(),
					true, translate("config.copy.title"));
			listenTo(cmc);
			cmc.activate();
		} else {
			showError("error.copy.to.repository");
		}
	}

	private void doAskCopyToCourse(UserRequest ureq, RepositoryEntry copyEntry) {
		copyToCourseCtrl = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), translate("config.copy.title"), translate("config.copy.to.course.text"));
		listenTo(copyToCourseCtrl);
		copyToCourseCtrl.setUserObject(copyEntry);
		copyToCourseCtrl.activate();
	}

	private void doCopyToCourse(UserRequest ureq, RepositoryEntry copyEntry) {
		OLATResource resource = copyEntry.getOlatResource();
		VFSContainer fResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(resource);
		for (VFSItem item : fResourceFileroot.getItems(new VFSSystemItemFilter())) {
			if (item instanceof VFSLeaf) {
				VFSLeaf sourceLeaf = (VFSLeaf)item;
				String filename = sourceLeaf.getName();
				filename = VFSManager.rename(courseFolderCont, filename);
				VFSLeaf targetLeaf = courseFolderCont.createChildLeaf(filename);
				VFSManager.copyContent(sourceLeaf, targetLeaf, true);
				
				VFSMetadata vfsMetadata = vfsRepositoryService.getMetadataFor(targetLeaf);
				vfsMetadata.setTitle(copyEntry.getDisplayname());
				if (licenseModule.isEnabled(repositoryEntryLicenseHandler) && licenseModule.isEnabled(folderLicenseHandler)) {
					OLATResource res = copyEntry.getOlatResource();
					ResourceLicense license = licenseService.loadLicense(res);
					if (license != null) {
						vfsMetadata.setLicenseType(license.getLicenseType() == null ? null : license.getLicenseType());
						vfsMetadata.setLicenseTypeName(license.getLicenseType() != null? license.getLicenseType().getName(): "");
						vfsMetadata.setLicensor(license.getLicensor() != null? license.getLicensor(): "");
						vfsMetadata.setLicenseText(LicenseUIFactory.getLicenseText(license));
					}
				}
				vfsRepositoryService.updateMetadata(vfsMetadata);
				
				documentSource = new DocumentSource(targetLeaf);
				doSetDocumentFromCourseFolder(ureq, filename);
			}
		}
	}

	private void doSetDocumentFromRepository(UserRequest ureq, RepositoryEntry newEntry) {
		courseNode.setDocumentFromRepository(newEntry);
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void doEditMetadata(UserRequest ureq) {
		VFSLeaf vfsLeaf = documentSource.getVfsLeaf();
		if (vfsLeaf == null) {
			showError("error.file.corrupt");
		} else {
			metadataCtrl = new MetaInfoFormController(ureq, getWindowControl(), vfsLeaf, null);
			listenTo(metadataCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), metadataCtrl.getInitialComponent(),
					true, translate("config.metadata.title"));
			listenTo(cmc);
			cmc.activate();
		}
	}

	private void doUpdateMetadata(VFSMetadata meta, String fileName) {
		if (meta != null) {
			vfsRepositoryService.updateMetadata(meta);
			if (metadataCtrl.isFileRenamed()) {
				VFSContainer container = documentSource.getVfsLeaf().getParentContainer();
				if (container.resolve(fileName) != null) {
					showError("error.file.name.in.use");
				} else {
					VFSStatus renameStatus = documentSource.getVfsLeaf().rename(fileName);
					if (VFSConstants.NO.equals(renameStatus)) {
						showError("error.file.not.renamed");
					}
				}
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private static final class PreviewSecurityCallback implements DocumentSecurityCallback {

		@Override
		public boolean canDownload() {
			return true;
		}

		@Override
		public boolean canEdit() {
			return false;
		}
	}

}
