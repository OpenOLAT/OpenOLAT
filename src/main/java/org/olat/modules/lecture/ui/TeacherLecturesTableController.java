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
package org.olat.modules.lecture.ui;

import java.io.IOException;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TimeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.model.RollCallSecurityCallbackImpl;
import org.olat.modules.lecture.ui.TeacherOverviewDataModel.TeachCols;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.modules.lecture.ui.component.YesNoCellRenderer;
import org.olat.modules.lecture.ui.event.ReopenLectureBlockEvent;
import org.olat.modules.lecture.ui.export.LectureBlockExport;
import org.olat.modules.lecture.ui.export.LecturesBlockPDFExport;
import org.olat.modules.lecture.ui.export.LecturesBlockSignaturePDFExport;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherLecturesTableController extends FormBasicController implements BreadcrumbPanelAware, Activateable2 {
	
	private FlexiTableElement tableEl;
	private BreadcrumbPanel toolbarPanel;
	private TeacherOverviewDataModel tableModel;

	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private TeacherRollCallController rollCallCtrl;
	
	private int counter;
	private final String id;
	private final boolean admin;
	private final boolean sortAsc;
	private final String emptyI18nKey;
	private final boolean withRepositoryEntry, withTeachers;
	
	private final boolean isAdministrativeUser;
	private final boolean authorizedAbsenceEnabled;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public TeacherLecturesTableController(UserRequest ureq, WindowControl wControl,
			boolean admin, String emptyI18nKey, boolean sortAsc, String id,
			boolean withRepositoryEntry, boolean withTeachers) {
		super(ureq, wControl, "teacher_view_table");
		this.id = id;
		this.admin = admin;
		this.sortAsc = sortAsc;
		this.emptyI18nKey = emptyI18nKey;
		this.withTeachers = withTeachers;
		this.withRepositoryEntry = withRepositoryEntry;
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		initForm(ureq);
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.toolbarPanel = stackPanel;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(withRepositoryEntry) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.externalRef, "open.course"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.entry, "open.course"));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.date, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.startTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.endTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.lectureBlock));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.compulsory, new YesNoCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.location));
		if(withTeachers) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.teachers));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.status, new LectureBlockStatusCellRenderer(getTranslator())));
		DefaultFlexiColumnModel detailsCol = new DefaultFlexiColumnModel(TeachCols.details.i18nHeaderKey(), TeachCols.details.ordinal(), "details",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.details"), "details"), null));
		// set sort key even though we do not sort - added as css classes to column headers for styling
		detailsCol.setSortKey(TeachCols.details.name());
		columnsModel.addFlexiColumnModel(detailsCol);
		DefaultFlexiColumnModel toolsCol = new DefaultFlexiColumnModel(TeachCols.tools);
		toolsCol.setSortable(false);
		columnsModel.addFlexiColumnModel(toolsCol);
		
		tableModel = new TeacherOverviewDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(TeachCols.date.name(), sortAsc));
		tableEl.setSortSettings(sortOptions);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setEmtpyTableMessageKey(emptyI18nKey);
		tableEl.setAndLoadPersistedPreferences(ureq, "lecture-teacher-overview-v2-".concat(id));
	}
	
	public int getRowCount() {
		return tableModel.getRowCount();
	}
	
	public LectureBlockRow getRow(Long lectureBlockKey) {
		if(lectureBlockKey == null) return null;
		
		for(LectureBlockRow row:tableModel.getObjects()) {
			if(row.getKey().equals(lectureBlockKey)) {
				return row;
			}
		}
		return null;
	}
	
	protected void setTablePageSize(int pageSize) {
		tableEl.setPageSize(pageSize);
	}
	
	protected void loadModel(List<LectureBlockRow> blocks) {
		for(LectureBlockRow row:blocks) {
			FormLink toolsLink = uifactory.addFormLink("tools_" + (counter++), "tools", "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_actions");
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
		}
		tableModel.setObjects(blocks);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String name = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("LectureBlock".equals(name)) {
			Long lectureBlockKey = entries.get(0).getOLATResourceable().getResourceableId();
			if(tableModel.getRowCount() > 0) {
				List<LectureBlockRow> rows = tableModel.getObjects();
				for(LectureBlockRow row:rows) {
					if(row.getKey().equals(lectureBlockKey)) {
						doSelectLectureBlock(ureq, row.getLectureBlock());
					}
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == rollCallCtrl) {
			if(event instanceof ReopenLectureBlockEvent) {
				LectureBlock lectureBlock = rollCallCtrl.getLectureBlock();
				toolbarPanel.popController(rollCallCtrl);
				doSelectLectureBlock(ureq, lectureBlock);
			} else if(event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		} else if(toolsCalloutCtrl == source) {
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolsCalloutCtrl.deactivate();
				cleanUp();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("details".equals(cmd)) {
					LectureBlockRow row = tableModel.getObject(se.getIndex());
					doSelectLectureBlock(ureq, row.getLectureBlock());
				} else if("export".equals(cmd)) {
					LectureBlockRow row = tableModel.getObject(se.getIndex());
					doExportLectureBlock(ureq, row.getLectureBlock());
				} else if("open.course".equals(cmd)) {
					LectureBlockRow row = tableModel.getObject(se.getIndex());
					doOpenCourseLectures(ureq, row);
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				LectureBlockRow row = (LectureBlockRow)link.getUserObject();
				doOpenTools(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		toolsCalloutCtrl = null;
		toolsCtrl = null;
	}
	
	private void doExportLectureBlock(UserRequest ureq, LectureBlock row) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		List<Identity> teachers = lectureService.getTeachers(lectureBlock);
		LectureBlockExport export = new LectureBlockExport(lectureBlock, teachers, isAdministrativeUser, authorizedAbsenceEnabled, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(export);
	}
	
	private void doExportAttendanceList(UserRequest ureq, LectureBlock row) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		List<Identity> participants = lectureService.getParticipants(lectureBlock);
		List<LectureBlockRollCall> rollCalls = lectureService.getRollCalls(row);
		try {
			LecturesBlockPDFExport export = new LecturesBlockPDFExport(lectureBlock, authorizedAbsenceEnabled, getTranslator());
			export.setTeacher(userManager.getUserDisplayName(getIdentity()));
			export.create(participants, rollCalls);
			ureq.getDispatchResult().setResultingMediaResource(export);
		} catch (IOException | TransformerException e) {
			logError("", e);
		}
	}
	
	private void doExportAttendanceListForSignature(UserRequest ureq, LectureBlock row) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		List<Identity> participants = lectureService.getParticipants(lectureBlock);
		try {
			LecturesBlockSignaturePDFExport export = new LecturesBlockSignaturePDFExport(lectureBlock, getTranslator());
			export.setTeacher(userManager.getUserDisplayName(getIdentity()));
			export.create(participants);
			ureq.getDispatchResult().setResultingMediaResource(export);
		} catch (IOException | TransformerException e) {
			logError("", e);
		}
	}
	
	private void doSelectLectureBlock(UserRequest ureq, LectureBlock block) {
		LectureBlock reloadedBlock = lectureService.getLectureBlock(block);
		List<Identity> participants = lectureService.startLectureBlock(getIdentity(), reloadedBlock);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("LectureBlock", block.getKey());
		WindowControl swControl = addToHistory(ureq, ores, null);
		rollCallCtrl = new TeacherRollCallController(ureq, swControl, reloadedBlock, participants, getRollCallSecurityCallback(reloadedBlock));
		listenTo(rollCallCtrl);
		toolbarPanel.pushController(reloadedBlock.getTitle(), rollCallCtrl);
	}
	
	private void doOpenCourseLectures(UserRequest ureq, LectureBlockRow row) {
		Long repoKey = row.getLectureBlock().getEntry().getKey();
		String businessPath = "[RepositoryEntry:" + repoKey + "][Lectures:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private void doOpenTools(UserRequest ureq, LectureBlockRow row, FormLink link) {
		if(toolsCtrl != null) return;
		
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		LectureBlock block = lectureService.getLectureBlock(row);
		if(block == null) {
			tableEl.reloadData();
			showWarning("lecture.blocks.not.existing");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}

	private RollCallSecurityCallback getRollCallSecurityCallback(LectureBlock block) {
		return new RollCallSecurityCallbackImpl(admin, true, block, lectureModule);
	}

	private class ToolsController extends BasicController {
		
		private final LectureBlockRow row;

		public ToolsController(UserRequest ureq, WindowControl wControl, LectureBlockRow row) {
			super(ureq, wControl);
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tools");
			addLink("export", "export", "o_icon o_filetype_xlsx", mainVC);
			addLink("attendance.list", "attendance.list", "o_icon o_filetype_pdf", mainVC);
			addLink("attendance.list.to.sign", "attendance.list.to.sign", "o_icon o_filetype_pdf", mainVC);
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS, VelocityContainer mainVC) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if("export".equals(cmd)) {
					LectureBlock block = lectureService.getLectureBlock(row);
					doExportLectureBlock(ureq, block);
				} else if("attendance.list.to.sign".equals(cmd)) {
					LectureBlock block = lectureService.getLectureBlock(row);
					doExportAttendanceListForSignature(ureq, block);
				} else if("attendance.list".equals(cmd)) {
					LectureBlock block = lectureService.getLectureBlock(row);
					doExportAttendanceList(ureq, block);
				}
			}
		}

		@Override
		protected void doDispose() {
			//
		}
	}
}
