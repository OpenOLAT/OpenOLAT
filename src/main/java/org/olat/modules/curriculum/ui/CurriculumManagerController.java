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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.UserSession;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumSecurityCallbackFactory;

/**
 * The root controller of the curriculum mamangement site
 * which holds the list controller and the bread crump / toolbar
 * 
 * Initial date: 15 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumManagerController extends BasicController implements Activateable2 {
	
	private final Link searchLink;
	private final Link curriculumsLink;
	private final TooledStackedPanel toolbarPanel;
	private final ButtonGroupComponent segmentButtonsCmp;
	
	private CurriculumSearchManagerController searchCtrl;
	private CurriculumListManagerController curriculumListCtrl;
	
	private final CurriculumSecurityCallback secCallback;
	
	public CurriculumManagerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		UserSession usess = ureq.getUserSession();
		secCallback = CurriculumSecurityCallbackFactory.createCallback(usess.getRoles());
		
		toolbarPanel = new TooledStackedPanel("categoriesStackPanel", getTranslator(), this);
		toolbarPanel.setShowCloseLink(false, false);
		putInitialPanel(toolbarPanel);
		
		CurriculumRootWrapperController rootCtrl = new CurriculumRootWrapperController(ureq, getWindowControl());
		listenTo(rootCtrl);
		toolbarPanel.pushController(translate("toolbar.curriculums"), rootCtrl);

		segmentButtonsCmp = new ButtonGroupComponent("segments");
		
		curriculumsLink = LinkFactory.createToolLink("curriculum.browser", translate("curriculum.browser"), this);
		curriculumsLink.setElementCssClass("o_sel_cur_browser");
		segmentButtonsCmp.addButton(curriculumsLink, true);
		
		searchLink = LinkFactory.createToolLink("curriculum.search", translate("curriculum.search"), this);
		searchLink.setElementCssClass("o_sel_cur_browser");
		segmentButtonsCmp.addButton(searchLink, false);

		toolbarPanel.addTool(segmentButtonsCmp, Align.segment, true);

		doOpenBrowser(ureq);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Curriculum".equalsIgnoreCase(type)) {
			doOpenBrowser(ureq);
			segmentButtonsCmp.setSelectedButton(curriculumsLink);
			curriculumListCtrl.activate(ureq, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(curriculumsLink == source) {
			doOpenBrowser(ureq);
		} else if(searchLink == source) {
			doOpenSearch(ureq);
		} else if(toolbarPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() instanceof CurriculumListManagerController
						|| pe.getController() instanceof CurriculumSearchManagerController) {
					doOpenBrowser(ureq);
					segmentButtonsCmp.setSelectedButton(curriculumsLink);
				}
			}
		}
	}
	
	private void doOpenBrowser(UserRequest ureq) {
		toolbarPanel.popUpToRootController(ureq);
		removeAsListenerAndDispose(curriculumListCtrl);
		
		curriculumListCtrl = new CurriculumListManagerController(ureq, getWindowControl(), toolbarPanel, secCallback);
		listenTo(curriculumListCtrl);
		toolbarPanel.pushController(translate("toolbar.curriculums.browser"), curriculumListCtrl);
	}
	
	private void doOpenSearch(UserRequest ureq) {
		toolbarPanel.popUpToRootController(ureq);
		removeAsListenerAndDispose(searchCtrl);
		
		searchCtrl = new CurriculumSearchManagerController(ureq, getWindowControl(), toolbarPanel, secCallback);
		listenTo(searchCtrl);
		toolbarPanel.pushController(translate("toolbar.curriculums.search"), searchCtrl);
	}
	
	private class CurriculumRootWrapperController extends BasicController {
		
		public CurriculumRootWrapperController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
			putInitialPanel(new Panel("root_curriculum"));
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			//
		}
	}
}
