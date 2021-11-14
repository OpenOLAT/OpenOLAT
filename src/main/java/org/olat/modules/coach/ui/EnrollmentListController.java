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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
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
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.RoleSecurityCallback;
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
public class EnrollmentListController extends FormBasicController implements Activateable2, GenericEventListener {

	private final TooledStackedPanel stackedPanel;
	private final Identity student;
	private final StudentStatEntry statEntry;
	private final RoleSecurityCallback roleSecurityCallback;

	private final List<UserPropertyHandler> userPropertyHandlers;

	private FlexiTableElement tableEl;
	private EfficiencyStatementEntryTableDataModel model;
	
	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private UserDetailsController statementCtrl;
	
	private boolean hasChanged = false;
	
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

	public EnrollmentListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel,
									StudentStatEntry statEntry, Identity student, RoleSecurityCallback roleSecurityCallback) {
		super(ureq, wControl, "user_relation_enrollments");
		
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, isAdministrativeUser);

		this.student = student;
		this.statEntry = statEntry;
		this.stackedPanel = stackedPanel;
		this.roleSecurityCallback = roleSecurityCallback;
		
		initForm(ureq);
		loadModel();

		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), CertificatesManager.ORES_CERTIFICATE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.repoName, "select"));
		if (roleSecurityCallback.canViewCourseProgressAndStatus()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.completion, new LearningProgressCompletionCellRenderer()));
		}
		if (roleSecurityCallback.canViewEfficiencyStatements()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.passed, new PassedCellRenderer()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.score, new ScoreCellRenderer()));
		}
		if (roleSecurityCallback.canReceiveCertificatesMail()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.certificate, new DownloadCertificateCellRenderer(getLocale())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.recertification, new DateFlexiCellRenderer(getLocale())));
		}

		if (roleSecurityCallback.canViewEfficiencyStatements()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.numberAssessments, new ProgressOfCellRenderer()));
		}
		if(lectureModule.isEnabled()) {
			if (roleSecurityCallback.canViewLecturesAndAbsences()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.plannedLectures));
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.attendedLectures));
				if (lectureModule.isAuthorizedAbsenceEnabled()) {
					columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.unauthorizedAbsenceLectures));
					columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.authorizedAbsenceLectures));
					if(lectureModule.isAbsenceNoticeEnabled()) {
						columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.dispensedLectures));
					}
				} else {
					columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.absentLectures));
				}
			}
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.lastModification));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.lastUserModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.lastCoachModified));
		
		model = new EfficiencyStatementEntryTableDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setEmptyTableSettings("default.tableEmptyMessage", null, "o_icon_user");
		tableEl.setAndLoadPersistedPreferences(ureq, "fStudentCourseListController");
	}

	@Override
	protected void doDispose() {
		stackedPanel.removeListener(this);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, CertificatesManager.ORES_CERTIFICATE_EVENT);
        super.doDispose();
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
		List<RepositoryEntry> courses = coachingService.getUserCourses(student);
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
		if(stackedPanel == source) {
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
			} 
		} else if (source == cmc) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(contactCtrl);
			cmc = null;
			contactCtrl = null;
		} else if (source == contactCtrl) {
			cmc.deactivate();
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(contactCtrl);
			cmc = null;
			contactCtrl = null;
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
		
		statementCtrl = new UserDetailsController(ureq, bwControl, stackedPanel,
				entry, student, details, entryIndex, model.getRowCount(), selectedTool, false, true);
		listenTo(statementCtrl);
		stackedPanel.pushController(displayName, statementCtrl);
	}
}
