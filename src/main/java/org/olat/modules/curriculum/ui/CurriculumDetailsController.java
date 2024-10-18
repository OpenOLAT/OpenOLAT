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
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_OVERVIEW;

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
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumManagedFlag;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumInfos;
import org.olat.modules.curriculum.ui.lectures.CurriculumElementLecturesController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumDetailsController extends BasicController implements Activateable2 {

	private int lecturesTab;
	private int overviewTab;
	private int implementationsTab;
	
	private Link deleteButton;
	private TabbedPane tabPane;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	
	private CloseableModalController cmc;
	private EditCurriculumController editMetadataCtrl;
	private CurriculumOverviewController overviewCtrl;
	private CurriculumComposerController implementationsCtrl;
	private CurriculumElementLecturesController lecturesCtrl;
	private CurriculumUserManagementController userManagementCtrl;
	private ConfirmCurriculumDeleteController deleteCurriculumCtrl;
	
	private Curriculum curriculum;
	private final CurriculumSecurityCallback secCallback;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumDetailsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			Curriculum curriculum, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		this.curriculum = curriculum;
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		
		mainVC = createVelocityContainer("curriculum_details");
		tabPane = new TabbedPane("tabs", getLocale());
		tabPane.addListener(this);
		initTabPane(ureq);
		initMetadata();
		
		mainVC.put("tabs", tabPane);
		putInitialPanel(mainVC);
	}
	
	public Curriculum getCurriculum() {
		return curriculum;
	}
	
	private void initMetadata() {
		if(secCallback.canEditCurriculum() && !CurriculumManagedFlag.isManaged(curriculum, CurriculumManagedFlag.delete)) {
			Dropdown commandsDropdown = DropdownUIFactory.createMoreDropdown("more", getTranslator());
			commandsDropdown.setDomReplaceable(false);
			commandsDropdown.setButton(true);
			commandsDropdown.setOrientation(DropdownOrientation.right);
			
			deleteButton = LinkFactory.createCustomLink("delete", "delete", "delete", Link.LINK, mainVC, this);
			deleteButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			commandsDropdown.addComponent(deleteButton);

			mainVC.put(commandsDropdown.getComponentName(), commandsDropdown);
		}

		mainVC.contextPut("curriculumDisplayName", curriculum.getDisplayName());
		if(StringHelper.containsNonWhitespace(curriculum.getIdentifier())) {
			mainVC.contextPut("curriculumExternalRef", curriculum.getIdentifier());
		}
	}

	private void initTabPane(UserRequest ureq) {
		// Overview
		overviewTab = tabPane.addTab(ureq, translate("curriculum.overview"), uureq -> {
			overviewCtrl = new CurriculumOverviewController(uureq, getWindowControl());
			listenTo(overviewCtrl);
			return overviewCtrl.getInitialComponent();
		});

		// Implementations
		implementationsTab = tabPane.addTab(ureq, translate("curriculum.implementations"), uureq -> {
			CurriculumComposerConfig config = CurriculumComposerConfig.curriculumView();
			config.setTitle(translate("curriculum.implementations"), 3, "o_icon_curriculum_implementations");
			config.setDefaultNumOfParticipants(true);
			config.setRootElementsOnly(true);
			config.setFlat(true);
			implementationsCtrl = new CurriculumComposerController(uureq, getWindowControl(), toolbarPanel,
					curriculum, null, config, secCallback);
			listenTo(implementationsCtrl);
			
			List<ContextEntry> all = BusinessControlFactory.getInstance().createCEListFromString("[All:0]");
			implementationsCtrl.activate(uureq, all, null);
			return implementationsCtrl.getInitialComponent();
		});
		
		// Lectures blocks / absences like
		lecturesTab = tabPane.addTab(ureq, translate("curriculum.lectures"), uureq -> {
			lecturesCtrl = new CurriculumElementLecturesController(uureq, getWindowControl(), toolbarPanel, curriculum, null, true, secCallback);
			listenTo(lecturesCtrl);
			return lecturesCtrl.getInitialComponent();
		});
		
		// Metadata
		tabPane.addTab(ureq, translate("curriculum.metadata"), uureq -> {
			editMetadataCtrl = new EditCurriculumController(uureq, getWindowControl(), curriculum, secCallback);
			listenTo(editMetadataCtrl);
			return editMetadataCtrl.getInitialComponent();
		});
		
		// User management
		tabPane.addTab(ureq, translate("tab.user.management"), uureq -> {
			userManagementCtrl = new CurriculumUserManagementController(uureq, getWindowControl(), curriculum, secCallback);
			listenTo(userManagementCtrl);
			return userManagementCtrl.getInitialComponent();
		});
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
			tabPane.setSelectedPane(ureq, lecturesTab);
		} else if(CONTEXT_OVERVIEW.equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, overviewTab);
		} else if(CONTEXT_ELEMENT.equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, implementationsTab);
			if(implementationsCtrl != null) {
				implementationsCtrl.activate(ureq, entries, state);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(deleteCurriculumCtrl == source) {
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
		}
	}
	
	private void doConfirmDeleteCurriculum(UserRequest ureq) {
		removeAsListenerAndDispose(deleteCurriculumCtrl);
		removeAsListenerAndDispose(cmc);
		
		CurriculumInfos curriculumToDelete = curriculumService.getCurriculumWithInfos(curriculum);
		if(curriculumToDelete == null || curriculumToDelete.curriculum() == null) {
			showWarning("warning.curriculum.deleted");
		} else {
			deleteCurriculumCtrl = new ConfirmCurriculumDeleteController(ureq, getWindowControl(),
					curriculumToDelete.curriculum(), curriculumToDelete.implementationsStatistics());
			listenTo(deleteCurriculumCtrl);
			
			String title = translate("delete.curriculum.title", StringHelper.escapeHtml(curriculum.getDisplayName()));
			cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteCurriculumCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
}
