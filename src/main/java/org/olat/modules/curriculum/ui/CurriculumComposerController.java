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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.member.wizard.ImportMemberByUsernamesController;
import org.olat.course.member.wizard.ImportMember_1a_LoginListStep;
import org.olat.course.member.wizard.ImportMember_1b_ChooseMemberStep;
import org.olat.course.member.wizard.MembersByNameContext;
import org.olat.course.member.wizard.MembersContext;
import org.olat.group.ui.main.MemberPermissionChangeEvent;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumManagedFlag;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumComposerTableModel.ElementCols;
import org.olat.modules.curriculum.ui.copy.CopySettingsController;
import org.olat.modules.curriculum.ui.event.SelectReferenceEvent;
import org.olat.modules.curriculum.ui.lectures.CurriculumElementLecturesController;
import org.olat.modules.curriculum.ui.member.CurriculumMembersManagementController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumComposerController extends FormBasicController implements Activateable2, FlexiTableCssDelegate, TooledController {
	
	private Link newElementButton;
	private Link manageFocusedMembersLink;
	private FlexiTableElement tableEl;
	private CurriculumComposerTableModel tableModel;
	private TooledStackedPanel toolbarPanel;
	private FormLink overrideLink;
	private FormLink unOverrideLink;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private CopySettingsController copyCtrl;
	private ReferencesController referencesCtrl;
	private StepsMainRunController importMembersWizard;
	private EditCurriculumElementController newElementCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private EditCurriculumElementController newSubElementCtrl;
	private MoveCurriculumElementController moveElementCtrl;
	private ConfirmCurriculumElementDeleteController confirmDeleteCtrl;
	private CurriculumElementCalendarController calendarsCtrl;
	private CurriculumElementLecturesController lecturesCtrl;
	private CurriculumElementLearningPathController learningPathController;
	
	private int counter;
	private final boolean managed;
	private boolean overrideManaged;
	private final Curriculum curriculum;
	private final CurriculumSecurityCallback secCallback;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumComposerController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			Curriculum curriculum, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "manage_curriculum_structure");
		this.toolbarPanel = toolbarPanel;
		this.curriculum = curriculum;
		this.secCallback = secCallback;
		managed = CurriculumManagedFlag.isManaged(curriculum, CurriculumManagedFlag.members);
		
		initForm(ureq);
		toolbarPanel.addListener(this);
		if(tableEl.getSelectedFilterValue() == null) {
			tableEl.setSelectedFilterKey("active");
		}
		loadModel();
	}
	
	public Curriculum getCurriculum() {
		return curriculum;
	}

	@Override
	public void initTools() {
		if(secCallback.canNewCurriculumElement()) {
			newElementButton = LinkFactory.createToolLink("add.curriculum.element", translate("add.curriculum.element"), this, "o_icon_add");
			newElementButton.setElementCssClass("o_sel_add_curriculum_element");
			toolbarPanel.addTool(newElementButton, Align.left);	
		}
		
		if(secCallback.canManagerCurriculumElementsUsers()) {
			manageFocusedMembersLink = LinkFactory.createToolLink("manage.members.top", translate("manage.members"), this, "o_icon_group");
			manageFocusedMembersLink.setElementCssClass("o_sel_curriculum_element_manage_members");
			manageFocusedMembersLink.setVisible(false);
			toolbarPanel.addTool(manageFocusedMembersLink, Align.left);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(secCallback.canManagerCurriculumElementsUsers()) {
			if(managed && isAllowedToOverrideManaged(ureq)) {
				overrideLink = uifactory.addFormLink("override.member", formLayout, Link.BUTTON);
				overrideLink.setIconLeftCSS("o_icon o_icon-fw o_icon_refresh");
				
				unOverrideLink = uifactory.addFormLink("unoverride.member", formLayout, Link.BUTTON);
				unOverrideLink.setIconLeftCSS("o_icon o_icon-fw o_icon_refresh");
				unOverrideLink.setVisible(false);
			}
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.key, "select"));

		TreeNodeFlexiCellRenderer treeNodeRenderer = new TreeNodeFlexiCellRenderer("select");
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.displayName, treeNodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.identifier, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.externalId));
		DateFlexiCellRenderer dateRenderer = new DateFlexiCellRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.beginDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.endDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.resources));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.numOfMembers, "members"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.numOfParticipants, "participants"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.numOfCoaches, "coachs"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.numOfOwners, "owners"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.calendars));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.lectures));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.learningProgress));

		DefaultFlexiColumnModel zoomColumn = new DefaultFlexiColumnModel("zoom", translate("zoom"), "tt-focus");
		zoomColumn.setExportable(false);
		zoomColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(zoomColumn);
		if(secCallback.canEditCurriculumElements() || (!managed && secCallback.canManagerCurriculumElementsUsers())) {
			StickyActionColumnModel toolsColumn = new StickyActionColumnModel(ElementCols.tools);
			toolsColumn.setExportable(false);
			toolsColumn.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(toolsColumn);
		}
		
		tableModel = new CurriculumComposerTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setRootCrumb(new CurriculumCrumb(curriculum.getDisplayName()));
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_curriculum_el_listing");		
		if(secCallback.canNewCurriculumElement()) {
			tableEl.setEmptyTableSettings("table.curriculum.element.empty", "table.curriculum.element.empty.hint", "o_icon_curriculum_element", "add.curriculum.element", "o_icon_add", true);
		} else {			
			tableEl.setEmptyTableSettings("table.curriculum.element.empty", null, "o_icon_curriculum_element");
		}
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setExportEnabled(true);
		tableEl.setPageSize(40);
		tableEl.setCssDelegate(this);
		tableEl.setSearchEnabled(true);
		tableEl.setFilters("activity", getFilters(), false);
		tableEl.setAndLoadPersistedPreferences(ureq, "curriculum-composer");
	}
	
	private List<FlexiTableFilter> getFilters() {
		List<FlexiTableFilter> filters = new ArrayList<>(5);
		filters.add(new FlexiTableFilter(translate("filter.active"), "active"));
		filters.add(new FlexiTableFilter(translate("filter.inactive"), "inactive"));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("show.all"), "all", true));
		return filters;
	}
	
	protected boolean isAllowedToOverrideManaged(UserRequest ureq) {
		if(curriculum != null) {
			Roles roles = ureq.getUserSession().getRoles();
			return roles.isAdministrator() && curriculumService.hasRoleExpanded(curriculum, getIdentity(),
					OrganisationRoles.administrator.name());
		}
		return false;
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
		CurriculumElementRow row = tableModel.getObject(pos);
		String cssClass;
		if(!row.isAcceptedByFilter()) {
			cssClass = "o_curriculum_element_unfiltered";
		} else if(row.getStatus() == CurriculumElementStatus.inactive) {
			cssClass = "o_curriculum_element_inactive";
		} else if(row.getStatus() == CurriculumElementStatus.deleted) {
			cssClass = "o_curriculum_element_deleted";
		} else {
			cssClass = "o_curriculum_element_active";
		}
		return cssClass;
	}

	@Override
	protected void doDispose() {
		toolbarPanel.removeListener(this);
		if(!toolbarPanel.isToolbarEnabled()) {
			toolbarPanel.setToolbarEnabled(true);
		}
        super.doDispose();
	}
	
	private void loadModel() {
		List<CurriculumElementInfos> elements = curriculumService.getCurriculumElementsWithInfos(curriculum);
		List<CurriculumElementRow> rows = new ArrayList<>(elements.size());
		Map<Long, CurriculumElementRow> keyToRows = new HashMap<>();
		for(CurriculumElementInfos element:elements) {
			CurriculumElementRow row = forgeRow(element);
			rows.add(row);
			keyToRows.put(element.getKey(), row);
		}
		//parent line
		for(CurriculumElementRow row:rows) {
			if(row.getParentKey() != null) {
				row.setParent(keyToRows.get(row.getParentKey()));
			}
		}
		Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private CurriculumElementRow forgeRow(CurriculumElementInfos element) {
		FormLink toolsLink = null;
		boolean tooled = secCallback.canEditCurriculumTree() || secCallback.canEditCurriculumElement(element.getCurriculumElement());
		if(tooled) {
			toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		}
		
		FormLink resourcesLink = null;
		if(element.getNumOfResources() > 0) {
			resourcesLink = uifactory.addFormLink("resources_" + (++counter), "resources", String.valueOf(element.getNumOfResources()), null, null, Link.NONTRANSLATED);
		}
		CurriculumElementRow row = new CurriculumElementRow(element.getCurriculumElement(), element.getNumOfResources(),
				element.getNumOfParticipants(), element.getNumOfCoaches(), element.getNumOfOwners(),
				toolsLink, resourcesLink);
		if(toolsLink != null) {
			toolsLink.setUserObject(row);
		}
		if(resourcesLink != null) {
			resourcesLink.setUserObject(row);
		}
		
		if(row.isCalendarsEnabled()) {
			FormLink calendarsLink = uifactory.addFormLink("cals_" + (++counter), "calendars", "calendars", null, null, Link.LINK);
			calendarsLink.setIconLeftCSS("o_icon o_icon_timetable o_icon-fw");
			row.setCalendarsLink(calendarsLink);
			calendarsLink.setUserObject(row);
		}
		if(row.isLecturesEnabled()) {
			FormLink lecturesLink = uifactory.addFormLink("lecs_" + (++counter), "lectures", "lectures", null, null, Link.LINK);
			lecturesLink.setIconLeftCSS("o_icon o_icon_lecture o_icon-fw");
			row.setLecturesLink(lecturesLink);
			lecturesLink.setUserObject(row);
		}

		if(row.isLearningProgressEnabled()) {
			FormLink learningProgressLink = uifactory.addFormLink("lp_" + (++counter), "learning.progress", "learning.progress", null, null, Link.LINK);
			learningProgressLink.setIconLeftCSS("o_icon o_CourseModule_icon o_icon-fw");
			row.setLearningProgressLink(learningProgressLink);
			learningProgressLink.setUserObject(row);
		}
		
		return row;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		List<ContextEntry> subEntries = entries.subList(1, entries.size());
		if("CurriculumElement".equalsIgnoreCase(type)) {
			Long elementKey = entries.get(0).getOLATResourceable().getResourceableId();
			CurriculumElementRow row = tableModel.getCurriculumElementRowByKey(elementKey);
			if(row != null) {
				doEditCurriculumElement(ureq, row, subEntries);
			}
		} else if("Members".equalsIgnoreCase(type)) {
			Long elementKey = entries.get(0).getOLATResourceable().getResourceableId();
			CurriculumMembersManagementController mgmtCtrl = null;
			if(elementKey.intValue() == 0) {
				mgmtCtrl = doManageMembers(ureq);
			} else {
				CurriculumElementRow row = tableModel.getCurriculumElementRowByKey(elementKey);
				if(row != null) {
					mgmtCtrl = doManageMembers(ureq, row);
				}
			}
			if(mgmtCtrl != null) {
				mgmtCtrl.activate(ureq, subEntries, state);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == importMembersWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(importMembersWizard);
				importMembersWizard = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					loadModel();
				}
			}
		} else if(newElementCtrl == source || newSubElementCtrl == source
				|| moveElementCtrl == source || confirmDeleteCtrl == source
				|| copyCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (referencesCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
			if(event instanceof SelectReferenceEvent) {
				launch(ureq, ((SelectReferenceEvent)event).getEntry());
			}
		} else if(cmc == source || toolsCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(moveElementCtrl);
		removeAsListenerAndDispose(newElementCtrl);
		removeAsListenerAndDispose(referencesCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(copyCtrl);
		removeAsListenerAndDispose(cmc);
		toolsCalloutCtrl = null;
		confirmDeleteCtrl = null;
		moveElementCtrl = null;
		newElementCtrl = null;
		referencesCtrl = null;
		copyCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(newElementButton == source) {
			doNewCurriculumElement(ureq);
		} else if(manageFocusedMembersLink == source) {
			doManageMembers(ureq);
		} else if(toolbarPanel == source) {
			if(!toolbarPanel.isToolbarEnabled()) {
				toolbarPanel.setToolbarEnabled(true);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == overrideLink) {
			doOverrideManagedResource();
		} else if (source == unOverrideLink) {
			doUnOverrideManagedResource();
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doEditCurriculumElement(ureq, row, null);
				} else if("members".equals(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doManager(ureq, row, "All");
				} else if("participants".equals(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doManager(ureq, row, "Participants");
				} else if("coachs".equals(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doManager(ureq, row, "Coachs");
				} else if("owners".equals(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doManager(ureq, row, "Owners");
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				tableEl.reset(false, true, true);// only reload
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doNewCurriculumElement(ureq);
			} else {
				doFocus();
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				doOpenTools(ureq, (CurriculumElementRow)link.getUserObject(), link);
			} else if("resources".equals(cmd)) {
				doOpenReferences(ureq, (CurriculumElementRow)link.getUserObject(), link);
			} else if("calendars".equals(cmd)) {
				doOpenCalendars(ureq, (CurriculumElementRow)link.getUserObject());
			} else if("lectures".equals(cmd)) {
				doOpenLectures(ureq, (CurriculumElementRow)link.getUserObject());
			} else if("learning.progress".equals(cmd)) {
				doOpenLearningProgress(ureq, (CurriculumElementRow)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doManager(UserRequest ureq, CurriculumElementRow row, String context) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance()
				.createCEListFromString(OresHelper.createOLATResourceableInstance(context, 0l));
		doManageMembers(ureq, row).activate(ureq, entries, null);
	}
	
	private void doFocus() {
		CurriculumElementRow focusedRow = tableModel.getFocusedCurriculumElementRow();
		boolean visible = focusedRow != null;
		if(manageFocusedMembersLink != null && manageFocusedMembersLink.isVisible() != visible) {
			manageFocusedMembersLink.setVisible(visible);
			toolbarPanel.getToolBar().setDirty(true);
		}
	}
	
	private void doOverrideManagedResource() {
		overrideManagedResource(true);
	}
	
	private void doUnOverrideManagedResource() {
		overrideManagedResource(false);
	}
	
	private void overrideManagedResource(boolean override) {
		overrideManaged = override;

		overrideLink.setVisible(!overrideManaged);
		unOverrideLink.setVisible(overrideManaged);
	}
	
	private void doNewCurriculumElement(UserRequest ureq) {
		if(guardModalController(newElementCtrl)) return;

		newElementCtrl = new EditCurriculumElementController(ureq, getWindowControl(), null, curriculum, secCallback);
		listenTo(newElementCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", newElementCtrl.getInitialComponent(), true, translate("add.curriculum.element"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doNewSubCurriculumElement(UserRequest ureq, CurriculumElementRow row) {
		CurriculumElement parentElement = curriculumService.getCurriculumElement(row);
		if(parentElement == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			newSubElementCtrl = new EditCurriculumElementController(ureq, getWindowControl(), parentElement, curriculum, secCallback);
			newSubElementCtrl.setParentElement(parentElement);
			listenTo(newSubElementCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), "close", newSubElementCtrl.getInitialComponent(), true, translate("add.curriculum.element"));
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doCopyCurriculumElement(UserRequest ureq, CurriculumElementRow row) {
		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			copyCtrl = new CopySettingsController(ureq, getWindowControl(), element);
			listenTo(copyCtrl);
			
			String title = translate("copy.element.title", new String[] { StringHelper.escapeHtml(element.getDisplayName() )});
			cmc = new CloseableModalController(getWindowControl(), "close", copyCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doEditCurriculumElement(UserRequest ureq, CurriculumElementRow row, List<ContextEntry> entries) {
		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			toolbar(false);
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(CurriculumElement.class, row.getKey()), null);
			EditCurriculumElementOverviewController editCtrl = new EditCurriculumElementOverviewController(ureq, swControl, element, curriculum, secCallback);
			listenTo(editCtrl);
			toolbarPanel.pushController(row.getDisplayName(), editCtrl);
			editCtrl.activate(ureq, entries, null);
		}
	}
	
	private void doMoveCurriculumElement(UserRequest ureq, CurriculumElementRow row) {
		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			List<CurriculumElement> elementsToMove = Collections.singletonList(element);
			moveElementCtrl = new MoveCurriculumElementController(ureq, getWindowControl(), elementsToMove, curriculum, secCallback);
			listenTo(moveElementCtrl);
			
			String title = translate("move.element.title", new String[] { StringHelper.escapeHtml(row.getDisplayName() )});
			cmc = new CloseableModalController(getWindowControl(), "close", moveElementCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doChooseMembers(UserRequest ureq, CurriculumElementRow focusedRow) {
		removeAsListenerAndDispose(importMembersWizard);

		CurriculumElement focusedElement = focusedRow == null ? null : focusedRow.getCurriculumElement();
		MembersContext membersContext= MembersContext.valueOf(curriculum, focusedElement, overrideManaged, true);
		Step start = new ImportMember_1b_ChooseMemberStep(ureq, membersContext);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			addMembers(uureq, runContext);
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.member"), "o_sel_group_import_1_wizard");
		listenTo(importMembersWizard);
		getWindowControl().pushAsModalDialog(importMembersWizard.getInitialComponent());
	}
	
	private void doImportMembers(UserRequest ureq, CurriculumElementRow focusedRow) {
		removeAsListenerAndDispose(importMembersWizard);
		
		CurriculumElement focusedElement = focusedRow == null ? null : focusedRow.getCurriculumElement();
		MembersContext membersContext= MembersContext.valueOf(curriculum, focusedElement, overrideManaged, true);
		Step start = new ImportMember_1a_LoginListStep(ureq, membersContext);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			addMembers(uureq, runContext);
			MembersByNameContext membersByNameContext = (MembersByNameContext)runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY);
			if(!membersByNameContext.getNotFoundNames().isEmpty()) {
				String notFoundNames = membersByNameContext.getNotFoundNames().stream()
						.collect(Collectors.joining(", "));
				showWarning("user.notfound", notFoundNames);
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("import.member"), "o_sel_group_import_logins_wizard");
		listenTo(importMembersWizard);
		getWindowControl().pushAsModalDialog(importMembersWizard.getInitialComponent());
	}
	
	private void addMembers(UserRequest ureq, StepsRunContext runContext) {
		Roles roles = ureq.getUserSession().getRoles();

		Set<Identity> members = ((MembersByNameContext)runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY)).getIdentities();
		MailTemplate template = (MailTemplate)runContext.get("mailTemplate");
		MailPackage mailing = new MailPackage(template, getWindowControl().getBusinessControl().getAsString(), template != null);
		MemberPermissionChangeEvent changes = (MemberPermissionChangeEvent)runContext.get("permissions");
		List<CurriculumElementMembershipChange> curriculumChanges = changes.generateCurriculumElementMembershipChange(members);
		curriculumService.updateCurriculumElementMemberships(getIdentity(), roles, curriculumChanges, mailing);
		MailHelper.printErrorsAndWarnings(mailing.getResult(), getWindowControl(), false, getLocale());
	}

	private CurriculumMembersManagementController doManageMembers(UserRequest ureq) {
		CurriculumElementRow focusedRow = tableModel.getFocusedCurriculumElementRow();
		if(focusedRow != null) {
			return doManageMembers(ureq, focusedRow);
		}
		return null;
	}
	
	private CurriculumMembersManagementController doManageMembers(UserRequest ureq, CurriculumElementRow focusedRow) {
		CurriculumElement curriculumElement = focusedRow.getCurriculumElement();
		Long focus = focusedRow.getKey();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Members", focus);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		CurriculumMembersManagementController membersMgmtCtrl = new CurriculumMembersManagementController(ureq, bwControl, toolbarPanel,
				curriculum, curriculumElement, secCallback);
		listenTo(membersMgmtCtrl);

		String displayName = focusedRow.getDisplayName();
		toolbarPanel.pushController(displayName, membersMgmtCtrl);
		return membersMgmtCtrl;
	}
	
	private void doOpenTools(UserRequest ureq, CurriculumElementRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), row, element);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doOpenReferences(UserRequest ureq, CurriculumElementRow row, FormLink link) {
		removeAsListenerAndDispose(referencesCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null ) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			referencesCtrl = new ReferencesController(ureq, getWindowControl(), getTranslator(), element);
			listenTo(referencesCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					referencesCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doOpenCalendars(UserRequest ureq, CurriculumElementRow row) {
		removeAsListenerAndDispose(calendarsCtrl);
		toolbar(false);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Calendars", row.getKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntriesWithDescendants(row.getCurriculumElement());
		calendarsCtrl = new CurriculumElementCalendarController(ureq, bwControl, row, entries, secCallback);
		listenTo(calendarsCtrl);
		toolbarPanel.pushController(translate("calendars"), calendarsCtrl);
	}
	
	private void doOpenLectures(UserRequest ureq, CurriculumElementRow row) {
		removeAsListenerAndDispose(lecturesCtrl);
		toolbar(false);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Lectures", row.getKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(row);
		Curriculum curriculum = curriculumElement.getCurriculum();
		lecturesCtrl = new CurriculumElementLecturesController(ureq, bwControl, toolbarPanel, curriculum, curriculumElement, true, secCallback);
		listenTo(lecturesCtrl);
		toolbarPanel.pushController(row.getDisplayName(), null, row);
		toolbarPanel.pushController(translate("lectures"), lecturesCtrl);
	}
	
	private void doOpenLearningProgress(UserRequest ureq, CurriculumElementRow row) {
		removeAsListenerAndDispose(learningPathController);
		toolbar(false);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("LearningProgress", row.getKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(row);
		learningPathController = new CurriculumElementLearningPathController(ureq, bwControl, toolbarPanel, curriculumElement);
		listenTo(learningPathController);
		toolbarPanel.pushController(row.getDisplayName(), null, row);
		toolbarPanel.pushController(translate("learning.progress"), learningPathController);
	}
	
	private void doConfirmDelete(UserRequest ureq, CurriculumElementRow row) {
		if(guardModalController(confirmDeleteCtrl)) return;
		
		confirmDeleteCtrl = new ConfirmCurriculumElementDeleteController(ureq, getWindowControl(), row);
		listenTo(confirmDeleteCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", confirmDeleteCtrl.getInitialComponent(), true,
				translate("confirmation.delete.element.title", new String[] { row.getDisplayName() }));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void launch(UserRequest ureq, RepositoryEntryRef ref) {
		String businessPath = "[RepositoryEntry:" + ref.getKey() + "]";
		if(!NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl())) {
			tableEl.reloadData();
		}
	}
	
	private void toolbar(boolean enable) {
		toolbarPanel.setToolbarEnabled(enable);
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		private Link newLink;
		private Link editLink;
		private Link moveLink;
		private Link copyLink;
		private Link deleteLink;
		private Link addMemberLink;
		private Link importMemberLink;
		private Link manageMembersLink;
		
		private CurriculumElementRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl,
				CurriculumElementRow row, CurriculumElement element) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");

			List<String> links = new ArrayList<>(4);
			if(secCallback.canEditCurriculumElement(element)) {
				editLink = addLink("edit", "o_icon_edit", links);
				if(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.move)) {
					moveLink = addLink("move.element", "o_icon_move", links);
				}
				if(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.addChildren)) {
					newLink = addLink("add.element.under", "o_icon_levels", links);
				}
				copyLink = addLink("copy.element", "o_icon_copy", links);
			}

			if(!managed && secCallback.canManagerCurriculumElementUsers(element)) {
				if(!links.isEmpty()) {
					links.add("-");
				}
				
				manageMembersLink = addLink("manage.members", "o_icon_group", links);
				addMemberLink = addLink("add.member", "o_icon_add_member", links);
				importMemberLink = addLink("import.member", "o_icon_import", links);
			}
			
			if(secCallback.canEditCurriculumElement(element) && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.delete)) {
				links.add("-");
				deleteLink = addLink("delete", "o_icon_delete_item", links);
			}

			mainVC.contextPut("links", links);
			
			putInitialPanel(mainVC);
		}
		
		private Link addLink(String name, String iconCss, List<String> links) {
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.LINK);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon " + iconCss);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(editLink == source) {
				close();
				doEditCurriculumElement(ureq, row, null);
			} else if(moveLink == source) {
				close();
				doMoveCurriculumElement(ureq, row);
			} else if(deleteLink == source) {
				close();
				doConfirmDelete(ureq, row);
			} else if(newLink == source) {
				close();
				doNewSubCurriculumElement(ureq, row);
			} else if(copyLink == source) {
				close();
				doCopyCurriculumElement(ureq, row);
			} else if(addMemberLink == source) {
				close();
				doChooseMembers(ureq, row);
			} else if(importMemberLink == source) {
				close();
				doImportMembers(ureq, row);
			} else if(manageMembersLink == source) {
				close();
				doManageMembers(ureq, row);
			}
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
	
	private static class CurriculumCrumb implements FlexiTreeTableNode {
		
		private final String curriculumDisplayName;
		
		public CurriculumCrumb(String curriculumDisplayName) {
			this.curriculumDisplayName = curriculumDisplayName;
		}

		@Override
		public FlexiTreeTableNode getParent() {
			return null;
		}

		@Override
		public String getCrump() {
			return curriculumDisplayName;
		}
	}
}
