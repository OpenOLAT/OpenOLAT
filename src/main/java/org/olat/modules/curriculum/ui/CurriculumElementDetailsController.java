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

import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_IMPLEMENTATIONS;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_OVERVIEW;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.*;

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
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.ui.lectures.CurriculumElementLecturesController;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.LecturesSecurityCallbackFactory;

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
	
	private TabbedPane tabPane;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	
	private CurriculumElementOverviewController overviewCtrl;
	private EditCurriculumElementController editMetadataCtrl;
	private CurriculumComposerController structureCtrl;
	private CurriculumElementLecturesController lecturesCtrl;
	private LectureListRepositoryController lectureBlocksCtrl;
	private CurriculumElementResourceListController resourcesCtrl;
	private CurriculumElementUserManagementController userManagementCtrl;
	
	private Curriculum curriculum;
	private CurriculumElement curriculumElement;
	private final CurriculumSecurityCallback secCallback;
	
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
			overviewCtrl = new CurriculumElementOverviewController(uureq, getWindowControl());
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
			structureCtrl = new CurriculumComposerController(uureq, getWindowControl(), toolbarPanel,
					curriculum, curriculumElement, config, secCallback);
			listenTo(structureCtrl);
			
			List<ContextEntry> all = BusinessControlFactory.getInstance().createCEListFromString("[All:0]");
			structureCtrl.activate(uureq, all, null);
			return structureCtrl.getInitialComponent();
		});
		
		// Courses
		tabPane.addTab(ureq, translate("tab.resources"), uureq -> {
			resourcesCtrl = new CurriculumElementResourceListController(uureq, getWindowControl(), curriculumElement, secCallback);
			listenTo(resourcesCtrl);
			return resourcesCtrl.getInitialComponent();
		});
		
		// Events / lectures blocks
		tabPane.addTab(ureq, translate("curriculum.lectures"), uureq -> {
			lecturesCtrl = new CurriculumElementLecturesController(uureq, getWindowControl(), toolbarPanel, curriculum, curriculumElement, true, secCallback);
			listenTo(lecturesCtrl);
			return lecturesCtrl.getInitialComponent();
		});
		
		lecturesTab = tabPane.addTab(ureq, translate("tab.lectureblocks"), uureq -> {
			LecturesSecurityCallback lecturesSecCallback = LecturesSecurityCallbackFactory.getSecurityCallback(true, false, false, LectureRoles.lecturemanager);
			lectureBlocksCtrl = new LectureListRepositoryController(uureq, getWindowControl(), curriculumElement, lecturesSecCallback);
			listenTo(lectureBlocksCtrl);
			return lectureBlocksCtrl.getInitialComponent();
		});
		
		// User management
		tabPane.addTab(ureq, translate("tab.user.management"), uureq -> {
			userManagementCtrl = new CurriculumElementUserManagementController(uureq, getWindowControl(), curriculumElement, secCallback);
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

		if(CONTEXT_OVERVIEW.equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, overviewTab);
		} else if(CONTEXT_LECTURES.equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, lecturesTab);
		} else if(CONTEXT_IMPLEMENTATIONS.equalsIgnoreCase(type) || CONTEXT_STRUCTURE.equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			tabPane.setSelectedPane(ureq, structuresTab);
			if(structureCtrl != null) {
				structureCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
}
