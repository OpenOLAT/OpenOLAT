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
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_MEMBERS;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_OVERVIEW;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_STRUCTURE;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.dropdown.DropdownUIFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.curriculum.ui.lectures.CurriculumElementLecturesController;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.LecturesSecurityCallbackFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementDetailsController extends BasicController implements Activateable2 {

	private int overviewTab;
	private int lecturesTab;
	private int structuresTab;
	private int userManagerTab;

	private Link deleteButton;
	private TabbedPane tabPane;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	
	private CloseableModalController cmc;
	private CurriculumElementOverviewController overviewCtrl;
	private EditCurriculumElementController editMetadataCtrl;
	private CurriculumComposerController structureCtrl;
	private CurriculumElementLecturesController lecturesCtrl;
	private LectureListRepositoryController lectureBlocksCtrl;
	private CurriculumElementResourceListController resourcesCtrl;
	private CurriculumElementUserManagementController userManagementCtrl;
	private ConfirmCurriculumElementDeleteController deleteCurriculumElementCtrl;
	
	private Curriculum curriculum;
	private CurriculumElement curriculumElement;
	private final CurriculumSecurityCallback secCallback;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumElementDetailsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			Curriculum curriculum, CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		this.curriculum = curriculum;
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		this.curriculumElement = curriculumElement;
		
		mainVC = createVelocityContainer("curriculum_element_details");
		tabPane = new TabbedPane("tabs", getLocale());
		tabPane.addListener(this);
		initTabPane(ureq);
		initMetadata();
		
		mainVC.put("tabs", tabPane);
		putInitialPanel(mainVC);
	}
	
	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}
	
	private void initMetadata() {
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
		
		mainVC.contextPut("displayName", curriculumElement.getDisplayName());
		if(StringHelper.containsNonWhitespace(curriculumElement.getIdentifier())) {
			mainVC.contextPut("externalRef", curriculumElement.getIdentifier());
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

	private void initTabPane(UserRequest ureq) {
		overviewTab = tabPane.addTab(ureq, translate("curriculum.overview"), uureq -> {
			WindowControl subControl = addToHistory(uureq, OresHelper
					.createOLATResourceableType(CurriculumListManagerController.CONTEXT_OVERVIEW), null);
			overviewCtrl = new CurriculumElementOverviewController(uureq, subControl);
			listenTo(overviewCtrl);
			return overviewCtrl.getInitialComponent();
		});
		
		// Implementations
		structuresTab = tabPane.addTab(ureq, translate("curriculum.structure"), uureq -> {
			CurriculumComposerConfig config = new CurriculumComposerConfig();
			config.setTitle(translate("curriculum.structure"), 3, "o_icon_curriculum_structure");
			config.setDefaultNumOfParticipants(true);
			config.setRootElementsOnly(false);
			config.setFlat(false);
			
			WindowControl subControl = addToHistory(uureq, OresHelper
					.createOLATResourceableType(CurriculumListManagerController.CONTEXT_STRUCTURE), null);
			structureCtrl = new CurriculumComposerController(uureq, subControl, toolbarPanel,
					curriculum, curriculumElement, config, secCallback);
			listenTo(structureCtrl);
			
			List<ContextEntry> all = BusinessControlFactory.getInstance().createCEListFromString("[All:0]");
			structureCtrl.activate(uureq, all, null);
			return structureCtrl.getInitialComponent();
		});
		
		// Courses
		tabPane.addTab(ureq, translate("tab.resources"), uureq -> {
			WindowControl subControl = addToHistory(uureq, OresHelper
					.createOLATResourceableType(CurriculumListManagerController.CONTEXT_RESOURCES), null);
			resourcesCtrl = new CurriculumElementResourceListController(uureq, subControl, curriculumElement, secCallback);
			listenTo(resourcesCtrl);
			return resourcesCtrl.getInitialComponent();
		});
		
		// Events / lectures blocks
		tabPane.addTab(ureq, translate("curriculum.lectures"), uureq -> {
			WindowControl subControl = addToHistory(uureq, OresHelper
					.createOLATResourceableType(CurriculumListManagerController.CONTEXT_LECTURES), null);
			lecturesCtrl = new CurriculumElementLecturesController(uureq, subControl, toolbarPanel, curriculum, curriculumElement, true, secCallback);
			listenTo(lecturesCtrl);
			return lecturesCtrl.getInitialComponent();
		});
		
		lecturesTab = tabPane.addTab(ureq, translate("tab.lectureblocks"), uureq -> {
			WindowControl subControl = addToHistory(uureq, OresHelper
					.createOLATResourceableType(CurriculumListManagerController.CONTEXT_LECTURES), null);
			LecturesSecurityCallback lecturesSecCallback = LecturesSecurityCallbackFactory.getSecurityCallback(true, false, false, LectureRoles.lecturemanager);
			lectureBlocksCtrl = new LectureListRepositoryController(uureq, subControl, curriculumElement, lecturesSecCallback);
			listenTo(lectureBlocksCtrl);
			return lectureBlocksCtrl.getInitialComponent();
		});
		
		// User management
		userManagerTab = tabPane.addTab(ureq, translate("tab.user.management"), uureq -> {
			WindowControl subControl = addToHistory(uureq, OresHelper
					.createOLATResourceableType(CurriculumListManagerController.CONTEXT_MEMBERS), null);
			userManagementCtrl = new CurriculumElementUserManagementController(uureq, subControl, curriculumElement, secCallback);
			listenTo(userManagementCtrl);
			return userManagementCtrl.getInitialComponent();
		});
		
		// Metadata
		tabPane.addTab(ureq, translate("curriculum.metadata"), uureq -> {
			editMetadataCtrl = new EditCurriculumElementController(uureq, getWindowControl(),
					curriculumElement, curriculumElement.getParent(), curriculum, secCallback);
			listenTo(editMetadataCtrl);
			return editMetadataCtrl.getInitialComponent();
		});
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();

		if(CONTEXT_OVERVIEW.equalsIgnoreCase(type) || "Overview".equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, overviewTab);
		} else if(CONTEXT_LECTURES.equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, lecturesTab);
		} else if(CONTEXT_IMPLEMENTATIONS.equalsIgnoreCase(type) || CONTEXT_STRUCTURE.equalsIgnoreCase(type)) {
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
		} else if(CONTEXT_ELEMENT.equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, structuresTab);
			if(structureCtrl != null) {
				structureCtrl.activate(ureq, entries, state);
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(deleteCurriculumElementCtrl);
		removeAsListenerAndDispose(cmc);
		deleteCurriculumElementCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(deleteButton == source) {
			doConfirmDeleteCurriculumElement(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(structureCtrl == source) {
			if(event instanceof ActivateEvent ae) {
				activate(ureq, ae.getEntries(), null);
			}
		} else if(deleteCurriculumElementCtrl == source) {
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
	
	private void doConfirmDeleteCurriculumElement(UserRequest ureq) {
		removeAsListenerAndDispose(deleteCurriculumElementCtrl);
		removeAsListenerAndDispose(cmc);
		
		CurriculumElement elementToDelete = curriculumService.getCurriculumElement(curriculumElement);
		if(elementToDelete == null) {
			showWarning("warning.curriculum.deleted");
		} else {
			deleteCurriculumElementCtrl = new ConfirmCurriculumElementDeleteController(ureq, getWindowControl(), elementToDelete);
			listenTo(deleteCurriculumElementCtrl);
			
			String title = translate("delete.curriculum.title", StringHelper.escapeHtml(curriculum.getDisplayName()));
			cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteCurriculumElementCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
}
