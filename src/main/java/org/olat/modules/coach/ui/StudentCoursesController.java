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
package org.olat.modules.coach.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.admin.user.UserChangePasswordController;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.bulk.PassedCellRenderer;
import org.olat.course.certificate.CertificateEvent;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.modules.assessment.AssessmentEntryScoring;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.coach.CoachingModule;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.IdentityRepositoryEntryKey;
import org.olat.modules.coach.model.IdentityResourceKey;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.modules.coach.ui.EfficiencyStatementEntryTableDataModel.Columns;
import org.olat.modules.coach.ui.UserDetailsController.Segment;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockStatistics;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Overview of all students under the scrutiny of a coach.
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class StudentCoursesController extends FormBasicController implements Activateable2, GenericEventListener, TooledController {

	private final Link homeLink, contactLink;
	private Link resetLink;
	private Link nextStudent, detailsStudentCmp, previousStudent;

	private FlexiTableElement tableEl;
	private final TooledStackedPanel stackPanel;
	private EfficiencyStatementEntryTableDataModel model;
	
	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private UserDetailsController statementCtrl;
	private UserChangePasswordController userChangePasswordController;
	
	private boolean hasChanged = false;
	
	private final int index;
	private final int numOfStudents;
	private final Identity student;
	private final boolean fullAccess;
	private final StudentStatEntry statEntry;

	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CoachingModule coachingModule;
	@Autowired
	private BaseSecurityManager securityManager;
	
	public StudentCoursesController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			StudentStatEntry statEntry, Identity student, int index, int numOfStudents, boolean fullAccess) {
		super(ureq, wControl, "student_course_list");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, isAdministrativeUser);

		this.index = index;
		this.student = student;
		this.statEntry = statEntry;
		this.fullAccess = fullAccess;
		this.stackPanel = stackPanel;
		this.numOfStudents = numOfStudents;
		
		initForm(ureq);
		loadModel();

		contactLink = LinkFactory.createButton("contact.link", flc.getFormItemComponent(), this);
		contactLink.setIconLeftCSS("o_icon o_icon_mail");
		flc.getFormItemComponent().put("contact", contactLink);
		
		homeLink = LinkFactory.createButton("home.link", flc.getFormItemComponent(), this);
		homeLink.setIconLeftCSS("o_icon o_icon_home");
		flc.getFormItemComponent().put("home", homeLink);
		
		Roles roles = securityManager.getRoles(student);
		if (coachingModule.isResetPasswordEnabled() && !(roles.isAuthor() || roles.isManager() || roles.isAdministrator() || roles.isSystemAdmin() || roles.isPrincipal())) {
			resetLink = LinkFactory.createButton("reset.link", flc.getFormItemComponent(), this);
			resetLink.setIconLeftCSS("o_icon o_icon_password");
			flc.getFormItemComponent().put("reset", resetLink);
		}
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), CertificatesManager.ORES_CERTIFICATE_EVENT);
	}

	@Override
	public void initTools() {
		previousStudent = LinkFactory.createToolLink("previous.student", translate("previous.student"), this);
		previousStudent.setIconLeftCSS("o_icon o_icon_previous");
		previousStudent.setEnabled(numOfStudents > 1);
		stackPanel.addTool(previousStudent);
		
		String fullName = StringHelper.escapeHtml(userManager.getUserDisplayName(student));
		String details = translate("students.details", new String[]{
				fullName, Integer.toString(index + 1), Integer.toString(numOfStudents)
		});
		detailsStudentCmp = LinkFactory.createToolLink("details.student", details, this);
		detailsStudentCmp.setIconLeftCSS("o_icon o_icon_user");
		stackPanel.addTool(detailsStudentCmp);

		nextStudent = LinkFactory.createToolLink("next.student", translate("next.student"), this);
		nextStudent.setIconLeftCSS("o_icon o_icon_next");
		nextStudent.setEnabled(numOfStudents > 1);
		stackPanel.addTool(nextStudent);
		stackPanel.addListener(this);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String fullName = userManager.getUserDisplayName(student);
			layoutCont.contextPut("studentName", StringHelper.escapeHtml(fullName));
		}
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colIndex = UserListController.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, "select",
					true, userPropertyHandler.i18nColumnDescriptorLabelKey()));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.repoName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.completion, new LearningProgressCompletionCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.passed, new PassedCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.score, new ScoreCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.certificate, new DownloadCertificateCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.recertification, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.numberAssessments, new ProgressOfCellRenderer()));
		if(lectureModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.plannedLectures));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.attendedLectures));
			if(lectureModule.isAuthorizedAbsenceEnabled()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.unauthorizedAbsenceLectures));
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.authorizedAbsenceLectures));
				if(lectureModule.isAbsenceNoticeEnabled()) {
					columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.dispensedLectures));
				}
			} else {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.absentLectures));
			}
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.lastModification));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.lastUserModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.lastCoachModified));
		
		model = new EfficiencyStatementEntryTableDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setEmptyTableSettings("default.tableEmptyMessage", null, "o_icon_user");
		tableEl.setAndLoadPersistedPreferences(ureq, "fStudentCourseListController-v2");
	}

	@Override
	protected void doDispose() {
		stackPanel.removeListener(this);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, CertificatesManager.ORES_CERTIFICATE_EVENT);
	}

	@Override
	public void event(Event event) {
		if(event instanceof CertificateEvent) {
			CertificateEvent ce = (CertificateEvent)event;
			if(student.getKey().equals(ce.getOwnerKey())) {
				updateCertificate(ce.getCertificateKey());
			}
		}
	}
	
	private void updateCertificate(Long certificateKey) {
		CertificateLight certificate = certificatesManager.getCertificateLightById(certificateKey);
		model.putCertificate(certificate);
	}
	
	public StudentStatEntry getEntry() {
		return statEntry;
	}
	
	private List<EfficiencyStatementEntry> loadModel() {
		List<RepositoryEntry> courses = fullAccess ? coachingService.getUserCourses(student)
				: coachingService.getStudentsCourses(getIdentity(), student);
		List<EfficiencyStatementEntry> statements = coachingService.getEfficencyStatements(student, courses, userPropertyHandlers, getLocale());
		
		
		List<CertificateLight> certificates = certificatesManager.getLastCertificates(student);
		ConcurrentMap<IdentityResourceKey, CertificateLight> certificateMap = new ConcurrentHashMap<>();
		for(CertificateLight certificate:certificates) {
			IdentityResourceKey key = new IdentityResourceKey(student.getKey(), certificate.getOlatResourceKey());
			certificateMap.put(key, certificate);
		}
		
		ConcurrentMap<IdentityRepositoryEntryKey, Double> completionsMap = new ConcurrentHashMap<>();
		List<Long> courseEntryKeys = courses.stream().map(RepositoryEntry::getKey).collect(Collectors.toList());
		List<AssessmentEntryScoring> assessmentEntries = assessmentService.loadRootAssessmentEntriesByAssessedIdentity(student, courseEntryKeys);
		for (AssessmentEntryScoring assessmentEntry : assessmentEntries) {
			if (assessmentEntry.getCompletion() != null) {
				IdentityRepositoryEntryKey key = new IdentityRepositoryEntryKey(student.getKey(), assessmentEntry.getRepositoryEntryKey());
				completionsMap.put(key, assessmentEntry.getCompletion());
			}
		}
		
		ConcurrentMap<IdentityRepositoryEntryKey, LectureBlockStatistics> lecturesMap = new ConcurrentHashMap<>();
		if(lectureModule.isEnabled()) {
			List<LectureBlockStatistics> lectureStats = lectureService.getParticipantLecturesStatistics(student);
			for(LectureBlockStatistics lectureStat:lectureStats) {
				IdentityRepositoryEntryKey key = new IdentityRepositoryEntryKey(student.getKey(), lectureStat.getRepoKey());
				lecturesMap.put(key, lectureStat);
			}
		}

		model.setObjects(statements, certificateMap, completionsMap, lecturesMap);
		tableEl.reset();
		tableEl.reloadData();
		return statements;
	}
	
	private void reloadModel() {
		if(hasChanged) {
			//reload
			loadModel();
			hasChanged = false;
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				EfficiencyStatementEntry selectedRow = model.getObject(se.getIndex());
				if("select".equals(cmd)) {
					selectDetails(ureq, selectedRow);
				}
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(previousStudent == source || nextStudent == source) {
			fireEvent(ureq, event);
		} else if (source == homeLink) {
			openHome(ureq);
		} else if (source == contactLink) {
			contact(ureq);
		} else if (source == resetLink) {
			resetPassword(ureq);
		} else if(stackPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() == statementCtrl && hasChanged) {
					reloadModel();
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == statementCtrl) {
			if(event == Event.CHANGED_EVENT) {
				hasChanged = true;
				fireEvent(ureq, event);
			} else if ("next".equals(event.getCommand())) {
				nextEntry(ureq);
			} else if ("previous".equals(event.getCommand())) {
				previousEntry(ureq);
			} 
		} else if (source == cmc) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(contactCtrl);
			removeAsListenerAndDispose(userChangePasswordController);
			cmc = null;
			contactCtrl = null;
			userChangePasswordController = null;
		} else if (source == contactCtrl) {
			cmc.deactivate();
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(contactCtrl);
			cmc = null;
			contactCtrl = null;
		} else if (source == userChangePasswordController) {
			cmc.deactivate();
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(userChangePasswordController);
			cmc = null;
			userChangePasswordController = null;
		}
		super.event(ureq, source, event);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry ce = entries.get(0);
		OLATResourceable ores = ce.getOLATResourceable();
		if("RepositoryEntry".equals(ores.getResourceableTypeName())) {
			Long entryKey = ores.getResourceableId();
			for(EfficiencyStatementEntry entry:model.getObjects()) {
				if(entryKey.equals(entry.getCourse().getKey())) {
					selectDetails(ureq, entry);
					statementCtrl.activate(ureq, entries.subList(1, entries.size()), ce.getTransientState());
					break;
				}
			}
		}
	}
	
	private void contact(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);

		ContactMessage cmsg = new ContactMessage(getIdentity());
		String fullName = userManager.getUserDisplayName(student);
		ContactList contactList = new ContactList(fullName);
		contactList.add(student);
		cmsg.addEmailTo(contactList);
		contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
		listenTo(contactCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), contactCtrl.getInitialComponent());
		cmc.activate();
		listenTo(cmc);
	}
	
	private void resetPassword(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		
		userChangePasswordController = new UserChangePasswordController(ureq, getWindowControl(), student);
		listenTo(userChangePasswordController);
		String name = student.getUser().getFirstName() + " " + student.getUser().getLastName();
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userChangePasswordController.getInitialComponent(), true, translate("reset.title", name));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void nextEntry(UserRequest ureq) {
		EfficiencyStatementEntry currentEntry = statementCtrl.getEntry();
		int nextIndex = model.getObjects().indexOf(currentEntry) + 1;
		if(nextIndex < 0 || nextIndex >= model.getRowCount()) {
			nextIndex = 0;
		}
		EfficiencyStatementEntry nextEntry = model.getObject(nextIndex);
		selectDetails(ureq, nextEntry);
	}
	
	private void previousEntry(UserRequest ureq) {
		EfficiencyStatementEntry currentEntry = statementCtrl.getEntry();
		int previousIndex = model.getObjects().indexOf(currentEntry) - 1;
		if(previousIndex < 0 || previousIndex >= model.getRowCount()) {
			previousIndex = model.getRowCount() - 1;
		}
		EfficiencyStatementEntry previousEntry = model.getObject(previousIndex);
		selectDetails(ureq, previousEntry);
	}
	
	private void selectDetails(UserRequest ureq, EfficiencyStatementEntry entry) {
		Segment selectedTool = null;
		if(statementCtrl != null) {
			selectedTool = statementCtrl.getSelectedSegment();
		}
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(RepositoryEntry.class, entry.getCourse().getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		String displayName = entry.getCourseDisplayName();
		int entryIndex = model.getObjects().indexOf(entry);
		String details = translate("students.details", new String[] {
				displayName, String.valueOf(entryIndex), String.valueOf(model.getRowCount())
		});
		
		statementCtrl = new UserDetailsController(ureq, bwControl, stackPanel,
				entry, student, details, entryIndex, model.getRowCount(), selectedTool, true, false);
		listenTo(statementCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(displayName, statementCtrl);
	}
	
	private void openHome(UserRequest ureq) {
		List<ContextEntry> ces = new ArrayList<>(4);
		ces.add(BusinessControlFactory.getInstance().createContextEntry(student));

		BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(ces);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
		NewControllerFactory.getInstance().launch(ureq, bwControl);
	}
}
