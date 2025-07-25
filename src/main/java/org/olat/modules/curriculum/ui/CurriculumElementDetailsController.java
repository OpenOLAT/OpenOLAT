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

import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_ABSENCES;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_ELEMENT;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_IMPLEMENTATIONS;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_LECTURES;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_MEMBERS;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_METADATA;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_OFFERS;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_OVERVIEW;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_RESOURCES;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_STRUCTURE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.dropdown.DropdownUIFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.curriculum.ui.event.CurriculumElementEvent;
import org.olat.modules.curriculum.ui.event.SelectCurriculumElementRowEvent;
import org.olat.modules.curriculum.ui.lectures.CurriculumElementLecturesController;
import org.olat.modules.curriculum.ui.member.CurriculumElementUserManagementController;
import org.olat.modules.curriculum.ui.reports.CurriculumReportsController;
import org.olat.modules.curriculum.ui.widgets.CoursesWidgetController;
import org.olat.modules.curriculum.ui.widgets.LectureBlocksWidgetController;
import org.olat.modules.curriculum.ui.widgets.MembersWidgetController;
import org.olat.modules.curriculum.ui.widgets.OffersWidgetController;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.ui.LectureListRepositoryConfig;
import org.olat.modules.lecture.ui.LectureListRepositoryConfig.Visibility;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementDetailsController extends BasicController implements Activateable2 {

	private static final int TITLE_SIZE = 3;
	
	private int overviewTab;
	private int lecturesTab;
	private int resourcesTab;
	private int structuresTab;
	private int userManagerTab;
	private int absencesTab;
	private int offersTab;
	private int metadataTab;

	private Dropdown statusDropdown;
	private Link nextButton;
	private Link deleteButton;
	private Link previousButton;
	private Link structureButton;
	private Link nextImplementationButton;
	private Link previousImplementationButton;
	private TabbedPane tabPane;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	
	private CloseableModalController cmc;
	private CurriculumReportsController reportsCtrl;
	private OffersWidgetController offersWidgetCtrl;
	private CoursesWidgetController coursesWidgetCtrl;
	private MembersWidgetController membersWidgetCtrl;
	private CurriculumComposerController structureCtrl;
	private CurriculumDashboardController overviewCtrl;
	private CurriculumElementOffersController offersCtrl;
	private EditCurriculumElementController editMetadataCtrl;
	private CurriculumElementLecturesController absencesCtrl;
	private LectureListRepositoryController lectureBlocksCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CurriculumElementResourcesController resourcesCtrl;
	private LectureBlocksWidgetController lectureBlocksWidgetCtrl;
	private CurriculumElementStatusChangeController statusChangeCtrl;
	private CurriculumElementUserManagementController userManagementCtrl;
	private CurriculumStructureCalloutController curriculumStructureCalloutCtrl;
	private ConfirmDeleteCurriculumElementController deleteCurriculumElementCtrl;
	
	private Curriculum curriculum;
	private final boolean canChildren;
	private boolean canRepositoryEntries;
	private CurriculumElement curriculumElement;
	private final CurriculumElement implementationElement;
	private final CurriculumSecurityCallback secCallback;
	private final LecturesSecurityCallback lecturesSecCallback;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private CatalogV2Module catalogV2Module;
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumElementDetailsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			Curriculum curriculum, CurriculumElement curriculumElement,
			CurriculumSecurityCallback secCallback, LecturesSecurityCallback lecturesSecCallback) {
		super(ureq, wControl);
		this.curriculum = curriculum;
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		this.curriculumElement = curriculumElement;
		this.lecturesSecCallback = lecturesSecCallback;
		canChildren = canChildren(curriculumElement);
		canRepositoryEntries = canRepositoryEntries(curriculumElement);
		implementationElement = getRootElement();
		
		mainVC = createVelocityContainer("curriculum_element_details");
		tabPane = new TabbedPane("tabs", getLocale());
		tabPane.addListener(this);
		initTabPane(ureq);
		initMetadata();
		initStructure();
		
		mainVC.put("tabs", tabPane);
		putInitialPanel(mainVC);
	}
	
	private boolean canChildren(CurriculumElement element) {
		CurriculumElementType type = element.getType();
		if(type != null && type.isSingleElement()) {
			return curriculumService.hasCurriculumElementChildren(element);
		}
		return true;
	}
	
	private boolean canRepositoryEntries(CurriculumElement element) {
		CurriculumElementType type = element.getType();
		if(type != null && type.getMaxRepositoryEntryRelations() == 0) {
			return curriculumService.hasRepositoryEntries(element);
		}
		return true;
	}
	
	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	private void initStructure() {
		structureButton = LinkFactory.createCustomLink("structure", "structure", "structure.goto", Link.BUTTON, mainVC, this);
		structureButton.setIconLeftCSS("o_icon o_icon-fw o_icon_structure");

		previousButton = LinkFactory.createCustomLink("structure.previous", "previous", "", Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
		previousButton.setIconLeftCSS("o_icon o_icon-fw o_icon_slide_up");
		previousButton.setTitle(translate("structure.previous"));
		
		nextButton = LinkFactory.createCustomLink("structure.next", "next", "", Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
		nextButton.setIconLeftCSS("o_icon o_icon-fw o_icon_slide_down");
		nextButton.setTitle(translate("structure.next"));
		
		previousImplementationButton = LinkFactory.createCustomLink("structure.implementation.previous", "previous-impl", "", Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
		previousImplementationButton.setIconLeftCSS("o_icon o_icon-fw o_icon_slide_backward");
		previousImplementationButton.setTitle(translate("structure.implementation.previous"));
		
		nextImplementationButton = LinkFactory.createCustomLink("structure.implementation.next", "next-impl", "", Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
		nextImplementationButton.setIconLeftCSS("o_icon o_icon-fw o_icon_slide_forward");
		nextImplementationButton.setTitle(translate("structure.implementation.next"));
		
		NextPrevious nextPrevious = nextPreviousCurriculumElements(implementationElement);
		nextButton.setEnabled(nextPrevious.next() != null);
		nextButton.setUserObject(nextPrevious.next());
		previousButton.setEnabled(nextPrevious.previous() != null);
		previousButton.setUserObject(nextPrevious.previous());
		
		NextPrevious nextPreviousImplementations = nextPreviousImplementations(implementationElement);
		nextImplementationButton.setEnabled(nextPreviousImplementations.next() != null);
		nextImplementationButton.setUserObject(nextPreviousImplementations.next());
		previousImplementationButton.setEnabled(nextPreviousImplementations.previous() != null);
		previousImplementationButton.setUserObject(nextPreviousImplementations.previous());
	}
	
	private record NextPrevious(CurriculumElement next, CurriculumElement previous) {
		//
	}
	
	private NextPrevious nextPreviousImplementations(CurriculumElement rootElement) {
		CurriculumElement next = null;
		CurriculumElement previous = null;
		
		List<CurriculumElement> implementations = curriculumService.getImplementations(curriculum);
		List<CurriculumElementRow> rows = new ArrayList<>(implementations.size());
		for(CurriculumElement element:implementations) {
			CurriculumElementStatus status = element.getElementStatus();
			if(status != CurriculumElementStatus.deleted) {
				rows.add(new CurriculumElementRow(element));
			}
		}
		
		if(rows.size() > 1) {
			Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
			
			int index = 0;
			for(int i=0; i<rows.size(); i++) {
				if(rootElement.getKey().equals(rows.get(i).getKey())) {
					index = i;
					break;
				}
			}
			
			if(index - 1 >= 0) {
				previous = rows.get(index - 1).getCurriculumElement();
			}
			if(index + 1 < rows.size()) {
				next = rows.get(index + 1).getCurriculumElement();
			}
		}
		
		return new NextPrevious(next, previous);
	}
	
	private NextPrevious nextPreviousCurriculumElements(CurriculumElement rootElement) {
		CurriculumElement next = null;
		CurriculumElement previous = null;
		// Load and evaluate implementation tree
		List<CurriculumElementRow> elements = buildCurriculumPartialTree(rootElement);
		if(elements.size() > 1) {
			int index = 0;
			for(int i=0; i<elements.size(); i++) {
				if(curriculumElement.getKey().equals(elements.get(i).getKey())) {
					index = i;
					break;
				}
			}
			
			if(index - 1 >= 0) {
				previous = elements.get(index - 1).getCurriculumElement();
			}
			if(index + 1 < elements.size()) {
				next = elements.get(index + 1).getCurriculumElement();
			}
		}
		return new NextPrevious(next, previous);
	}
	
	private List<CurriculumElementRow> buildCurriculumPartialTree(CurriculumElement rootElement) {
		List<CurriculumElement> elements = curriculumService.getCurriculumElementsDescendants(rootElement);
		if(rootElement != null && !elements.contains(rootElement)) {
			elements.add(rootElement);
		}
		
		List<CurriculumElementRow> rows = new ArrayList<>(elements.size());
		Map<Long, CurriculumElementRow> keyToRows = new HashMap<>();
		for(CurriculumElement element:elements) {
			CurriculumElementStatus status = element.getElementStatus();
			if(status != CurriculumElementStatus.deleted) {
				CurriculumElementRow row = new CurriculumElementRow(element);
				rows.add(row);
				keyToRows.put(element.getKey(), row);
			}
		}
		// Build parent line
		for(CurriculumElementRow row:rows) {
			if(row.getParentKey() != null) {
				row.setParent(keyToRows.get(row.getParentKey()));
			}
		}
		
		if(rows.size() > 1) {
			Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
		}
		return rows;
	}
	
	private void initMetadata() {
		statusDropdown = new Dropdown("status", null, false, getTranslator());
		statusDropdown.setOrientation(DropdownOrientation.right);
		statusDropdown.setEmbbeded(true);
		statusDropdown.setLabeled(true, true);
		mainVC.put("status", statusDropdown);
		updateStatusDropdown();
		
		if(secCallback.canEditCurriculumElement(curriculumElement)
				&& !CurriculumElementManagedFlag.isManaged(curriculumElement, CurriculumElementManagedFlag.delete)) {
			Dropdown commandsDropdown = DropdownUIFactory.createMoreDropdown("more", getTranslator());
			commandsDropdown.setDomReplaceable(false);
			commandsDropdown.setButton(true);
			commandsDropdown.setOrientation(DropdownOrientation.right);
			
			deleteButton = LinkFactory.createCustomLink("delete", "delete", "delete", Link.LINK, mainVC, this);
			deleteButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			commandsDropdown.addComponent(deleteButton);

			mainVC.put(commandsDropdown.getComponentName(), commandsDropdown);
		}
		
		updateMetadataUI();
	}

	private void updateMetadataUI() {
		mainVC.contextPut("level", getLevel());
		mainVC.contextPut("displayName", curriculumElement.getDisplayName());
		if(StringHelper.containsNonWhitespace(curriculumElement.getIdentifier())) {
			mainVC.contextPut("externalRef", curriculumElement.getIdentifier());
		}
		
		if(curriculum != null) {
			String avatar;
			if(StringHelper.containsNonWhitespace(curriculum.getIdentifier())) {
				avatar = curriculum.getIdentifier();
			} else {
				avatar = curriculum.getDisplayName();
			}
			mainVC.contextPut("curriculumExternalRef", avatar);
		}

		if(curriculumElement.getType() != null) {
			mainVC.contextPut("typeDisplayName", curriculumElement.getType().getDisplayName());
		}
		
		Formatter formatter = Formatter.getInstance(getLocale());
		StringBuilder dates = new StringBuilder();
		if(curriculumElement.getBeginDate() != null) {
			dates.append(formatter.formatDate(curriculumElement.getBeginDate()));
		}
		if(curriculumElement.getEndDate() != null) {
			if(!dates.isEmpty()) dates.append(" \u2013 ");
			dates.append(formatter.formatDate(curriculumElement.getEndDate()));
		}
		mainVC.contextPut("dates", dates.toString());
	}
	
	private void updateRepositoryEntries() {
		boolean updatedCanRepositoryEntries = canRepositoryEntries(curriculumElement);
		if(canRepositoryEntries != updatedCanRepositoryEntries) {
			tabPane.setVisible(resourcesTab, updatedCanRepositoryEntries);
			if(coursesWidgetCtrl != null) {
				coursesWidgetCtrl.getInitialComponent().setVisible(updatedCanRepositoryEntries);
			}
			canRepositoryEntries = updatedCanRepositoryEntries;
		}
	}
	
	private void updateStatusDropdown() {
		if (statusDropdown == null || curriculumElement == null) {
			return;
		}
		
		boolean canChangeStatus = CurriculumElementStatus.deleted != curriculumElement.getElementStatus()
				&& secCallback.canEditCurriculumElement(curriculumElement)
				&& !CurriculumElementManagedFlag.isManaged(curriculumElement, CurriculumElementManagedFlag.status);
		
		CurriculumElementStatus currentStatus = curriculumElement.getElementStatus();
		statusDropdown.setIconCSS("o_icon o_icon_curriculum_status_" + currentStatus);
		statusDropdown.setInnerText(translate("status." + currentStatus));
		statusDropdown.setToggleCSS("o_labeled o_curriculum_status_" + currentStatus);
		statusDropdown.removeAllComponents();
		
		if (!canChangeStatus) {
			return;
		}
		
		for (CurriculumElementStatus elementStatus : CurriculumElementStatus.selectableAdmin()) {
			if (isSelecteable(elementStatus)) {
				Link statusLink = LinkFactory.createCustomLink("status." + elementStatus, "status", "status." + elementStatus, Link.LINK, mainVC, this);
				statusLink.setIconLeftCSS("o_icon o_icon-fw o_icon_curriculum_status_" + elementStatus);
				statusLink.setElementCssClass("o_labeled o_curriculum_status_" + elementStatus);
				statusLink.setUserObject(elementStatus);
				statusDropdown.addComponent(statusLink);
			}
		}
	}
	
	private boolean isSelecteable(CurriculumElementStatus status) {
		if (curriculumElement.getElementStatus() == status) {
			return false;
		}
		if (curriculumElement.getParent() == null && CurriculumElementStatus.active == status) {
			return false;
		}
		if (curriculumElement.getParent() != null && (CurriculumElementStatus.provisional == status || CurriculumElementStatus.confirmed == status)) {
			return false;
		}
		
		return true;
	}
	
	private String getLevel() {
		return curriculumElement.getParent() == null ? null : curriculumElement.getNumberImpl();
	}

	private void initTabPane(UserRequest ureq) {
		overviewTab = tabPane.addTab(ureq, translate("curriculum.overview"), uureq -> createDashBoard(uureq).getInitialComponent());
		
		// Implementations
		if(canChildren) {
			structuresTab = tabPane.addTab(ureq, translate("curriculum.structure"), "o_sel_curriculum_composer", uureq -> {
				CurriculumComposerConfig config = new CurriculumComposerConfig();
				config.setTitle(translate("curriculum.structure"), TITLE_SIZE, "o_icon_structure");
				config.setDefaultNumOfParticipants(true);
				config.setRootElementsOnly(false);
				config.setFlat(false);
				
				WindowControl subControl = addToHistory(uureq, OresHelper
						.createOLATResourceableType(CurriculumListManagerController.CONTEXT_STRUCTURE), null);
				structureCtrl = new CurriculumComposerController(uureq, subControl, toolbarPanel,
						curriculum, curriculumElement, config, secCallback, lecturesSecCallback);
				listenTo(structureCtrl);
				
				List<ContextEntry> all = BusinessControlFactory.getInstance().createCEListFromString("[All:0]");
				structureCtrl.activate(uureq, all, null);
				return structureCtrl.getInitialComponent();
			}, false);
		}
		
		// Courses
		resourcesTab = tabPane.addTab(ureq, translate("tab.resources"), "o_sel_curriculum_resources", uureq -> {
			WindowControl subControl = addToHistory(uureq, OresHelper
					.createOLATResourceableType(CurriculumListManagerController.CONTEXT_RESOURCES), null);
			resourcesCtrl = new CurriculumElementResourcesController(uureq, subControl, curriculumElement, secCallback);
			listenTo(resourcesCtrl);
			return resourcesCtrl.getInitialComponent();
		}, true);
		tabPane.setVisible(resourcesTab, canRepositoryEntries);
		
		// Events / lectures blocks
		if(lectureModule.isEnabled()) {
			lecturesTab = tabPane.addTab(ureq, translate("tab.lectureblocks"), "o_sel_curriculum_lectures", uureq -> {
				WindowControl subControl = addToHistory(uureq, OresHelper
						.createOLATResourceableType(CurriculumListManagerController.CONTEXT_LECTURES), null);
				LectureListRepositoryConfig config = LectureListRepositoryConfig.curriculumElementConfig("curriculum-element-details-v1.1")
						.withExternalRef(Visibility.SHOW)
						.withCurriculum(Visibility.HIDE)
						.withRepositoryEntry(Visibility.HIDE)
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
				lectureBlocksCtrl = new LectureListRepositoryController(uureq, subControl, toolbarPanel, curriculumElement, config, lecturesSecCallback);
				listenTo(lectureBlocksCtrl);
				
				List<ContextEntry> allFilter = BusinessControlFactory.getInstance().createCEListFromString("[Relevant:0]");
				lectureBlocksCtrl.activate(uureq, allFilter, null);
				return lectureBlocksCtrl.getInitialComponent();
			}, true);
		}
		
		// User management
		userManagerTab = tabPane.addTab(ureq, translate("tab.user.management"), "o_sel_curriculum_members", uureq -> {
			WindowControl subControl = addToHistory(uureq, OresHelper
					.createOLATResourceableType(CurriculumListManagerController.CONTEXT_MEMBERS), null);
			userManagementCtrl = new CurriculumElementUserManagementController(uureq, subControl, toolbarPanel, curriculumElement, secCallback);
			listenTo(userManagementCtrl);
			List<ContextEntry> all = BusinessControlFactory.getInstance().createCEListFromString("[Active:0][All:0]");
			userManagementCtrl.activate(uureq, all, null);
			return userManagementCtrl.getInitialComponent();
		}, false);
		
		// Offers
		if (acModule.isEnabled() && catalogV2Module.isEnabled() && curriculumElement.getParent() == null) {
			offersTab = tabPane.addTab(ureq, translate("tab.offers"), "o_sel_curriculum_offers", uureq -> {
				offersCtrl = new CurriculumElementOffersController(uureq, getWindowControl(),
						curriculumElement, secCallback);
				listenTo(offersCtrl);
				return offersCtrl.getInitialComponent();
			}, true);
		}
		
		// Metadata
		metadataTab = tabPane.addTab(ureq, translate("tab.settings"), uureq -> {
			editMetadataCtrl = new EditCurriculumElementController(uureq, getWindowControl(), toolbarPanel,
					curriculumElement, curriculumElement.getParent(), curriculum, secCallback);
			listenTo(editMetadataCtrl);
			return editMetadataCtrl.getInitialComponent();
		});
		
		// Absences
		if(CurriculumLectures.isEnabled(curriculumElement, curriculumElement.getType())) {
			absencesTab = tabPane.addTab(ureq, translate("tab.absences"), "o_sel_curriculum_absences", uureq -> {
				absencesCtrl = new CurriculumElementLecturesController(uureq, getWindowControl(), toolbarPanel,
						curriculum, curriculumElement, true, secCallback);
				listenTo(absencesCtrl);
				return absencesCtrl.getInitialComponent();
			}, true);
		}
		
		// Reports
		if(secCallback.canCurriculumReports(curriculum)) {	
			tabPane.addTab(ureq, translate("curriculum.reports"), uureq -> {
				reportsCtrl = new CurriculumReportsController(uureq, getWindowControl(),
						null, curriculum, curriculumElement, ArchiveType.CURRICULUMELEMENT, TITLE_SIZE);
				listenTo(reportsCtrl);
				return reportsCtrl.getInitialComponent();
			});
		}
	}
	
	private CurriculumDashboardController createDashBoard(UserRequest ureq) {
		removeAsListenerAndDispose(overviewCtrl);
		removeAsListenerAndDispose(offersWidgetCtrl);
		removeAsListenerAndDispose(coursesWidgetCtrl);
		removeAsListenerAndDispose(lectureBlocksWidgetCtrl);
		
		WindowControl subControl = addToHistory(ureq, OresHelper
				.createOLATResourceableType(CurriculumListManagerController.CONTEXT_OVERVIEW), null);
		overviewCtrl = new CurriculumDashboardController(ureq, subControl);
		listenTo(overviewCtrl);
		
		membersWidgetCtrl = new MembersWidgetController(ureq, getWindowControl(), curriculumElement);
		listenTo(membersWidgetCtrl);
		overviewCtrl.addWidget("members", membersWidgetCtrl);

		if(lectureModule.isEnabled()) {
			lectureBlocksWidgetCtrl = new LectureBlocksWidgetController(ureq, getWindowControl(),
					curriculumElement, lecturesSecCallback);
			listenTo(lectureBlocksWidgetCtrl);
			overviewCtrl.addWidget("lectures", lectureBlocksWidgetCtrl);
		}
		
		coursesWidgetCtrl = new CoursesWidgetController(ureq, getWindowControl(), curriculumElement, secCallback);
		listenTo(coursesWidgetCtrl);
		overviewCtrl.addWidget("courses", coursesWidgetCtrl);
		coursesWidgetCtrl.getInitialComponent().setVisible(canRepositoryEntries);
		
		if(acModule.isEnabled() && catalogV2Module.isEnabled() && curriculumElement.getParent() == null) {
			offersWidgetCtrl = new OffersWidgetController(ureq, getWindowControl(), curriculumElement);
			listenTo(offersWidgetCtrl);
			overviewCtrl.addWidget("offers", offersWidgetCtrl);
		}
		
		return overviewCtrl;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(CONTEXT_OVERVIEW.equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, overviewTab);
		} else if(CONTEXT_LECTURES.equalsIgnoreCase(type)) {
			activateLectures(ureq, entries);
		} else if(CONTEXT_RESOURCES.equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, resourcesTab);
			if(resourcesCtrl != null) {
				resourcesCtrl.loadModel();
			}
		} else if((CONTEXT_IMPLEMENTATIONS.equalsIgnoreCase(type) || CONTEXT_STRUCTURE.equalsIgnoreCase(type))
				&& structuresTab >= 0) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			tabPane.setSelectedPane(ureq, structuresTab);
			if(structureCtrl != null) {
				structureCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if(CONTEXT_MEMBERS.equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			tabPane.setSelectedPane(ureq, userManagerTab);
			if(userManagementCtrl != null) {
				userManagementCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if(CONTEXT_OFFERS.equalsIgnoreCase(type) && offersTab > 0) {
			tabPane.setSelectedPane(ureq, offersTab);
		} else if(CONTEXT_METADATA.equalsIgnoreCase(type) && metadataTab > 0) {
			tabPane.setSelectedPane(ureq, metadataTab);
		} else if(CONTEXT_ABSENCES.equalsIgnoreCase(type) && absencesTab > 0) {
			tabPane.setSelectedPane(ureq, absencesTab);
		} else if(CONTEXT_ELEMENT.equalsIgnoreCase(type) || "CurriculumElement".equalsIgnoreCase(type)) {
			if(entries.size() > 1 && "Lectures".equalsIgnoreCase(entries.get(1).getOLATResourceable().getResourceableTypeName())) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				activateLectures(ureq, subEntries);
			} else {
				tabPane.setSelectedPane(ureq, structuresTab);
				if(structureCtrl != null) {
					structureCtrl.activate(ureq, entries, state);
				}
			}
		}
	}
	
	private void activateLectures(UserRequest ureq, List<ContextEntry> entries) {
		tabPane.setSelectedPane(ureq, lecturesTab);
		List<ContextEntry> subEntries = entries.subList(1, entries.size());
		if(lectureBlocksCtrl != null) {
			lectureBlocksCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(curriculumStructureCalloutCtrl);
		removeAsListenerAndDispose(deleteCurriculumElementCtrl);
		removeAsListenerAndDispose(statusChangeCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(cmc);
		curriculumStructureCalloutCtrl = null;
		deleteCurriculumElementCtrl = null;
		statusChangeCtrl = null;
		toolsCalloutCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(deleteButton == source) {
			doConfirmDeleteCurriculumElement(ureq);
		} else if(structureButton == source) {
			doOpenStructure( ureq, structureButton);
		} else if(previousButton == source && previousButton.getUserObject() instanceof CurriculumElement el) {
			fireEvent(ureq, new CurriculumElementEvent(el, List.of()));
		} else if(previousImplementationButton == source && previousImplementationButton.getUserObject() instanceof CurriculumElement el) {
			fireEvent(ureq, new CurriculumElementEvent(el, List.of()));
		} else if(nextButton == source && nextButton.getUserObject() instanceof CurriculumElement el) {
			fireEvent(ureq, new CurriculumElementEvent(el, List.of()));
		} else if(nextImplementationButton == source && nextImplementationButton.getUserObject() instanceof CurriculumElement el) {
			fireEvent(ureq, new CurriculumElementEvent(el, List.of()));
		} else if (source instanceof Link link) {
			if ("status".equals(link.getCommand())) {
				if (link.getUserObject() instanceof CurriculumElementStatus status) {
					doChangeStatus(ureq, status);
				}
			}
		} else if(tabPane == source) {
			if(event instanceof TabbedPaneChangedEvent tpce) {
				if(overviewCtrl != null && tpce.getNewComponent() == overviewCtrl.getInitialComponent()) {
					updateOverviewDashboard(ureq);
				} else if(resourcesCtrl != null && tpce.getNewComponent() == resourcesCtrl.getInitialComponent()) {
					resourcesCtrl.loadModel();
				} else if(lectureBlocksCtrl != null && tpce.getNewComponent() == lectureBlocksCtrl.getInitialComponent() && !tpce.isCreated()) {
					lectureBlocksCtrl.loadModel(ureq);
				}
			}
		}
	}

	private void updateOverviewDashboard(UserRequest ureq) {
		if(coursesWidgetCtrl != null) {
			coursesWidgetCtrl.loadModel();
		}
		if(lectureBlocksWidgetCtrl != null) {
			lectureBlocksWidgetCtrl.loadModel(ureq.getRequestTimestamp());
		}
		if(offersWidgetCtrl != null) {
			offersWidgetCtrl.loadModel();
		}
		if(membersWidgetCtrl != null) {
			membersWidgetCtrl.loadModel(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(structureCtrl == source || coursesWidgetCtrl == source || offersWidgetCtrl == source
				|| lectureBlocksWidgetCtrl == source || lectureBlocksCtrl == source
				|| resourcesCtrl == source || membersWidgetCtrl == source) {
			if(event instanceof ActivateEvent ae) {
				activate(ureq, ae.getEntries(), null);
			} else if(event instanceof CurriculumElementEvent) {
				fireEvent(ureq, event);
			}
		} else if(editMetadataCtrl == source) {
			if(event == Event.DONE_EVENT) {
				curriculumElement = editMetadataCtrl.getElement();
				updateMetadataUI();
				updateRepositoryEntries();
			}
		} else if(statusChangeCtrl == source) {
			if(event == Event.DONE_EVENT) {
				curriculumElement = statusChangeCtrl.getCurriculumElement();
				updateStatusDropdown();
				updateOffersView(curriculumElement.getElementStatus());
				if (offersWidgetCtrl != null) {
					offersWidgetCtrl.loadModel();
				}
				if (structureCtrl != null) {
					structureCtrl.loadModel();
				}
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(deleteCurriculumElementCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolbarPanel.popController(this);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(curriculumStructureCalloutCtrl == source) {
			toolsCalloutCtrl.deactivate();
			if(event instanceof SelectCurriculumElementRowEvent scere) {
				fireEvent(ureq, new CurriculumElementEvent(scere.getCurriculumElement(), List.of()));
			}
			cleanUp();
		} else if(cmc == source || toolsCalloutCtrl == source) {
			cleanUp();
		}
	}

	private void doOpenStructure(UserRequest ureq, Link link) {
		CurriculumElement rootElement = getRootElement();
		curriculumStructureCalloutCtrl = new CurriculumStructureCalloutController(ureq, getWindowControl(),
				rootElement, curriculumElement, true);
		listenTo(curriculumStructureCalloutCtrl);
		
		CalloutSettings settings = new CalloutSettings(true, CalloutOrientation.bottom, true,  null);
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				curriculumStructureCalloutCtrl.getInitialComponent(), "o_c" + link.getDispatchID(), "", true, "", settings);
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private CurriculumElement getRootElement() {
		CurriculumElement rootElement;
		if(curriculumElement.getParent() == null) {
			rootElement = curriculumElement;
		} else {
			List<CurriculumElement> parentLine = curriculumService.getCurriculumElementParentLine(curriculumElement);
			rootElement = parentLine.get(0);
		}
		return rootElement;
	}
	
	private void doChangeStatus(UserRequest ureq, CurriculumElementStatus newStatus) {
		curriculumElement = curriculumService.getCurriculumElement(curriculumElement);
		if (curriculumElement == null) {
			showWarning("warning.curriculum.deleted");
			return;
		} else if (curriculumElement.getElementStatus() == newStatus) {
			updateStatusDropdown();
			if (offersWidgetCtrl != null) {
				offersWidgetCtrl.loadModel();
			}
			return;
		}
		
		statusChangeCtrl = new CurriculumElementStatusChangeController(ureq, getWindowControl(), curriculumElement, newStatus);
		listenTo(statusChangeCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				statusChangeCtrl.getInitialComponent(), true, translate("change.status.title"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void updateOffersView(CurriculumElementStatus status) {
		if (offersCtrl != null) {
			offersCtrl.updateStatus(status);
		}
	}

	private void doConfirmDeleteCurriculumElement(UserRequest ureq) {
		removeAsListenerAndDispose(deleteCurriculumElementCtrl);
		removeAsListenerAndDispose(cmc);
		
		CurriculumElement elementToDelete = curriculumService.getCurriculumElement(curriculumElement);
		if(elementToDelete == null) {
			showWarning("warning.curriculum.deleted");
		} else {
			List<CurriculumElement> descendants = curriculumService.getCurriculumElementsDescendants(curriculumElement);
			ConfirmDelete confirmDelete = ConfirmDelete.valueOf(elementToDelete, descendants, getTranslator());
			
			deleteCurriculumElementCtrl = new ConfirmDeleteCurriculumElementController(ureq, getWindowControl(),
					confirmDelete.message(), confirmDelete.confirmation(), confirmDelete.confirmationButton(),
					elementToDelete, descendants);
			listenTo(deleteCurriculumElementCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteCurriculumElementCtrl.getInitialComponent(),
					true, confirmDelete.title());
			listenTo(cmc);
			cmc.activate();
		}
	}
}
