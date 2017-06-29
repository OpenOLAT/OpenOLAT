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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockManagedFlag;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LocationHistory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
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
	private static final String[] plannedLecturesKeys = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
	
	private TextElement titleEl;
	private TextElement descriptionEl;
	private TextElement preparationEl;
	private AutoCompleter locationEl;
	private DateChooser startDateEl;
	private SingleSelection plannedLecturesEl;
	private TextElement endHourEl, endMinuteEl;
	private MultipleSelectionElement groupsEl, teacherEl, compulsoryEl;
	
	private RepositoryEntry entry;
	private LectureBlock lectureBlock;
	
	private List<Identity> teachers;
	private List<GroupBox> groupBox;
	private String[] teacherKeys, teacherValues;
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
	private BusinessGroupService businessGroupService;

	public EditLectureBlockController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		this(ureq, wControl, entry, null);
	}
	
	public EditLectureBlockController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, LectureBlock lectureBlock) {
		super(ureq, wControl);
		this.entry = entry;
		this.lectureBlock = lectureBlock;
		locations = getLocations(ureq);
		lectureManagementManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lecturemanagement);
		if(lectureBlock != null && lectureBlock.getKey() != null) {
			teachers = lectureService.getTeachers(lectureBlock);
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(lectureBlock != null && StringHelper.containsNonWhitespace(lectureBlock.getManagedFlagsString())) {
			setFormWarning("form.managedflags.intro.short", null);
		}

		String title = lectureBlock == null ? null : lectureBlock.getTitle();
		titleEl = uifactory.addTextElement("title", "lecture.title", 128, title, formLayout);
		titleEl.setEnabled(!lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.title));
		titleEl.setMandatory(true);

		plannedLecturesEl = uifactory.addDropdownSingleselect("planned.lectures", "planned.lectures", formLayout,
				plannedLecturesKeys, plannedLecturesKeys, null);
		plannedLecturesEl.setEnabled(!lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.plannedLectures));
		plannedLecturesEl.setMandatory(true);
		String plannedlectures = lectureBlock == null ? "4" : Integer.toString(lectureBlock.getPlannedLecturesNumber());
		for(String plannedLecturesKey:plannedLecturesKeys) {
			if(plannedlectures.equals(plannedLecturesKey)) {
				plannedLecturesEl.select(plannedLecturesKey, true);
				break;
			}
		}
		
		String[] onValues = new String[]{ "" };
		boolean compulsory = lectureBlock == null ? true : lectureBlock.isCompulsory();
		compulsoryEl = uifactory.addCheckboxesVertical("compulsory", "lecture.compulsory", formLayout, onKeys, onValues, 1);
		compulsoryEl.setEnabled(!lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.compulsory));
		if(compulsory) {
			compulsoryEl.select(onKeys[0], true);
		}

		List<Identity> coaches = repositoryService.getMembers(entry, GroupRoles.coach.name());
		teacherKeys = new String[coaches.size() + 1];
		teacherValues = new String[coaches.size() + 1];
		for(int i=coaches.size() + 1; i-->1; ) {
			Identity coach = coaches.get(i - 1);
			teacherKeys[i] = coach.getKey().toString();
			teacherValues[i] = userManager.getUserDisplayName(coach);
		}
		teacherKeys[0] = "-";
		teacherValues[0] = "-";
		
		teacherEl = uifactory.addCheckboxesVertical("teacher", "lecture.teacher", formLayout, teacherKeys, teacherValues, 2);
		teacherEl.setMandatory(true);
		teacherEl.setEnabled(!lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.teachers));
		
		boolean found = false;
		if(teachers != null && teachers.size() > 0) {
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
		String[] groupKeys = new String[groupBox.size()];
		String[] groupValues = new String[groupBox.size()];
		for(int i=groupBox.size(); i-->0; ) {
			groupKeys[i] = Integer.toString(i);
			groupValues[i] = groupBox.get(i).getName();
		}
		groupsEl = uifactory.addCheckboxesVertical("lecture.groups", "lecture.groups", formLayout, groupKeys, groupValues, 2);
		groupsEl.setEnabled(!lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.groups));
		groupsEl.setMandatory(true);
		if(lectureBlock != null) {
			List<Group> selectedGroups = lectureService.getLectureBlockToGroups(lectureBlock);
			for(int i=0; i<groupBox.size(); i++) {
				if(selectedGroups.contains(groupBox.get(i).getBaseGroup())) {
					groupsEl.select(Integer.toString(i), true);
				}
			}
		}

		String description = lectureBlock == null ? "" : lectureBlock.getDescription();
		descriptionEl = uifactory.addTextAreaElement("lecture.descr", 4, 72, description, formLayout);
		descriptionEl.setEnabled(!lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.description));
		String preparation = lectureBlock == null ? "" : lectureBlock.getPreparation();
		preparationEl = uifactory.addTextAreaElement("lecture.preparation", 4, 72, preparation, formLayout);
		preparationEl.setEnabled(!lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.preparation));
		String location = lectureBlock == null ? "" : lectureBlock.getLocation();
		locationEl = uifactory.addTextElementWithAutoCompleter("location", "lecture.location", 128, location, formLayout);
		locationEl.setEnabled(!lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.location));
		locationEl.setListProvider(new LocationListProvider(), ureq.getUserSession());
		locationEl.setMinLength(1);

		Date startDate = lectureBlock == null ? null : lectureBlock.getStartDate();
		startDateEl = uifactory.addDateChooser("lecture.start", startDate, formLayout);
		startDateEl.setEnabled(!lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.dates));
		startDateEl.setDomReplacementWrapperRequired(false);
		startDateEl.setDateChooserTimeEnabled(true);
		startDateEl.setMandatory(true);
		
		String datePage = velocity_root + "/date_start_end.html";
		FormLayoutContainer dateCont = FormLayoutContainer.createCustomFormLayout("start_end", getTranslator(), datePage);
		dateCont.setLabel("lecture.end", null);
		formLayout.add(dateCont);
		
		endHourEl = uifactory.addTextElement("lecture.end.hour", null, 2, "", dateCont);
		endHourEl.setEnabled(!lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.dates));
		endHourEl.setDomReplacementWrapperRequired(false);
		endHourEl.setDisplaySize(2);
		endHourEl.setMandatory(true);
		endMinuteEl = uifactory.addTextElement("lecture.end.minute", null, 2, "", dateCont);
		endMinuteEl.setEnabled(!lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.dates));
		endMinuteEl.setDomReplacementWrapperRequired(false);
		endMinuteEl.setDisplaySize(2);
		
		if(lectureBlock != null && lectureBlock.getEndDate() != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(lectureBlock.getEndDate());
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int minute = cal.get(Calendar.MINUTE);
			endHourEl.setValue(Integer.toString(hour));
			endMinuteEl.setValue(Integer.toString(minute));
		} else {
			endHourEl.setValue("00");
			endMinuteEl.setValue("00");
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
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
		
		startDateEl.clearError();
		if(startDateEl.getDate() == null) {
			startDateEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		allOk &= validateInt(endHourEl, 24);
		allOk &= validateInt(endMinuteEl, 60);
		return allOk & super.validateFormLogic(ureq);
	}
	
	private boolean validateInt(TextElement element, int max) {
		boolean allOk = true;
		
		element.clearError();
		if(StringHelper.containsNonWhitespace(element.getValue())) {
			try {
				int val = Integer.parseInt(element.getValue());
				if(val < 0 || val > max) {
					element.setErrorKey("form.legende.mandatory", new String[] { "0", Integer.toString(max)} );
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				element.setErrorKey("form.legende.mandatory", new String[] { "0", Integer.toString(max)} );
				allOk &= false;
			}
		} else {
			element.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean create = false;
		StringBuilder audit = new StringBuilder();
		if(lectureBlock == null) {
			audit.append("Create;");
			lectureBlock = lectureService.createLectureBlock(entry);
			create = true;
		} else {
			audit.append("Update;");
		}
		lectureBlock.setTitle(titleEl.getValue());
		lectureBlock.setCompulsory(compulsoryEl.isAtLeastSelected(1));
		lectureBlock.setDescription(descriptionEl.getValue());
		lectureBlock.setPreparation(preparationEl.getValue());
		lectureBlock.setLocation(locationEl.getValue());
		lectureBlock.setStartDate(startDateEl.getDate());
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDateEl.getDate());
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endHourEl.getValue()));
		cal.set(Calendar.MINUTE, Integer.parseInt(endMinuteEl.getValue()));
		lectureBlock.setEndDate(cal.getTime());
		
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
						Identity teacher = securityManager.loadIdentityByKey(new Long(selectedTeacherKey));
						lectureService.addTeacher(lectureBlock, teacher);
						audit.append("add teacher: ").append(userManager.getUserDisplayName(teacher)).append(" (").append(teacher.getKey()).append(");");
					}
				}
			}
		}
		
		dbInstance.commit();
		updateLocationsPrefs(ureq);
		lectureService.syncCalendars(lectureBlock);
		fireEvent(ureq, Event.DONE_EVENT);

		lectureService.appendToLectureBlockLog(lectureBlock, getIdentity(), null, audit.toString());
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
		private final Group baseGroup;
		
		public GroupBox(RepositoryEntry entry, Group baseGroup) {
			this.repoEntry = entry;
			this.baseGroup = baseGroup;
		}
		
		public GroupBox(BusinessGroup businessGroup) {
			this.businessGroup = businessGroup;
			baseGroup = businessGroup.getBaseGroup();
		}
		
		public String getName() {
			if(repoEntry != null) {
				return repoEntry.getDisplayname();
			}
			if(businessGroup != null) {
				return businessGroup.getName();
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
		public void getResult(String searchValue, ListReceiver receiver) {
			if(locations != null && locations.size() > 0) {
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