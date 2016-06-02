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

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
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
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.ui.ToolbarController.Position;
import org.olat.modules.coach.ui.UserEfficiencyStatementTableDataModel.Columns;
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
public class StudentOverviewController extends BasicController implements Activateable2 {

	private final Link next, previous, backLink;
	private final Link homeLink, contactLink;
	private final TextComponent detailsCmp;
	private final TableController tableCtr;
	private final VelocityContainer mainVC;
	private final VelocityContainer detailsVC;
	
	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private final ToolbarController toolbar;
	private EfficiencyStatementDetailsController statementCtrl;
	
	private final Identity student;
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public StudentOverviewController(UserRequest ureq, WindowControl wControl, Identity student) {
		super(ureq, wControl);
		
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, isAdministrativeUser);
		
		this.student = student;
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("error.no.found"));
		tableConfig.setDownloadOffered(true);
		tableConfig.setPreferencesOffered(true, "studentCourseListController");
		
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), null, null, null, null, true, getTranslator());
		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("student.name", Columns.studentName.ordinal(), "select", getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.course.name", Columns.repoName.ordinal(), "select", getLocale()));
		tableCtr.addColumnDescriptor(new BooleanColumnDescriptor("table.header.passed", Columns.passed.ordinal(), translate("passed.true"), translate("passed.false")));
		CustomCellRenderer scoreRenderer = new ScoreCellRenderer();
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.score", Columns.score.ordinal(), "select", getLocale(),
				ColumnDescriptor.ALIGNMENT_RIGHT, scoreRenderer));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.progress", Columns.progress.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new ProgressRenderer(true, getTranslator())));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.lastScoreDate", Columns.lastModification.ordinal(), "select", getLocale()));

		listenTo(tableCtr);
		
		List<UserEfficiencyStatement> statements = coachingService.getEfficencyStatements(student);

		TableDataModel<UserEfficiencyStatement> model = new UserEfficiencyStatementTableDataModel(statements);
		tableCtr.setTableDataModel(model);
		
		mainVC = createVelocityContainer("student_course_list");
		detailsVC = createVelocityContainer("student_details");
		
		String fullName = StringHelper.escapeHtml(userManager.getUserDisplayName(student));
		detailsVC.contextPut("studentName", fullName);
		mainVC.put("studentDetails", detailsVC);
		mainVC.put("studentsTable", tableCtr.getInitialComponent());
		
		toolbar = new ToolbarController(ureq, wControl, getTranslator());
		listenTo(toolbar);
		
		mainVC.put("toolbar", toolbar.getInitialComponent());

		previous = toolbar.addToolbarLink("previous", this, Position.center);
		previous.setIconLeftCSS("o_icon o_icon_move_left");
		previous.setCustomDisabledLinkCSS("navbar-text");
		previous.setEnabled(statements.size() > 1);
		detailsCmp = toolbar.addToolbarText("details", this, Position.center);
		
		next = toolbar.addToolbarLink("next", this, Position.center);
		next.setIconRightCSS("o_icon o_icon_move_right");
		next.setCustomDisabledLinkCSS("navbar-text");
		next.setEnabled(statements.size() > 1);
		
		backLink = toolbar.addToolbarLink("back", this, Position.left);
		backLink.setIconLeftCSS("o_icon o_icon_back");

		contactLink = LinkFactory.createButton("contact.link", detailsVC, this);
		contactLink.setIconLeftCSS("o_icon o_icon_mail");
		detailsVC.put("contact", contactLink);
		
		homeLink = LinkFactory.createButton("home.link", detailsVC, this);
		homeLink.setIconLeftCSS("o_icon o_icon_home");
		detailsVC.put("home", homeLink);

		setDetailsToolbarVisible(false);
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == next) {
			nextEntry(ureq);
		} else if (source == previous) {
			previousEntry(ureq);
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
					UserEfficiencyStatement entry = (UserEfficiencyStatement)tableCtr.getTableDataModel().getObject(e.getRowId());
					selectDetails(ureq, entry);					
				}
			}
		} else if (source == statementCtrl) {
			removeDetails(ureq);
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
			if ("next".equals(event.getCommand())) {
				nextEntry(ureq);
			} else if ("previous".equals(event.getCommand())) {
				previousEntry(ureq);
			} else if ("contact.link".equals(event.getCommand())) {
				contact(ureq);
			} else if ("next.student".equals(event.getCommand())) {
				fireEvent(ureq, event);
			} else if ("previous.student".equals(event.getCommand())) {
				fireEvent(ureq, event);
			} else if ("back".equals(event.getCommand())) {
				back(ureq);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
	}
	
	private void setDetailsToolbarVisible(boolean visible) {
		if(next.isVisible() == visible) return;
		
		next.setVisible(visible);
		previous.setVisible(visible);
		detailsCmp.setVisible(visible);
		backLink.setVisible(visible);
	}
	
	private void back(UserRequest ureq) {
		removeDetails(ureq);
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
	
	private void nextEntry(UserRequest ureq) {
		UserEfficiencyStatement currentEntry = statementCtrl.getEntry().getUserEfficencyStatement();
		int nextIndex = tableCtr.getIndexOfSortedObject(currentEntry) + 1;
		if(nextIndex < 0 || nextIndex >= tableCtr.getRowCount()) {
			nextIndex = 0;
		}
		UserEfficiencyStatement nextEntry = (UserEfficiencyStatement)tableCtr.getSortedObjectAt(nextIndex);
		selectDetails(ureq, nextEntry);
	}
	
	private void previousEntry(UserRequest ureq) {
		UserEfficiencyStatement currentEntry = statementCtrl.getEntry().getUserEfficencyStatement();
		int previousIndex = tableCtr.getIndexOfSortedObject(currentEntry) - 1;
		if(previousIndex < 0 || previousIndex >= tableCtr.getRowCount()) {
			previousIndex = tableCtr.getRowCount() - 1;
		}
		UserEfficiencyStatement previousEntry = (UserEfficiencyStatement)tableCtr.getSortedObjectAt(previousIndex);
		selectDetails(ureq, previousEntry);
	}
	
	private void selectDetails(UserRequest ureq, UserEfficiencyStatement statement) {
		boolean selectAssessmentTool = false;
		if(statementCtrl != null) {
			selectAssessmentTool = statementCtrl.isAssessmentToolSelected();
			removeAsListenerAndDispose(statementCtrl);
		}
		
		EfficiencyStatementEntry entry = coachingService.getEfficencyStatement(statement, userPropertyHandlers, getLocale());
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(RepositoryEntry.class, statement.getCourseRepoKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		statementCtrl = new EfficiencyStatementDetailsController(ureq, bwControl, entry, selectAssessmentTool);
		listenTo(statementCtrl);
		detailsCmp.setText(statement.getShortTitle());

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
