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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.editor.importnodes.ConfigurationCourseNodesTableModel.ConfigurationCols;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.BlogCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.DocumentCourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.PodcastCourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.course.nodes.sp.SPEditController;
import org.olat.course.reminder.CourseNodeRuleSPI;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderModule;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.RuleSPI;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.springframework.beans.factory.annotation.Autowired;

import de.bps.course.nodes.LLCourseNode;
import de.bps.course.nodes.ll.LLModel;

/**
 * 
 * Initial date: 1 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfigurationCourseNodesController extends StepFormBasicController {

	private FlexiTableElement tableEl;
	private ConfigurationCourseNodesTableModel dataModel;
	
	private int counter = 0;
	private final ImportCourseNodesContext importCourseContext;
	
	private final VFSContainer sourceCourseFolderCont;
	
	@Autowired
	private ReminderService reminderService;
	@Autowired
	private ReminderModule reminderModule;
	
	public ConfigurationCourseNodesController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, ImportCourseNodesContext importCourseContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "select_nodes");
		this.importCourseContext = importCourseContext;
		
		ICourse sourceCourse = CourseFactory.loadCourse(importCourseContext.getEntry());
		sourceCourseFolderCont = sourceCourse.getCourseEnvironment()
				.getCourseFolderContainer(CourseContainerOptions.courseFolder());

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		IndentedNodeRenderer intendedNodeRenderer = new IndentedNodeRenderer();
		intendedNodeRenderer.setIndentationEnabled(false);
		DefaultFlexiColumnModel nodeModel = new DefaultFlexiColumnModel(ConfigurationCols.node);
		nodeModel.setCellRenderer(new TreeNodeFlexiCellRenderer(intendedNodeRenderer));
		nodeModel.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(nodeModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigurationCols.resource));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigurationCols.reminder));
		DefaultFlexiColumnModel messageCol = new DefaultFlexiColumnModel(ConfigurationCols.messages);
		messageCol.setCellRenderer(new TextFlexiCellRenderer(EscapeMode.antisamy));
		columnsModel.addFlexiColumnModel(messageCol);
		
		dataModel = new ConfigurationCourseNodesTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 250, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_import_course_nodes o_import_course_nodes_configuration");
		tableEl.setMultiSelect(false);
		tableEl.setSelectAllEnable(false);
		tableEl.setEmptyTableMessageKey("table.course.node.empty");
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		List<ImportCourseNode> selectedNodes = importCourseContext.getSelectedNodes();
		Map<String,ImportCourseNode> selectedNodesMap = selectedNodes.stream()
				.collect(Collectors.toMap(ImportCourseNode::getIdent, n -> n));

		RepositoryEntry entry = importCourseContext.getEntry();
		ICourse course = CourseFactory.loadCourse(entry);
		TreeNode rootNode = course.getEditorTreeModel().getRootNode();
		List<ConfigurationCourseNodeRow> rows = new ArrayList<>();
		recursive(rows, rootNode, selectedNodesMap, null);
		calculateNumberOfReminders(rows);
	
		dataModel.setObjects(rows);
		tableEl.reset(true, false, true);
	}
	
	private void recursive(List<ConfigurationCourseNodeRow> rows, INode node, Map<String,ImportCourseNode> selectedNodesMap, ConfigurationCourseNodeRow parent) {
		if(selectedNodesMap.containsKey(node.getIdent())) {
			ImportCourseNode importNode = selectedNodesMap.get(node.getIdent());
			ConfigurationCourseNodeRow row = forgeRow(importNode, parent);
			if(parent != null) {
				importNode.setParent(parent.getImportCourseNode());
				parent.getImportCourseNode().getChildren().add(importNode);
			}
			rows.add(row);
			parent = row;
		}
		
		if (node instanceof CourseEditorTreeNode) {
			CourseEditorTreeNode editorNode = (CourseEditorTreeNode)node;

			int childCount = editorNode.getChildCount();
			for (int i = 0; i < childCount; i++) {
				INode child = editorNode.getChildAt(i);
				recursive(rows, child, selectedNodesMap, parent);
				
			}
		}
	}
	
	private ConfigurationCourseNodeRow forgeRow(ImportCourseNode importNode, ConfigurationCourseNodeRow parent) {
		ConfigurationCourseNodeRow row = new ConfigurationCourseNodeRow(importNode, parent);
		
		CourseNode courseNode = importNode.getCourseNode();
		if(courseNode instanceof BCCourseNode) {
			forgeBCRow(row, importNode, (BCCourseNode)courseNode);
		} else if(courseNode instanceof SPCourseNode) {
			forgeSPRow(row, importNode, (SPCourseNode)courseNode);
		} else if(courseNode instanceof GTACourseNode) {
			forgeGTARow(row, importNode);
		} else if(courseNode instanceof DocumentCourseNode) {
			forgeDocumentRow(row, importNode, (DocumentCourseNode)courseNode);
		} else if(courseNode instanceof BlogCourseNode || courseNode instanceof PodcastCourseNode || courseNode instanceof WikiCourseNode) {
			forgeReferenceOrEmptyRow(row, importNode, CopyType.reference);
		} else if(courseNode instanceof LLCourseNode) {
			forgeLinkListRow(importNode, (LLCourseNode)courseNode);
		}

		if(courseNode.hasBusinessGroups()) {
			ImportHelper.warningMessage(row, "error.business.groups", null, getTranslator());
		}
		if(courseNode.hasBusinessGroupAreas()) {
			ImportHelper.warningMessage(row, "error.areas", null, getTranslator());
		}
		return row;
	}
	
	private void forgeBCRow(ConfigurationCourseNodeRow row, ImportCourseNode importNode, BCCourseNode courseNode) {
		if(courseNode.isSharedFolder()) {
			if(isSameSharedFolder()) {
				// OK -> no options	
			} else {
				StaticTextElement msgEl = uifactory.addStaticTextElement("", null, translate("error.different.shared.folder"), flc);
				row.setConfigurationItem(msgEl);
				importNode.setExcludeFromImport(true);
				ImportHelper.errorMessage(row, "error.cannot.import", null, getTranslator());
			}	
		} else if(courseNode.getModuleConfiguration().getBooleanSafe(BCCourseNode.CONFIG_AUTO_FOLDER)) {
			// auto is a self contained folder, all files are copied
			forgeCopyOrNotRow(row, importNode, CopyType.copy);
		} else  {
			String subPath = courseNode.getModuleConfiguration().getStringValue(BCCourseNode.CONFIG_SUBPATH);
			importNode.addCourseFolderSubPath(subPath);
			importNode.setImportSetting(CopyType.copy);
		}
	}
	
	private void forgeSPRow(ConfigurationCourseNodeRow row, ImportCourseNode importNode, SPCourseNode courseNode) {
		importNode.setImportSetting(CopyType.ignore);
		String filePath = courseNode.getModuleConfiguration().getStringValue(SPEditController.CONFIG_KEY_FILE);
		if(StringHelper.containsNonWhitespace(filePath)) {
			VFSItem item = sourceCourseFolderCont.resolve(filePath);
			if(item != null) {
				importNode.addCourseFolderSubPath(filePath);
				importNode.setImportSetting(CopyType.copy);
				VFSContainer parentContainer = item.getParentContainer();
				if(!"coursefolder".equals(parentContainer.getName())) {
					// try to copy the whole parent folder
					importNode.setCourseFolderSubPathWithParent(true);
				}
			} else if(filePath.contains("_sharedfolder")) {
				if(!isSameSharedFolder()) {
					importNode.setExcludeFromImport(true);
					ImportHelper.errorMessage(row, "error.different.shared.folder", null, getTranslator());
				}
			} else {
				ImportHelper.errorMessage(row, "error.file.not.exists", null, getTranslator());
			}
		} else {
			ImportHelper.errorMessage(row, "error.file.not.exists", null, getTranslator());
		}

		if(courseNode.getModuleConfiguration().getBooleanSafe(SPEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS, false)) {
			ImportHelper.infoMessage(row, "infos.relative.links", null, getTranslator());
		}
	}
	
	private boolean isSameSharedFolder() {
		ICourse sourceCourse = CourseFactory.loadCourse(importCourseContext.getEntry());
		ICourse targetCourse = CourseFactory.loadCourse(importCourseContext.getTargetEntry());
		String sourceSoftKey = sourceCourse.getCourseConfig().getSharedFolderSoftkey();
		String targetSoftKey = targetCourse.getCourseConfig().getSharedFolderSoftkey();
		return sourceSoftKey != null && targetSoftKey != null && sourceSoftKey.equals(targetSoftKey);
	}
	
	private void forgeDocumentRow(ConfigurationCourseNodeRow row, ImportCourseNode importNode, DocumentCourseNode courseNode) {
		if(courseNode.needsReferenceToARepositoryEntry()) {
			forgeReferenceRow(row, importNode, CopyType.reference);
		} else {
			String path = courseNode.getModuleConfiguration().getStringValue(DocumentCourseNode.CONFIG_DOC_COURSE_REL_PATH);
			importNode.addCourseFolderSubPath(path);
			importNode.setImportSetting(CopyType.copy);
		}	
	}

	private void forgeLinkListRow(ImportCourseNode importNode, LLCourseNode courseNode) {
		List<LLModel> links = courseNode.getLinks();
		for(LLModel link:links) {
			String target = link.getTarget();
			if(!target.contains("://") && !target.contains("/library/")) {
				importNode.addCourseFolderSubPath(target);
			}
		}
	}
	
	private void forgeGTARow(ConfigurationCourseNodeRow row, ImportCourseNode importNode) {
		SelectionValues settings = new SelectionValues();
		settings.add(new SelectionValue(CopyType.copy.name(), translate("options.copy.assignment.solution")));
		settings.add(new SelectionValue(CopyType.ignore.name(), translate("options.ignore.assignment.solution")));
		forgeReferenceRowCopy(row, importNode, settings, CopyType.copy);
	}
	
	private void forgeCopyOrNotRow(ConfigurationCourseNodeRow row, ImportCourseNode importNode, CopyType defaultType) {
		SelectionValues settings = new SelectionValues();
		settings.add(new SelectionValue(CopyType.copy.name(), translate("options.copy")));
		settings.add(new SelectionValue(CopyType.ignore.name(), translate("options.configure.later")));
		forgeReferenceRowCopy(row, importNode, settings, defaultType);
	}
	
	private void forgeReferenceRow(ConfigurationCourseNodeRow row, ImportCourseNode importNode, CopyType defaultType) {
		SelectionValues settings = new SelectionValues();
		settings.add(new SelectionValue(CopyType.reference.name(), translate("options.reference")));
		settings.add(new SelectionValue(CopyType.ignore.name(), translate("options.configure.later")));
		forgeReferenceRowCopy(row, importNode, settings, defaultType);
	}
	
	private void forgeReferenceOrEmptyRow(ConfigurationCourseNodeRow row, ImportCourseNode importNode, CopyType defaultType) {
		SelectionValues settings = new SelectionValues();
		settings.add(new SelectionValue(CopyType.reference.name(), translate("options.reference")));
		settings.add(new SelectionValue(CopyType.createNew.name(), translate("options.empty.resource")));
		settings.add(new SelectionValue(CopyType.ignore.name(), translate("options.configure.later")));
		forgeReferenceRowCopy(row, importNode, settings, defaultType);
	}

	private void forgeReferenceRowCopy(ConfigurationCourseNodeRow row, ImportCourseNode importNode, SelectionValues settings, CopyType defaultType) {
		String id = "config_" + (++counter);
		SingleSelection copyEl = uifactory.addDropdownSingleselect(id, id, null, flc, settings.keys(), settings.values(), null);
		row.setConfigurationItem(copyEl);
		if(importNode.getImportSetting() != null) {
			copyEl.select(importNode.getImportSetting().name(), true);
		} else if(defaultType != null) {
			importNode.setImportSetting(defaultType);
			copyEl.select(defaultType.name(), true);
		}
		copyEl.setUserObject(row);
	}
	
	private void calculateNumberOfReminders(List<ConfigurationCourseNodeRow> rows) {
		Map<String,AtomicInteger> nodeIdents = new HashMap<>();
		List<Reminder> reminders = reminderService.getReminders(importCourseContext.getEntry());
		for(Reminder reminder:reminders) {
			String configuration = reminder.getConfiguration();
			if (StringHelper.containsNonWhitespace(configuration)) {
				List<ReminderRule> rules = reminderService.toRules(configuration).getRules();
				if(rules != null && !rules.isEmpty()) {
					for (ReminderRule rule : rules) {
						RuleSPI ruleSPI = reminderModule.getRuleSPIByType(rule.getType());
						if (ruleSPI instanceof CourseNodeRuleSPI) {
							String nodeIdent = ((CourseNodeRuleSPI)ruleSPI).getCourseNodeIdent(rule);
							nodeIdents
								.computeIfAbsent(nodeIdent, ident -> new AtomicInteger(0))
								.incrementAndGet();
						}
					}
				}
			}
		}
		
		for(ConfigurationCourseNodeRow row:rows) {
			AtomicInteger count = nodeIdents.get(row.getEditorTreeNode().getIdent());
			if(count != null) {
				row.setNumOfReminders(count.getPlain());
			} else {
				row.setNumOfReminders(0);
			}
		}
	}	

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formNext(UserRequest ureq) {
		int numOfRows = dataModel.getRowCount();
		for(int i=0; i<numOfRows; i++) {
			ConfigurationCourseNodeRow row = dataModel.getObject(i);
			FormItem configurationItem = row.getConfigurationItem();
			if(configurationItem instanceof SingleSelection) {
				SingleSelection copySelection = (SingleSelection)configurationItem;
				if(copySelection.isOneSelected()) {
					row.getImportCourseNode().setImportSetting(CopyType.valueOf(copySelection.getSelectedKey()));
				} else {
					row.getImportCourseNode().setImportSetting(CopyType.ignore);
				}
			}
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
