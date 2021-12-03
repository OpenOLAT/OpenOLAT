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

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
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
import org.olat.course.CourseFactory;
import org.olat.course.certificate.CertificateEvent;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.course.config.CourseConfig;
import org.olat.modules.assessment.AssessmentEntryCompletion;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.IdentityRepositoryEntryKey;
import org.olat.modules.coach.model.IdentityResourceKey;
import org.olat.modules.coach.ui.EfficiencyStatementEntryTableDataModel.Columns;
import org.olat.modules.coach.ui.UserDetailsController.Segment;
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
public class CourseController extends FormBasicController implements Activateable2, GenericEventListener, TooledController {
	
	private FormLink openCourse;
	private Link nextCourse;
	private Link previousCourse;

	private FlexiTableElement tableEl;
	private EfficiencyStatementEntryTableDataModel model;
	private final TooledStackedPanel stackPanel;
	private UserDetailsController statementCtrl;
	
	private boolean hasChanged = false;
	private int index;
	private int numOfCourses;
	
	private final RepositoryEntry course;
	private final CourseStatEntry courseStat;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private AssessmentService assessmentService;
	
	public CourseController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry course, CourseStatEntry courseStat, int index, int numOfCourses) {
		super(ureq, wControl, "course");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, isAdministrativeUser);
		
		this.course = course;
		this.courseStat = courseStat;
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.index = index;
		this.numOfCourses = numOfCourses;
		
		initForm(ureq);
		loadModel();
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), CertificatesManager.ORES_CERTIFICATE_EVENT);
	}

	@Override
	public void initTools() {
		//courses next,previous
		previousCourse = LinkFactory.createToolLink("previous.course", translate("previous.course"), this);
		previousCourse.setIconLeftCSS("o_icon o_icon_previous");
		previousCourse.setEnabled(numOfCourses > 1);
		stackPanel.addTool(previousCourse);
		
		String details = translate("students.details", new String[]{
				StringHelper.escapeHtml(course.getDisplayname()),
				Integer.toString(index + 1), Integer.toString(numOfCourses)
		});
		
		Link detailsCourseCmp = LinkFactory.createToolLink("details.course", details, this);
		detailsCourseCmp.setIconLeftCSS("o_icon o_CourseModule_icon");
		stackPanel.addTool(detailsCourseCmp);
		
		nextCourse = LinkFactory.createToolLink("next.course", translate("next.course"), this);
		nextCourse.setIconLeftCSS("o_icon o_icon_next");
		nextCourse.setEnabled(numOfCourses > 1);
		stackPanel.addTool(nextCourse);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		openCourse = uifactory.addFormLink("open.course", formLayout, Link.BUTTON);
		openCourse.setIconLeftCSS("o_icon o_CourseModule_icon");
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("courseName", StringHelper.escapeHtml(course.getDisplayname()));
		}
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = UserListController.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(UserListController.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, "select",
					true, userPropertyHandler.i18nColumnDescriptorLabelKey()));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.repoName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.completion, new LearningProgressCompletionCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.passed, new PassedCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.score, new ScoreCellRenderer()));
		CourseConfig courseConfig = CourseFactory.loadCourse(course).getCourseConfig();
		if(courseConfig.isCertificateEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.certificate, new DownloadCertificateCellRenderer(getLocale())));
			if(courseConfig.isRecertificationEnabled()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.recertification, new DateFlexiCellRenderer(getLocale())));
			}
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.lastModification));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.lastUserModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.lastCoachModified));
		
		model = new EfficiencyStatementEntryTableDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setEmptyTableSettings("default.tableEmptyMessage", null, "o_icon_user");
		tableEl.setAndLoadPersistedPreferences(ureq, "fCourseController-v2");
	}

	@Override
	protected void doDispose() {
		stackPanel.removeListener(this);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, CertificatesManager.ORES_CERTIFICATE_EVENT);
        super.doDispose();
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
		
		ConcurrentMap<IdentityRepositoryEntryKey, Double> completionsMap = new ConcurrentHashMap<>();
		List<Long> identityKeys = entries.stream().map(EfficiencyStatementEntry::getIdentityKey).collect(Collectors.toList());
		List<AssessmentEntryCompletion> completions = assessmentService.loadAvgCompletionsByIdentities(course, identityKeys);
		for (AssessmentEntryCompletion completion : completions) {
			IdentityRepositoryEntryKey key = new IdentityRepositoryEntryKey(completion.getKey(), course.getKey());
			if (completion.getCompletion() != null) {
				completionsMap.put(key, completion.getCompletion());
				
			}
		}
		
		model.setObjects(entries, certificateMap, completionsMap, null);
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
					doSelectDetails(ureq, selectedRow);
				}
			}
		} else if (source == openCourse) {
			doOpenCourse(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(nextCourse == source || previousCourse == source) {
			fireEvent(ureq, event);
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
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if("next".equals(event.getCommand())) {
				nextEntry(ureq);
			} else if("previous".equals(event.getCommand())) {
				previousEntry(ureq);
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
					doSelectDetails(ureq, entry);
					statementCtrl.activate(ureq, entries.subList(1, entries.size()), ce.getTransientState());
					break;
				}
			}
		}
	}

	private void previousEntry(UserRequest ureq) {
		EfficiencyStatementEntry currentEntry = statementCtrl.getEntry();
		int previousIndex = model.getObjects().indexOf(currentEntry) - 1;
		if(previousIndex < 0 || previousIndex >= model.getRowCount()) {
			previousIndex = model.getRowCount() - 1;
		}
		EfficiencyStatementEntry previousEntry = model.getObject(previousIndex);
		doSelectDetails(ureq, previousEntry);
	}
	
	private void nextEntry(UserRequest ureq) {
		EfficiencyStatementEntry currentEntry = statementCtrl.getEntry();
		int nextIndex = model.getObjects().indexOf(currentEntry) + 1;
		if(nextIndex < 0 || nextIndex >= model.getRowCount()) {
			nextIndex = 0;
		}
		EfficiencyStatementEntry nextEntry = model.getObject(nextIndex);
		doSelectDetails(ureq, nextEntry);
	}
	
	private void doSelectDetails(UserRequest ureq,  EfficiencyStatementEntry entry) {
		Segment selectedTool = null;
		if(statementCtrl != null) {
			selectedTool = statementCtrl.getSelectedSegment();
			removeAsListenerAndDispose(statementCtrl);
		}

		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, entry.getIdentityKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		int entryIndex = model.getObjects().indexOf(entry) + 1;
		Identity assessedIdentity = securityManager.loadIdentityByKey(entry.getIdentityKey());
		String fullname = userManager.getUserDisplayName(assessedIdentity);
		String details = translate("students.details", new String[] {
				fullname, String.valueOf(entryIndex), String.valueOf(model.getRowCount())
		});
		
		statementCtrl = new UserDetailsController(ureq, bwControl, stackPanel,
				entry, assessedIdentity, details, entryIndex, model.getRowCount(), selectedTool, true, false);
		listenTo(statementCtrl);
		
		stackPanel.popUpToController(this);
		stackPanel.pushController(fullname, statementCtrl);
	}
	
	private void doOpenCourse(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("RepositoryEntry", courseStat.getRepoKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(getWindowControl(), ores);
		NewControllerFactory.getInstance().launch(ureq, bwControl);
	}
}
