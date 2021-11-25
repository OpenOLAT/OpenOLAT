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
package org.olat.course.editor.importnodes;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.ui.author.AuthorListConfiguration;
import org.olat.repository.ui.author.AuthorListController;
import org.olat.repository.ui.author.AuthoringEntryRow;
import org.olat.repository.ui.author.AuthoringEntryRowSelectionEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectCourseController extends StepFormBasicController {
	
	private ImportCourseNodesContext importCourseContext;
	
	private final AuthorListController listCtrl;
	
	@Autowired
	private RepositoryService repositoryService;
	
	public SelectCourseController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, ImportCourseNodesContext importCourseContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "select_course");
		
		this.importCourseContext = importCourseContext;
		
		Roles roles = ureq.getUserSession().getRoles();
		AuthorListConfiguration config = AuthorListConfiguration.selectRessource("copy-course-element-v1", "CourseModule");
		config.setImportRessources(false);
		config.setCreateRessources(false);
		SearchAuthorRepositoryEntryViewParams searchParams = new SearchAuthorRepositoryEntryViewParams(getIdentity(), roles);
		searchParams.addResourceTypes("CourseModule");
		searchParams.setExcludeEntryKeys(List.of(importCourseContext.getTargetEntry().getKey()));
		listCtrl = new AuthorListController(ureq, wControl, rootForm, searchParams, config);
		listenTo(listCtrl);
		
		ContextEntry myCoursesEntry = BusinessControlFactory.getInstance()
				.createContextEntry(OresHelper.createOLATResourceableType("MyCourses"));
		listCtrl.activate(ureq, List.of(myCoursesEntry), null);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("selection", listCtrl.getInitialFormItem());
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof AuthoringEntryRowSelectionEvent) {
			AuthoringEntryRow row = ((AuthoringEntryRowSelectionEvent)event).getRow();
			RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
			importCourseContext.setEntry(entry);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected boolean validateFormItem(FormItem item) {
		boolean allOk = super.validateFormItem(item);
		
		if(listCtrl.getMultiSelectedRows().isEmpty() && importCourseContext.getEntry() == null) {
			listCtrl.getInitialFormItem().setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			listCtrl.getInitialFormItem().clearError();
		}
		
		return allOk;
	}

	@Override
	protected void formNext(UserRequest ureq) {
		List<AuthoringEntryRow> rows = listCtrl.getMultiSelectedRows();
		if(rows.size() == 1) {
			RepositoryEntry entry = repositoryService.loadByKey(rows.get(0).getKey());
			importCourseContext.setEntry(entry);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else {
			listCtrl.getInitialFormItem().setErrorKey("form.legende.mandatory", null);
		}
	}
}
