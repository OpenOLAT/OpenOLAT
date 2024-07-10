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
package org.olat.home;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.search.SearchModule;
import org.olat.search.SearchServiceUIFactory;
import org.olat.search.SearchServiceUIFactory.DisplayOption;
import org.olat.search.service.indexer.group.GroupFolderIndexer;
import org.olat.search.service.indexer.repository.SharedFolderRepositoryIndexer;
import org.olat.search.service.indexer.repository.course.BCCourseNodeIndexer;
import org.olat.search.service.indexer.repository.course.DialogCourseNodeIndexer;
import org.olat.search.ui.SearchInputController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class PersonalFileHubMainController extends BasicController implements Activateable2 {
	
	private static final String SEARCH_DOCUMENT_TYPE = List.of(
			BCCourseNodeIndexer.TYPE,
			DialogCourseNodeIndexer.TYPE_FILE,
			SharedFolderRepositoryIndexer.TYPE,
			GroupFolderIndexer.TYPE)
		.stream()
		.collect(Collectors.joining(" "));
			
	private SearchInputController searchCtrl;
	private PersonalFileHubMountPointsController vfsSourcesCtrl;
	
	@Autowired
	private SearchModule searchModule;
	@Autowired
	private SearchServiceUIFactory searchUIFactory;

	public PersonalFileHubMainController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("file_hub");
		putInitialPanel(mainVC);
		
		Roles roles = ureq.getUserSession().getRoles();
		if (searchModule.isSearchAllowed(roles)) {
			searchCtrl = searchUIFactory.createInputController(ureq, wControl, DisplayOption.STANDARD_TEXT, null);
			listenTo(searchCtrl);
			mainVC.put("search", searchCtrl.getInitialComponent());
			searchCtrl.setResourceContextEnable(false);
			searchCtrl.setResourceUrl(null);
			searchCtrl.setDocumentType(SEARCH_DOCUMENT_TYPE);
		}
		
		mainVC.contextPut("storageTitle", translate("file.hub.storage"));
		vfsSourcesCtrl = new PersonalFileHubMountPointsController(ureq, wControl, stackedPanel, translate("file.hub"));
		listenTo(vfsSourcesCtrl);
		mainVC.put("vfsSources", vfsSourcesCtrl.getInitialComponent());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		vfsSourcesCtrl.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
