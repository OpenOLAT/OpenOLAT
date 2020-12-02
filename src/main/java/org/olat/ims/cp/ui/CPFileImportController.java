/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.ims.cp.ui;

import java.util.Collection;
import java.util.LinkedList;

import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.ContentPackage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * File import controller. Supported files: - html - zip (including folders and
 * html files)
 * <p>
 * Fires: NewCPPageEvent
 * <P>
 * Initial Date: May 5, 2009 <br>
 * 
 * @author gwassmann
 */
public class CPFileImportController extends FormBasicController {
	private static final String ALL = "all";
	private static final VFSItemFilter excludeMetaFilesFilter = new VFSSystemItemFilter();

	private FileElement file;
	private FormLink cancelButton;
	private MultipleSelectionElement checkboxes;
	private ContentPackage cp;
	private CPPage currentPage;
	private CPPage pageToBeSelected = null;
	private boolean isSingleFile;
	
	@Autowired
	private CPManager cpManager;

	public CPFileImportController(UserRequest ureq, WindowControl control, ContentPackage cp, CPPage currentPage) {
		super(ureq, control);
		this.cp = cp;
		this.currentPage = currentPage;
		
		//need a translation from FileUploadController (avoiding key-duplicates)
		setTranslator(Util.createPackageTranslator(FileUploadController.class, getLocale(), getTranslator()));
		
		initForm(ureq);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		String fileName = file.getUploadFileName();
		if (fileName == null) {
			file.setErrorKey("NoFileChosen", null);
			return false;
		}
		if (file.getUploadSize() / 1024 > file.getMaxUploadSizeKB()) {
			return false;
		}
		
		return super.validateFormLogic(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_cp_import");
		setFormDescription("cpfileuploadcontroller.form.description");

		file = uifactory.addFileElement(getWindowControl(), getIdentity(), "file", flc);
		file.setLabel("cpfileuploadcontroller.import.text", null);
		file.addActionListener(FormEvent.ONCHANGE);
		
		Long uploadLimitKb = getUploadLimitKb();
		if(uploadLimitKb != null) {
			Long uploadLimitMb =  new Long(uploadLimitKb / 1024);
			file.setMaxUploadSizeKB(uploadLimitKb.intValue(), "cpfileuploadcontroller.tooBig", new String[]{ uploadLimitMb.toString() });
		}

		// checkboxes
		String[] keys = { "htm", "pdf", "doc", "xls", "ppt", ALL };
		String[] values = { "HTML", "PDF", "Word", "Excel", "PowerPoint", translate("cpfileuploadcontroller.form.all.types") };
		checkboxes = uifactory.addCheckboxesVertical("checkboxes", "cpfileuploadcontroller.form.file.types", this.flc, keys, values, 1);
		checkboxes.setVisible(false);

		// Submit and cancel buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		this.flc.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", "cpfileuploadcontroller.import.button", buttonLayout);
		cancelButton = uifactory.addFormLink("cancel", buttonLayout, Link.BUTTON);
	}
	
	private Long getUploadLimitKb() {
		if(cp.getRootDir() != null && cp.getRootDir().getLocalSecurityCallback() != null
				&& cp.getRootDir().getLocalSecurityCallback().getQuota() != null) {
			return cp.getRootDir().getLocalSecurityCallback().getQuota().getUlLimitKB();
		}
		return null;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// nothing to do
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		uploadFiles();
		Event pageImported = new NewCPPageEvent("pages imported", pageToBeSelected());
		this.fireEvent(ureq, pageImported);
	}

	/**
	 * Upload the selected files and import them into the content package.
	 */
	private void uploadFiles() {
		VFSContainer root = cp.getRootDir();
		String filenName = file.getUploadFileName();
		// don't check on mimetypes - some browser use different mime types when sending files (OLAT-4547)
		if (filenName.toLowerCase().endsWith(".zip")) { 
			// unzip and add files
			VFSLeaf archive = new LocalFileImpl(file.getUploadFile());
			String archiveName = file.getUploadFileName();
			String unzipDirName = archiveName.substring(0, archiveName.toLowerCase().indexOf(".zip"));
			unzipDirName = VFSManager.similarButNonExistingName(root, unzipDirName);
			VFSContainer unzipDir = root.createChildContainer(unzipDirName);
			ZipUtil.unzip(archive, unzipDir);
			// add items of unzipDir to tree
			pageToBeSelected = addItem(unzipDir, currentPage.getIdentifier(), true);
			cpManager.writeToFile(cp);

		} else {
			// Single file
			VFSLeaf uploadedItem = new LocalFileImpl(file.getUploadFile());
			uploadedItem.rename(file.getUploadFileName());
			// rename to unique name in root folder
			renameToNonExistingDesignation(root, uploadedItem);
			// move item to root folder
			root.copyFrom(uploadedItem);
			pageToBeSelected = addItem(uploadedItem, currentPage.getIdentifier(), false);
			cpManager.writeToFile(cp);
		}
	}

	/**
	 * Adds all vfs items of parent to the menu tree item identified by parentId.
	 * 
	 * @param root
	 * @param parent
	 * @param parentId
	 */
	private void addSubItems(VFSContainer parent, String parentId) {
		for (VFSItem item : parent.getItems(excludeMetaFilesFilter)) {
			addItem(item, parentId, false);
		}
	}

	/**
	 * Adds the vfs item to the menu tree below the parentId item.
	 * 
	 * @param item
	 * @param parentId
	 */
	private CPPage addItem(VFSItem item, String parentId, boolean isRoot) {
		// Make an item in the menu tree only if the item is a container that
		// contains any items to be added or its type is selected in the form.
		// Upload any files in case they are referenced to.

		// Get the file types that should be added as items in the menu tree
		Collection<String> menuItemTypes = checkboxes.getSelectedKeys();
		if (menuItemTypes.contains("htm")) menuItemTypes.add("html");

		// If item is the root node and it doesn't contain any items to be added,
		// show info.
		if (isRoot && item instanceof VFSContainer && !containsItemsToAdd((VFSContainer) item, menuItemTypes)) {
			showInfo("cpfileuploadcontroller.no.files.imported");
		}

		CPPage newPage = null;
		if (isSingleFile || item instanceof VFSLeaf && isToBeAdded((VFSLeaf) item, menuItemTypes) || item instanceof VFSContainer
				&& containsItemsToAdd((VFSContainer) item, menuItemTypes)) {
			// Create the menu item
			String newId = cpManager.addBlankPage(cp, item.getName(), parentId);
			newPage = new CPPage(newId, cp);
			if (item instanceof VFSLeaf) {
				VFSLeaf leaf = (VFSLeaf) item;
				newPage.setFile(leaf);
			}
			cpManager.updatePage(cp, newPage);
		}

		// Add any sub items
		if (item instanceof VFSContainer && containsItemsToAdd((VFSContainer) item, menuItemTypes)) {
			VFSContainer dir = (VFSContainer) item;
			addSubItems(dir, newPage.getIdentifier());
		}
		return newPage;
	}

	/**
	 * Breadth-first search for leafs inside the container that are to be added to
	 * the tree.
	 * 
	 * @param container
	 * @param menuItemTypes
	 * @return true if there is a leaf inside container that should be added
	 */
	private boolean containsItemsToAdd(VFSContainer container, Collection<String> menuItemTypes) {
		LinkedList<VFSItem> queue = new LinkedList<>();
		// enqueue root node
		queue.add(container);
		do {
			// dequeue and exmaine
			VFSItem item = queue.poll();
			if (item instanceof VFSLeaf) {
				if (isToBeAdded((VFSLeaf) item, menuItemTypes)) {
					// node found, return
					return true;
				}
			} else {
				// enqueue successors
				VFSContainer parent = (VFSContainer) item;
				queue.addAll(parent.getItems());
			}
		} while (!queue.isEmpty());
		return false;
	}

	/**
	 * @param item
	 * @param menuItemTypes
	 * @return true if the item is to be added to the tree
	 */
	private boolean isToBeAdded(VFSLeaf item, Collection<String> menuItemTypes) {
		String extension = null;
		if (!menuItemTypes.contains(ALL)) {
			String name = item.getName();
			int dotIndex = name.lastIndexOf(".");
			if (dotIndex > 0) extension = name.substring(dotIndex + 1);
		}
		return menuItemTypes.contains(ALL) || menuItemTypes.contains(extension);
	}

	/**
	 * Renames a file to a non existing file designation.
	 * 
	 * @param root
	 * @param item
	 */
	private void renameToNonExistingDesignation(VFSContainer root, VFSItem item) {
		String newName = VFSManager.similarButNonExistingName(root, item.getName());
		item.rename(newName);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.form.flexible.FormItem,
	 *      org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == cancelButton && event.wasTriggerdBy(FormEvent.ONCLICK)) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if (source == file && event.wasTriggerdBy(FormEvent.ONCHANGE)) {
			// If a zip file was selected show import options. Else hide'em.
			if (file == null) {
				checkboxes.setVisible(false);
				isSingleFile = true;
			} else if (file.getUploadFileName().endsWith(".zip")) {
				checkboxes.setVisible(true);
				checkboxes.selectAll();
				checkboxes.select(ALL, false);
				isSingleFile = false;
			} else {
				checkboxes.setVisible(false);
				// If a single file is selected, it is added to the menu tree no matter
				// what type it is.
				isSingleFile = true;
			}
			// Needed since checkbox component wasn't initially rendered
			flc.setDirty(true);
		}
	}

	/**
	 * @return The file element of this form
	 */
	public CPPage pageToBeSelected() {
		return pageToBeSelected != null ? pageToBeSelected : currentPage;
	}
}
