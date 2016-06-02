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

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
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
import org.olat.course.certificate.CertificateEvent;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.IdentityResourceKey;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.modules.coach.ui.EfficiencyStatementEntryTableDataModel.Columns;
import org.olat.modules.coach.ui.ToolbarController.Position;
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
public class StudentCoursesController extends BasicController implements Activateable2, GenericEventListener {

	private final Link backLink, next, previous;
	private final Link nextStudent, previousStudent;
	private final Link homeLink, contactLink;
	private final TextComponent detailsCmp, detailsStudentCmp;
	private final TableController tableCtr;
	private final VelocityContainer mainVC;
	private final VelocityContainer detailsVC;
	private EfficiencyStatementEntryTableDataModel model;
	
	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private final ToolbarController toolbar;
	private EfficiencyStatementDetailsController statementCtrl;
	
	private boolean hasChanged = false;
	
	private final Identity student;
	private final boolean fullAccess;
	private final StudentStatEntry statEntry;

	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CertificatesManager certificatesManager;
	
	public StudentCoursesController(UserRequest ureq, WindowControl wControl, StudentStatEntry statEntry,
			Identity student, int index, int numOfStudents, boolean fullAccess) {
		super(ureq, wControl);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, isAdministrativeUser);

		this.student = student;
		this.statEntry = statEntry;
		this.fullAccess = fullAccess;

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("error.no.found"));
		tableConfig.setDownloadOffered(true);
		tableConfig.setPreferencesOffered(true, "studentCourseListController");
		
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), null, null, null, null, true, getTranslator());
		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("student.name", Columns.name.ordinal(), "select", getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.course.name", Columns.repoName.ordinal(), "select", getLocale()));
		tableCtr.addColumnDescriptor(new BooleanColumnDescriptor("table.header.passed", Columns.passed.ordinal(), translate("passed.true"), translate("passed.false")));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.score", Columns.score.ordinal(), "select", getLocale(),
				ColumnDescriptor.ALIGNMENT_RIGHT, new ScoreCellRenderer()));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.certificate", Columns.certificate.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new DownloadCertificateCellRenderer(student)));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.progress", Columns.progress.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new ProgressRenderer(true, getTranslator())));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.lastScoreDate", Columns.lastModification.ordinal(), "select", getLocale()));

		listenTo(tableCtr);
		List<EfficiencyStatementEntry> statements = loadModel();

		mainVC = createVelocityContainer("student_course_list");
		detailsVC = createVelocityContainer("student_details");
		
		String fullName = StringHelper.escapeHtml(userManager.getUserDisplayName(student));
		
		detailsVC.contextPut("studentName", fullName);
		mainVC.put("studentDetails", detailsVC);
		mainVC.put("studentsTable", tableCtr.getInitialComponent());
		
		toolbar = new ToolbarController(ureq, wControl, getTranslator());
		listenTo(toolbar);
		
		mainVC.put("toolbar", toolbar.getInitialComponent());
		backLink = toolbar.addToolbarLink("back", this, Position.left);
		backLink.setIconLeftCSS("o_icon o_icon_back");
		previous = toolbar.addToolbarLink("previous.course", this, Position.center);
		previous.setIconLeftCSS("o_icon o_icon_move_left");
		previous.setCustomDisabledLinkCSS("navbar-text");
		previous.setEnabled(statements.size() > 1);
		detailsCmp = toolbar.addToolbarText("details", this, Position.center);
		next = toolbar.addToolbarLink("next.course", this, Position.center);
		next.setIconRightCSS("o_icon o_icon_move_right");
		next.setCustomDisabledLinkCSS("navbar-text");
		next.setEnabled(statements.size() > 1);
		
		//students next,previous
		previousStudent = toolbar.addToolbarLink("previous.student", this, Position.center);
		previousStudent.setIconLeftCSS("o_icon o_icon_move_left");
		previousStudent.setCustomDisabledLinkCSS("navbar-text");
		previousStudent.setEnabled(numOfStudents > 1);
		
		detailsStudentCmp = toolbar.addToolbarText("details.student", "", this, Position.center);
		detailsStudentCmp.setCssClass("navbar-text");
		detailsStudentCmp.setText(translate("students.details", new String[]{
				fullName, Integer.toString(index + 1), Integer.toString(numOfStudents)
		}));
		nextStudent = toolbar.addToolbarLink("next.student", this, Position.center);
		nextStudent.setIconRightCSS("o_icon o_icon_move_right");
		nextStudent.setCustomDisabledLinkCSS("navbar-text");
		nextStudent.setEnabled(numOfStudents > 1);
		
		contactLink = LinkFactory.createButton("contact.link", detailsVC, this);
		contactLink.setIconLeftCSS("o_icon o_icon_mail");
		detailsVC.put("contact", contactLink);
		
		homeLink = LinkFactory.createButton("home.link", detailsVC, this);
		homeLink.setIconLeftCSS("o_icon o_icon_home");
		detailsVC.put("home", homeLink);

		setDetailsToolbarVisible(false);
		putInitialPanel(mainVC);
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), CertificatesManager.ORES_CERTIFICATE_EVENT);
	}
	
	@Override
	protected void doDispose() {
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

		//model = new EfficiencyStatementEntryTableDataModel(statements, certificateMap);
		//tableCtr.setTableDataModel(model);
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
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == next) {
			nextEntry(ureq);
		} else if (source == previous) {
			previousEntry(ureq);
		} else if(source == backLink) {
			back(ureq);
		} else if (source == homeLink) {
			openHome(ureq);
		} else if (source == contactLink) {
			contact(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableCtr) {
			if(event instanceof TableEvent) {
				TableEvent e = (TableEvent) event;
				if("select".equals(e.getActionId())) {
					EfficiencyStatementEntry entry = (EfficiencyStatementEntry)tableCtr.getTableDataModel().getObject(e.getRowId());
					selectDetails(ureq, entry);					
				}
			}
		} else if (source == statementCtrl) {
			if(event == Event.CHANGED_EVENT) {
				hasChanged = true;
				fireEvent(ureq, event);
			} else {
				reloadModel();
				removeDetails(ureq);
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
		} else if (source == toolbar) {
			if("back".equals(event.getCommand())) {
				reloadModel();
				back(ureq);
			} else if ("next.course".equals(event.getCommand())) {
				nextEntry(ureq);
			} else if ("previous.course".equals(event.getCommand())) {
				previousEntry(ureq);
			} else if ("contact.link".equals(event.getCommand())) {
				contact(ureq);
			} else if ("next.student".equals(event.getCommand())) {
				fireEvent(ureq, event);
			} else if ("previous.student".equals(event.getCommand())) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry ce = entries.get(0);
		OLATResourceable ores = ce.getOLATResourceable();
		if("RepositoryEntry".equals(ores.getResourceableTypeName())) {
			Long identityKey = ores.getResourceableId();
			for(int i=tableCtr.getRowCount(); i-->0; ) {
				EfficiencyStatementEntry entry = (EfficiencyStatementEntry)tableCtr.getTableDataModel().getObject(i);
				if(identityKey.equals(entry.getCourse().getKey())) {
					selectDetails(ureq, entry);
					statementCtrl.activate(ureq, entries.subList(1, entries.size()), ce.getTransientState());
					break;
				}
			}
		}
	}
	
	private void setDetailsToolbarVisible(boolean visible) {
		next.setVisible(visible);
		previous.setVisible(visible);
		detailsCmp.setVisible(visible);
		
		nextStudent.setVisible(!visible);
		previousStudent.setVisible(!visible);
		detailsStudentCmp.setVisible(!visible);
	}
	
	private void contact(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);

		ContactMessage cmsg = new ContactMessage(getIdentity());
		ContactList contactList = new ContactList("to");
		contactList.add(student);
		cmsg.addEmailTo(contactList);
		contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
		listenTo(contactCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), contactCtrl.getInitialComponent());
		cmc.activate();
		listenTo(cmc);
	}
	
	private void removeDetails(UserRequest ureq) {
		mainVC.remove(statementCtrl.getInitialComponent());
		removeAsListenerAndDispose(statementCtrl);
		statementCtrl = null;
		setDetailsToolbarVisible(false);
		addToHistory(ureq);
	}
	
	private void back(UserRequest ureq) {
		if(statementCtrl == null) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else {
			removeDetails(ureq);
		}
	}
	
	private void nextEntry(UserRequest ureq) {
		EfficiencyStatementEntry currentEntry = statementCtrl.getEntry();
		int nextIndex = tableCtr.getIndexOfSortedObject(currentEntry) + 1;
		if(nextIndex < 0 || nextIndex >= tableCtr.getRowCount()) {
			nextIndex = 0;
		}
		EfficiencyStatementEntry nextEntry = (EfficiencyStatementEntry)tableCtr.getSortedObjectAt(nextIndex);
		selectDetails(ureq, nextEntry);
	}
	
	private void previousEntry(UserRequest ureq) {
		EfficiencyStatementEntry currentEntry = statementCtrl.getEntry();
		int previousIndex = tableCtr.getIndexOfSortedObject(currentEntry) - 1;
		if(previousIndex < 0 || previousIndex >= tableCtr.getRowCount()) {
			previousIndex = tableCtr.getRowCount() - 1;
		}
		EfficiencyStatementEntry previousEntry = (EfficiencyStatementEntry)tableCtr.getSortedObjectAt(previousIndex);
		selectDetails(ureq, previousEntry);
	}
	
	private void selectDetails(UserRequest ureq, EfficiencyStatementEntry entry) {
		boolean selectAssessmentTool = false;
		if(statementCtrl != null) {
			selectAssessmentTool = statementCtrl.isAssessmentToolSelected();
			removeAsListenerAndDispose(statementCtrl);
		}
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(RepositoryEntry.class, entry.getCourse().getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		statementCtrl = new EfficiencyStatementDetailsController(ureq, bwControl, entry, selectAssessmentTool);
		listenTo(statementCtrl);
		detailsCmp.setText(entry.getCourse().getDisplayname());

		mainVC.put("efficiencyDetails", statementCtrl.getInitialComponent());	
		setDetailsToolbarVisible(true);
	}
	
	private void openHome(UserRequest ureq) {
		List<ContextEntry> ces = new ArrayList<ContextEntry>(4);
		ces.add(BusinessControlFactory.getInstance().createContextEntry(student));

		BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(ces);
	  WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
		NewControllerFactory.getInstance().launch(ureq, bwControl);
	}
}
