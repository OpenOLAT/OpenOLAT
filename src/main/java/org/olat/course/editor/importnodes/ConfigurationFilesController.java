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
package org.olat.course.editor.importnodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.editor.importnodes.ConfigurationFilesTableModel.FilesCols;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.sp.SPEditController;

/**
 * 
 * Initial date: 1 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfigurationFilesController extends StepFormBasicController {
	
	private static final String CMD_TOOLS = "file_tools";
	
	private int counter = 0;
	private FlexiTableElement tableEl;
	private ConfigurationFilesTableModel dataModel;
	
	private RenameController renameCtrl;
	private CloseableModalController cmc;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	private final ImportCourseNodesContext importCourseContext;

	private final VFSContainer sourceCourseFolderCont;
	private final VFSContainer targetCourseFolderCont;
	
	public ConfigurationFilesController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, ImportCourseNodesContext importCourseContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "select_nodes");
		this.importCourseContext = importCourseContext;
		
		ICourse sourceCourse = CourseFactory.loadCourse(importCourseContext.getEntry());
		sourceCourseFolderCont = sourceCourse.getCourseFolderContainer(CourseContainerOptions.courseFolder());

		ICourse targetCourse = CourseFactory.loadCourse(importCourseContext.getTargetEntry());
		targetCourseFolderCont = targetCourse.getCourseFolderContainer(CourseContainerOptions.courseFolder());
		
		initForm(ureq);
		loadModel();
		recalculateSelection();
		optimizeOpenElements();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		DefaultFlexiColumnModel nodeModel = new DefaultFlexiColumnModel(FilesCols.file);
		nodeModel.setCellRenderer(new TreeNodeFlexiCellRenderer(new IndentConfigurationFileRowRenderer()));
		nodeModel.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(nodeModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FilesCols.size));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FilesCols.usedBy, new ConfigurationFileRowUseRenderer()));
		DefaultFlexiColumnModel messageCol = new DefaultFlexiColumnModel(FilesCols.messages);
		messageCol.setCellRenderer(new TextFlexiCellRenderer(EscapeMode.antisamy));
		columnsModel.addFlexiColumnModel(messageCol);
		DefaultFlexiColumnModel toolsCol = new DefaultFlexiColumnModel(FilesCols.tools);
		toolsCol.setHeaderLabel("");
		toolsCol.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
		columnsModel.addFlexiColumnModel(toolsCol);
		
		dataModel = new ConfigurationFilesTableModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 250, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_import_course_nodes o_import_course_files_configuration");
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setEmptyTableMessageKey("table.files.empty");
	}
	
	/**
	 * Close folders without at least one element selected.
	 */
	private void optimizeOpenElements() {
		List<ConfigurationFileRow> rootElements = dataModel.getAllObjects().stream()
			.filter(row -> row.getParent() == null).collect(Collectors.toList());
		
		for(ConfigurationFileRow rootElement:rootElements) {
			if(rootElement.isSelected() || rootElement.isParentLine()) {
				continue;
			}
			
			int index = dataModel.getObjects().indexOf(rootElement);
			if(index >= 0) {
				dataModel.close(index);
			}
		}
		
		int numOfRows = dataModel.getRowCount();
		Set<Integer> selectedIndexes = new HashSet<>();
		for(int i=0; i<numOfRows; i++) {
			ConfigurationFileRow fileRow = dataModel.getObject(i);
			if(fileRow.isSelected() || fileRow.isParentLine()) {
				selectedIndexes.add(Integer.valueOf(i));
			}
		}
		tableEl.setMultiSelectedIndex(selectedIndexes);
	}
	
	private void loadModel() {
		// used items
		List<ImportCourseNode> nodeWithItems = new ArrayList<>();
		List<ImportCourseNode> nodes = importCourseContext.getNodes();
		for(ImportCourseNode node:nodes) {
			for(String courseFolderSubPath:node.getCourseFolderSubPathList()) {
				VFSItem item = sourceCourseFolderCont.resolve(courseFolderSubPath);
				if(item != null) {
					nodeWithItems.add(node);
				}
			}
		}
		
		// load them
		List<ConfigurationFileRow> rows = new ArrayList<>();
		loadCourseFolder(sourceCourseFolderCont, sourceCourseFolderCont, null, false, List.of(), rows, nodeWithItems, targetCourseFolderCont);
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private int loadCourseFolder(VFSContainer courseFolder, VFSContainer container, ConfigurationFileRow parent,
			final boolean parentRenameProposed, final List<ImportCourseNode> inheritedSelections,
			List<ConfigurationFileRow> rows, List<ImportCourseNode> nodeWithItems, VFSContainer targetCourseFolder) {
		int count = 0;
		List<VFSItem> items = container.getItems(new VFSSystemItemFilter());
		for(VFSItem item:items) {
			ConfigurationFileRow row = new ConfigurationFileRow(parent, item);
			if(parent != null) {
				parent.getChildren().add(row);
			}
			
			String relPath = VFSManager.getRelativeItemPath(item, courseFolder, "");
			row.setCourseFolderSubPath(relPath);
			// need cleaning, without or without starting /
			
			List<ImportCourseNode> childInheritedSelections = new ArrayList<>(inheritedSelections);
			for(ImportCourseNode nodeItem:childInheritedSelections) {
				row.setSelected(row.isSelected() || (nodeItem.isSelected() && !nodeItem.isExcludeFromImport()));
				row.addUsedByList(nodeItem);
			}
			
			for(ImportCourseNode nodeItem:nodeWithItems) {
				List<String> courseFolderSubPathList = nodeItem.getCourseFolderSubPathList();
				for(String courseFolderSubPath:courseFolderSubPathList) {
					String subPath = VFSManager.trimSlash(courseFolderSubPath);
					if(relPath.equals(subPath)) {
						row.addUsedByList(nodeItem);
						boolean itemSelected = (nodeItem.isSelected() && !nodeItem.isExcludeFromImport());
						if(itemSelected) {
							addMessage(row, nodeItem);
						}
						row.setSelected(row.isSelected() || itemSelected);
						if(nodeItem.getCourseNode() instanceof BCCourseNode) {
							childInheritedSelections.add(nodeItem);
						}
					}
				}
			}
			
			boolean renamedProposed = parentRenameProposed;
			VFSItem targetItem = targetCourseFolder.resolve(relPath);
			
			FormLink toolsLink = forgeTools();
			row.setToolLink(toolsLink);
			toolsLink.setUserObject(row);
			
			if(targetItem != null && !renamedProposed) {
				toolsLink.setEnabled(true);
				String alternativeName = VFSManager.similarButNonExistingName(targetItem.getParentContainer(), item.getName(), "_import");
				row.setRenamedFilename(alternativeName);
				renamedProposed = true;

				String i18n = targetItem instanceof VFSContainer ? "error.foldername.conflict" : "error.filename.conflict";
				ImportHelper.warningMessage(row, i18n, new String[] { targetItem.getName() },  getTranslator());
			}

			rows.add(row);
			if(item instanceof VFSContainer) {
				int numOfChildren = loadCourseFolder(courseFolder, (VFSContainer)item, row, renamedProposed, childInheritedSelections, rows, nodeWithItems, targetCourseFolder);
				row.setNumOfChildren(numOfChildren);
				count += numOfChildren;
			}
			count++;
		}
		return count;
	}
	
	private void addMessage(ConfigurationFileRow row, ImportCourseNode nodeItem) {
		if(nodeItem.getCourseNode() instanceof SPCourseNode
				&& nodeItem.getCourseNode().getModuleConfiguration().getBooleanSafe(SPEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS, false)) {
			ImportHelper.infoMessage(row, "infos.relative.links", null, getTranslator());
		}
	}
	
	private FormLink forgeTools() {
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), CMD_TOOLS, "", null, flc, Link.LINK | Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsLink.setEnabled(false);
		return toolsLink;
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(renameCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		} else if(cmc == source || toolsCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(renameCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		toolsCalloutCtrl = null;
		renameCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if(CMD_TOOLS.equals(link.getCmd()) && link.getUserObject() instanceof ConfigurationFileRow) {
				doOpenTools(ureq, link, (ConfigurationFileRow)link.getUserObject());
			}
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if(FlexiTableElement.ROW_CHECKED_EVENT.equals(se.getCommand())) {
					doSelect(se.getIndex());
				} else if(FlexiTableElement.ROW_UNCHECKED_EVENT.equals(se.getCommand())) {
					doUnselect(se.getIndex());
				}
				recalculateSelection();
				tableEl.reset(false, false, true);
			} else {
				recalculateSelection();
				tableEl.reset(false, false, true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelect(int index) {
		ConfigurationFileRow fileRow = dataModel.getObject(index);
		fileRow.setSelected(true);
	}
	
	private void doUnselect(int index) {
		ConfigurationFileRow fileRow = dataModel.getObject(index);
		fileRow.setSelected(false);
	}
	
	private void recalculateSelection() {
		List<ConfigurationFileRow> fileRows = dataModel.getObjects();
		fileRows.stream()
			.forEach(row -> row.setParentLine(false));
		
		Set<Integer> selectedIndexes = new HashSet<>();

		int numOfRows = fileRows.size();
		for(int i=0; i<numOfRows; i++) {
			ConfigurationFileRow fileRow = fileRows.get(i);
			if(fileRow.isSelected()) {
				for(ConfigurationFileRow parent=fileRow.getParent(); parent != null; parent=parent.getParent()) {
					parent.setParentLine(true);
				}
			}
		}
		
		for(int i=0; i<numOfRows; i++) {
			ConfigurationFileRow fileRow = fileRows.get(i);
			if(fileRow.isSelected() || fileRow.isParentLine()) {
				selectedIndexes.add(Integer.valueOf(i));
			}
		}
		tableEl.setMultiSelectedIndex(selectedIndexes);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<ConfigurationFileRow> rows = dataModel.getAllObjects();
		// only relevant rows
		rows = rows.stream()
				.filter(row -> row.isSelected() || row.isParentLine())
				.collect(Collectors.toList());

		Map<ConfigurationFileRow,ImportCourseFile> map = new HashMap<>();
		List<ImportCourseFile> files = new ArrayList<>(rows.size());
		for(ConfigurationFileRow row:rows) {
			VFSItem item = row.getItem();
			ImportCourseFile file = new ImportCourseFile(item);
			files.add(file);
			file.setRenamed(row.isRename());
			file.setRenamedFilename(row.getRenamedFilename());
			file.setCourseFolderSubPath(row.getCourseFolderSubPath());

			for(ImportCourseNode node:row.getUsedByList()) {
				node.getFiles().add(file);
			}
			map.put(row, file);
		}
		
		for(ConfigurationFileRow row:rows) {
			ConfigurationFileRow parent = row.getParent();
			if(parent != null) {
				ImportCourseFile file = map.get(row);
				ImportCourseFile parentFile = map.get(parent);
				file.setParent(parentFile);
				parentFile.getChildren().add(file);
			}	
		}
		
		importCourseContext.setCourseFolderFiles(files);

		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
	
	private void doOpenTools(UserRequest ureq, FormLink link, ConfigurationFileRow fileRow) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), fileRow);
		listenTo(toolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doRename(UserRequest ureq, ConfigurationFileRow fileRow) {
		renameCtrl = new RenameController(ureq, getWindowControl(), fileRow, sourceCourseFolderCont, targetCourseFolderCont);
		listenTo(renameCtrl);
		
		String title = translate("rename.file");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), renameCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private final Link renameLink;
		
		private final ConfigurationFileRow fileRow;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, ConfigurationFileRow fileRow) {
			super(ureq, wControl);
			this.fileRow = fileRow;

			VelocityContainer mainVC = createVelocityContainer("tools_files");
			renameLink = LinkFactory.createLink("rename", "rename", getTranslator(), mainVC, this, Link.LINK);
			renameLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(renameLink == source) {
				doRename(ureq, fileRow);
			}
		}
	}
}
