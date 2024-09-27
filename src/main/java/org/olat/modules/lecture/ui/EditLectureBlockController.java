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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityNames;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.AutoCompleter;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.panel.EmptyPanelItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.member.MemberListController;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.MemberViewQueries;
import org.olat.group.model.MemberView;
import org.olat.group.ui.main.CourseMembership;
import org.olat.group.ui.main.CourseRoleCellRenderer;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.group.ui.main.SearchMembersParams.Origin;
import org.olat.group.ui.main.SearchMembersParams.UserType;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockManagedFlag;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LocationHistory;
import org.olat.modules.lecture.ui.component.LocationDateComparator;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditLectureBlockController extends FormBasicController {

	private static final String USER_PROPS_ID = MemberListController.class.getCanonicalName();
	private static final String ON_KEY = "on";
	
	private TextElement titleEl;
	private TextElement externalRefEl;
	private TextElement descriptionEl;
	private TextElement preparationEl;
	private AutoCompleter locationEl;
	private DateChooser dateEl;
	private SingleSelection plannedLecturesEl;
	private MultipleSelectionElement teacherEl;
	private MultipleSelectionElement compulsoryEl;
	
	private final boolean readOnly;
	private RepositoryEntry entry;
	private LectureBlock lectureBlock;
	private CurriculumElement curriculumElement;
	
	private List<MemberView> possibleTeachersList;

	private final List<Identity> teachers;
	private final boolean lectureManagementManaged;
	private final List<LocationHistory> locations;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private MemberViewQueries memberQueries;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;

	public EditLectureBlockController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		this(ureq, wControl, entry, null, null, false);
	}
	
	public EditLectureBlockController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			LectureBlock lectureBlock, boolean readOnly) {
		this(ureq, wControl, entry, null, lectureBlock, readOnly);
	}
	
	public EditLectureBlockController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement) {
		this(ureq, wControl, null, curriculumElement, null, false);
	}
	
	public EditLectureBlockController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement,
			LectureBlock lectureBlock, boolean readOnly) {
		this(ureq, wControl, null, curriculumElement, lectureBlock, readOnly);
	}
	
	private EditLectureBlockController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, CurriculumElement curriculumElement,
			LectureBlock lectureBlock, boolean readOnly) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.readOnly = readOnly;
		this.lectureBlock = lectureBlock;
		this.curriculumElement = curriculumElement;
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, true);
		
		locations = getLocations(ureq);
		lectureManagementManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lecturemanagement);
		if(lectureBlock != null && lectureBlock.getKey() != null) {
			teachers = lectureService.getTeachers(lectureBlock);
		} else {
			teachers = List.of();
		}
		
		initForm(ureq);
		updateUI();
	}
	
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_repo_edit_lecture_form");
		
		if(lectureBlock != null && StringHelper.containsNonWhitespace(lectureBlock.getManagedFlagsString())) {
			setFormWarning("form.managedflags.intro.short", null);
		}

		String title = lectureBlock == null ? null : lectureBlock.getTitle();
		titleEl = uifactory.addTextElement("title", "lecture.title", 128, title, formLayout);
		titleEl.setElementCssClass("o_sel_repo_lecture_title");
		titleEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.title));
		titleEl.setMandatory(true);
		if(!StringHelper.containsNonWhitespace(title)) {
			titleEl.setFocus(true);
		}

		String externalRef = lectureBlock == null ? null : lectureBlock.getExternalRef();
		externalRefEl = uifactory.addTextElement("externalref", "lecture.external.ref", 128, externalRef, formLayout);
		externalRefEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.externalRef));

		Date startDate = lectureBlock == null ? null : lectureBlock.getStartDate();
		Date endDate = lectureBlock == null ? null : lectureBlock.getEndDate();
		dateEl = uifactory.addDateChooser("lecture.date", startDate, formLayout);
		dateEl.setSecondDate(endDate);
		dateEl.setElementCssClass("o_sel_repo_lecture_date");
		dateEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.dates));
		dateEl.setMandatory(true);
		dateEl.setSameDay(true);
		dateEl.setSecondDate(true);
		dateEl.setDateChooserTimeEnabled(true);
		dateEl.setValidDateCheck("form.error.date");
		
		int plannedNumOfLectures = lectureBlock == null ? lectureModule.getDefaultPlannedLectures() : lectureBlock.getPlannedLecturesNumber();
		int maxNumOfLectures = Math.max(12, plannedNumOfLectures);
		SelectionValues plannedLecturesKeys = new SelectionValues();
		for(int i=1; i<=maxNumOfLectures; i++) {
			String num = String.valueOf(i);
			plannedLecturesKeys.add(SelectionValues.entry(num, num));
		}
		plannedLecturesEl = uifactory.addDropdownSingleselect("planned.lectures", "planned.lectures", formLayout,
				plannedLecturesKeys.keys(), plannedLecturesKeys.values(), null);
		plannedLecturesEl.setMandatory(true);
		String plannedlectures = lectureBlock == null
				? Integer.toString(lectureModule.getDefaultPlannedLectures()) : Integer.toString(lectureBlock.getPlannedLecturesNumber());
		for(String plannedLecturesKey:plannedLecturesKeys.keys()) {
			if(plannedlectures.equals(plannedLecturesKey)) {
				plannedLecturesEl.select(plannedLecturesKey, true);
				break;
			}
		}
		//freeze it after roll call done
		boolean plannedLecturesEditable = (lectureBlock == null ||
				(lectureBlock.getStatus() != LectureBlockStatus.done
					&& lectureBlock.getRollCallStatus() != LectureRollCallStatus.closed
					&& lectureBlock.getRollCallStatus() != LectureRollCallStatus.autoclosed))
			&& !lectureManagementManaged
			&& !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.plannedLectures);
		plannedLecturesEl.setEnabled(!readOnly && plannedLecturesEditable);
		
		// Location
		String location = lectureBlock == null ? "" : lectureBlock.getLocation();
		locationEl = uifactory.addTextElementWithAutoCompleter("location", "lecture.location", 128, location, formLayout);
		locationEl.setElementCssClass("o_sel_repo_lecture_location");
		locationEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.location));
		locationEl.setListProvider(new LocationListProvider(), ureq.getUserSession());
		locationEl.setMinLength(1);
		
		possibleTeachersList = loadTeachers();
		SelectionValues teachersPK = new SelectionValues();
		for(MemberView teacher:possibleTeachersList) {
			teachersPK.add(teacherPK(teacher));
		}
		teacherEl = uifactory.addCheckboxesVertical("teacher", "lecture.teacher", formLayout, teachersPK.keys(), teachersPK.values(),
				null, teachersPK.icons(), 2);
		teacherEl.setElementCssClass("o_sel_repo_lecture_teachers");
		teacherEl.setMandatory(true);
		teacherEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.teachers));
		teacherEl.setVisible(!teachersPK.isEmpty());
		
		EmptyPanelItem emptyTeachersList = uifactory.addEmptyPanel("teacher.empty", "lecture.teacher", formLayout);
		emptyTeachersList.setTitle(translate("lecture.no.teacher.title"));
		emptyTeachersList.setInformations(translate("lecture.no.teacher"));
		emptyTeachersList.setInformations(translate("lecture.no.teacher"));
		emptyTeachersList.setIconCssClass("o_icon o_icon-lg o_icon_user");
		emptyTeachersList.setVisible(teachersPK.isEmpty());
		
		boolean found = false;
		if(possibleTeachersList != null && !possibleTeachersList.isEmpty()) {
			for(MemberView teacher:possibleTeachersList) {
				if(containsIdentity(teacher, teachers)) {
					teacherEl.select(teacher.getIdentityKey().toString(), true);
					found = true;
				}
			}
		} 
		if(!found && teachersPK.size() > 0 && lectureBlock == null) {
			teacherEl.select(teachersPK.keys()[0], true);
		}
		
		List<TaxonomyLevel> levels = lectureService.getTaxonomy(lectureBlock);
		if(!levels.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for(TaxonomyLevel level:levels) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(TaxonomyUIFactory.translateDisplayName(getTranslator(), level));
			}
			uifactory.addStaticTextElement("lecture.taxonomy", sb.toString(), formLayout);
		}

		String description = lectureBlock == null ? "" : lectureBlock.getDescription();
		descriptionEl = uifactory.addTextAreaElement("lecture.descr", 4, 72, description, formLayout);
		descriptionEl.setElementCssClass("o_sel_repo_lecture_description");
		descriptionEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.description));
		
		String preparation = lectureBlock == null ? "" : lectureBlock.getPreparation();
		preparationEl = uifactory.addTextAreaElement("lecture.preparation", 4, 72, preparation, formLayout);
		preparationEl.setElementCssClass("o_sel_repo_lecture_preparation");
		preparationEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.preparation));
		
		boolean compulsory = lectureBlock == null || lectureBlock.isCompulsory();
		SelectionValues compulsoryPK = new SelectionValues();
		compulsoryPK.add(SelectionValues.entry(ON_KEY, translate("lecture.compulsory.on")));
		compulsoryEl = uifactory.addCheckboxesVertical("compulsory", "lecture.compulsory", formLayout, compulsoryPK.keys(), compulsoryPK.values(), 1);
		compulsoryEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.compulsory));
		compulsoryEl.addActionListener(FormEvent.ONCHANGE);
		if(compulsory) {
			compulsoryEl.select(ON_KEY, true);
		}

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		if(!readOnly) {
			uifactory.addFormSubmitButton("save", buttonsCont);
		}
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private boolean containsIdentity(MemberView member, Collection<Identity> identities) {
		return identities.stream()
				.anyMatch(id -> id.getKey().equals(member.getIdentityKey()));
	}
	
	private boolean containsIdentity(Identity identity, Collection<MemberView> members) {
		return members.stream()
				.anyMatch(mem -> mem.getIdentityKey().equals(identity.getKey()));
	}
	
	private SelectionValue teacherPK(MemberView teacher) {
		String key = teacher.getIdentityKey().toString();
		IdentityNames identityNames = teacher.getIdentityNames(userPropertyHandlers);
		String displayName = StringHelper.escapeHtml(userManager.getUserDisplayName(identityNames));
		
		try(StringOutput sb = new StringOutput()) {
			sb.append(displayName);
			CourseMembership membership = teacher.getMemberShip();
			if(membership.isCoach() || membership.isOwner()) {
				sb.append(" (");
				
				CourseRoleCellRenderer roleRenderer = new CourseRoleCellRenderer(getLocale());
				roleRenderer.render(sb, membership);
	
				sb.append(")");
			}
			displayName = sb.toString();
		} catch(Exception e) {
			logError("", e);
		}
		
		return SelectionValues.entry(key, displayName, null, "o_icon o_icon-fw o_icon_user", null, true);
	}
	
	private List<MemberView> loadTeachers() {
		List<MemberView> memberViews;
		if(curriculumElement != null && entry == null) {
			SearchMembersParams params = new SearchMembersParams();
			params.setRoles(new GroupRoles[] { GroupRoles.coach });
			params.setOrigins(Set.of(Origin.curriculum));
			params.setUserTypes(Set.of(UserType.user));
			memberViews = memberQueries.getCurriculumElementMembers(curriculumElement, params, userPropertyHandlers, getLocale());
		} else {
			SearchMembersParams params = new SearchMembersParams();
			params.setRoles(new GroupRoles[] { GroupRoles.coach });
			params.setOrigins(Set.of(Origin.repositoryEntry, Origin.curriculum));
			params.setUserTypes(Set.of(UserType.user));
			memberViews = memberQueries.getRepositoryEntryMembers(entry, params, userPropertyHandlers, getLocale());
		}
		
		Set<MemberView> allPossibleTeachers = new HashSet<>();
		allPossibleTeachers.addAll(memberViews);
		if(teachers != null && !teachers.isEmpty()) {// eventually external teachers
			for(Identity teacher:teachers) {
				if(!containsIdentity(teacher, allPossibleTeachers)) {
					allPossibleTeachers.add(new MemberView(teacher, userPropertyHandlers, getLocale()));
				}
			}
		}
		return new ArrayList<>(allPossibleTeachers);
	}
	
	private void updateUI() {
		if(compulsoryEl.isAtLeastSelected(1)) {
			setFormWarning(null);
		} else {
			setFormWarning("warning.edit.lecture");
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		plannedLecturesEl.clearError();
		if(!plannedLecturesEl.isOneSelected()) {
			plannedLecturesEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		teacherEl.clearError();
		if(teacherEl.isVisible() && entry != null && !teacherEl.isAtLeastSelected(1)) {
			teacherEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		dateEl.clearError();
		if(dateEl.getDate() == null) {
			dateEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(!validateFormItem(ureq, dateEl)) {
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		if(locationEl != fiSrc) {
			super.propagateDirtinessToContainer(fiSrc, event);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(locationEl == source) {
			// Do nothing
		} else if(compulsoryEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean create = false;
		int currentPlannedLectures = -1;
		
		String beforeXml;
		LectureBlockAuditLog.Action action;
		StringBuilder audit = new StringBuilder();
		List<Group> selectedGroups;
		if(lectureBlock == null) {
			beforeXml = null;
			action = LectureBlockAuditLog.Action.createLectureBlock;
			selectedGroups = new ArrayList<>();
			if(entry != null) {
				lectureBlock = lectureService.createLectureBlock(entry);
				Group defGroup = repositoryService.getDefaultGroup(entry);
				selectedGroups.add(defGroup);
			} else if(curriculumElement != null) {
				List<RepositoryEntry> curriculumElementEntries = curriculumService.getRepositoryEntries(curriculumElement);
				RepositoryEntry singleEntry = curriculumElementEntries.size() == 1 ? curriculumElementEntries.get(0) : null;
				lectureBlock = lectureService.createLectureBlock(curriculumElement, singleEntry);
				if(singleEntry != null) {
					Group defGroup = repositoryService.getDefaultGroup(singleEntry);
					selectedGroups.add(defGroup);
				}
			} else {
				showWarning("error.no.entry.curriculum");
				return;
			}
			create = true;
		} else {
			beforeXml = lectureService.toAuditXml(lectureBlock);
			action = LectureBlockAuditLog.Action.createLectureBlock;
			currentPlannedLectures = lectureBlock.getPlannedLecturesNumber();
			selectedGroups = lectureService.getLectureBlockToGroups(lectureBlock);
		}
		lectureBlock.setTitle(titleEl.getValue());
		lectureBlock.setExternalRef(externalRefEl.getValue());
		lectureBlock.setCompulsory(compulsoryEl.isAtLeastSelected(1));
		lectureBlock.setDescription(descriptionEl.getValue());
		lectureBlock.setPreparation(preparationEl.getValue());
		if(locationEl.isEnabled()) {// autocompleter don't collect value if disabled
			lectureBlock.setLocation(locationEl.getValue());
		}
		lectureBlock.setStartDate(dateEl.getDate());
		lectureBlock.setEndDate(dateEl.getSecondDate());
		
		int plannedLectures = Integer.parseInt(plannedLecturesEl.getSelectedKey());
		lectureBlock.setPlannedLecturesNumber(plannedLectures);

		lectureBlock = lectureService.save(lectureBlock, selectedGroups);
		
		if(teacherEl.isAtLeastSelected(1)) {
			synchronizeTeachers(audit);
		}

		String afterxml = lectureService.toAuditXml(lectureBlock);
		lectureService.auditLog(action, beforeXml, afterxml, audit.toString(), lectureBlock, null,
				entry, curriculumElement, null, getIdentity());
		dbInstance.commit();
		if(currentPlannedLectures >= 0) {
			lectureService.adaptRollCalls(lectureBlock);
		}
		updateLocationsPrefs(ureq);
		lectureService.syncCalendars(lectureBlock);
		//update eventual assessment mode
		AssessmentMode assessmentMode = assessmentModeMgr.getAssessmentMode(lectureBlock);
		if(assessmentMode != null) {
			assessmentModeMgr.syncAssessmentModeToLectureBlock(assessmentMode);
			assessmentModeMgr.merge(assessmentMode, false);
		}
		fireEvent(ureq, Event.DONE_EVENT);

		if(create) {
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LECTURE_BLOCK_CREATED, getClass(),
					CoreLoggingResourceable.wrap(lectureBlock, OlatResourceableType.lectureBlock, lectureBlock.getTitle()));
		} else {
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LECTURE_BLOCK_EDITED, getClass(),
					CoreLoggingResourceable.wrap(lectureBlock, OlatResourceableType.lectureBlock, lectureBlock.getTitle()));
		}
	}
	
	private void synchronizeTeachers(StringBuilder audit) {
		List<Identity> currentTeachers = lectureService.getTeachers(lectureBlock);
		List<String> selectedTeacherKeys = new ArrayList<>(teacherEl.getSelectedKeys());
		
		// Remove deselected
		for(Identity teacher:currentTeachers) {
			boolean found = selectedTeacherKeys.contains(teacher.getKey().toString());
			if(!found) {
				lectureService.removeTeacher(lectureBlock, teacher);
				audit.append("remove teacher: ").append(userManager.getUserDisplayName(teacher)).append(" (").append(teacher.getKey()).append(");");
			}
		}
		
		// Add new one
		for(String selectedTeacherKey:selectedTeacherKeys) {
			for(MemberView teacherView:possibleTeachersList) {
				if(selectedTeacherKey.equals(teacherView.getIdentityKey().toString())) {
					Identity teacher = securityManager.loadIdentityByKey(teacherView.getIdentityKey());
					lectureService.addTeacher(lectureBlock, teacher);
					audit.append("add teacher: ").append(userManager.getUserDisplayName(teacher)).append(" (").append(teacher.getKey()).append(");");
				}
			}
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private List<LocationHistory> getLocations(UserRequest ureq) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		List<LocationHistory> showConfig  = guiPrefs.getList(LectureBlock.class, getLocationsPrefsId(), LocationHistory.class);
		return showConfig == null ? new ArrayList<>() : showConfig;
	}
	
	private void updateLocationsPrefs(UserRequest ureq) {
		String location = lectureBlock.getLocation();
		if(StringHelper.containsNonWhitespace(location)) {
			List<LocationHistory> newLocations = new ArrayList<>(locations);
			LocationHistory newLocation = new LocationHistory(location, new Date());
			if(locations.contains(newLocation)) {
				int index = locations.indexOf(newLocation);
				locations.get(index).setLastUsed(new Date());
			} else {
				newLocations.add(newLocation);
				Collections.sort(newLocations, new LocationDateComparator());
				if(newLocations.size() > 10) {
					newLocations = new ArrayList<>(newLocations.subList(0, 10));//pack it in a new list for XStream
				}
			}
			
			Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
			if (guiPrefs != null) {
				guiPrefs.putAndSave(LectureBlock.class, getLocationsPrefsId(), newLocations);
			}
		}
	}
	
	private String getLocationsPrefsId() {
		return "Lectures::Location::" + getIdentity().getKey();
	}
	
	public static class GroupBox {
		
		private BusinessGroup businessGroup;
		private RepositoryEntry repoEntry;
		private CurriculumElement curriculumElement;
		private final Group baseGroup;
		
		public GroupBox(RepositoryEntry entry, Group baseGroup) {
			this.repoEntry = entry;
			this.baseGroup = baseGroup;
		}
		
		public GroupBox(BusinessGroup businessGroup) {
			this.businessGroup = businessGroup;
			baseGroup = businessGroup.getBaseGroup();
		}
		
		public GroupBox(CurriculumElement curriculumElement) {
			this.curriculumElement = curriculumElement;
			baseGroup = curriculumElement.getGroup();
		}
		
		public String getName() {
			if(repoEntry != null) {
				return repoEntry.getDisplayname();
			}
			if(businessGroup != null) {
				return businessGroup.getName();
			}
			if(curriculumElement != null) {
				return curriculumElement.getDisplayName();
			}
			return null;
		}
		
		public Group getBaseGroup() {
			return baseGroup;
		}
		
		public RepositoryEntry getEntry() {
			return repoEntry;
		}
		
		public BusinessGroup getBusinessGroup() {
			return businessGroup;
		}
	}
	
	public class LocationListProvider implements ListProvider {
		
		@Override
		public int getMaxEntries() {
			return locations.size();
		}

		@Override
		public void getResult(String searchValue, ListReceiver receiver) {
			if(locations != null && !locations.isEmpty()) {
				if(locations.size() > 2) {
					Collections.sort(locations, new LocationDateComparator());
				}
				
				for(LocationHistory location:locations) {
					String val = StringHelper.xssScan(location.getLocation());
					if(StringHelper.containsNonWhitespace(val)) {
						receiver.addEntry(val, val);
					}
				}
			}
		}
	}
}