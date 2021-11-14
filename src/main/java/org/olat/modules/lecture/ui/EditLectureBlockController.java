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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
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
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.prefs.Preferences;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockManagedFlag;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LocationHistory;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditLectureBlockController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private TextElement titleEl;
	private TextElement descriptionEl;
	private TextElement preparationEl;
	private AutoCompleter locationEl;
	private DateChooser dateEl;
	private SingleSelection plannedLecturesEl;
	private MultipleSelectionElement groupsEl;
	private MultipleSelectionElement teacherEl;
	private MultipleSelectionElement compulsoryEl;
	
	private final boolean readOnly;
	private RepositoryEntry entry;
	private LectureBlock lectureBlock;
	
	private List<Identity> teachers;
	private List<GroupBox> groupBox;
	private String[] teacherKeys;
	private final boolean lectureManagementManaged;
	private final List<LocationHistory> locations;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private BusinessGroupService businessGroupService;

	public EditLectureBlockController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		this(ureq, wControl, entry, null, false);
	}
	
	public EditLectureBlockController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, LectureBlock lectureBlock, boolean readOnly) {
		super(ureq, wControl);
		this.entry = entry;
		this.readOnly = readOnly;
		this.lectureBlock = lectureBlock;
		locations = getLocations(ureq);
		lectureManagementManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lecturemanagement);
		if(lectureBlock != null && lectureBlock.getKey() != null) {
			teachers = lectureService.getTeachers(lectureBlock);
		}
		
		initForm(ureq);
		updateUI();
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
		
		int plannedNumOfLectures = lectureBlock == null ? 4 : lectureBlock.getPlannedLecturesNumber();
		int maxNumOfLectures = Math.max(12, plannedNumOfLectures);
		SelectionValues plannedLecturesKeys = new SelectionValues();
		for(int i=1; i<=maxNumOfLectures; i++) {
			String num = String.valueOf(i);
			plannedLecturesKeys.add(SelectionValues.entry(num, num));
		}
		plannedLecturesEl = uifactory.addDropdownSingleselect("planned.lectures", "planned.lectures", formLayout,
				plannedLecturesKeys.keys(), plannedLecturesKeys.values(), null);
		plannedLecturesEl.setMandatory(true);
		String plannedlectures = lectureBlock == null ? "4" : Integer.toString(lectureBlock.getPlannedLecturesNumber());
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
		
		String[] onValues = new String[]{ "" };
		boolean compulsory = lectureBlock == null || lectureBlock.isCompulsory();
		compulsoryEl = uifactory.addCheckboxesVertical("compulsory", "lecture.compulsory", formLayout, onKeys, onValues, 1);
		compulsoryEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.compulsory));
		compulsoryEl.addActionListener(FormEvent.ONCHANGE);
		if(compulsory) {
			compulsoryEl.select(onKeys[0], true);
		}

		List<Identity> coaches = repositoryService.getMembers(entry, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.coach.name());
		List<Identity> allPossibleTeachers = new ArrayList<>();
		allPossibleTeachers.addAll(coaches);
		if(teachers != null) {// eventually external teachers
			allPossibleTeachers.addAll(teachers);
		}
		allPossibleTeachers = new ArrayList<>(new HashSet<>(allPossibleTeachers));
		teacherKeys = new String[allPossibleTeachers.size() + 1];
		String[] teacherValues = new String[allPossibleTeachers.size() + 1];
		for(int i=allPossibleTeachers.size() + 1; i-->1; ) {
			Identity coach = allPossibleTeachers.get(i - 1);
			teacherKeys[i] = coach.getKey().toString();
			teacherValues[i] = userManager.getUserDisplayName(coach);
		}
		teacherKeys[0] = "-";
		teacherValues[0] = translate("no.teachers");
		teacherEl = uifactory.addCheckboxesVertical("teacher", "lecture.teacher", formLayout, teacherKeys, teacherValues, 2);
		teacherEl.setElementCssClass("o_sel_repo_lecture_teachers");
		teacherEl.setMandatory(true);
		teacherEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.teachers));
		
		boolean found = false;
		if(teachers != null && !teachers.isEmpty()) {
			for(Identity teacher:teachers) {
				String currentTeacherKey = teacher.getKey().toString();
				for(String teacherKey:teacherKeys) {
					if(currentTeacherKey.equals(teacherKey)) {
						teacherEl.select(currentTeacherKey, true);
						found = true;
					}
				}
			}
		} 
		if(!found) {
			teacherEl.select(teacherKeys[0], true);
		}
		
		Group entryBaseGroup = repositoryService.getDefaultGroup(entry);
		groupBox = new ArrayList<>();
		groupBox.add(new GroupBox(entry, entryBaseGroup));
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		List<BusinessGroup> businessGroups = businessGroupService.findBusinessGroups(params, entry, 0, -1, BusinessGroupOrder.nameAsc);
		for(BusinessGroup businessGroup:businessGroups) {
			groupBox.add(new GroupBox(businessGroup));
		}
		List<CurriculumElement> elements = curriculumService.getCurriculumElements(entry);
		for(CurriculumElement element:elements) {
			groupBox.add(new GroupBox(element));
		}
		String[] groupKeys = new String[groupBox.size()];
		String[] groupValues = new String[groupBox.size()];
		for(int i=groupBox.size(); i-->0; ) {
			groupKeys[i] = Integer.toString(i);
			groupValues[i] = groupBox.get(i).getName();
		}
		groupsEl = uifactory.addCheckboxesVertical("lecture.groups", "lecture.groups", formLayout, groupKeys, groupValues, 2);
		groupsEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.groups));
		groupsEl.setMandatory(true);
		if(lectureBlock != null) {
			List<Group> selectedGroups = lectureService.getLectureBlockToGroups(lectureBlock);
			for(int i=0; i<groupBox.size(); i++) {
				if(selectedGroups.contains(groupBox.get(i).getBaseGroup())) {
					groupsEl.select(Integer.toString(i), true);
				}
			}
		} else if(groupKeys.length == 1) {
			groupsEl.select(groupKeys[0], true);
		}
		
		List<TaxonomyLevel> levels = lectureService.getTaxonomy(lectureBlock);
		if(!levels.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for(TaxonomyLevel level:levels) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(level.getDisplayName());
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
		String location = lectureBlock == null ? "" : lectureBlock.getLocation();
		locationEl = uifactory.addTextElementWithAutoCompleter("location", "lecture.location", 128, location, formLayout);
		locationEl.setElementCssClass("o_sel_repo_lecture_location");
		locationEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.location));
		locationEl.setListProvider(new LocationListProvider(), ureq.getUserSession());
		locationEl.setMinLength(1);

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
		dateEl.setSeparator("lecture.time.until");

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		if(!readOnly) {
			uifactory.addFormSubmitButton("save", buttonsCont);
		}
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
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		plannedLecturesEl.clearError();
		if(!plannedLecturesEl.isOneSelected()) {
			plannedLecturesEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		teacherEl.clearError();
		if(!teacherEl.isAtLeastSelected(1)) {
			teacherEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		groupsEl.clearError();
		if(!groupsEl.isAtLeastSelected(1)) {
			groupsEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		dateEl.clearError();
		if(dateEl.getDate() == null) {
			dateEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(compulsoryEl == source) {
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
		if(lectureBlock == null) {
			beforeXml = null;
			action = LectureBlockAuditLog.Action.createLectureBlock;
			lectureBlock = lectureService.createLectureBlock(entry);
			create = true;
		} else {
			beforeXml = lectureService.toAuditXml(lectureBlock);
			action = LectureBlockAuditLog.Action.createLectureBlock;
			currentPlannedLectures = lectureBlock.getPlannedLecturesNumber();
		}
		lectureBlock.setTitle(titleEl.getValue());
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

		List<Group> selectedGroups = new ArrayList<>();
		if(groupsEl.isAtLeastSelected(1)) {
			for(String selectedGroupPos:groupsEl.getSelectedKeys()) {
				Group bGroup = groupBox.get(Integer.parseInt(selectedGroupPos)).getBaseGroup();
				selectedGroups.add(bGroup);
			}	
		}

		lectureBlock = lectureService.save(lectureBlock, selectedGroups);
		
		if(teacherEl.isAtLeastSelected(1)) {
			List<Identity> currentTeachers = lectureService.getTeachers(lectureBlock);
			List<String> selectedTeacherKeys = new ArrayList<>(teacherEl.getSelectedKeys());
			if(selectedTeacherKeys.size() == 1 && teacherKeys[0].equals(selectedTeacherKeys.get(0))) {
				//remove all
				for(Identity teacher:currentTeachers) {
					lectureService.removeTeacher(lectureBlock, teacher);
					audit.append("remove teacher: ").append(userManager.getUserDisplayName(teacher)).append(" (").append(teacher.getKey()).append(");");
				}
			} else {
				//remove deselected
				for(Identity teacher:currentTeachers) {
					boolean found = selectedTeacherKeys.contains(teacher.getKey().toString());
					if(!found) {
						lectureService.removeTeacher(lectureBlock, teacher);
						audit.append("remove teacher: ").append(userManager.getUserDisplayName(teacher)).append(" (").append(teacher.getKey()).append(");");
					}
				}
				//add new one
				for(String selectedTeacherKey:selectedTeacherKeys) {
					if(selectedTeacherKey.equals(teacherKeys[0])) continue;
					
					boolean found = false;
					for(Identity teacher:currentTeachers) {
						if(selectedTeacherKey.equals(teacher.getKey().toString())) {
							found = true;
						}
					}
					
					if(!found) {
						Identity teacher = securityManager.loadIdentityByKey(Long.valueOf(selectedTeacherKey));
						lectureService.addTeacher(lectureBlock, teacher);
						audit.append("add teacher: ").append(userManager.getUserDisplayName(teacher)).append(" (").append(teacher.getKey()).append(");");
					}
				}
			}
		}

		String afterxml = lectureService.toAuditXml(lectureBlock);
		lectureService.auditLog(action, beforeXml, afterxml, audit.toString(), lectureBlock, null, entry, null, getIdentity());
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
	
	public class GroupBox {
		
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
					receiver.addEntry(location.getLocation(), location.getLocation());
				}
			}
		}
	}
	
	private static class LocationDateComparator implements Comparator<LocationHistory> {

		@Override
		public int compare(LocationHistory o1, LocationHistory o2) {
			if(o1 == null) {
				if(o2 == null) {
					return 0;
				} else {
					return -1;
				}
			} else if(o2 == null) {
				return 1;
			}
			
			Date d1 = o1.getLastUsed();
			Date d2 = o2.getLastUsed();
			if(d1 == null) {
				if(d2 == null) {
					return 0;
				} else {
					return -1;
				}
			} else if(d2 == null) {
				return 1;
			}
			
			return -d1.compareTo(d2);
		}
	}
}