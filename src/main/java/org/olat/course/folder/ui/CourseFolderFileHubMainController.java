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
package org.olat.course.folder.ui;

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
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.run.CourseRuntimeController;
import org.olat.repository.RepositoryEntry;
import org.olat.search.SearchModule;
import org.olat.search.SearchServiceUIFactory;
import org.olat.search.SearchServiceUIFactory.DisplayOption;
import org.olat.search.service.indexer.repository.SharedFolderRepositoryIndexer;
import org.olat.search.service.indexer.repository.course.BCCourseNodeIndexer;
import org.olat.search.service.indexer.repository.course.DialogCourseNodeIndexer;
import org.olat.search.ui.SearchInputController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CourseFolderFileHubMainController extends BasicController implements Activateable2 {
	
	private static final String SEARCH_DOCUMENT_TYPE = List.of(
			BCCourseNodeIndexer.TYPE,
			DialogCourseNodeIndexer.TYPE_FILE,
			SharedFolderRepositoryIndexer.TYPE)
		.stream()
		.collect(Collectors.joining(" "));
			
	private SearchInputController searchCtrl;
	private CourseFoldersController vfsSourcesCtrl;
	
	@Autowired
	private SearchModule searchModule;
	@Autowired
	private SearchServiceUIFactory searchUIFactory;

	public CourseFolderFileHubMainController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel,
			RepositoryEntry repositoryEntry, ICourse course, boolean overrideReadOnly) {
		super(ureq, wControl, Util.createPackageTranslator(CourseRuntimeController.class, ureq.getLocale()));
		
		VelocityContainer mainVC = createVelocityContainer("file_hub");
		putInitialPanel(mainVC);
		
		Roles roles = ureq.getUserSession().getRoles();
		if (searchModule.isSearchAllowed(roles) && repositoryEntry != null) {
			searchCtrl = searchUIFactory.createInputController(ureq, wControl, DisplayOption.STANDARD_TEXT, null);
			listenTo(searchCtrl);
			mainVC.put("search", searchCtrl.getInitialComponent());
			searchCtrl.setResourceContextEnable(false);
			searchCtrl.setResourceUrl("[RepositoryEntry:" + repositoryEntry.getKey() + "]");
			searchCtrl.setDocumentType(SEARCH_DOCUMENT_TYPE);
		}
		
		mainVC.contextPut("storageTitle", translate("coursefolder.hub.storages"));
		vfsSourcesCtrl = new CourseFoldersController(ureq, wControl, stackedPanel, repositoryEntry, course, overrideReadOnly);
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
