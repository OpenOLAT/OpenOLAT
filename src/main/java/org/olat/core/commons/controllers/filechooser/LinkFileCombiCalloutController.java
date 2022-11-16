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
package org.olat.core.commons.controllers.filechooser;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.IconPanel;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent.LabelText;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSContainerFilter;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSItemSuffixFilter;
import org.olat.core.util.vfs.filters.VFSOrFilter;
import org.olat.course.run.tools.CourseToolLinkTreeModel;
import org.olat.modules.edusharing.VFSEdusharingProvider;


/**
 * Description:
 * <p>This controller provides a view with three link options:
 * to browse for a file, to create a new file or to upload a file. 
 * </p>
 * 
 * Initial date: 18.12.2014<br>
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class LinkFileCombiCalloutController extends BasicController {
	
	private static final Set<String> MIME_TYPE_HTML = Set.of("text/html", "application/zip");
	
	private VelocityContainer contentVC;
	private EmptyState fileNotAvailableCmp;
	private IconPanel fileCmp;
	private IconPanelLabelTextContent contentCmp;
	private Link createLink;
	private Link selectLink;
	private Link importLink;
	private Link previewLink;
	private Link createCmdLink;
	private Link importCmdLink;
	private Link removeLink;
	private Link editLink;
	
	private CustomLinkTreeModel customLinkTreeModel;
	private CustomLinkTreeModel courseToolLinkTreeModel;
	
	private CloseableModalController cmc;
	private Controller currentModalController;
	private LayoutMain3ColsPreviewController previewLayoutCtr;

	private final VFSContainer baseContainer;
	private VFSLeaf file;

	private String relFilePath;
	private final String defaultRelFilePath;
	private boolean editable = true;
	private boolean relFilPathIsProposal;
	private boolean allowEditorRelativeLinks;
	private final VFSEdusharingProvider edusharingProvider;

	/**
	 * 
	 * @param ureq
	 *            User request
	 * @param wControl
	 *            Window control
	 * @param baseContainer
	 *            container from which files can be selected or where files can
	 *            be stored
	 * @param relFilePath
	 *            path relative to course folder of already selected file, null
	 *            if no file present yet
	 * @param relFilPathIsProposal
	 *            if true, relFilePath is just a proposal which can be changed
	 *            by user
	 * @param allowEditorRelativeLinks
	 *            true: editor can link to all files from baseContainer false:
	 *            editor can link only to files relative to the position of the
	 *            edited file;
	 * @param customLinkTreeModel
	 *            The custom link tree model or NULL if no link tree model used
	 *            in HTML editor
	 * @param courseToolLinkTreeModel 
	 * @param courseToolLinkTreeModel 
	 * @param edusharingProviderm
	 *            Enable content from edu-sharing with this provider
	 */
	public LinkFileCombiCalloutController(UserRequest ureq, WindowControl wControl, VFSContainer baseContainer,
			String relFilePath, boolean relFilPathIsProposal, boolean allowEditorRelativeLinks, boolean allowRemove,
			CustomLinkTreeModel customLinkTreeModel, CourseToolLinkTreeModel courseToolLinkTreeModel,
			VFSEdusharingProvider edusharingProvider) {
		super(ureq, wControl);
		this.baseContainer = baseContainer;
		this.defaultRelFilePath = relFilePath;
		this.relFilPathIsProposal = relFilPathIsProposal;
		this.allowEditorRelativeLinks = allowEditorRelativeLinks;
		this.customLinkTreeModel = customLinkTreeModel;
		this.courseToolLinkTreeModel = courseToolLinkTreeModel;
		this.edusharingProvider = edusharingProvider;
		
		// Main container for everything
		contentVC = createVelocityContainer("combi_file_callout");
		putInitialPanel(contentVC);
		
		fileNotAvailableCmp = EmptyStateFactory.create("file.not.available", contentVC, this);
		fileNotAvailableCmp.setIconCss("o_icon o_filetype_html");
		fileNotAvailableCmp.setMessageI18nKey("file.not.available.message");
		
		createLink = LinkFactory.createButton("create", contentVC, this);
		createLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		createLink.setElementCssClass("o_sel_filechooser_create");
		createLink.setPrimary(true);
		selectLink = LinkFactory.createButton("select", contentVC, this);
		selectLink.setIconLeftCSS("o_icon o_icon-fw o_icon_search");
		importLink = LinkFactory.createButton("import", contentVC, this);
		importLink.setIconLeftCSS("o_icon o_icon-fw o_icon_import");
		importLink.setElementCssClass("o_sel_filechooser_import");
		
		fileCmp = new IconPanel("file.available");
		fileCmp.setElementCssClass("o_block_bottom");
		fileCmp.setIconCssClass("o_icon o_icon-fw o_filetype_html");
		contentVC.put(fileCmp.getComponentName(), fileCmp);
		
		contentCmp = new IconPanelLabelTextContent("content");
		fileCmp.setContent(contentCmp);
		
		editLink = LinkFactory.createButton("edit.page", contentVC, this);
		editLink.setElementCssClass("o_sel_filechooser_edit");
		editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		editLink.setGhost(true);
		fileCmp.addLink(editLink);
		
		previewLink = LinkFactory.createButton("preview", contentVC, this);
		previewLink.setElementCssClass("o_sel_filechooser_preview");
		previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		
		Dropdown commandsDropdown = new Dropdown("commands", null, true, getTranslator());
		commandsDropdown.setDomReplaceable(false);
		commandsDropdown.setButton(true);
		commandsDropdown.setOrientation(DropdownOrientation.right);
		contentVC.put(commandsDropdown.getComponentName(), commandsDropdown);
		
		createCmdLink = LinkFactory.createCustomLink("create.cmd", "create", "create", Link.LINK, contentVC, this);
		createCmdLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		commandsDropdown.addComponent(createCmdLink);
		importCmdLink = LinkFactory.createCustomLink("import.cmd", "import", "import", Link.LINK, contentVC, this);
		importCmdLink.setIconLeftCSS("o_icon o_icon-fw o_icon_import");
		commandsDropdown.addComponent(importCmdLink);
		
		if(allowRemove) {
			removeLink = LinkFactory.createLink("command.remove", contentVC, this);
			removeLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			commandsDropdown.addComponent(removeLink);
		}
		
		// Load file from configuration and update links
		setRelFilePath(relFilePath);
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
		updateUI();
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == createLink || source == createCmdLink) {
			doOpenCreate(ureq);
		} else if (source == selectLink) {
			doOpenSelect(ureq);
		} else if (source == importLink || source == importCmdLink) {
			doOpenImport(ureq);
		} else if(source == editLink){
			doOpenWysiwygEditor(ureq);
		} else if (source == previewLink){
			doShowPreview(ureq);
		} else if(removeLink == source) {
			doRemove();
			fireEvent(ureq, new FileRemoveEvent());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof FileChooserController) {
			// catch the events from the file chooser controller here
			if (event instanceof FileChoosenEvent) {
				FileChoosenEvent fce = (FileChoosenEvent)event;
				file = (VFSLeaf)FileChooserUIFactory.getSelectedItem(fce);
				relFilPathIsProposal = false;
				setRelFilePath(FileChooserUIFactory.getSelectedRelativeItemPath(fce, baseContainer, null));
				fireEvent(ureq, Event.DONE_EVENT);
			} else if (event == Event.FAILED_EVENT) {
				// selection failed for unknown reason
			} else if(event == Event.CANCELLED_EVENT){
				// nothing to do
			}
			cleanupModal(true);
		} else if (source instanceof FileUploadController) {
			if(event == Event.DONE_EVENT){
				FileUploadController uploadCtr = (FileUploadController) source;
				VFSLeaf newFile = uploadCtr.getUploadedFile();
				if (newFile.getName().toLowerCase().endsWith("zip")) {
					// Cleanup modal first
					cleanupModal(true);
					// Unzip file and open file chooser in new modal
					VFSContainer zipContainer = doUnzip(newFile, newFile.getParentContainer());
					if (zipContainer != null) {
						FileChooserController fileChooserCtr = FileChooserUIFactory.createFileChooserController(ureq, getWindowControl(), zipContainer, null, true);
						fileChooserCtr.setShowTitle(true);
						displayModal(fileChooserCtr);
						return;						
					}
				} else {
					// All other files				
					file = uploadCtr.getUploadedFile();
					relFilPathIsProposal = false;
					setRelFilePath(VFSManager.getRelativeItemPath(file, baseContainer, null));
					fireEvent(ureq, Event.DONE_EVENT);
				}
			} else if (event.getCommand().equals(FolderEvent.UPLOAD_EVENT)) {
				// Do nothing, ignore this internal event. When finished, done is fired
				return;
			}
			cleanupModal(true);
		} else if (source instanceof HTMLEditorController) {
			if(event == Event.DONE_EVENT){
				relFilPathIsProposal = false;
				fireEvent(ureq, Event.DONE_EVENT);
			}
			cleanupModal(true);
			updateUI();
		} else if (source instanceof FileCreatorController) {
			if(event == Event.DONE_EVENT){
				FileCreatorController createCtr = (FileCreatorController) source;
				file = createCtr.getCreatedFile();
				relFilPathIsProposal = false;
				setRelFilePath(VFSManager.getRelativeItemPath(file, baseContainer, null));
				fireEvent(ureq, Event.DONE_EVENT);
				cleanupModal(true);
				doOpenWysiwygEditor(ureq);
			} else if (event == Event.CANCELLED_EVENT){
				cleanupModal(true);
			}
		} else if (source == cmc && event == CloseableModalController.CLOSE_MODAL_EVENT) {
			// User closed dialog, same as cancel in a sub-controller
			cleanupModal(false);			
		} else if (source == previewLayoutCtr && event == Event.BACK_EVENT) {
			removeAsListenerAndDispose(previewLayoutCtr);
		}
		
		super.event(ureq, source, event);
	}
	
	private void doOpenCreate(UserRequest ureq) {
		String folderPath = null;
		String fileName = null;
		if (StringHelper.containsNonWhitespace(relFilePath)) {
			int folderFileIndex = relFilePath.lastIndexOf("/");
			folderPath = relFilePath.substring(0, folderFileIndex);
			fileName = relFilePath.substring(folderFileIndex + 1);
		}
		FileCreatorController fileCreatorCtrl = new FileCreatorController(ureq, getWindowControl(), baseContainer, folderPath, fileName);
		displayModal(fileCreatorCtrl);
	}
	
	private void doOpenSelect(UserRequest ureq) {
		VFSItemFilter filter = new VFSOrFilter(List.of(
				new VFSContainerFilter(),
				new VFSItemSuffixFilter(new String[] { "html", "htm" })));
		FileChooserController fileChooserCtrl = FileChooserUIFactory.createFileChooserController(ureq, getWindowControl(), baseContainer, filter, true);
		fileChooserCtrl.setShowTitle(true);
		fileChooserCtrl.selectPath(relFilePath);
		displayModal(fileChooserCtrl);
	}
	
	private void doOpenImport(UserRequest ureq) {
		long quotaLeftKB = VFSManager.getQuotaLeftKB(baseContainer);
		String folderPath = null;
		if (StringHelper.containsNonWhitespace(relFilePath)) {
			// remove file name from relFilePath to represent directory path
			folderPath = relFilePath.substring(0, relFilePath.lastIndexOf("/"));
		}
		FileUploadController fileUploadCtrl = new FileUploadController(getWindowControl(), baseContainer, ureq,
				quotaLeftKB, quotaLeftKB, MIME_TYPE_HTML, false, true, false, false, true, true, folderPath);
		displayModal(fileUploadCtrl);
	}
	
	private void doOpenWysiwygEditor(UserRequest ureq) {
		if(relFilPathIsProposal){
			file = VFSManager.resolveOrCreateLeafFromPath(baseContainer, relFilePath);
		}
		if (file == null) {
			// huh? no idea what happend, do nothing and log error
			logError("Could not load or create file with relFilePath::" + relFilePath + " in baseContainer::" + VFSManager.getRealPath(baseContainer), null);
			return;
		}
		// Configure editor depending on limitEditorToRelativeFiles flag
		// either based on baseContainer or the files direct parent
		VFSContainer editorBaseContainer = baseContainer;
		String editorRelPath = relFilePath;
		if (!allowEditorRelativeLinks && relFilePath.indexOf("/", 1) != 0) {
			editorBaseContainer  = file.getParentContainer();
			editorRelPath = file.getName();
		}
		// Open HTML editor in dialog
		HTMLEditorController wysiwygCtr = WysiwygFactory.createWysiwygControllerWithInternalLink(ureq,
				getWindowControl(), editorBaseContainer, editorRelPath, true, customLinkTreeModel,
				courseToolLinkTreeModel, edusharingProvider);
		displayModal(wysiwygCtr);
	}
	
	private void doShowPreview(UserRequest ureq) {
		SinglePageController previewController = new SinglePageController(ureq, getWindowControl(), file.getParentContainer(), file.getName(), false);
		previewLayoutCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null, previewController.getInitialComponent(), null);
		previewLayoutCtr.addDisposableChildController(previewController);
		previewLayoutCtr.activate();
		listenTo(previewLayoutCtr);
	}

	private VFSContainer doUnzip(VFSLeaf vfsItem, VFSContainer currentContainer) {
		String name = vfsItem.getName();
		// we make a new folder with the same name as the zip file
		String sZipContainer = name.substring(0, name.length() - 4);

		// if zip already exists, create another folder 1,2,3 etc.
		VFSContainer zipContainer = currentContainer.createChildContainer(sZipContainer);
		int i = 1;
		while (zipContainer == null && i<100) {
			i++;
			sZipContainer = FileUtils.appendNumberAtTheEndOfFilename(sZipContainer, i);
			zipContainer = currentContainer.createChildContainer(sZipContainer);
		}
		if (!ZipUtil.unzip(vfsItem, zipContainer)) {
			// operation failed - rollback
			zipContainer.delete();
			return null;
		} else {
			// check quota
			long quotaLeftKB = VFSManager.getQuotaLeftKB(currentContainer);
			if (quotaLeftKB != Quota.UNLIMITED && quotaLeftKB < 0) {
				// quota exceeded - rollback
				zipContainer.delete();
				getWindowControl().setError(translate("QuotaExceeded"));
				return null;
			}
		}
		return zipContainer;
	}
	
	private void doRemove() {
		file = null;
		relFilePath = defaultRelFilePath;
		updateUI();
	}

	/**
	 * Helper to cleanup the current modal and its content controller
	 * 
	 * @param pop
	 *            true: pop dialog from stack; false: don't pop dialog, already
	 *            done
	 */
	private void cleanupModal(boolean pop) {
		if (pop) {
			cmc.deactivate();
		}
		removeAsListenerAndDispose(currentModalController);
		removeAsListenerAndDispose(cmc);
	}
	/**
	 * Helper to setup the modal controller for the content and activate it
	 * @param newModalContentController
	 */
	private void displayModal(Controller newModalContentController) {
		if (newModalContentController == null) return;
		
		currentModalController = newModalContentController;
		listenTo(currentModalController);
		cmc = new CloseableModalController(getWindowControl(), "close", newModalContentController.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	private void updateUI(){
		boolean fileAvailable = file != null;
		contentVC.contextPut("fileAvailable", Boolean.valueOf(fileAvailable));
		
		String filename = null;
		if (fileAvailable) {
			filename = file.getName();
			int suffixIndex = filename.lastIndexOf(".");
			if (suffixIndex > 0) {
				filename = filename.substring(0, suffixIndex);
			}
		}
		fileCmp.setTitle(filename);
		
		
		if (fileAvailable) {
			List<LabelText> labelTexts = new ArrayList<>(2);
			labelTexts.add(new LabelText(translate("html.page"), relFilePath));
			String lastModified = file.getMetaInfo() != null
					? Formatter.getInstance(getLocale()).formatDateAndTime(file.getMetaInfo().getLastModified())
					: null;
			labelTexts.add(new LabelText(translate("file.last.modified"), lastModified));
			contentCmp.setLabelTexts(labelTexts);
		}
		
		// Enable edit link when file is editable 
		fileCmp.removeLink(editLink.getComponentName());
		if (isEditorEnabled()){
			fileCmp.addLink(editLink);
		}
	}
	
	public void setAllowEditorRelativeLinks(boolean allowEditorRelativeLinks) {
		this.allowEditorRelativeLinks = allowEditorRelativeLinks;
	}
	
	public void setRelFilePath(String relFilePath) {
		this.relFilePath = relFilePath;
		if(StringHelper.containsNonWhitespace(relFilePath)) {
			VFSItem item = baseContainer.resolve(relFilePath);
			if(!(item instanceof VFSContainer)) {
				file = (VFSLeaf)item;
				if (file == null && !this.relFilPathIsProposal) {
					// System assumed that this page would exist. Maybe deleted by
					// someone in folder. Tell user and offer to create the page
					// again. 
					this.relFilPathIsProposal = true;				
					contentVC.contextPut("deleted", Boolean.valueOf(true));
				}
			}
		}
		
		updateUI();
	}

	public boolean isDoProposal() {
		return relFilPathIsProposal;
	}
	
	public VFSLeaf getFile(){
		return file;
	}
	
	/**
	 * @return The path of the file relative to the base container.
	 */
	public String getRelativeItemPath() {
		return VFSManager.getRelativeItemPath(getFile(), baseContainer, null);
	}
	
	public boolean isEditorEnabled() {
		// enable html editor for html files
		return editable && isHtmlFile();
	}
	
	public boolean isHtmlFile() {
		if(StringHelper.containsNonWhitespace(relFilePath)) {
			String lowercase = relFilePath.toLowerCase().trim();
			if (lowercase.endsWith(".html") || lowercase.endsWith(".htm")) {
				return true;
			}
		}
		return false;		
	}
}