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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.EmptyPanelItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
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
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.MemberViewQueries;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.MemberView;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.group.model.comparator.MemberViewNamesComparator;
import org.olat.group.ui.main.CourseMembership;
import org.olat.group.ui.main.CourseRoleCellRenderer;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.group.ui.main.SearchMembersParams.Origin;
import org.olat.group.ui.main.SearchMembersParams.UserType;
import org.olat.ims.lti13.LTI13Service;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonTemplatePermissions;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper;
import org.olat.modules.bigbluebutton.ui.EditBigBlueButtonMeetingController;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementToTaxonomyLevel;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockManagedFlag;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureBlockToTaxonomyLevel;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LocationHistory;
import org.olat.modules.lecture.ui.addwizard.AddLectureContext;
import org.olat.modules.lecture.ui.component.LocationDateComparator;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelectionSource;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.ui.EditTeamsMeetingController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryToTaxonomyLevel;
import org.olat.repository.RepositoryModule;
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

	private static final String TEAMS_MEETING = "teams";
	private static final String OTHER_MEETING = "other";
	private static final String BIGBLUEBUTTON_MEETING = "bigbluebutton";
	
	private TextElement titleEl;
	private TextElement externalRefEl;
	private TextElement descriptionEl;
	private TextElement preparationEl;
	private TextElement locationEl;
	private DateChooser dateEl;
	private FormToggle compulsoryEl;
	private FormLink editOnlineMeetingButton;
	private SingleSelection onlineMeetingEl;
	private FormToggle enabledOnlineMeetingEl;
	private ObjectSelectionElement taxonomyLevelEl;
	private SingleSelection plannedLecturesEl;
	private MultipleSelectionElement teacherEl;
	private TextElement onlineMeetingProviderUrlEl;
	private TextElement onlineMeetingProviderNameEl;
	
	private final boolean readOnly;
	private final boolean embedded;
	private RepositoryEntry entry;
	private LectureBlock lectureBlock;
	private TeamsMeeting teamsMeeting;
	private StepsListener stepsListener;
	private AddLectureContext addLectureCtxt;
	private CurriculumElement curriculumElement;
	private BigBlueButtonMeeting bigBlueButtonMeeting;
	private FormLink adoptButton;
	
	private List<MemberView> possibleTeachersList;

	private final List<Identity> teachers;
	private final boolean lectureManagementManaged;
	private final List<LocationHistory> locations;

	private CloseableModalController cmc;
	private EditTeamsMeetingController editTeamsMeetingCtrl;
	private EditBigBlueButtonMeetingController editBigBlueButtonMeetingCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private TeamsModule teamsModule;
	@Autowired
	private TeamsService teamsService;
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
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private RepositoryModule repositoryModule;

	public EditLectureBlockController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			LectureBlock lectureBlock, boolean readOnly) {
		this(ureq, wControl, entry, null, lectureBlock, readOnly);
	}
	
	public EditLectureBlockController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement,
			LectureBlock lectureBlock, boolean readOnly) {
		this(ureq, wControl, null, curriculumElement, lectureBlock, readOnly);
	}
	
	private EditLectureBlockController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, CurriculumElement curriculumElement,
			LectureBlock lectureBlock, boolean readOnly) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		embedded = false;
		this.entry = entry;
		this.readOnly = readOnly;
		this.lectureBlock = lectureBlock;
		this.curriculumElement = curriculumElement;
		
		locations = getLocations(ureq);
		lectureManagementManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lecturemanagement);
		if(lectureBlock != null && lectureBlock.getKey() != null) {
			teachers = lectureService.getTeachers(lectureBlock);
			if(lectureBlock.getBBBMeeting() != null) {
				bigBlueButtonMeeting = bigBlueButtonManager.getMeeting(lectureBlock.getBBBMeeting());
			}
			if(lectureBlock.getTeamsMeeting() != null) {
				teamsMeeting = teamsService.getMeeting(lectureBlock.getTeamsMeeting());
			}
		} else {
			teachers = List.of();
		}
		
		initForm(ureq);
		updateUI();
	}
	
	public EditLectureBlockController(UserRequest ureq, WindowControl wControl, Form rootForm,
			AddLectureContext addLecture, StepsListener stepsListener) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, rootForm);
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		embedded = true;
		entry = addLecture.getEntry();
		readOnly = false;
		lectureBlock = null;
		this.addLectureCtxt = addLecture;
		this.stepsListener = stepsListener;
		curriculumElement = addLecture.getCurriculumElement() != null ? addLecture.getCurriculumElement() : addLecture.getRootElement();
		
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

		String externalRef = null;
		if(lectureBlock == null || lectureBlock.getKey() == null) {
			externalRef = curriculumElement == null ? null : curriculumElement.getIdentifier();
		} else {
			externalRef = lectureBlock.getExternalRef();
		}
		externalRefEl = uifactory.addTextElement("externalref", "lecture.external.ref", 128, externalRef, formLayout);
		externalRefEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.externalRef));
		
		String page = velocity_root + "/dates_planned.html";
		FormLayoutContainer datesAndPlannedCont = uifactory.addCustomFormLayout("datesAndPlanned", null, page, formLayout);
		datesAndPlannedCont.setElementCssClass("o_lecture_date_planned");
		datesAndPlannedCont.setFormLayout("0_12");

		Date startDate = lectureBlock == null ? null : lectureBlock.getStartDate();
		Date endDate = lectureBlock == null ? null : lectureBlock.getEndDate();
		dateEl = uifactory.addDateChooser("lecture.date", startDate, datesAndPlannedCont);
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
		plannedLecturesEl = uifactory.addDropdownSingleselect("planned.lectures", "planned.lectures", datesAndPlannedCont,
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
		locationEl = uifactory.addTextElement("location", "lecture.location", 128, location, formLayout);
		locationEl.setElementCssClass("o_sel_repo_lecture_location");
		locationEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.location));
	
		// Online meeting
		String onlineMeetingUrl = lectureBlock == null ? null : lectureBlock.getMeetingUrl();
		SelectionValues meetingPK = new SelectionValues();
		if(bigBlueButtonModule.isEnabled() && bigBlueButtonModule.isLecturesEnabled()) {
			meetingPK.add(SelectionValues.entry(BIGBLUEBUTTON_MEETING, translate("lecture.online.meeting.bigbluebutton"), null, "o_icon o_bigbluebuttonmeeting_icon", null, true));
		}
		if(teamsModule.isEnabled() && teamsModule.isLecturesEnabled()) {
			meetingPK.add(SelectionValues.entry(TEAMS_MEETING, translate("lecture.online.meeting.teams"), null, "o_icon o_teamsmeeting_icon", null, true));
		}
		meetingPK.add(SelectionValues.entry(OTHER_MEETING, translate("lecture.online.meeting.other"), null, "o_icon o_icon_external_link", null, true));
		enabledOnlineMeetingEl = uifactory.addToggleButton("lecture.online.meeting", "lecture.online.meeting",
				translate("on"), translate("off"), formLayout);
		enabledOnlineMeetingEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.onlineMeeting));
		enabledOnlineMeetingEl.toggle(bigBlueButtonMeeting != null || teamsMeeting != null || StringHelper.containsNonWhitespace(onlineMeetingUrl));
		enabledOnlineMeetingEl.setVisible(!meetingPK.isEmpty());
		enabledOnlineMeetingEl.addActionListener(FormEvent.ONCHANGE);
		
		onlineMeetingEl = uifactory.addCardSingleSelectHorizontal("onlinemeeting.provider", null, formLayout, meetingPK);
		onlineMeetingEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.onlineMeeting));
		onlineMeetingEl.setVisible(enabledOnlineMeetingEl.isVisible() && enabledOnlineMeetingEl.isOn());
		onlineMeetingEl.addActionListener(FormEvent.ONCHANGE);
		if(meetingPK.containsKey(BIGBLUEBUTTON_MEETING) && bigBlueButtonMeeting != null) {
			onlineMeetingEl.select(BIGBLUEBUTTON_MEETING, true);
		} else if(meetingPK.containsKey(TEAMS_MEETING) && teamsMeeting != null) {
			onlineMeetingEl.select(TEAMS_MEETING, true);
		} else if(meetingPK.containsKey(OTHER_MEETING) && lectureBlock != null
				&& StringHelper.containsNonWhitespace(lectureBlock.getMeetingUrl())) {
			onlineMeetingEl.select(OTHER_MEETING, true);
		}
		
		editOnlineMeetingButton = uifactory.addFormLink("edit.online.meeting", formLayout, Link.BUTTON_SMALL);
		editOnlineMeetingButton.setVisible(false);
		
		String onlineMeetingTitle = lectureBlock == null ? "Zoom" : lectureBlock.getMeetingTitle();
		onlineMeetingProviderNameEl = uifactory.addTextElement("lecture.online.meeting.provider.name", 32, onlineMeetingTitle, formLayout);
		onlineMeetingProviderNameEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.onlineMeeting));
		onlineMeetingProviderNameEl.setMandatory(true);
		onlineMeetingProviderUrlEl = uifactory.addTextElement("lecture.online.meeting.provider.url", 256, onlineMeetingUrl, formLayout);
		onlineMeetingProviderUrlEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.onlineMeeting));
		onlineMeetingProviderUrlEl.setMandatory(true);
		
		updateOnlineMeetingUI();
		
		// Subjects
		Collection<TaxonomyRef> taxonomyRefs = getTaxonomyRefs();
		if (taxonomyModule.isEnabled() && !taxonomyRefs.isEmpty()) {
			boolean taxonomyEnabled = !readOnly && !lectureManagementManaged &&
					!LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.subjects);
			String subjectsLayoutPage = velocity_root + "/subjects.html";
			String taxonomyLevelLabelI18n = "lecture.subjects";
			FormLayoutContainer subjectsLayout = uifactory.addCustomFormLayout("lecture.subjects.layout", 
					taxonomyLevelLabelI18n, subjectsLayoutPage, formLayout);
			
			Set<TaxonomyLevel> taxonomyLevels = lectureBlock == null ? Set.of() : lectureBlock.getTaxonomyLevels()
					.stream().map(LectureBlockToTaxonomyLevel::getTaxonomyLevel).collect(Collectors.toSet());
			TaxonomyLevelSelectionSource source = new TaxonomyLevelSelectionSource(getLocale(),
					taxonomyLevels,
					() -> taxonomyService.getTaxonomyLevels(taxonomyRefs),
					translate(taxonomyLevelLabelI18n), translate(taxonomyLevelLabelI18n));
			source.setAriaTitleLabel(translate(taxonomyLevelLabelI18n));
			taxonomyLevelEl = uifactory.addObjectSelectionElement("taxonomy.levels", null, subjectsLayout, getWindowControl(), true, source);
			taxonomyLevelEl.setEnabled(taxonomyEnabled);
			
			adoptButton = uifactory.addFormLink("adopt", subjectsLayout, Link.BUTTON);
			adoptButton.setEnabled(taxonomyEnabled);
		}
		
		// Teachers
		possibleTeachersList = loadTeachers();
		SelectionValues teachersPK = new SelectionValues();
		for(MemberView teacher:possibleTeachersList) {
			teachersPK.add(teacherPK(teacher));
		}
		teacherEl = uifactory.addCheckboxesVertical("teacher", "lecture.teacher", formLayout, teachersPK.keys(), teachersPK.values(),
				null, teachersPK.icons(), 1);
		teacherEl.setElementCssClass("o_sel_repo_lecture_teachers");
		teacherEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.teachers));
		teacherEl.setVisible(!teachersPK.isEmpty());
		
		EmptyPanelItem emptyTeachersList = uifactory.addEmptyPanel("teacher.empty", "lecture.teacher", formLayout);
		emptyTeachersList.setTitle(translate("lecture.no.teacher.title"));
		emptyTeachersList.setInformations(translate("lecture.no.teacher"));
		emptyTeachersList.setIconCssClass("o_icon o_icon-lg o_icon_user");
		emptyTeachersList.setVisible(teachersPK.isEmpty());
		
		boolean found = false;
		if(possibleTeachersList != null && !possibleTeachersList.isEmpty()) {
			for(MemberView teacher:possibleTeachersList) {
				if(containsIdentity(teacher, teachers)) {
					teacherEl.select(teacher.getKey().toString(), true);
					found = true;
				}
			}
		} 
		if(!found && !teachersPK.isEmpty() && (lectureBlock == null || lectureBlock.getKey() == null)) {
			teacherEl.selectAll();
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
		compulsoryEl = uifactory.addToggleButton("compulsory", "lecture.compulsory", translate("on"), translate("off"), formLayout);
		compulsoryEl.setEnabled(!readOnly && !lectureManagementManaged && !LectureBlockManagedFlag.isManaged(lectureBlock, LectureBlockManagedFlag.compulsory));
		compulsoryEl.addActionListener(FormEvent.ONCHANGE);
		compulsoryEl.toggle(compulsory);
		
		if(!embedded) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonsCont);
			if(!readOnly) {
				uifactory.addFormSubmitButton("save", buttonsCont);
			}
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		}
	}
	
	private Collection<TaxonomyRef> getTaxonomyRefs() {
		Set<TaxonomyRef> taxonomyRefs = new HashSet<>();
		if (curriculumElement != null) {
			taxonomyRefs.addAll(curriculumModule.getTaxonomyRefs());
		} else {
			if (lectureBlock != null && lectureBlock.getCurriculumElement() != null) {
				taxonomyRefs.addAll(curriculumModule.getTaxonomyRefs());
			}
		}
		if (entry != null) {
			taxonomyRefs.addAll(repositoryModule.getTaxonomyRefs());
		} else {
			if (lectureBlock != null && lectureBlock.getEntry() != null) {
				taxonomyRefs.addAll(repositoryModule.getTaxonomyRefs());
			}
		}
		return taxonomyRefs;
	}

	private boolean containsIdentity(MemberView member, Collection<Identity> identities) {
		return identities.stream()
				.anyMatch(id -> id.getKey().equals(member.getKey()));
	}
	
	private boolean containsIdentity(Identity identity, Collection<MemberView> members) {
		return members.stream()
				.anyMatch(mem -> mem.getKey().equals(identity.getKey()));
	}
	
	private SelectionValue teacherPK(MemberView teacher) {
		String key = teacher.getKey().toString();
		String displayName = userManager.getUserDisplayName(teacher.getIdentity());
		
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
			memberViews = memberQueries.getCurriculumElementMembers(curriculumElement, params);
		} else {
			SearchMembersParams params = new SearchMembersParams();
			params.setRoles(new GroupRoles[] { GroupRoles.coach });
			params.setOrigins(Set.of(Origin.repositoryEntry, Origin.curriculum));
			params.setUserTypes(Set.of(UserType.user));
			memberViews = memberQueries.getRepositoryEntryMembers(entry, params);
		}
		
		Set<MemberView> allPossibleTeachers = new HashSet<>();
		allPossibleTeachers.addAll(memberViews);
		if(teachers != null && !teachers.isEmpty()) {// eventually external teachers
			for(Identity teacher:teachers) {
				if(!containsIdentity(teacher, allPossibleTeachers)) {
					allPossibleTeachers.add(new MemberView(teacher));
				}
			}
		}
		List<MemberView> views = new ArrayList<>(allPossibleTeachers);
		Collections.sort(views, new MemberViewNamesComparator(getLocale()));
		return views;
	}
	
	private void updateUI() {
		if(compulsoryEl.isOn()) {
			setFormWarning(null);
		} else {
			setFormWarning("warning.edit.lecture");
		}
	}
	
	private void updateOnlineMeetingUI() {
		boolean enableOnlineMeeting = enabledOnlineMeetingEl.isVisible() && enabledOnlineMeetingEl.isOn();
		onlineMeetingEl.setVisible(enableOnlineMeeting);
		if(!onlineMeetingEl.isOneSelected()) {
			if(bigBlueButtonModule.isEnabled() && bigBlueButtonModule.isLecturesEnabled()) {
				onlineMeetingEl.select(BIGBLUEBUTTON_MEETING, true);
			} else if(teamsModule.isEnabled() && teamsModule.isLecturesEnabled()) {
				onlineMeetingEl.select(TEAMS_MEETING, true);
			} else {
				onlineMeetingEl.select(OTHER_MEETING, true);
			}
		}
		
		editOnlineMeetingButton.setVisible(enableOnlineMeeting && onlineMeetingEl.isOneSelected()
				&& (BIGBLUEBUTTON_MEETING.equals(onlineMeetingEl.getSelectedKey()) || TEAMS_MEETING.equals(onlineMeetingEl.getSelectedKey()))
				&& !embedded);
		
		boolean enabledOnlineMeetingUrl = enableOnlineMeeting && onlineMeetingEl.isOneSelected()
				&& OTHER_MEETING.equals(onlineMeetingEl.getSelectedKey()) && !embedded;
		onlineMeetingProviderNameEl.setVisible(enabledOnlineMeetingUrl);
		onlineMeetingProviderUrlEl.setVisible(enabledOnlineMeetingUrl);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editBigBlueButtonMeetingCtrl == source || editTeamsMeetingCtrl == source) {
			validateFormLogic(ureq);
			markDirty();
			if(event == Event.CANCELLED_EVENT) {
				// Remove temporary meeting
				if(editBigBlueButtonMeetingCtrl == source && bigBlueButtonMeeting != null && bigBlueButtonMeeting.getKey() == null) {
					bigBlueButtonMeeting = null;
				}
				if(editTeamsMeetingCtrl == source && teamsMeeting != null && teamsMeeting.getKey() == null) {
					teamsMeeting = null;
				}
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editBigBlueButtonMeetingCtrl);
		removeAsListenerAndDispose(editTeamsMeetingCtrl);
		removeAsListenerAndDispose(cmc);
		editBigBlueButtonMeetingCtrl = null;
		editTeamsMeetingCtrl = null;
		cmc = null;
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
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
		
		dateEl.clearError();
		if(dateEl.getDate() == null || dateEl.getSecondDate() == null) {
			dateEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(!validateFormItem(ureq, dateEl)) {
			allOk &= false;
		} else if(dateEl.getDate().compareTo(dateEl.getSecondDate()) == 0) {
			dateEl.setErrorKey("error.min.duration");
			allOk &= false;
		} else if(dateEl.getDate().after(dateEl.getSecondDate())) {
			dateEl.setErrorKey("error.start.after.end.date");
			allOk &= false;
		}
		
		onlineMeetingEl.clearError();
		onlineMeetingProviderNameEl.clearError();
		onlineMeetingProviderUrlEl.clearError();
		if(onlineMeetingEl.isVisible()) {
			if(!onlineMeetingEl.isOneSelected()) {
				onlineMeetingEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if(!embedded) {
				if(TEAMS_MEETING.equals(onlineMeetingEl.getSelectedKey()) && teamsMeeting == null) {
					onlineMeetingEl.setErrorKey("error.configure.online.meeting");
					allOk &= false;
				} else if(BIGBLUEBUTTON_MEETING.equals(onlineMeetingEl.getSelectedKey())) {
					if(bigBlueButtonMeeting == null || bigBlueButtonMeeting.getTemplate() == null) {
						onlineMeetingEl.setErrorKey("error.configure.online.meeting");
						allOk &= false;
					} else {
						long leadTime = bigBlueButtonMeeting.getLeadTime();
						long followUpTime = bigBlueButtonMeeting.getFollowupTime();
						BigBlueButtonMeetingTemplate template = bigBlueButtonMeeting.getTemplate();
						if(!BigBlueButtonUIHelper.validateDuration(dateEl.getDate(), dateEl.getSecondDate(), leadTime,  followUpTime, template)) {
							onlineMeetingEl.setErrorKey("error.duration", template.getMaxDuration().toString());
							allOk &= false;
						} else if(!BigBlueButtonUIHelper.validateSlot(bigBlueButtonMeeting, template, dateEl.getDate(), dateEl.getSecondDate(), leadTime,  followUpTime)) {
							onlineMeetingEl.setErrorKey("server.overloaded");
							allOk &= false;
						}
					}
				} else if(OTHER_MEETING.equals(onlineMeetingEl.getSelectedKey()) && teamsMeeting == null) {
					if(!StringHelper.containsNonWhitespace(onlineMeetingProviderNameEl.getValue())) {
						onlineMeetingProviderNameEl.setErrorKey("form.legende.mandatory");
						allOk &= false;
					}
						
					if (!StringHelper.containsNonWhitespace(onlineMeetingProviderUrlEl.getValue())) {
						onlineMeetingProviderUrlEl.setErrorKey("form.legende.mandatory");
						allOk &= false;
					} else {
						allOk &= validateUrl(onlineMeetingProviderUrlEl);
					}
				}
			}
		}
		return allOk;
	}
	
	private boolean validateUrl(TextElement textEl) {
		boolean allOk = true;

		if (StringHelper.containsNonWhitespace(textEl.getValue())) {
			try {
				new URL(textEl.getValue()).toURI();
			} catch(Exception e) {
				textEl.setErrorKey("error.url.not.valid");
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		if(locationEl != fiSrc && dateEl != fiSrc) {
			super.propagateDirtinessToContainer(fiSrc, event);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(locationEl == source) {
			// Do nothing
		} else if(compulsoryEl == source) {
			updateUI();
		} else if(enabledOnlineMeetingEl == source) {
			updateOnlineMeetingUI();
			if(stepsListener != null) {
				addLectureCtxt.setWithBigBlueButtonMeeting(BIGBLUEBUTTON_MEETING.equals(onlineMeetingEl.getSelectedKey()));
				addLectureCtxt.setWithTeamsMeeting(TEAMS_MEETING.equals(onlineMeetingEl.getSelectedKey()));
				stepsListener.onStepsChanged(ureq);
				fireEvent(ureq, StepsEvent.STEPS_CHANGED);
			}
		} else if(onlineMeetingEl == source) {
			updateOnlineMeetingUI();
		} else if(editOnlineMeetingButton == source) {
			if(BIGBLUEBUTTON_MEETING.equals(onlineMeetingEl.getSelectedKey())) {
				doEditBigBlueButtonMeeting(ureq);
			} else if(TEAMS_MEETING.equals(onlineMeetingEl.getSelectedKey())) {
				doEditTeamsMeeting(ureq);
			}
		} else if (adoptButton == source) {
			doAdopt();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void formOK(UserRequest ureq) {
		boolean create = false;
		int currentPlannedLectures = -1;
		
		String beforeXml;
		LectureBlockAuditLog.Action action;
		StringBuilder audit = new StringBuilder();
		List<Group> selectedGroups;
		if(lectureBlock == null || lectureBlock.getKey() == null) {
			beforeXml = null;
			action = LectureBlockAuditLog.Action.createLectureBlock;
			selectedGroups = new ArrayList<>();	
			if(curriculumElement != null) {
				RepositoryEntry singleEntry;
				if(entry != null) {
					singleEntry = entry;
				} else {
					List<RepositoryEntry> curriculumElementEntries = curriculumService.getRepositoryEntries(curriculumElement);
					singleEntry = curriculumElementEntries.size() == 1 ? curriculumElementEntries.get(0) : null;
				}
				lectureBlock = lectureBlock != null && lectureBlock.getKey() == null
						? lectureBlock
						: lectureService.createLectureBlock(curriculumElement, singleEntry);
				selectedGroups.add(curriculumElement.getGroup());
			} else if(entry != null) {
				lectureBlock = lectureBlock != null && lectureBlock.getKey() == null
						? lectureBlock
						: lectureService.createLectureBlock(entry);
				// Add default group and business groups automatically
				Group defGroup = repositoryService.getDefaultGroup(entry);
				selectedGroups.add(defGroup);
				
				BusinessGroupQueryParams params = new BusinessGroupQueryParams();
				params.setTechnicalTypes(List.of(BusinessGroup.BUSINESS_TYPE, LTI13Service.LTI_GROUP_TYPE));
				List<StatisticsBusinessGroupRow> businessGroups = businessGroupService.findBusinessGroupsFromRepositoryEntry(params, null, entry);
				for(StatisticsBusinessGroupRow businessGroup:businessGroups) {
					selectedGroups.add(businessGroup.getBaseGroup());
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
		lectureBlock.setCompulsory(compulsoryEl.isOn());
		lectureBlock.setDescription(descriptionEl.getValue());
		lectureBlock.setPreparation(preparationEl.getValue());
		lectureBlock.setLocation(locationEl.getValue());
		lectureBlock.setStartDate(dateEl.getDate());
		lectureBlock.setEndDate(dateEl.getSecondDate());
		
		int plannedLectures = Integer.parseInt(plannedLecturesEl.getSelectedKey());
		lectureBlock.setPlannedLecturesNumber(plannedLectures);
		
		if(addLectureCtxt != null) {
			addLectureCtxt.setTaxonomyLevelKeys(getSelectedTaxonomyLevelKeys());
			addLectureCtxt.setTeachers(getSelectedTeachers());
			addLectureCtxt.setLectureBlock(lectureBlock);
			boolean enableOnlineMeeting = enabledOnlineMeetingEl.isVisible() && enabledOnlineMeetingEl.isOn();
			addLectureCtxt.setWithBigBlueButtonMeeting(enableOnlineMeeting && BIGBLUEBUTTON_MEETING.equals(onlineMeetingEl.getSelectedKey()));
			addLectureCtxt.setWithTeamsMeeting(enableOnlineMeeting && TEAMS_MEETING.equals(onlineMeetingEl.getSelectedKey()));
		} else {
			updateOnlineMeetings();
			lectureBlock = lectureService.save(lectureBlock, selectedGroups);
			lectureService.updateTaxonomyLevels(lectureBlock, getSelectedTaxonomyLevelKeys());
			
			synchronizeTeachers(audit);
	
			String afterxml = lectureService.toAuditXml(lectureBlock);
			lectureService.auditLog(action, beforeXml, afterxml, audit.toString(), lectureBlock, null,
					entry, curriculumElement, null, getIdentity());
			dbInstance.commit();
			if(currentPlannedLectures >= 0) {
				lectureService.adaptRollCalls(lectureBlock);
			}
			lectureService.syncCalendars(lectureBlock);
			//update eventual assessment mode
			AssessmentMode assessmentMode = assessmentModeMgr.getAssessmentMode(lectureBlock);
			if(assessmentMode != null) {
				assessmentModeMgr.syncAssessmentModeToLectureBlock(assessmentMode);
				assessmentModeMgr.merge(assessmentMode, false, getIdentity());
			}
			dbInstance.commitAndCloseSession();
			fireEvent(ureq, Event.DONE_EVENT);
	
			if(create) {
				ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LECTURE_BLOCK_CREATED, getClass(),
						CoreLoggingResourceable.wrap(lectureBlock, OlatResourceableType.lectureBlock, lectureBlock.getTitle()));
			} else {
				ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LECTURE_BLOCK_EDITED, getClass(),
						CoreLoggingResourceable.wrap(lectureBlock, OlatResourceableType.lectureBlock, lectureBlock.getTitle()));
			}
		}
		
		updateLocationsPrefs(ureq);
	}
	
	private void updateOnlineMeetings() {
		boolean enableOnlineMeeting = enabledOnlineMeetingEl.isVisible() && enabledOnlineMeetingEl.isOn() && onlineMeetingEl.isOneSelected();
		if(enableOnlineMeeting && BIGBLUEBUTTON_MEETING.equals(onlineMeetingEl.getSelectedKey())) {
			bigBlueButtonMeeting = bigBlueButtonManager.updateMeeting(bigBlueButtonMeeting, true);
			lectureBlock.setBBBMeeting(bigBlueButtonMeeting);
			lectureBlock.setTeamsMeeting(null);
			if(teamsMeeting != null) {
				teamsService.deleteMeeting(teamsMeeting);
			}
			lectureBlock.setMeetingTitle(null);
			lectureBlock.setMeetingUrl(null);
		} else if(enableOnlineMeeting && TEAMS_MEETING.equals(onlineMeetingEl.getSelectedKey())) {
			teamsMeeting = teamsService.updateMeeting(teamsMeeting, true);
			lectureBlock.setBBBMeeting(null);
			lectureBlock.setTeamsMeeting(teamsMeeting);
			if(bigBlueButtonMeeting != null) {
				bigBlueButtonManager.deleteMeeting(bigBlueButtonMeeting, null);
			}
			lectureBlock.setMeetingTitle(null);
			lectureBlock.setMeetingUrl(null);
		} else if(enableOnlineMeeting && OTHER_MEETING.equals(onlineMeetingEl.getSelectedKey())) {
			lectureBlock.setMeetingTitle(onlineMeetingProviderNameEl.getValue());
			lectureBlock.setMeetingUrl(onlineMeetingProviderUrlEl.getValue());
			lectureBlock.setBBBMeeting(null);
			if(bigBlueButtonMeeting != null) {
				bigBlueButtonManager.deleteMeeting(bigBlueButtonMeeting, null);
			}
			lectureBlock.setTeamsMeeting(null);
			if(teamsMeeting != null) {
				teamsService.deleteMeeting(teamsMeeting);
			}
		} else {
			lectureBlock.setBBBMeeting(null);
			lectureBlock.setTeamsMeeting(null);
			if(bigBlueButtonMeeting != null) {
				bigBlueButtonManager.deleteMeeting(bigBlueButtonMeeting, null);
			}
			if(teamsMeeting != null) {
				teamsService.deleteMeeting(teamsMeeting);
			}
			lectureBlock.setMeetingTitle(null);
			lectureBlock.setMeetingUrl(null);
		}
	}
	
	private Set<Long> getSelectedTaxonomyLevelKeys() {
		if (taxonomyLevelEl == null) {
			return null;
		}
		return taxonomyLevelEl.getSelectedKeys().stream().map(Long::valueOf).collect(Collectors.toSet());
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
				if(selectedTeacherKey.equals(teacherView.getKey().toString())) {
					Identity teacher = securityManager.loadIdentityByKey(teacherView.getKey());
					lectureService.addTeacher(lectureBlock, teacher);
					audit.append("add teacher: ").append(userManager.getUserDisplayName(teacher)).append(" (").append(teacher.getKey()).append(");");
				}
			}
		}
	}
	
	private List<Identity> getSelectedTeachers() {
		List<Long> selectedTeacherKeys = teacherEl.getSelectedKeys().stream()
				.filter(StringHelper::isLong)
				.map(Long::valueOf)
				.toList();
		return securityManager.loadIdentityByKeys(selectedTeacherKeys);
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
	
	private void doEditBigBlueButtonMeeting(UserRequest ureq) {
		if(bigBlueButtonMeeting == null) {
			bigBlueButtonMeeting = bigBlueButtonManager.createMeeting(titleEl.getValue(), dateEl.getDate(), dateEl.getSecondDate(),
					null, null, null, getIdentity());
		} else if(bigBlueButtonMeeting.getKey() != null) {
			bigBlueButtonMeeting = bigBlueButtonManager.getMeeting(bigBlueButtonMeeting);
		} else if(!StringHelper.containsNonWhitespace(bigBlueButtonMeeting.getName())) {
			bigBlueButtonMeeting.setName(titleEl.getValue());
			bigBlueButtonMeeting.setStartDate(dateEl.getDate());
			bigBlueButtonMeeting.setEndDate(dateEl.getSecondDate());
		}
		List<BigBlueButtonTemplatePermissions> permissions = bigBlueButtonManager
				.calculatePermissions(entry, null, getIdentity(), ureq.getUserSession().getRoles());
		editBigBlueButtonMeetingCtrl = new EditBigBlueButtonMeetingController(ureq, getWindowControl(), bigBlueButtonMeeting, permissions);
		listenTo(editBigBlueButtonMeetingCtrl);
		editBigBlueButtonMeetingCtrl.removeDates();

		String title = translate("edit.online.meeting.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editBigBlueButtonMeetingCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditTeamsMeeting(UserRequest ureq) {
		if(teamsMeeting == null) {
			teamsMeeting = teamsService.createMeeting(titleEl.getValue(), dateEl.getDate(), dateEl.getSecondDate(), null, null, null, getIdentity());
		} else if(teamsMeeting.getKey() != null) {
			teamsMeeting = teamsService.getMeeting(teamsMeeting);
		} else if(!StringHelper.containsNonWhitespace(teamsMeeting.getSubject())) {
			teamsMeeting.setSubject(titleEl.getValue());
			teamsMeeting.setStartDate(dateEl.getDate());
			teamsMeeting.setEndDate(dateEl.getSecondDate());
		}
		editTeamsMeetingCtrl = new EditTeamsMeetingController(ureq, getWindowControl(), teamsMeeting);
		listenTo(editTeamsMeetingCtrl);
		editTeamsMeetingCtrl.removeDates();

		String title = translate("edit.online.meeting.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editTeamsMeetingCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAdopt() {
		if (curriculumElement != null) {
			doAdoptFromCurriculumElement(curriculumElement.getKey());
		} else {
			if (lectureBlock != null && lectureBlock.getCurriculumElement() != null) {
				doAdoptFromCurriculumElement(lectureBlock.getCurriculumElement().getKey());
			}
		}
		if (entry != null) {
			doAdoptFromEntry(entry.getKey());
		}
	}
	
	private void doAdoptFromCurriculumElement(Long key) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(key));
		Set<TaxonomyLevel> taxonomyLevels = curriculumElement.getTaxonomyLevels().stream()
				.map(CurriculumElementToTaxonomyLevel::getTaxonomyLevel).collect(Collectors.toSet());
		updateTaxonomyLevelSelection(taxonomyLevels);
	}
	
	private void doAdoptFromEntry(Long key) {
		RepositoryEntry repositoryEntry = repositoryService.loadByKey(key);
		Set<TaxonomyLevel> taxonomyLevels = repositoryEntry.getTaxonomyLevels().stream()
				.map(RepositoryEntryToTaxonomyLevel::getTaxonomyLevel).collect(Collectors.toSet());
		updateTaxonomyLevelSelection(taxonomyLevels);
	}
	
	private void updateTaxonomyLevelSelection(Set<TaxonomyLevel> taxonomyLevels) {
		taxonomyLevels.forEach(level -> taxonomyLevelEl.select(level.getKey().toString()));
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
	
	public interface StepsListener {
		
		void onStepsChanged(UserRequest ureq);

	}
}