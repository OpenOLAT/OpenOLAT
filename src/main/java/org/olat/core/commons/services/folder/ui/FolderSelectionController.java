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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.olat.core.commons.services.folder.ui.FolderDataModel.FolderCols;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSMetadataContainer;
import org.olat.core.commons.services.vfs.VFSMetadataItem;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSStatus;
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
public class FolderSelectionController extends FormBasicController implements FlexiTableCssDelegate {
	
	private static final String CMD_FOLDER = "folder";
	
	private TooledStackedPanel stackedPanel;
	private FolderDataModel dataModel;
	private FlexiTableElement tableEl;

	private final VFSContainer rootContainer;
	private VFSContainer currentContainer;
	private VFSItemFilter vfsFilter = new VFSSystemItemFilter();
	private final FileBrowserSelectionMode selectionMode;
	private final String submitButtonText;
	private int counter = 0;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private AVModule avModule;
	@Autowired
	private MapperService mapperService;

	public FolderSelectionController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel,
			VFSContainer rootContainer, FileBrowserSelectionMode selectionMode, String submitButtonText) {
		super(ureq, wControl, "folder_selection");
		this.stackedPanel = stackedPanel;
		if (stackedPanel != null) {
			stackedPanel.addListener(this);
		}
		this.rootContainer = rootContainer;
		this.selectionMode = selectionMode;
		this.submitButtonText = submitButtonText;
		
		initForm(ureq);
		
		updateCurrentContainer(rootContainer);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
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
		tableEl.setCssDelegate(this);
		tableEl.sort(FolderCols.title.name(), true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("submit", "submit", "noTransOnlyParam", new String[] {submitButtonText}, buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void loadModel() {
		List<VFSItem> items = getCachedContainer(currentContainer).getItems(vfsFilter);
		
		List<FolderRow> rows = new ArrayList<>(items.size());
		for (VFSItem vfsItem : items) {
			FolderRow row = new FolderRow(vfsItem);
			VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
			row.setMetadata(vfsMetadata);
			
			row.setIconCssClass(FolderUIFactory.getIconCssClass(vfsMetadata, vfsItem));
			row.setTitle(FolderUIFactory.getDisplayName(vfsMetadata, vfsItem));
			row.setLastModifiedDate(FolderUIFactory.getLastModifiedDate(vfsMetadata, vfsItem));
			row.setTranslatedType(FolderUIFactory.getTranslatedType(getTranslator(), vfsMetadata, vfsItem));
			
			forgeThumbnail(row);
			forgeTitleLink(row);
			
			rows.add(row);
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
		
		boolean multiSelect = FileBrowserSelectionMode.sourceMulti == selectionMode && isAnyChildCopyable();
		tableEl.setMultiSelect(multiSelect);
	}
	
	private boolean isAnyChildCopyable() {
		return dataModel.getObjects().stream().anyMatch(row -> VFSStatus.YES == row.getVfsItem().canCopy());
	}

	private VFSContainer getCachedContainer(VFSContainer vfsContainer) {
		if (VFSStatus.YES == vfsContainer.canMeta()) {
			return new VFSMetadataContainer(vfsRepositoryService, true, vfsContainer);
		}
		return vfsContainer;
	}
	
	private VFSItem getUncachedItem(VFSItem item) {
		if (item instanceof VFSMetadataItem cachedItem) {
			return cachedItem.getItem();
		}
		
		return item;
	}
	
	private void forgeThumbnail(FolderRow row) {
		if (row.getVfsItem() instanceof VFSLeaf vfsLeaf && row.getMetadata() != null && isThumbnailAvailable(row.getMetadata(), vfsLeaf)) {
			// One mapper per thumbnail per leaf version. The mapper is cached for 10 min or all users.
			FolderThumbnailMapper thumbnailMapper = new FolderThumbnailMapper(vfsRepositoryService, avModule, row.getMetadata(), row.getVfsItem());
			MapperKey mapperKey = mapperService.register(null, getThumbnailMapperId(row.getMetadata()), thumbnailMapper, 10);
			
			row.setThumbnailAvailable(true);
			row.setThumbnailUrl(mapperKey.getUrl());
			row.setThumbnailTopVisible(FolderUIFactory.isThumbnailTopVisible(row.getMetadata(), row.getVfsItem()));
		}
	}
	
	private boolean isThumbnailAvailable(VFSMetadata vfsMetadata, VFSLeaf vfsLeaf) {
		if (FolderThumbnailMapper.isAudio(vfsMetadata, vfsLeaf)) {
			return true;
		}
		return vfsRepositoryService.isThumbnailAvailable(vfsLeaf, vfsMetadata);
	}
	
	private String getThumbnailMapperId(VFSMetadata vfsMetadata) {
		return vfsMetadata.getUuid() + Formatter.formatDatetimeFilesystemSave(vfsMetadata.getFileLastModified());
	}
	
	private void forgeTitleLink(FolderRow row) {
		if (row.getVfsItem() instanceof VFSContainer) {
			FormLink link = uifactory.addFormLink("title_" + counter++, CMD_FOLDER, "", null, null, Link.NONTRANSLATED);
			link.setI18nKey(row.getTitle());
			link.setUserObject(row);
			row.setTitleItem(link);
		} else {
			StaticTextElement titleEl = uifactory.addStaticTextElement("title_" + counter++, null, row.getTitle(), flc);
			titleEl.setStaticFormElement(false);
			row.setTitleItem(titleEl);
		}
	}
	
	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		FolderRow row = dataModel.getObject(pos);
		String cssClass = null;
		if (row.getVfsItem() instanceof VFSLeaf) {
			cssClass = "o_folder_muted_row";
		}
		return cssClass;
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == stackedPanel) {
			if (event instanceof PopEvent popEvent) {
				Object userObject = popEvent.getUserObject();
				if (userObject instanceof VFSContainer vfsContainer) {
					updateCurrentContainer(vfsContainer.getParentContainer());
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
				doOpenFolder(folderRow);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		if (stackedPanel != null) {
			stackedPanel.removeListener(this);
		}
		super.doDispose();
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<VFSItem> items;
		if (FileBrowserSelectionMode.sourceMulti == selectionMode) {
			Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
			if (selectedIndex == null || selectedIndex.isEmpty()) {
				items = List.of();
			} else {
				items = selectedIndex.stream()
					.map(index -> dataModel.getObject(index.intValue()))
					.filter(Objects::nonNull)
					.map(row -> getUncachedItem(row.getVfsItem()))
					.filter(Objects::nonNull)
					.toList();
			}
		} else {
			items = List.of(getUncachedItem(currentContainer));
		}
		fireEvent(ureq, new FileBrowserSelectionEvent(items));
	}

	private void doOpenFolder(FolderRow folderRow) {
		if (isItemNotAvailable(folderRow, true)) return;
		
		if (folderRow.getVfsItem() instanceof VFSContainer vfsContainer) {
			updateCurrentContainer(vfsContainer);
			loadModel();
		}
	}
	
	public void updateCurrentContainer(VFSContainer container) {
		currentContainer = container;
		List<VFSContainer> parents = new ArrayList<>(1);
		getParentsToRoot(parents, container);
		
		stackedPanel.popUpToController(this);
		Collections.reverse(parents);
		for (VFSContainer parent : parents) {
			stackedPanel.pushController(parent.getName(), null, parent);
		}
		stackedPanel.setDirty(true);
		
		loadModel();
	}
	
	private void getParentsToRoot(List<VFSContainer> parents, VFSContainer container) {
		if (container == rootContainer) {
			return;
		}
		VFSContainer parentContainer = container.getParentContainer();
		if (parentContainer != null) {
			parents.add(container);
			getParentsToRoot(parents, parentContainer);
		}
	}
	
	private boolean isItemNotAvailable(FolderRow row, boolean showDeletedMessage) {
		VFSItem vfsItem = row.getVfsItem();
		if (!vfsItem.exists()) {
			if (showDeletedMessage) {
				if (vfsItem instanceof VFSContainer) {
					showError("error.deleted.container");
				} else {
					showError("error.deleted.leaf");
				}
				loadModel();
			}
			return true;
		}
		return false;
	}

}
