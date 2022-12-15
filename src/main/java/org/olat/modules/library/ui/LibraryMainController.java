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
package org.olat.modules.library.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsBackController;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsConfig;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.modules.bc.commands.FolderCommand;
import org.olat.core.commons.modules.bc.comparators.TitleComparator;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.services.commentAndRating.ui.UserRatingChangedEvent;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.layout.MainLayout3ColumnsController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSContainerFilter;
import org.olat.modules.library.LibraryEvent;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.LibraryModule;
import org.olat.modules.library.model.CatalogItem;
import org.olat.modules.library.ui.event.OpenFileEvent;
import org.olat.modules.library.ui.event.OpenFolderEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * This is the main three columns layout controller for the library tab. It
 * contains a menu tree, a toolbox and the catalog controller.
 * 
 * <P>
 * Initial Date: Jun 16, 2009 <br>
 * 
 * @author gwassmann
 */
public class LibraryMainController extends MainLayoutBasicController implements GenericEventListener, Activateable2 {
	public static final String ICON_CSS_CLASS = "o_library_icon";
	private static final String GUI_CONF_LAYOUT_KEY = "library_layout_conf";
	private static final String I18N_UPLOAD_FOLDER_DISPLAYNAME = "library.upload.folder.displayname";

	private Link editLink;
	private Link reviewLink;
	private Link uploadLink;
	private MenuTree menuTree;
	private LibraryTreeModel treeModel;
	private VelocityContainer overviewVC;
	
	private CatalogController catalogCtr;
	private CloseableModalController dialogCtr;
	private FolderRunController editFolderCtr;
	private NewCatalogItemController newCatalogItemCtr;
	private NewestFilesController newestFilesCtr;
	private MostViewedFilesController mostViewedFilesCtr;
	private MostRatedFilesController mostRatedFilesCtr;
	private LayoutMain3ColsBackController editLayoutCtr;
	private FileUploadController uploadController;
	private CloseableModalController uploadModalController;
	private LayoutMain3ColsBackController reviewLayoutController;
	private ReviewController reviewController;
	private SearchQueryController searchCtr;
	private SearchCatalogItemController searchItemCtr;
	private MainLayout3ColumnsController columnLayoutCtr;
	
	private String basePath = "";
	private String mapperBaseURL;
	private String thumbnailMapperBaseURL;
	private OLATResourceable libraryOres;
	private VFSContainer sharedFolder;
	private VFSContainer currentFolder;
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private LibraryModule libraryModule;
	@Autowired
	private LibraryManager libraryManager;
	@Autowired
	private RepositoryService repositoryService;
	
	/**
	 * Constructor
	 * 
	 * @param ureq
	 * @param control
	 */
	public LibraryMainController(UserRequest ureq, WindowControl control) {	
		super(ureq, control);
		
		RepositoryEntry catalogEntry = libraryManager.getCatalogRepoEntry();
		if (catalogEntry != null) {
			Long resId = catalogEntry.getOlatResource().getResourceableId();
			basePath = "/repository/" + resId + "/" + libraryManager.getSharedFolder().getName();
			libraryOres = libraryManager.getLibraryResourceable();
			CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), libraryOres);
		}

		// url mapper
		final Mapper mapper = new LibraryMapper(libraryManager);
		final Mapper thumbnailMapper = new LibraryThumbnailMapper(basePath);
		mapperBaseURL = registerCacheableMapper(ureq, "LibrarySite", mapper);
		thumbnailMapperBaseURL = registerCacheableMapper(ureq, "LibraryThumbnail", thumbnailMapper);
		
		initCtrs(ureq, catalogEntry);
	}

	@Override
	public void event(Event event) {
		if(event instanceof LibraryEvent && LibraryEvent.DOWNLOAD.equals(event.getCommand())) {
			if(mostViewedFilesCtr != null && !mostViewedFilesCtr.isDisposed()) {
				mostViewedFilesCtr.updateView(getLocale());
			}
		} else if (event instanceof UserRatingChangedEvent) {
			if(mostRatedFilesCtr != null && !mostRatedFilesCtr.isDisposed()) {
				mostRatedFilesCtr.updateView(getLocale());
			}
		}
	}
	
	/**
	 * Initialize the content
	 * 
	 * @param ureq
	 */
	private void initCtrs(UserRequest ureq, RepositoryEntry catalogEntry) {
		// Menu tree
		menuTree = new MenuTree("menuTree");
		menuTree.setExpandSelectedNode(false);
		menuTree.addListener(this);
		initializeMenuTreeAndCatalog(ureq);

		overviewVC = createVelocityContainer("overview");
		overviewVC.contextPut("cssIconClass", ICON_CSS_CLASS);
		
		boolean isAdmin = ureq.getUserSession().getRoles().isAdministrator();
		boolean isOwner = false;
		if (catalogEntry != null) {
			isOwner = repositoryService.hasRoleExpanded(getIdentity(), catalogEntry,
					OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name(),
					GroupRoles.owner.name());
		}

		//admin tools
		if (isAdmin || isOwner) {
			// add edit link
			editLink = LinkFactory.createButton("edit", overviewVC, this);
			editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_edit");
			
		// add review link if there are documents to be reviewed
			reviewLink = LinkFactory.createButton("library.toolbox.review.singular", overviewVC, this);
			reviewLink.setIconLeftCSS("o_icon o_icon-lg o_icon_review");
			int numNewItems = libraryManager.getUploadFolder().getItems().size(); 
			if (numNewItems == 1) {
				reviewLink.setCustomDisplayText(translate("library.toolbox.review.singular"));
			} else if (numNewItems > 1){
				reviewLink.setCustomDisplayText(translate("library.toolbox.review.plural", Integer.toString(numNewItems)));
			} else {
				reviewLink.setVisible(false);
			}
		}
		uploadLink = LinkFactory.createButton("library.toolbox.upload", overviewVC, this);
		uploadLink.setCustomEnabledLinkCSS("btn btn-primary");
		uploadLink.setElementCssClass("o_sel_upload_document");
		
		//search
		searchCtr = new SearchQueryController(ureq, getWindowControl());
		listenTo(searchCtr);
		overviewVC.put("search", searchCtr.getInitialComponent());

		
		newestFilesCtr = new NewestFilesController(ureq, getWindowControl());
		listenTo(newestFilesCtr);
		overviewVC.put("newestFiles", newestFilesCtr.getInitialComponent());

		mostViewedFilesCtr = new MostViewedFilesController(ureq, getWindowControl());
		listenTo(mostViewedFilesCtr);
		overviewVC.put("mostViewedFiles", mostViewedFilesCtr.getInitialComponent());
		
		mostRatedFilesCtr = new MostRatedFilesController(ureq, getWindowControl(), libraryOres);
		listenTo(mostRatedFilesCtr);
		overviewVC.put("mostRatedFiles", mostRatedFilesCtr.getInitialComponent());


		OLATResourceable newOres = OresHelper.createOLATResourceableInstance("notifications", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(newOres, null, getWindowControl());
		newCatalogItemCtr = new NewCatalogItemController(ureq, bwControl, mapperBaseURL, thumbnailMapperBaseURL, libraryOres);
		listenTo(newCatalogItemCtr);
		
		// Use very large left column width
		LayoutMain3ColsConfig columnConfig = new LayoutMain3ColsConfig(22, 18);
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, null, overviewVC, GUI_CONF_LAYOUT_KEY,
				columnConfig);
		columnLayoutCtr.addCssClassToMain("o_library");
		listenTo(columnLayoutCtr);
		putInitialPanel(columnLayoutCtr.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		if(libraryOres != null) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, libraryOres);
		}
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree && event instanceof TreeEvent) {
			TreeEvent treeEvent = (TreeEvent) event;
			// get content and display folder
			String nodeId = treeEvent.getNodeId();
			if (treeModel == null) {
				// Special case when no shared folder is configured and user
				// presses the
				// link in the dummy menu: Nothing to do
				return;
			}
			if (nodeId.equals(treeModel.getRootNode().getIdent())) {
				// Root node is treated specially. Display welcome page.
				columnLayoutCtr.setCol3(overviewVC);
				currentFolder = null;
			} else {
				final String selectedPath = (String) treeModel.getNodeById(nodeId).getUserObject();
				if ("new.docs".equals(selectedPath)) {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.MONTH, -1);
					newCatalogItemCtr.updateUI(ureq, cal.getTime());
					columnLayoutCtr.setCol3(newCatalogItemCtr.getInitialComponent());
					addToHistory(ureq, newCatalogItemCtr);
				} else {
					String subCmd = treeEvent.getSubCommand();
					if(TreeEvent.COMMAND_TREENODE_OPEN.equals(subCmd)) {
						Collection<String> openNodeIds = menuTree.getOpenNodeIds();
						openNodeIds.add(nodeId);
						menuTree.setOpenNodeIds(openNodeIds);
					} else if (TreeEvent.COMMAND_TREENODE_CLOSE.equals(subCmd)) {
						Collection<String> openNodeIds = menuTree.getOpenNodeIds();
						openNodeIds.remove(nodeId);
						menuTree.setOpenNodeIds(openNodeIds);

					} else {
						OLATResourceable ores = getSelectedPathResource(selectedPath);
						addToHistory(ureq, ores, null);
						activateDirectory(ureq, selectedPath, null);
					}	
				}
			}
		} else if (source == editLink) {
			displayEditController(ureq);
		} else if (source == uploadLink) {
			displayUploadController(ureq);
		} else if (source == reviewLink) {
			displayReviewController(ureq);
		}
	}
	
	private OLATResourceable getSelectedPathResource(String relativePath) {
		String directoryPath = libraryManager.getDirectoryPath();
		String path;
		if(relativePath != null && directoryPath != null) {
			int index = relativePath.indexOf(directoryPath);
			if(index >= 0) {
				int start = index + directoryPath.length();
				path = relativePath.substring(start);
			} else {
				path = relativePath;
			}
		} else {
			path = "/";
		}
		return OresHelper.createOLATResourceableTypeWithoutCheck("path=" + path);
	}
	
	private void doSearch(UserRequest ureq) {
		removeAsListenerAndDispose(searchItemCtr);
		
		try {
			String query = searchCtr.getSearchQuery();
			if(!StringHelper.containsNonWhitespace(query)) {
				return;
			}

			searchItemCtr = new SearchCatalogItemController(ureq, getWindowControl(), query, mapperBaseURL, thumbnailMapperBaseURL, libraryOres);
			listenTo(searchItemCtr);
			columnLayoutCtr.setCol3(searchItemCtr.getInitialComponent());
			searchItemCtr.doSearch(ureq);
		} catch (Exception e) {
			logError("Unexpected error while searching in library", e);
		}
	}

	/**
	 * Internal helper to activate a certain directory level
	 * 
	 * @param ureq
	 * @param selectedPath
	 * @param fileName the name of a file that is in the given directory. If not
	 *          NULL, a download of this will be triggered automatically
	 */
	public String activateDirectory(UserRequest ureq, String selectedPath, String fileName) {
		File selectedFilePath = new File(FolderConfig.getCanonicalRoot(), selectedPath);
		if (selectedFilePath.exists()) {
			currentFolder = VFSManager.olatRootContainer(selectedPath, sharedFolder);
			
			VFSItem file = currentFolder.resolve(fileName);
			if(fileName != null && file instanceof VFSContainer) {
				currentFolder = (VFSContainer)file;
				selectedPath += (selectedPath.endsWith("/") ? "" : "/") + fileName;
				fileName = null;
			}
			
			catalogCtr.display(currentFolder, fileName);
			columnLayoutCtr.setCol3(catalogCtr.getInitialComponent());

			if (selectedPath.endsWith("/")) {
				selectedPath = selectedPath.substring(0, selectedPath.length() - 1);
			}
			TreeNode node = treeModel.findNodeByUserObject(selectedPath);
			menuTree.setSelectedNode(node);
			return selectedPath;
		} else {
			// the menu tree is out of date - update
			initializeMenuTreeAndCatalog(ureq);
			// do not update the catalog view
			showWarning("library.catalog.folder.not.found");
			return null;
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == dialogCtr) {
			//
		} else if (source == editLayoutCtr) {
			if (event == Event.BACK_EVENT) {
				removeAsListenerAndDispose(editLayoutCtr);
				editLayoutCtr = null;
				initializeMenuTreeAndCatalog(ureq);
				displayCurrentFolder(ureq);
			}
		} else if (source == uploadController) {
			if (event == Event.DONE_EVENT) {
				doUploadDocument();
			} else if (event == Event.CANCELLED_EVENT) {
				uploadModalController.deactivate();
				cleanUpUploadController();
			}
		} else if (source == editFolderCtr) {
			if (event == FolderCommand.FOLDERCOMMAND_FINISHED) {
				libraryManager.markPublisherNews();
				newestFilesCtr.updateView(ureq.getLocale());
			}
		} else if (source == newCatalogItemCtr) {
			if (event instanceof OpenFolderEvent) {
				openFolder(ureq, (OpenFolderEvent)event);
			} else if (event instanceof OpenFileEvent) {
				openFile(ureq, (OpenFileEvent)event);
			}
		} else if (source == reviewLayoutController) {
			if (event == Event.BACK_EVENT) {
				removeAsListenerAndDispose(reviewLayoutController);
				reviewLayoutController = null;
				initializeMenuTreeAndCatalog(ureq);
				displayCurrentFolder(ureq);
			}
		} else if (source == reviewController) {
			if (event == Event.CHANGED_EVENT) {
				// a new file has been accepted by a reviewer
				newestFilesCtr.updateView(ureq.getLocale());
			}
		} else if (source == searchItemCtr) {
			removeAsListenerAndDispose(searchItemCtr);
			if (event == Event.BACK_EVENT) {
				displayCurrentFolder(ureq);
				if (currentFolder != null) {
					columnLayoutCtr.setCol3(catalogCtr.getInitialComponent());
				}
			} else if (event instanceof OpenFolderEvent) {
				openFolder(ureq, (OpenFolderEvent)event);
			} else if (event instanceof OpenFileEvent) {
				openFile(ureq, (OpenFileEvent)event);
			}
		} else if (source == searchCtr) {
			doSearch(ureq);
		} else if (source == newestFilesCtr || source == mostViewedFilesCtr || source == mostRatedFilesCtr) {
			if (event instanceof OpenFolderEvent) {
				openFolder(ureq, (OpenFolderEvent)event);
			} else if (event instanceof OpenFileEvent) {
				openFile(ureq, (OpenFileEvent)event);
			}
		}
	}
	
	private void openFile(UserRequest ureq, OpenFileEvent openEvent) {
		VFSMetadata metadata = openEvent.getItem().getMetaInfo();
		String dirPath = "/" + metadata.getRelativePath();
		activateDirectory(ureq, dirPath, metadata.getFilename());
	}
	
	private void openFolder(UserRequest ureq, OpenFolderEvent openFolderEvent) {
		CatalogItem item = openFolderEvent.getItem();
		String fileName = item.getName();
		String relativePath = item.getRelativePath();
		String dirPath = relativePath.substring(0, relativePath.length() - fileName.length());
		activateDirectory(ureq, dirPath, null);
	}
	
	private void cleanUpUploadController() {
		if(uploadModalController != null) {
			removeAsListenerAndDispose(uploadController);
			removeAsListenerAndDispose(uploadModalController);
			uploadController = null;
			uploadModalController = null;
		}
	}

	/**
	 * Displays the current folder or the first folder of the resource folder or
	 * nothing.
	 * 
	 * @param ureq
	 */
	private void displayCurrentFolder(UserRequest ureq) {
		if (currentFolder != null && currentFolder.exists()) {
			// the current folder still exists, display it
			catalogCtr.display(currentFolder, null);
			String relPath = currentFolder.getRelPath();
			// reselect the node in the menu tree
			TreeNode node = treeModel.findNodeByUserObject(relPath);
			if(node != null) {
				menuTree.setSelectedNode(node);
			}
		} else {
			// display the overview panel
			columnLayoutCtr.setCol3(overviewVC);
		}
	}

	/**
	 * Displays the shared folder edit controller
	 * 
	 * @param ureq
	 */
	private void displayEditController(UserRequest ureq) {
		if (sharedFolder != null) {
			if (editFolderCtr != null) {
				removeAsListenerAndDispose(editFolderCtr);
			}
			editFolderCtr = new FolderRunController(sharedFolder, true, ureq, getWindowControl());
			listenTo(editFolderCtr);

			if (editLayoutCtr != null) {
				removeAsListenerAndDispose(editLayoutCtr);
			}
			editLayoutCtr = new LayoutMain3ColsBackController(ureq, getWindowControl(), null, editFolderCtr.getInitialComponent(), null);
			editLayoutCtr.addDisposableChildController(editFolderCtr);
			editLayoutCtr.addCssClassToMain("o_editor");
			listenTo(editLayoutCtr);
			editLayoutCtr.activate();
		} else {
			showInfo("library.catalog.folder.cannot.be.edited");
		}
	}

	/**
	 * Displays the catalog based on the shared folder.
	 */
	private void initializeMenuTreeAndCatalog(UserRequest ureq) {
		removeAsListenerAndDispose(catalogCtr);
		sharedFolder = libraryManager.getSharedFolder();
		if (sharedFolder != null) {
			// Rebuild Catalog controller
			catalogCtr = new CatalogController(ureq, getWindowControl(), mapperBaseURL, thumbnailMapperBaseURL, libraryOres);
			listenTo(catalogCtr);

			// build a tree model
			treeModel = new LibraryTreeModel(sharedFolder, new VFSContainerFilter(), new TitleComparator(getLocale()), getLocale(), true);
			treeModel.getRootNode().setTitle(translate("main.menu.title"));
			menuTree.setTreeModel(treeModel);
		} else {
			// remove catalog controller
			catalogCtr = null;

			// dummy model (empty)
			GenericTreeNode rootNode = new GenericTreeNode();
			rootNode.setTitle(translate("main.menu.title"));
			rootNode.setUserObject("library");
			GenericTreeModel gtm = new GenericTreeModel();
			gtm.setRootNode(rootNode);
			menuTree.setTreeModel(gtm);
		}

		menuTree.setOpenNodeIds(Collections.singleton(menuTree.getTreeModel().getRootNode().getIdent()));
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.remove(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		
		if (type.startsWith("path=")) {
			// trimm path= and :0 at the end of the string
			String path = BusinessControlFactory.getInstance().getPath(entry);
			int lastSlash = path.lastIndexOf("/");
			if (lastSlash != -1) {
				String dirPath = path.substring(0, lastSlash);
				String fileName = path.substring(lastSlash + 1);
				//compatibility between old jump-in / new businesspath
				if (fileName.endsWith(":0")) {
					fileName = fileName.split(":")[0];
				}
				RepositoryEntry repoEntry = libraryManager.getCatalogRepoEntry();
				if (repoEntry == null) return;
				dirPath = libraryManager.getDirectoryPath() + dirPath;
				// finally activate the directory
				
				String activatedPath = activateDirectory(ureq, dirPath, fileName);
				OLATResourceable ores = getSelectedPathResource(activatedPath);
				addToHistory(ureq, ores, null);
			}
		} else if (type.startsWith("uuid=")) {
			String uuid = type.substring(type.indexOf("=") + 1);
			int nameIndex = uuid.indexOf('/');
			if(nameIndex > 0) {
				uuid = uuid.substring(0, nameIndex);
			}
			VFSLeaf file = libraryManager.getFileByUUID(uuid);
			if(file instanceof LocalFileImpl && file.getParentContainer() instanceof LocalFolderImpl) {
				LocalFileImpl olatFile = (LocalFileImpl)file;
				LocalFolderImpl olatFolder = (LocalFolderImpl)olatFile.getParentContainer();
				
				String activatedPath = activateDirectory(ureq, olatFolder.getRelPath(), olatFile.getName());
				OLATResourceable ores = getSelectedPathResource(activatedPath);
				addToHistory(ureq, ores, null);
			}
		} else if (type.startsWith("notifications") || type.startsWith("library.newdocuments")) {
			columnLayoutCtr.setCol3(newCatalogItemCtr.getInitialComponent());
			TreeNode node = treeModel.findNodeByUserObject(LibraryTreeModel.NEW_DOCS_USER_OBJ);
			menuTree.setSelectedNode(node);
			addToHistory(ureq, newCatalogItemCtr);
		} else {
			addToHistory(ureq);
		}
	}

	/**
	 * Displays the upload controller.
	 * 
	 * @param ureq The user request.
	 */
	private void displayUploadController(UserRequest ureq) {
		if (uploadModalController != null) {
			removeAsListenerAndDispose(uploadController);
			removeAsListenerAndDispose(uploadModalController);
		}

		VFSContainer namedUploadFolder = new NamedContainerImpl(translate(I18N_UPLOAD_FOLDER_DISPLAYNAME), libraryManager.getUploadFolder());
		FolderComponent folderComponent = new FolderComponent(ureq, "folderComponent", namedUploadFolder, null, null);
		folderComponent.addListener(this);
		uploadController = new FileUploadController(getWindowControl(), namedUploadFolder, ureq, 1024 * 1024, 1024 * 1024, null,false,
				true, true, true, true, true);
		listenTo(uploadController);
		uploadModalController = new CloseableModalController(getWindowControl(), translate("close"), uploadController.getInitialComponent());
		listenTo(uploadModalController);
		uploadModalController.activate();
	}
	
	private void displayReviewController(UserRequest ureq) {
		if (libraryManager.getUploadFolder() != null && !libraryManager.getUploadFolder().getItems().isEmpty()) {
			if (reviewController != null) {
				removeAsListenerAndDispose(this.reviewController);
			}
			reviewController = new ReviewController(ureq, getWindowControl());
			listenTo(this.reviewController);
			
			if (reviewLayoutController != null) {
				removeAsListenerAndDispose(this.reviewLayoutController);
			}
			reviewLayoutController = new LayoutMain3ColsBackController(ureq, getWindowControl(), null, reviewController.getInitialComponent(), null);
			reviewLayoutController.addDisposableChildController(this.reviewController);
			listenTo(this.reviewLayoutController);
			reviewLayoutController.activate();
		} else {
			showError("library.review.error");
		}
	}
	
	private void doUploadDocument() {
		String newFileName = uploadController.getNewFileName();
		uploadModalController.deactivate();
		cleanUpUploadController();
		if (sharedFolder != null) {
			// notify user
			showInfo("library.uploadnotification.success", newFileName);
			// create an e-mailer
			String contact = libraryModule.getEmailContactsToNotifyAfterUpload();
			if(StringHelper.containsNonWhitespace(contact)) {
				// create an e-mailer and send the mail.
				try {
					MailBundle bundle = new MailBundle();
					bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
					bundle.setContent(translate("library.uploadnotification.subject"),
							translate("library.uploadnotification.body", newFileName));
					bundle.setTo(contact);
					mailManager.sendMessage(bundle);
				} catch (Exception e) {
					logWarn("Could not send mail to owner group of library shared folder.", e);
				}
				
			} else {
				// Retrieve a list of the shared folder's owners.
				RepositoryEntry re = libraryManager.getCatalogRepoEntry();
				List<Identity> sharedFolderOwners = repositoryService
						.getMembers(re, RepositoryEntryRelationType.all, GroupRoles.owner.name());
				
				// Put these owners into a list of ContactLists.
				List<ContactList> recipients = new ArrayList<>();
				for (Identity identity : sharedFolderOwners) {
					ContactList contactList = new ContactList(identity.getName());
					contactList.add(identity);
					recipients.add(contactList);
				}
				try {
					MailBundle bundle = new MailBundle();
					bundle.setContent(translate("library.uploadnotification.subject"),
							translate("library.uploadnotification.body", newFileName));
					bundle.setContactLists(recipients);
					mailManager.sendMessage(bundle);
				} catch (Exception e) {
					logWarn("Could not send mail to owner group of library shared folder.", e);
				}
			}
		} else {
			logWarn("Could not send mail to owner group of library shared folder.", null);
		}
	}
}
