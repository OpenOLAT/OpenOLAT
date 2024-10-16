/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.curriculum.CurriculumSecurityCallback;

/**
 * 
 * Initial date: 14 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumManagerRootController extends BasicController implements Activateable2 {

	private final Link lecturesBlocksLink;
	private final Link curriculumsLink;
	private final Link implementationsLink;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	private final CurriculumSecurityCallback secCallback;

	private CurriculumManagerEventsController eventsCtrl;
	private CurriculumSearchManagerController searchCtrl;
	private CurriculumComposerController implementationsCtrl;
	private CurriculumListManagerController curriculumListCtrl;
	private final CurriculumSearchHeaderController searchFieldCtrl;
	
	public CurriculumManagerRootController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel toolbarPanel, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		
		mainVC = createVelocityContainer("manager_overview");
		
		searchFieldCtrl = new CurriculumSearchHeaderController(ureq, getWindowControl());
		listenTo(searchFieldCtrl);
		mainVC.put("searchField", searchFieldCtrl.getInitialComponent());

		curriculumsLink = LinkFactory.createLink("curriculum.browser", "curriculums", getTranslator(), mainVC, this, Link.BUTTON_LARGE);
		curriculumsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_curriculum");
		curriculumsLink.setElementCssClass("o_sel_cur_browser");
		
		implementationsLink = LinkFactory.createLink("curriculum.implementations", "implementations", getTranslator(), mainVC, this, Link.BUTTON_LARGE);
		implementationsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_curriculum_implementations");
		implementationsLink.setElementCssClass("o_sel_cur_implementations");
		
		lecturesBlocksLink = LinkFactory.createLink("curriculum.lectures", "lecturesblocks", getTranslator(), mainVC, this, Link.BUTTON_LARGE);
		lecturesBlocksLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar_day");
		lecturesBlocksLink.setElementCssClass("o_sel_cur_lectures");

		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Curriculum".equalsIgnoreCase(type)) {
			doOpenCurriculumsList(ureq).activate(ureq, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchFieldCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doSearch(ureq, searchFieldCtrl.getSearchString());
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == curriculumsLink) {
			doOpenCurriculumsList(ureq);
		} else if (source == implementationsLink){
			doOpenImplementations(ureq);
		} else if (source == lecturesBlocksLink) {
			doOpenLecturesBlocks(ureq);
		}
	}
	
	private void doSearch(UserRequest ureq, String searchString) {
		toolbarPanel.popUpToRootController(ureq);
		removeAsListenerAndDispose(searchCtrl);
		
		searchCtrl = new CurriculumSearchManagerController(ureq, getWindowControl(), toolbarPanel, searchString, secCallback);
		listenTo(searchCtrl);
		toolbarPanel.pushController(translate("curriculum.search"), searchCtrl);
	}
	
	private CurriculumListManagerController doOpenCurriculumsList(UserRequest ureq) {
		toolbarPanel.popUpToRootController(ureq);
		removeAsListenerAndDispose(curriculumListCtrl);
		
		curriculumListCtrl = new CurriculumListManagerController(ureq, getWindowControl(), toolbarPanel, secCallback);
		listenTo(curriculumListCtrl);
		toolbarPanel.pushController(translate("toolbar.curriculums"), curriculumListCtrl);
		return curriculumListCtrl;
	}
	
	private void doOpenImplementations(UserRequest ureq) {
		toolbarPanel.popUpToRootController(ureq);
		removeAsListenerAndDispose(implementationsCtrl);
		
		CurriculumComposerConfig config = CurriculumComposerConfig.implementationsView();
		config.setTitle(translate("curriculum.implementations"), 2, "o_icon_curriculum_implementations");
		implementationsCtrl = new CurriculumComposerController(ureq, getWindowControl(), toolbarPanel,
				null, null, config , secCallback);
		listenTo(implementationsCtrl);
		toolbarPanel.pushController(translate("toolbar.implementations"), implementationsCtrl);
		
		List<ContextEntry> all = BusinessControlFactory.getInstance().createCEListFromString("[All:0]");
		implementationsCtrl.activate(ureq, all, null);
	}
	
	private void doOpenLecturesBlocks(UserRequest ureq) {
		toolbarPanel.popUpToRootController(ureq);
		removeAsListenerAndDispose(eventsCtrl);
		
		eventsCtrl = new CurriculumManagerEventsController(ureq, getWindowControl());
		listenTo(eventsCtrl);
		toolbarPanel.pushController(translate("curriculum.lectures"), eventsCtrl);
	}
}
