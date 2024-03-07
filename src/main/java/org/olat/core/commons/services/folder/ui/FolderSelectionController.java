/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.commons.services.folder.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.folder.ui.FolderDataModel.FolderCols;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.modules.audiovideorecording.AVModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 Mar 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FolderSelectionController extends FormBasicController {
	
	private static final String CMD_FOLDER = "folder";
	
	private TooledStackedPanel folderBreadcrumb;
	private FolderDataModel dataModel;
	private FlexiTableElement tableEl;

	private final VFSContainer rootContainer;
	private VFSContainer currentContainer;
	private VFSItemFilter vfsFilter = new VFSSystemItemFilter();
	private final List<VFSItem> itemsToCopy;
	private final String submitButtonI18nKey;
	private Object userObject;
	private int counter = 0;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private AVModule avModule;

	public FolderSelectionController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer,
			VFSContainer currentContainer, List<VFSItem> itemsToCopy, String submitButtonI18nKey) {
		super(ureq, wControl, "folder_selection");
		this.rootContainer = rootContainer;
		this.itemsToCopy = itemsToCopy;
		this.submitButtonI18nKey = submitButtonI18nKey;
		
		initForm(ureq);
		
		updateCurrentContainer(ureq, currentContainer);
	}
	
	public List<VFSItem> getItemsToCopy() {
		return itemsToCopy;
	}
	
	public VFSContainer getSelectedContainer() {
		return currentContainer;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		folderBreadcrumb = new TooledStackedPanel("folderBreadcrumb", getTranslator(), this);
		formLayout.add(new ComponentWrapperElement(folderBreadcrumb));
		folderBreadcrumb.setToolbarEnabled(false);
		folderBreadcrumb.pushController(rootContainer.getName(), null, "/");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel iconCol = new DefaultFlexiColumnModel(FolderCols.icon, new FolderIconRenderer());
		iconCol.setExportable(false);
		columnsModel.addFlexiColumnModel(iconCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.lastModifiedDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.type));
		
		dataModel = new FolderDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), flc);
		tableEl.setAndLoadPersistedPreferences(ureq, "folder.selection");
		tableEl.sort(FolderCols.title.name(), true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton(submitButtonI18nKey, buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void loadModel(UserRequest ureq) {
		List<VFSItem> items = currentContainer.getItems(vfsFilter);
		List<FolderRow> rows = new ArrayList<>(items.size());
		
		for (VFSItem vfsItem : items) {
			FolderRow row = new FolderRow(vfsItem);
			
			row.setTitle(FolderUIFactory.getDisplayName(vfsItem));
			row.setLastModifiedDate(FolderUIFactory.getLastModifiedDate(vfsItem));
			row.setTranslatedType(FolderUIFactory.getTranslatedType(getTranslator(), vfsItem));
			
			forgeThumbnail(ureq, row);
			forgeTitleLink(row);
			
			rows.add(row);
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void forgeThumbnail(UserRequest ureq, FolderRow row) {
		if (row.getVfsItem() instanceof VFSLeaf vfsLeaf && isThumbnailAvailable(vfsLeaf)) {
			VFSLeaf thumbnail = getThumbnail(vfsLeaf);
			if (thumbnail != null) {
				row.setThumbnailAvailable(true);
				VFSMediaMapper thumbnailMapper = new VFSMediaMapper(thumbnail);
				String thumbnailUrl = registerCacheableMapper(ureq, null, thumbnailMapper);
				row.setThumbnailUrl(thumbnailUrl);
			}
		}
	}
	
	private boolean isThumbnailAvailable(VFSLeaf vfsLeaf) {
		if (isAudio(vfsLeaf)) {
			return true;
		}
		if (vfsLeaf.getSize() == 0) {
			return false;
		}
		return vfsRepositoryService.isThumbnailAvailable(vfsLeaf, vfsLeaf.getMetaInfo());
	}

	private VFSLeaf getThumbnail(VFSLeaf vfsLeaf) {
		if (isAudio(vfsLeaf)) {
			return vfsRepositoryService.getLeafFor(avModule.getAudioWaveformUrl());
		}
		return vfsRepositoryService.getThumbnail(vfsLeaf, 30, 30, false);
	}
	
	private boolean isAudio(VFSLeaf vfsLeaf) {
		if ("m4a".equalsIgnoreCase(FileUtils.getFileSuffix(vfsLeaf.getRelPath()))) {
			return true;
		}
		return false;
	}
	
	private void forgeTitleLink(FolderRow row) {
		if (row.getVfsItem() instanceof VFSContainer) {
			FormLink link = uifactory.addFormLink("title_" + counter++, CMD_FOLDER, "", null, null, Link.NONTRANSLATED);
			link.setI18nKey(row.getTitle());
			link.setUserObject(row);
			row.setTitleItem(link);
		} else {
			FormItem staticEl = uifactory.addStaticTextElement("title_" + counter++, null, row.getTitle(), flc);
			row.setTitleItem(staticEl);
		}
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == folderBreadcrumb) {
			if (event instanceof PopEvent popEvent) {
				Object userObject = popEvent.getUserObject();
				if (userObject instanceof String relativePath) {
					String parentPath = relativePath.substring(0, relativePath.lastIndexOf("/"));
					updateCurrentContainer(ureq, parentPath);
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			 if (CMD_FOLDER.equals(link.getCmd()) && link.getUserObject() instanceof FolderRow folderRow) {
				doOpenFolder(ureq, folderRow);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void doOpenFolder(UserRequest ureq, FolderRow folderRow) {
		if (isItemNotAvailable(ureq, folderRow, true)) return;
		
		if (folderRow.getVfsItem() instanceof VFSContainer vfsContainer) {
			updateCurrentContainer(ureq, vfsContainer);
			loadModel(ureq);
		}
	}
	
	public void updateCurrentContainer(UserRequest ureq, VFSContainer container) {
		String relativePath = VFSManager.getRelativeItemPath(container, rootContainer, "/");
		updateCurrentContainer(ureq, relativePath);
	}
	
	public void updateCurrentContainer(UserRequest ureq, String relativePath) {
		String path = relativePath;
		if (path == null) {
			path = "/";
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		
		VFSItem vfsItem = rootContainer.resolve(path);
		if (vfsItem instanceof VFSContainer vfsContainer) {
			currentContainer = vfsContainer;
		} else {
			currentContainer = rootContainer;
			path = "/";
		}
		
		updateFolderBreadcrumpUI(ureq, path);
		loadModel(ureq);
	}
	
	private void updateFolderBreadcrumpUI(UserRequest ureq, String path) {
		String[] pathParts = path.split("/");
		String ralativePath = "";
		folderBreadcrumb.popUpToRootController(ureq);
		for (int i = 1; i < pathParts.length; i++) {
			String pathPart = pathParts[i];
			ralativePath += "/" + pathPart;
			folderBreadcrumb.pushController(pathPart, null, ralativePath);
		}
	}

	private boolean isItemNotAvailable(UserRequest ureq, FolderRow row, boolean showDeletedMessage) {
		VFSItem vfsItem = row.getVfsItem();
		if (!vfsItem.exists()) {
			if (showDeletedMessage) {
				if (vfsItem instanceof VFSContainer) {
					showError("error.deleted.container");
				} else {
					showError("error.deleted.leaf");
				}
				loadModel(ureq);
			}
			return true;
		}
		return false;
	}

}
