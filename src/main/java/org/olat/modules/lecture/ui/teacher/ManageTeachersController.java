/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.lecture.ui.teacher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateWithDayFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.YesNoCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.ui.LectureListDetailsController;
import org.olat.modules.lecture.ui.LectureListRepositoryConfig;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.component.IdentityComparator;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.modules.lecture.ui.component.LocationCellRenderer;
import org.olat.modules.lecture.ui.component.ReferenceRenderer;
import org.olat.modules.lecture.ui.teacher.ManageTeachersDataModel.BlockTeachersCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.MembershipInfos;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ManageTeachersController extends FormBasicController implements FlexiTableComponentDelegate {
	
	public static final int TEACHERS_OFFSET = 500;
	public static final String FILTER_TEACHERS = "Teachers";
	public static final String NO_TEACHER = "noteacher";

	private static final String ON_KEY = "on";
	private static final String ALL_TAB_ID = "All";
	private static final String WITHOUT_TEACHERS_TAB_ID = "WithoutTeachers";
	private static final String TOGGLE_DETAILS_CMD = "toggle-details";
	
	private FormLink assignToAllEventsButton;
	private FormLink removeFromAllEventsButton;
	
	private FlexiTableElement tableEl;
	private ManageTeachersDataModel tableModel;
	
	private FlexiFiltersTab allTab;
	private FlexiFiltersTab withoutTeachersTab;
	private FlexiTableMultiSelectionFilter teachersFilter;
	private final VelocityContainer detailsVC;

	private final CoachList teachers;
	private final boolean taxonomyEnabled;
	private final RepositoryEntry entry;
	private final List<LectureBlockRow> lectureBlocksRows;
	private final LectureListRepositoryConfig config;
	private final LecturesSecurityCallback secCallback;
	
	private TeachersController assignTeachersCtrl;
	private TeachersController removeTeachersCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryService repositoryService;
	
	public ManageTeachersController(UserRequest ureq, WindowControl wControl, List<LectureBlockRow> lectureBlocksRows,
			LectureListRepositoryConfig config, RepositoryEntry entry, boolean taxonomyEnabled, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "manage_teachers", Util
				.createPackageTranslator(LectureListRepositoryController.class, ureq.getLocale()));
		this.entry = entry;
		this.config = config;
		this.secCallback = secCallback;
		this.lectureBlocksRows = new ArrayList<>(lectureBlocksRows);
		teachers = loadTeacherList(lectureBlocksRows);
		this.taxonomyEnabled = taxonomyEnabled;
		detailsVC = createVelocityContainer("lecture_details");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initButtons(formLayout, ureq);
		initTable(formLayout, ureq);
	}
	
	private void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		assignToAllEventsButton = uifactory.addFormLink("assign.to.all.events", formLayout, Link.BUTTON);
		assignToAllEventsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_check_on");
		assignToAllEventsButton.setIconRightCSS("o_icon o_icon-fw o_icon_caret");
		removeFromAllEventsButton = uifactory.addFormLink("remove.from.all.events", formLayout, Link.BUTTON);
		removeFromAllEventsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_check_off");
		removeFromAllEventsButton.setIconRightCSS("o_icon o_icon-fw o_icon_caret");
		
		uifactory.addFormSubmitButton("apply", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	private void initTable(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockTeachersCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockTeachersCols.externalId));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockTeachersCols.date,
				new DateWithDayFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockTeachersCols.startTime,
				new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockTeachersCols.endTime,
				new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockTeachersCols.lecturesNumber));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockTeachersCols.title, TOGGLE_DETAILS_CMD));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockTeachersCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockTeachersCols.curriculumElement,
				new ReferenceRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockTeachersCols.status,
				new LectureBlockStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockTeachersCols.entry,
				new ReferenceRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockTeachersCols.location,
				new LocationCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockTeachersCols.numParticipants));
		
		DefaultFlexiColumnModel compulsoryColumn = new DefaultFlexiColumnModel(false, BlockTeachersCols.compulsory,
				new YesNoCellRenderer());
		compulsoryColumn.setIconHeader("o_icon o_icon_compulsory o_icon-lg");
		columnsModel.addFlexiColumnModel(compulsoryColumn);
		
		int numOfTeachers = teachers.teachers().size();
		for(int j=0; j<numOfTeachers; j++) {
			Identity teacher = teachers.teachers().get(j);
			int colIndex = TEACHERS_OFFSET + j;
			String fullName = userManager.getUserDisplayName(teacher);
			DefaultFlexiColumnModel col = new DefaultFlexiColumnModel(true, null, colIndex, false, teacher.getKey().toString());
			col.setHeaderLabel(fullName);
			columnsModel.addFlexiColumnModel(col);
		}
		
		tableModel = new ManageTeachersDataModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
		
		initFilters();
		initFiltersPresets();
		
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues teachersValues = new SelectionValues();
		teachersValues.add(SelectionValues.entry(NO_TEACHER, translate("filter.no.teachers")));
		for(Identity teacher: teachers.teachers()) {
			String fullName = StringHelper.escapeHtml(userManager.getUserDisplayName(teacher));
			teachersValues.add(SelectionValues.entry(teacher.getKey().toString(), fullName));
		}
		teachersFilter = new FlexiTableMultiSelectionFilter(translate("filter.teachers"),
				FILTER_TEACHERS, teachersValues, true);
		filters.add(teachersFilter);

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setElementCssClass("o_sel_elements_all");
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		withoutTeachersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(WITHOUT_TEACHERS_TAB_ID, translate("filter.without.teachers"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_TEACHERS, NO_TEACHER)));
		withoutTeachersTab.setFiltersExpanded(true);
		tabs.add(withoutTeachersTab);

		tableEl.setFilterTabs(true, tabs);
	}

	private void loadModel() {
		List<CurriculumElementMembership> curriculumElementCoaches = teachers.curriculumElementsMemberships();
		List<MembershipInfos> entryCoaches = teachers.entriesMemberships();
		List<LectureBlockTeachersRow> rows = new ArrayList<>(lectureBlocksRows.size());
		for(LectureBlockRow lectureBlock:lectureBlocksRows) {
			LectureBlockTeachersRow row = forgeRow(lectureBlock, curriculumElementCoaches, entryCoaches);
			rows.add(row);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private CoachList loadTeacherList(List<LectureBlockRow> blocks) {
		List<CurriculumElementMembership> curriculumElementsMemberships = curriculumElementCoaches(blocks);
		List<MembershipInfos> entriesMemberships = repositoryEntryCoaches(blocks);
		
		Set<Long> teachersAndCoachesKeys = new HashSet<>();
		List<Identity> teachersAndCoaches = new ArrayList<>();
		for(LectureBlockRow block:blocks) {
			List<Identity> teachersList = block.getTeachersList();
			for(Identity teacher:teachersList) {
				if(!teachersAndCoachesKeys.contains(teacher.getKey())) {
					teachersAndCoaches.add(teacher);
					teachersAndCoachesKeys.add(teacher.getKey());
				}
			}
		}
		
		Set<Long> missingIdentities = new HashSet<>();
		for(CurriculumElementMembership membership:curriculumElementsMemberships) {
			if(!teachersAndCoachesKeys.contains(membership.getIdentityKey())) {
				missingIdentities.add(membership.getIdentityKey());
				teachersAndCoachesKeys.add(membership.getIdentityKey());
			}
		}
		for(MembershipInfos membership:entriesMemberships) {
			if(!teachersAndCoachesKeys.contains(membership.getIdentityKey())) {
				missingIdentities.add(membership.getIdentityKey());
				teachersAndCoachesKeys.add(membership.getIdentityKey());
			}
		}
		
		if(!missingIdentities.isEmpty()) {
			List<Identity> moreTeachers = securityManager.loadIdentityByKeys(missingIdentities);
			teachersAndCoaches.addAll(moreTeachers);
		}
		
		if(teachersAndCoaches.size() > 1) {
			Collections.sort(teachersAndCoaches, new IdentityComparator(getLocale()));
		}
		return new CoachList(curriculumElementsMemberships, entriesMemberships, teachersAndCoaches);
	}
	
	private List<CurriculumElementMembership> curriculumElementCoaches(List<LectureBlockRow> blocks) {
		Collection<CurriculumElementRef> elements = new HashSet<>();
		for(LectureBlockRow block:blocks) {
			if(block.getCurriculumElement() != null && block.getCurriculumElement().key() != null) {
				elements.add(new CurriculumElementRefImpl(block.getCurriculumElement().key()));
			}
		}
		List<CurriculumElementMembership> coaches = List.of();
		if(!elements.isEmpty()) {
			List<CurriculumElementMembership> memberships = curriculumService.getCurriculumElementMemberships(elements);
			coaches = memberships.stream()
					.filter(m -> m.isCoach())
					.toList();
		}
		return coaches;
	}
	
	private List<MembershipInfos> repositoryEntryCoaches(List<LectureBlockRow> blocks) {
		Set<RepositoryEntryRef> entries = new HashSet<>();
		for(LectureBlockRow block:blocks) {
			if((block.getCurriculumElement() == null || block.getCurriculumElement().key() == null)
					&& block.getEntry() != null && block.getEntry().key() != null) {
				entries.add(new RepositoryEntryRefImpl(block.getEntry().key()));
			}
		}
		List<MembershipInfos> coaches = List.of();
		if(!entries.isEmpty()) {
			coaches = repositoryService.getMemberships(new ArrayList<>(entries), GroupRoles.coach.name());
		}
		return coaches;
	}
	
	private LectureBlockTeachersRow forgeRow(LectureBlockRow lectureBlock,
			List<CurriculumElementMembership> curriculumElementCoaches,
			List<MembershipInfos> entryCoaches) {
		LectureBlockTeachersRow row = new LectureBlockTeachersRow(lectureBlock);
		List<Identity> currentTeachers = lectureBlock.getTeachersList();

		SelectionValues pk = new SelectionValues();
		pk.add(SelectionValues.entry(ON_KEY, ""));
		
		final String idPrefix = "teacher_" + lectureBlock.getKey() + "_";
		final int numOfTeachers = teachers.teachers().size();
		MultipleSelectionElement[] teachersEl = new MultipleSelectionElement[numOfTeachers];
		for(int i=0; i<numOfTeachers; i++) {
			Identity teacher = teachers.teachers().get(i);
			boolean coach = isCoach(lectureBlock, teacher, curriculumElementCoaches, entryCoaches);

			MultipleSelectionElement teacherEl = uifactory.addCheckboxesHorizontal(idPrefix + teacher.getKey(), null, flc, pk.keys(), pk.values());
			teacherEl.setAjaxOnly(true);
			teacherEl.setVisible(coach);
			teacherEl.setUserObject(teacher);
			if(currentTeachers != null && currentTeachers.contains(teacher)) {
				teacherEl.select(ON_KEY, true);
				teacherEl.setVisible(true);
			}
			teachersEl[i] = teacherEl;
		}
		row.setTeachersEl(teachersEl);
		return row;
	}
	
	private boolean isCoach(LectureBlockRow lectureBlock, Identity teacher,
			List<CurriculumElementMembership> curriculumElementCoaches,
			List<MembershipInfos> entryCoaches) {
		final Long teacherKey = teacher.getKey();
		if(lectureBlock.getCurriculumElement() != null && lectureBlock.getCurriculumElement().key() != null) {
			final Long curriculumElementKey = lectureBlock.getCurriculumElement().key();
			return curriculumElementCoaches.stream()
					.anyMatch(ec -> curriculumElementKey.equals(ec.getCurriculumElementKey()) && teacherKey.equals(ec.getIdentityKey()));
		}
		if(lectureBlock.getEntry() != null && lectureBlock.getEntry().key() != null) {
			final Long entryKey = lectureBlock.getEntry().key();
			return entryCoaches.stream()
					.anyMatch(ec -> entryKey.equals(ec.getRepositoryEntryKey()) && teacherKey.equals(ec.getIdentityKey()));
		}
		return false;
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>(1);
		if(rowObject instanceof LectureBlockTeachersRow lectureRow) {
			if(lectureRow.getDetailsController() != null) {
				components.add(lectureRow.getDetailsController().getInitialFormItem().getComponent());
			}
		}
		return components;
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(calloutCtrl == source) {
			cleanUp();
		} else if(assignTeachersCtrl == source || removeTeachersCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if(assignTeachersCtrl == source) {
					doTeachers(assignTeachersCtrl.getSelectedTeacher(), true);
				} else if(removeTeachersCtrl == source) {
					doTeachers(removeTeachersCtrl.getSelectedTeacher(), false);
				}
				if(calloutCtrl != null) {
					calloutCtrl.deactivate();
					cleanUp();
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(assignTeachersCtrl);
		removeAsListenerAndDispose(removeTeachersCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		assignTeachersCtrl = null;
		removeTeachersCtrl = null;
		calloutCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(assignToAllEventsButton == source) {
			doOpenAssignTeachersList(ureq, assignToAllEventsButton);
		} else if(removeFromAllEventsButton == source) {
			doOpenRemoveTeachersList(ureq, removeFromAllEventsButton);
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				LectureBlockTeachersRow row = tableModel.getObject(se.getIndex());
				if(TOGGLE_DETAILS_CMD.equals(cmd)) {
					if(row.getDetailsController() != null) {
						doCloseLectureBlockDetails(row);
						tableEl.collapseDetails(se.getIndex());
					} else {
						doOpenLectureBlockDetails(ureq, row);
						tableEl.expandDetails(se.getIndex());
					}
				}
			} else if(event instanceof DetailsToggleEvent toggleEvent) {
				LectureBlockTeachersRow row = tableModel.getObject(toggleEvent.getRowIndex());
				if(toggleEvent.isVisible()) {
					doOpenLectureBlockDetails(ureq, row);
				} else {
					doCloseLectureBlockDetails(row);
				}
			} else if(event instanceof FlexiTableFilterTabEvent
					|| event instanceof FlexiTableSearchEvent
					|| event instanceof FlexiTableFilterTabEvent) {
				tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
				tableEl.reset(true, true, true);
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
		doApply();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doApply() {
		List<LectureBlockTeachersRow> rows = tableModel.getObjects();
		for(LectureBlockTeachersRow row:rows) {
			final List<Identity> currentTeachers = row.getLectureBlockRow().getTeachersList();
			final LectureBlock lectureBlock = row.getLectureBlock();
			
			MultipleSelectionElement[] teachersEl = row.getTeachersEl();
			for(MultipleSelectionElement teacherEl:teachersEl) {
				Identity teacher = (Identity)teacherEl.getUserObject();
				boolean selected = teacherEl.isVisible() && teacherEl.isAtLeastSelected(1);
				if(currentTeachers.contains(teacher) && !selected) {
					lectureService.removeTeacher(lectureBlock, teacher);
				} else if(!currentTeachers.contains(teacher) && selected) {
					lectureService.addTeacher(lectureBlock, teacher);
				}
			}
			dbInstance.commitAndCloseSession();
		}
	}
	
	private void doOpenAssignTeachersList(UserRequest ureq, FormLink link) {
		removeAsListenerAndDispose(assignTeachersCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		int numOfLecturesBlocks = tableModel.getRowCount();
		List<LectureBlockTeachersRow> rows = tableModel.getObjects();
		List<Teacher> teachersInfos = new ArrayList<>(teachers.teachers().size());
		for(Identity teacher:teachers.teachers()) {
			int teacherIndex = teachers.teachers().indexOf(teacher);
			int open = 0;
			for(LectureBlockTeachersRow row:rows) {
				MultipleSelectionElement teacherEl = row.getTeacherEl(teacherIndex);
				if(teacherEl.isVisible() && !teacherEl.isAtLeastSelected(1)) {
					open++;
				}
			}
			teachersInfos.add(new Teacher(teacher, open, numOfLecturesBlocks));
		}
		assignTeachersCtrl = new TeachersController(ureq, getWindowControl(), teachersInfos);
		listenTo(assignTeachersCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				assignTeachersCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doOpenRemoveTeachersList(UserRequest ureq, FormLink link) {
		removeAsListenerAndDispose(removeTeachersCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		
		int numOfLecturesBlocks = tableModel.getRowCount();
		List<LectureBlockTeachersRow> rows = tableModel.getObjects();
		List<Teacher> teachersInfos = new ArrayList<>(teachers.teachers().size());
		for(Identity teacher:teachers.teachers()) {
			int teacherIndex = teachers.teachers().indexOf(teacher);
			int assigned = 0;
			for(LectureBlockTeachersRow row:rows) {
				MultipleSelectionElement teacherEl = row.getTeacherEl(teacherIndex);
				if(teacherEl.isVisible() && teacherEl.isAtLeastSelected(1)) {
					assigned++;
				}
			}
			teachersInfos.add(new Teacher(teacher, assigned, numOfLecturesBlocks));
		}
		removeTeachersCtrl = new TeachersController(ureq, getWindowControl(), teachersInfos);
		listenTo(removeTeachersCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				removeTeachersCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doTeachers(Identity teacher, final boolean select) {
		int teacherIndex = teachers.teachers().indexOf(teacher);
		if(teacherIndex >= 0) {
			tableModel.getObjects().stream().forEach(row -> {
				row.selectTeacher(teacherIndex, select);
			});
		}
	}
	
	private void doOpenLectureBlockDetails(UserRequest ureq, LectureBlockTeachersRow row) {
		if(row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}

		LectureListDetailsController detailsCtrl = new LectureListDetailsController(ureq, getWindowControl(), row.getLectureBlockRow(),
				mainForm, config, true, entry != null, taxonomyEnabled, secCallback);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}

	private void doCloseLectureBlockDetails(LectureBlockTeachersRow row) {
		if(row.getDetailsController() == null) return;
		removeAsListenerAndDispose(row.getDetailsController());
		flc.remove(row.getDetailsController().getInitialFormItem());
		row.setDetailsController(null);
	}
	
	protected record Teacher(Identity identity, int number, int total) {
		//
	}
	
	protected record CoachList(List<CurriculumElementMembership> curriculumElementsMemberships, List<MembershipInfos> entriesMemberships, List<Identity> teachers) {
		//
	}
	
	private class TeachersController extends BasicController {
		
		private VelocityContainer mainVC;
		
		private Teacher selectedTeacher;
		
		private TeachersController(UserRequest ureq, WindowControl wControl, List<Teacher> teachersInfos) {
			super(ureq, wControl);
			
			mainVC = createVelocityContainer("teachers_dropdown");
			List<String> teachersNames = new ArrayList<>(teachersInfos.size());
			for(Teacher teacherInfos:teachersInfos) {
				teachersNames.add(addLink(teacherInfos));
			}
			mainVC.contextPut("teachersNames", teachersNames);
			putInitialPanel(mainVC);
		}
		
		public Identity getSelectedTeacher() {
			return selectedTeacher == null ? null : selectedTeacher.identity();
		}
		
		private String addLink(Teacher teacher) {
			String name = "teacher_" + teacher.identity().getKey();
			StringBuilder sb = new StringBuilder();
			sb.append(StringHelper.escapeHtml(userManager.getUserDisplayName(teacher.identity())));
			sb.append(" (").append(teacher.number()).append("/").append(teacher.total()).append(")");
			
			Link link = LinkFactory.createLink(name, name, "teacher", sb.toString(), getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
			link.setEscapeMode(EscapeMode.none);
			link.setUserObject(teacher);
			mainVC.put(name, link);
			return name;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(source instanceof Link link && link.getUserObject() instanceof Teacher teacher) {
				selectedTeacher = teacher;
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}
}
