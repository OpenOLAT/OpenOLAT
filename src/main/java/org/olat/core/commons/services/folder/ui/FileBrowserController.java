/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.commons.services.folder.ui;

import java.util.List;

import org.olat.core.commons.services.folder.ui.event.FileBrowserPushEvent;
import org.olat.core.commons.services.folder.ui.event.FileBrowserSearchEvent;
import org.olat.core.commons.services.folder.ui.event.FileBrowserTitleEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel.BreadCrumb;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 17 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FileBrowserController extends BasicController {

	private TextComponent titleCmp;
	private final TooledStackedPanel stackedPanel;
	
	private final FileBrowserSearchController searchCtrl;
	private FileBrowserSearchableController searcheableCtrl;
	private final FileBrowserMainController mainCtrl;
	
	private Object userObject;

	public FileBrowserController(UserRequest ureq, WindowControl wControl, FileBrowserSelectionMode selectionMode,
			FolderQuota folderQuota, String submitButtonText) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("browser");
		putInitialPanel(mainVC);
		
		titleCmp = TextFactory.createTextComponentFromString("title", null, "o_block_bottom", false, mainVC);
		
		searchCtrl = new FileBrowserSearchController(ureq, wControl);
		searchCtrl.disable();
		listenTo(searchCtrl);
		mainVC.put("search", searchCtrl.getInitialComponent());
		
		stackedPanel = new TooledStackedPanel("folderBreadcrumb", getTranslator(), this);
		stackedPanel.setCssClass("o_toolbar_top");
		stackedPanel.setToolbarEnabled(false);
		mainVC.put("stackedPanel", stackedPanel);
		
		mainCtrl = new FileBrowserMainController(ureq, wControl, stackedPanel, selectionMode, folderQuota, submitButtonText);
		listenTo(mainCtrl);
		stackedPanel.pushController(translate("browser.main"), mainCtrl);
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	protected void doDispose() {
		if (stackedPanel != null) {
			stackedPanel.removeListener(this);
		}
		super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == stackedPanel) {
			doUpdateSearchUI();
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == searchCtrl) {
			if (event instanceof FileBrowserSearchEvent searchEvent) {
				if (searcheableCtrl != null && searcheableCtrl.isSearchAvailable()) {
					searcheableCtrl.search(ureq, searchEvent.getSearchTerm());
				}
			}
		} else if (source == mainCtrl) {
			if (event instanceof FileBrowserTitleEvent titleEvent) {
				updateTitleUI(titleEvent.getTitle());
			} else if (event instanceof FileBrowserPushEvent) {
				doUpdateSearchUI();
			} else {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	private void updateTitleUI(String title) {
		if (StringHelper.containsNonWhitespace(title)) {
			titleCmp.setText("<h2>" + title + "</h2>");
		} else {
			titleCmp.setText(null);
		}
	}

	private void doUpdateSearchUI() {
		searcheableCtrl = getSearchableController();
		if (searcheableCtrl == null) {
			searchCtrl.setVisible(false);
		} else if (searcheableCtrl.isSearchAvailable()) {
			searchCtrl.setVisible(true);
			searchCtrl.enable(searcheableCtrl.getPlaceholder());
		} else {
			searchCtrl.setVisible(true);
			searchCtrl.disable();
		}
	}

	private FileBrowserSearchableController getSearchableController() {
		List<Link> breadCrumbs = stackedPanel.getBreadCrumbs();
		for (int i = breadCrumbs.size()-1 ; i >= 0; i--) {
			Link link = breadCrumbs.get(i);
			if (link.getUserObject() instanceof BreadCrumb breadCrumb
					&& breadCrumb.getController() instanceof FileBrowserSearchableController sController) {
				return sController;
			}
		}
		return null;
	}

}
