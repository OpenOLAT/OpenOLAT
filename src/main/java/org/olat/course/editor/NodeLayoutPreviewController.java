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
package org.olat.course.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.spacesaver.ExpandController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.st.Overview;
import org.olat.course.nodes.st.OverviewController;
import org.olat.course.style.Header;
import org.olat.course.style.ui.CourseStyleUIFactory;
import org.olat.course.style.ui.HeaderController;

/**
 * 
 * Initial date: 20 Jul 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NodeLayoutPreviewController extends BasicController {

	private VelocityContainer mainVC;
	private TabbedPane tabPane;
	private TextComponent emptyHeaderCmp;
	private final ExpandController collapseCtrl;
	private HeaderController headerCtrl;
	private TextComponent emptyOverviewCmp;
	private OverviewController overviewCtrl;

	protected NodeLayoutPreviewController(UserRequest ureq, WindowControl wControl, CourseNode courseNode) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("layout_preview");
		
		tabPane = new TabbedPane("tab", getLocale());
		tabPane.setDirtyCheck(false);
		tabPane.addListener(this);
		mainVC.put("tabs", tabPane);
		
		collapseCtrl = new ExpandController(ureq, wControl, "edit-" + courseNode.getIdent());
		listenTo(collapseCtrl);
		
		emptyHeaderCmp = TextFactory.createTextComponentFromString("header.empty", translate("layout.preview.header.empty"), "o_info", false, mainVC);
		tabPane.addTab(translate("layout.preview.header"), emptyHeaderCmp);
		
		emptyOverviewCmp = TextFactory.createTextComponentFromString("overview.empty", translate("layout.preview.overview.empty"), "o_info", false, mainVC);
		tabPane.addTab(translate("layout.preview.overview"), emptyOverviewCmp);
		
		putInitialPanel(mainVC);
	}
	
	public void update(UserRequest ureq, Header header, Overview overview) {
		removeAsListenerAndDispose(headerCtrl);
		headerCtrl = null;
		if (header != null && CourseStyleUIFactory.hasValues(header)) {
			headerCtrl = new HeaderController(ureq, getWindowControl(), header);
			listenTo(headerCtrl);
			collapseCtrl.setCollapsibleController(headerCtrl);
			tabPane.replaceTab(0, collapseCtrl);
		} else {
			tabPane.replaceTab(0, emptyHeaderCmp);
		}
		
		removeAsListenerAndDispose(overviewCtrl);
		overviewCtrl = null;
		if (overview != null) {
			overviewCtrl = new OverviewController(ureq, getWindowControl(), overview, null);
			listenTo(overviewCtrl);
			tabPane.replaceTab(1, overviewCtrl);
		} else {
			tabPane.replaceTab(1, emptyOverviewCmp);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
