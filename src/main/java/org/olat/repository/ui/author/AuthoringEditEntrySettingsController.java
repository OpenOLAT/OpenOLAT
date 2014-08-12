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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEditEntrySettingsController extends BasicController {

	private final TabbedPane tabbedPane;
	private final AuthoringEditAccessController accessCtrl;
	private final RepositoryEditDescriptionController descriptionCtrl;
	
	private RepositoryEntry entry;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
	public AuthoringEditEntrySettingsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntryRef entryRef) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));

		entry = repositoryService.loadByKey(entryRef.getKey());

		descriptionCtrl = new RepositoryEditDescriptionController(ureq, getWindowControl(), entry, false);
		listenTo(descriptionCtrl);
		accessCtrl = new AuthoringEditAccessController(ureq, wControl, entry);
		listenTo(accessCtrl);

		tabbedPane = new TabbedPane("editSettingsTabbedPane", getLocale());
		tabbedPane.addTab(translate("tab.public"), descriptionCtrl.getInitialComponent());
		tabbedPane.addTab(translate("tab.accesscontrol"), accessCtrl.getInitialComponent());
		
		RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(entry);
		handler.addExtendedEditionControllers(ureq, getWindowControl(), this, entry);
		
		putInitialPanel(tabbedPane);
		stackPanel.pushController(translate("settings.editor"), this);
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
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		fireEvent(ureq, event);
		super.event(ureq, source, event);
	}
}