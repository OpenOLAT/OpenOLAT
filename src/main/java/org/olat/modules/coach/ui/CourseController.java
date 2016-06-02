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

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.course.certificate.CertificateEvent;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.IdentityResourceKey;
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
public class CourseController extends FormBasicController implements Activateable2, GenericEventListener {
	
	private final Link backLink, next, previous;
	private final Link nextCourse, previousCourse;
	private final Link openCourse;
	private final TextComponent detailsCmp, detailsCourseCmp;
	
	private FlexiTableElement tableEl;
	private EfficiencyStatementEntryTableDataModel model;
	
	private CloseableModalController cmc;
	private ContactController contactCtrl;
	private final ToolbarController toolbar;
	private EfficiencyStatementDetailsController statementCtrl;
	
	private boolean hasChanged = false;
	
	private final RepositoryEntry course;
	private final CourseStatEntry courseStat;

	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private CertificatesManager certificatesManager;
	
	public CourseController(UserRequest ureq, WindowControl wControl, RepositoryEntry course, CourseStatEntry courseStat, int index, int numOfCourses) {
		super(ureq, wControl, "course");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, isAdministrativeUser);
		
		this.course = course;
		this.courseStat = courseStat;
		
		initForm(ureq);

		List<EfficiencyStatementEntry> entries = loadModel();


		toolbar = new ToolbarController(ureq, wControl, getTranslator());
		listenTo(toolbar);
		flc.getFormItemComponent().put("toolbar", toolbar.getInitialComponent());

		backLink = toolbar.addToolbarLink("back", this, Position.left);
		backLink.setIconLeftCSS("o_icon o_icon_back");

		previous = toolbar.addToolbarLink("previous", this, Position.center);
		previous.setIconLeftCSS("o_icon o_icon_move_left");
		previous.setCustomDisabledLinkCSS("navbar-text");
		previous.setEnabled(entries.size() > 1);
		
		detailsCmp = toolbar.addToolbarText("", this, Position.center);

		next = toolbar.addToolbarLink("next", this, Position.center);
		next.setIconRightCSS("o_icon o_icon_move_right");
		next.setCustomDisabledLinkCSS("navbar-text");
		next.setEnabled(entries.size() > 1);
		
		//courses next,previous
		previousCourse = toolbar.addToolbarLink("previous.course", this, Position.center);
		previousCourse.setIconLeftCSS("o_icon o_icon_move_left");
		previousCourse.setCustomDisabledLinkCSS("navbar-text");
		previousCourse.setEnabled(numOfCourses > 1);
		
		detailsCourseCmp = toolbar.addToolbarText("details.course", "", this, Position.center);
		detailsCourseCmp.setCssClass("navbar-text");
		detailsCourseCmp.setText(translate("students.details", new String[]{
				StringHelper.escapeHtml(course.getDisplayname()),
				Integer.toString(index + 1), Integer.toString(numOfCourses)
		}));
		nextCourse = toolbar.addToolbarLink("next.course", this, Position.center);
		nextCourse.setIconRightCSS("o_icon o_icon_move_right");
		nextCourse.setCustomDisabledLinkCSS("navbar-text");
		nextCourse.setEnabled(numOfCourses > 1);
		
		openCourse = LinkFactory.createButton("open", flc.getFormItemComponent(), this);
		openCourse.setIconLeftCSS("o_icon o_CourseModule_icon");
		flc.getFormItemComponent().put("open.group", openCourse);

		setDetailsToolbarVisible(false);
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), CertificatesManager.ORES_CERTIFICATE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("courseName", StringHelper.escapeHtml(course.getDisplayname()));
		}
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.name, "select"));
		}
		
		int colIndex = UserListController.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(UserListController.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, "select", false, null));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.repoName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.passed));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.certificate, new DownloadCertificateCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.score, new ScoreCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.lastModification));
		
		
		model = new EfficiencyStatementEntryTableDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setEmtpyTableMessageKey("error.no.found");
		tableEl.setAndLoadPersistedPreferences(ureq, "fCourseController");
		
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
			if(course.getOlatResource().getKey().equals(ce.getResourceKey())) {
				updateCertificate(ce.getCertificateKey());
			}
		}
	}
	
	private void updateCertificate(Long certificateKey) {
		CertificateLight certificate = certificatesManager.getCertificateLightById(certificateKey);
		model.putCertificate(certificate);
	}
	
	public CourseStatEntry getEntry() {
		return courseStat;
	}
	
	public List<EfficiencyStatementEntry> loadModel() {
		List<EfficiencyStatementEntry> entries = coachingService.getCourse(getIdentity(), course, userPropertyHandlers, getLocale());
		
		Long resourceKey = course.getOlatResource().getKey();
		List<CertificateLight> certificates = certificatesManager.getLastCertificates(course.getOlatResource());
		ConcurrentMap<IdentityResourceKey, CertificateLight> certificateMap = new ConcurrentHashMap<>();
		for(CertificateLight certificate:certificates) {
			IdentityResourceKey key = new IdentityResourceKey(certificate.getIdentityKey(), resourceKey);
			certificateMap.put(key, certificate);
		}
		
		model.setObjects(entries, certificateMap);
		tableEl.reloadData();
		tableEl.reset();
		return entries;
	}
	
	private void reloadModel() {
		if(hasChanged) {
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
		if (source == next) {
			nextEntry(ureq);
		} else if (source == previous) {
			previousEntry(ureq);
		} else if (source == backLink) {
			back(ureq);
		} else if (source == openCourse) {
			openCourse(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == statementCtrl) {
			if(event == Event.CHANGED_EVENT) {
				hasChanged = true;
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else {
				removeDetails(ureq);
			}
		} else if (source == cmc) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(contactCtrl);
			cmc = null;
			contactCtrl = null;
		} else if (source == toolbar) {
			if("back".equals(event.getCommand())) {
				reloadModel();
				back(ureq);
			} else if("next".equals(event.getCommand())) {
				nextEntry(ureq);
			} else if("previous".equals(event.getCommand())) {
				previousEntry(ureq);
			} else if("contact.link".equals(event.getCommand())) {
				contact(ureq);
			} else if ("next.course".equals(event.getCommand())) {
				fireEvent(ureq, event);
			} else if ("previous.course".equals(event.getCommand())) {
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
		if("Identity".equals(ores.getResourceableTypeName())) {
			Long identityKey = ores.getResourceableId();
			for(EfficiencyStatementEntry entry:model.getObjects()) {
				if(identityKey.equals(entry.getIdentityKey())) {
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

		nextCourse.setVisible(!visible);
		previousCourse.setVisible(!visible);
		detailsCourseCmp.setVisible(!visible);
	}
	
	private void contact(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		if(statementCtrl != null) {
			contactCtrl = new ContactController(ureq, getWindowControl());
			cmc = new CloseableModalController(getWindowControl(), translate("close"), contactCtrl.getInitialComponent());
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void back(UserRequest ureq) {
		if(statementCtrl == null) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else {
			removeDetails(ureq);
		}
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
	
	private void nextEntry(UserRequest ureq) {
		EfficiencyStatementEntry currentEntry = statementCtrl.getEntry();
		int nextIndex = model.getObjects().indexOf(currentEntry) + 1;
		if(nextIndex < 0 || nextIndex >= model.getRowCount()) {
			nextIndex = 0;
		}
		EfficiencyStatementEntry nextEntry = model.getObject(nextIndex);
		selectDetails(ureq, nextEntry);
	}
	
	private void removeDetails(UserRequest ureq) {
		flc.getFormItemComponent().remove(statementCtrl.getInitialComponent());	
		removeAsListenerAndDispose(statementCtrl);
		statementCtrl = null;
		setDetailsToolbarVisible(false);
		addToHistory(ureq);
	}
	
	private void selectDetails(UserRequest ureq,  EfficiencyStatementEntry entry) {
		boolean selectAssessmentTool = false;
		if(statementCtrl != null) {
			selectAssessmentTool = statementCtrl.isAssessmentToolSelected();
			removeAsListenerAndDispose(statementCtrl);
		}

		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, entry.getIdentityKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		statementCtrl = new EfficiencyStatementDetailsController(ureq, bwControl, entry, selectAssessmentTool);
		listenTo(statementCtrl);
		
		flc.getFormItemComponent().put("efficiencyDetails", statementCtrl.getInitialComponent());
		
		int index = model.getObjects().indexOf(entry) + 1;
		String details = translate("students.details", new String[] {
				StringHelper.escapeHtml(entry.getIdentityKey().toString()),//TODO user props
				String.valueOf(index), String.valueOf(model.getRowCount())
		});
		detailsCmp.setText(details);
		setDetailsToolbarVisible(true);
	}
	
	private void openCourse(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("RepositoryEntry", courseStat.getRepoKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(getWindowControl(), ores);
		NewControllerFactory.getInstance().launch(ureq, bwControl);
	}
}
