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
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.ui.lectures.CurriculumElementLecturesController;

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
	
	private TabbedPane tabPane;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	
	private EditCurriculumController editMetadataCtrl;
	private CurriculumOverviewController overviewCtrl;
	private CurriculumComposerController implementationsCtrl;
	private CurriculumElementLecturesController lecturesCtrl;
	private CurriculumUserManagementController userManagementCtrl;
	
	private Curriculum curriculum;
	private final CurriculumSecurityCallback secCallback;
	
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
	
	private void initMetadata() {
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
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
}
