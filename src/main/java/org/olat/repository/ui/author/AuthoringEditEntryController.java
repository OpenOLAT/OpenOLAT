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
package org.olat.repository.ui.author;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.RepositoryEditDescriptionController;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * 
 * Initial date: 05.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEditEntryController extends BasicController {
	
	private Link copyLink;
	
	private final TabbedPane tabbedPane;
	private CloseableModalController cmc;
	private CopyRepositoryEntryController copyCtrl;
	private final AuthoringEditAccessController accessCtrl;
	private final RepositoryEditDescriptionController descriptionCtrl;
	
	private RepositoryEntry entry;
	private RepositoryService repositoryService;
	
	public AuthoringEditEntryController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			AuthoringEntryRow row) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		entry = repositoryService.loadByKey(row.getKey());

		descriptionCtrl = new RepositoryEditDescriptionController(ureq, getWindowControl(), entry, false);
		listenTo(descriptionCtrl);
		accessCtrl = new AuthoringEditAccessController(ureq, wControl, entry);
		listenTo(accessCtrl);

		tabbedPane = new TabbedPane("editPane", getLocale());
		tabbedPane.addTab(translate("tab.public"), descriptionCtrl.getInitialComponent());
		tabbedPane.addTab(translate("tab.accesscontrol"), accessCtrl.getInitialComponent());
		
		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(entry);
		handler.addExtendedEditionControllers(ureq, getWindowControl(), this, entry);
		
		putInitialPanel(tabbedPane);
		stackPanel.pushController("Editor", this);
		initToolbar(stackPanel);
	}
	
	private void initToolbar(TooledStackedPanel stackPanel) {
		copyLink = LinkFactory.createToolLink("copy", translate("details.copy"), this);
		stackPanel.addTool(copyLink, false);
		
	}
	
	@Override
	public Translator getTranslator() {
		return super.getTranslator();
	}
	
	public void appendEditor(String tabName, Controller controller) {
		tabbedPane.addTab(tabName, controller.getInitialComponent());
		listenTo(controller);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(copyLink == source) {
			doCopy(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(copyCtrl == source) {
			cmc.deactivate();
			doCleanUp();
		} else if(cmc == source) {
			doCleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void doCleanUp() {
		removeAsListenerAndDispose(copyCtrl);
		removeAsListenerAndDispose(cmc);
		copyCtrl = null;
		cmc = null;
	}

	private void doCopy(UserRequest ureq) {
		copyCtrl = new CopyRepositoryEntryController(ureq, getWindowControl(), entry);
		listenTo(copyCtrl);
		
		String title = translate("details.copy");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), copyCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
}