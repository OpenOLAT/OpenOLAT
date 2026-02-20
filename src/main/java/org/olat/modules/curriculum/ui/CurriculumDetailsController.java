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
package org.olat.modules.curriculum.ui;

import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_ELEMENT;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_IMPLEMENTATIONS;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_LECTURES;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_METADATA;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_OVERVIEW;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_OWNERS;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_REPORTS;

import java.util.List;

import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.dropdown.DropdownUIFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dashboard.BentoBoxSize;
import org.olat.core.gui.control.generic.dashboard.DashboardController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumManagedFlag;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumInfos;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.curriculum.ui.member.CurriculumUserManagementController;
import org.olat.modules.curriculum.ui.reports.CurriculumReportsController;
import org.olat.modules.curriculum.ui.widgets.CurriculumLectureBlocksWidgetController;
import org.olat.modules.curriculum.ui.widgets.ImplementationWidgetController;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.ui.LectureListRepositoryConfig;
import org.olat.modules.lecture.ui.LectureListRepositoryConfig.Visibility;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumDetailsController extends BasicController implements Activateable2 {

	private static final int TITLE_SIZE = 3;
	
	private int ownersTab;
	private int reportsTab;
	private int lecturesTab;
	private int overviewTab;
	private int metadataTab;
	private int implementationsTab;
	
	private Link deleteButton;
	private Link exportButton;
	private TabbedPane tabPane;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	
	private CloseableModalController cmc;
	private CurriculumReportsController reportsCtrl;
	private EditCurriculumController editMetadataCtrl;
	private DashboardController overviewCtrl;
	private LectureListRepositoryController lectureBlocksCtrl;
	private CurriculumComposerController implementationsCtrl;
	private CurriculumUserManagementController userManagementCtrl;
	private ConfirmDeleteCurriculumController deleteCurriculumCtrl;
	private ImplementationWidgetController implementationWidgetCtrl;
	private CurriculumLectureBlocksWidgetController lectureBlocksWidgetCtrl;
	
	private Curriculum curriculum;
	private final CurriculumSecurityCallback secCallback;
	private final LecturesSecurityCallback lecturesSecCallback;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumDetailsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			Curriculum curriculum, CurriculumSecurityCallback secCallback, LecturesSecurityCallback lecturesSecCallback) {
		super(ureq, wControl);
		this.curriculum = curriculum;
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		this.lecturesSecCallback = lecturesSecCallback;
		
		mainVC = createVelocityContainer("curriculum_details");
		tabPane = new TabbedPane("tabs", getLocale());
		tabPane.addListener(this);
		initTabPane(ureq);
		initMetadata();
		toolbarPanel.addListener(this);
		
		mainVC.put("tabs", tabPane);
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		if(toolbarPanel != null) {
			toolbarPanel.removeListener(this);
		}
		super.doDispose();
	}
	
	public Curriculum getCurriculum() {
		return curriculum;
	}
	
	private void initMetadata() {
		Dropdown commandsDropdown = DropdownUIFactory.createMoreDropdown("more", getTranslator());
		commandsDropdown.setDomReplaceable(false);
		commandsDropdown.setButton(true);
		commandsDropdown.setOrientation(DropdownOrientation.right);
		
		if(secCallback.canExportCurriculum(curriculum)) {
			exportButton = LinkFactory.createCustomLink("export", "export", "export", Link.LINK, mainVC, this);
			exportButton.setIconLeftCSS("o_icon o_icon-fw o_icon_export");
			commandsDropdown.addComponent(exportButton);
		}
		if(secCallback.canDeleteCurriculum() && !CurriculumManagedFlag.isManaged(curriculum, CurriculumManagedFlag.delete)) {
			if(!commandsDropdown.isEmpty()) {
				commandsDropdown.addComponent(new Spacer("delete-space"));
			}
			deleteButton = LinkFactory.createCustomLink("delete", "delete", "delete", Link.LINK, mainVC, this);
			deleteButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			commandsDropdown.addComponent(deleteButton);
		}
		commandsDropdown.setVisible(!commandsDropdown.isEmpty());
		mainVC.put(commandsDropdown.getComponentName(), commandsDropdown);

		mainVC.contextPut("curriculumDisplayName", curriculum.getDisplayName());
		if(StringHelper.containsNonWhitespace(curriculum.getIdentifier())) {
			mainVC.contextPut("curriculumExternalRef", curriculum.getIdentifier());
		}
	}

	private void initTabPane(UserRequest ureq) {
		// Overview
		overviewTab = tabPane.addTab(ureq, translate("curriculum.overview"), uureq -> createDashboard(uureq).getInitialComponent());

		// Implementations
		implementationsTab = tabPane.addTab(ureq, translate("curriculum.implementations"), "o_sel_curriculum_composer", uureq -> {
			CurriculumComposerConfig config = CurriculumComposerConfig.curriculumView();
			config.setTitle(translate("curriculum.implementations"), TITLE_SIZE, "o_icon_curriculum_implementations");
			config.setDefaultNumOfParticipants(true);
			config.setWithMixMaxColumn(true);
			config.setImplementationsOnly(true);
			config.setFlat(true);
			WindowControl subControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CONTEXT_IMPLEMENTATIONS), null);
			implementationsCtrl = new CurriculumComposerController(uureq, subControl, toolbarPanel,
					curriculum, null, config, secCallback, lecturesSecCallback);
			listenTo(implementationsCtrl);
			
			List<ContextEntry> all = BusinessControlFactory.getInstance().createCEListFromString("[Relevant:0]");
			implementationsCtrl.activate(uureq, all, null);
			return implementationsCtrl.getInitialComponent();
		}, false);
		
		// Events / lectures blocks
		if(lectureModule.isEnabled()) {
			lecturesTab = tabPane.addTab(ureq, translate("tab.lectureblocks"), uureq -> {
				WindowControl subControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CONTEXT_LECTURES), null);
				LectureListRepositoryConfig config = LectureListRepositoryConfig.curriculumConfig("curriculum-details-v1.1")
						.withExternalRef(Visibility.HIDE)
						.withCurriculum(Visibility.HIDE)
						.withRepositoryEntry(Visibility.SHOW)
						.withLocation(Visibility.SHOW)
						.withCompulsoryPresence(Visibility.HIDE)
						.withNumberOfParticipants(Visibility.HIDE)
						.withNumberOfLectures(Visibility.HIDE)
						.withExam(Visibility.HIDE)
						.withOnlineMeeting(Visibility.HIDE)
						.withEdit(Visibility.HIDE)
						.withRollCall(Visibility.NO)
						.withAllMineSwitch(false, false)
						.withFilterPresetWithoutTeachers(true)
						.withDetailsParticipantsGroups(true)
						.withDetailsRepositoryEntry(true)
						.withDetailsExam(false)
						.withDetailsUnits(true)
						.withDetailsExternalRef(true)
						.withinCurriculums(true);
				LecturesSecurityCallback curriculumLecturesSecCallback = evaluateLecturesSecurityCallback();
				lectureBlocksCtrl = new LectureListRepositoryController(uureq, subControl, toolbarPanel, curriculum, config,
						curriculumLecturesSecCallback, secCallback);
				listenTo(lectureBlocksCtrl);

				List<ContextEntry> allFilter = BusinessControlFactory.getInstance().createCEListFromString("[All:0]");
				lectureBlocksCtrl.activate(uureq, allFilter, null);
				return lectureBlocksCtrl.getInitialComponent();
			});
		}
		
		// User management
		ownersTab = tabPane.addTab(ureq, translate("tab.owner.management"), uureq -> {
			WindowControl subControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CONTEXT_OWNERS), null);
			userManagementCtrl = new CurriculumUserManagementController(uureq, subControl, curriculum, secCallback);
			listenTo(userManagementCtrl);
			return userManagementCtrl.getInitialComponent();
		});
		
		// Metadata
		metadataTab = tabPane.addTab(ureq, translate("curriculum.metadata"), "o_sel_curriculum_metadata", uureq -> {
			WindowControl subControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CONTEXT_METADATA), null);
			editMetadataCtrl = new EditCurriculumController(uureq, subControl, curriculum, secCallback);
			listenTo(editMetadataCtrl);
			return editMetadataCtrl.getInitialComponent();
		}, true);
		
		// Reports
		if(secCallback.canCurriculumReports(curriculum)) {
			reportsTab = tabPane.addTab(ureq, translate("curriculum.reports"), "o_sel_curriculum_reports", uureq -> {
				WindowControl subControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CONTEXT_REPORTS), null);
				reportsCtrl = new CurriculumReportsController(uureq, subControl, null, curriculum, null, ArchiveType.CURRICULUM, TITLE_SIZE);
				listenTo(reportsCtrl);
				return reportsCtrl.getInitialComponent();
			}, true);
		}
	}
	
	private DashboardController createDashboard(UserRequest ureq) {
		removeAsListenerAndDispose(lectureBlocksWidgetCtrl);
		removeAsListenerAndDispose(implementationWidgetCtrl);
		removeAsListenerAndDispose(overviewCtrl);
		
		overviewCtrl = new DashboardController(ureq, getWindowControl());
		overviewCtrl.setDashboardCss("o_curriculum_overview");
		listenTo(overviewCtrl);
		
		implementationWidgetCtrl = new ImplementationWidgetController(ureq, getWindowControl(), curriculum, secCallback);
		listenTo(implementationWidgetCtrl);
		overviewCtrl.addWidget("implementations", implementationWidgetCtrl, BentoBoxSize.box_4_1);
		
		if(lectureModule.isEnabled()) {
			lectureBlocksWidgetCtrl = new CurriculumLectureBlocksWidgetController(ureq, getWindowControl(),
					curriculum);
			lectureBlocksWidgetCtrl.reload();
			listenTo(lectureBlocksWidgetCtrl);
			overviewCtrl.addWidget("lectures", lectureBlocksWidgetCtrl, BentoBoxSize.box_4_1);
		}
		return overviewCtrl;
	}
	
	private LecturesSecurityCallback evaluateLecturesSecurityCallback() {
		return secCallback.canEditCurriculum(curriculum)
				? lecturesSecCallback
				: lecturesSecCallback.readOnlyCopy();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(CONTEXT_IMPLEMENTATIONS.equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			tabPane.setSelectedPane(ureq, implementationsTab);
			if(implementationsCtrl != null) {
				implementationsCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if(CONTEXT_LECTURES.equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			tabPane.setSelectedPane(ureq, lecturesTab);
			if(lectureBlocksCtrl != null) {
				lectureBlocksCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if(CONTEXT_OVERVIEW.equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, overviewTab);
		} else if(CONTEXT_ELEMENT.equalsIgnoreCase(type) || "CurriculumElement".equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, implementationsTab);
			if(implementationsCtrl != null) {
				implementationsCtrl.activate(ureq, entries, state);
			}
		} else if(CONTEXT_METADATA.equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, metadataTab);
		} else if(CONTEXT_OWNERS.equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, ownersTab);
		} else if(CONTEXT_REPORTS.equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, reportsTab);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(overviewCtrl == source || lectureBlocksWidgetCtrl == source || lectureBlocksCtrl == source
				|| implementationWidgetCtrl == source) {
			if(event instanceof ActivateEvent ae) {
				activate(ureq, ae.getEntries(), null);
			}
		} else if(deleteCurriculumCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolbarPanel.popController(this);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(deleteCurriculumCtrl);
		removeAsListenerAndDispose(cmc);
		deleteCurriculumCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(deleteButton == source) {
			doConfirmDeleteCurriculum(ureq);
		} else if(exportButton == source) {
			doExport(ureq);
		} else if(toolbarPanel == source) {
			if(event instanceof PopEvent pe) {
				doProcessPopEvent(ureq, pe);
			}
		}
	}
	
	private void doProcessPopEvent(UserRequest ureq, PopEvent pe) {
		if(pe.getUserObject() instanceof CurriculumElement || pe.getController() instanceof CurriculumElementDetailsController) {	
			final Object uobject = toolbarPanel.getLastUserObject();
			final Controller uctrl = toolbarPanel.getLastController();
			
			toolbarPanel.popUpToController(this);
			if(uctrl == this) {
				// Only pop up to this
			} else {
				CurriculumElement elementToOpen = null;
				if(uobject instanceof CurriculumElement curriculumElement) {
					elementToOpen = curriculumElement;
				} else if(pe.getController() instanceof CurriculumElementDetailsController detailsCtrl) {
					elementToOpen = detailsCtrl.getCurriculumElement();
				}
				
				if(elementToOpen != null) {
					tabPane.setSelectedPane(ureq, implementationsTab);
					if(implementationsCtrl != null) {
						List<ContextEntry> entries = BusinessControlFactory.getInstance()
								.createCEListFromResourceable(elementToOpen, null);
						implementationsCtrl.activate(ureq, entries, null);
					}
				}
			}
		}
	}
	
	private void doConfirmDeleteCurriculum(UserRequest ureq) {
		removeAsListenerAndDispose(deleteCurriculumCtrl);
		removeAsListenerAndDispose(cmc);
		
		CurriculumInfos curriculumToDelete = curriculumService.getCurriculumWithInfos(curriculum);
		if(curriculumToDelete == null || curriculumToDelete.curriculum() == null) {
			showWarning("warning.curriculum.deleted");
		} else {
			List<CurriculumElement> implementations = curriculumService.getImplementations(curriculum, CurriculumElementStatus.notDeleted());
			if(implementations.isEmpty()) {
				deleteCurriculumCtrl = new ConfirmDeleteCurriculumController(ureq, getWindowControl(),
						translate("confirmation.delete.curriculum.text", StringHelper.escapeHtml(curriculum.getDisplayName())),
						translate("confirmation.delete.curriculum"),
						translate("delete"), curriculumToDelete.curriculum());
				listenTo(deleteCurriculumCtrl);

				String title = translate("delete.curriculum.title", StringHelper.escapeHtml(curriculum.getDisplayName()));
				cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteCurriculumCtrl.getInitialComponent(), true, title);
				listenTo(cmc);
				cmc.activate();
			} else {
				showWarning("warning.curriculum.implementations", StringHelper.escapeHtml(curriculumToDelete.curriculum().getDisplayName()));
			}
		}
	}
	
	private void doExport(UserRequest ureq) {
		List<ContextEntry> entries = getWindowControl().getBusinessControl().getEntries();
		String url = BusinessControlFactory.getInstance().getAsURIString(entries, true);
		
		List<Curriculum> curriculums = List.of(curriculum);
		Roles roles = ureq.getUserSession().getRoles();
		CurriculumExport export = new CurriculumExport(curriculums, getIdentity(), roles, url, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(export.createMediaResource());
	}
}
