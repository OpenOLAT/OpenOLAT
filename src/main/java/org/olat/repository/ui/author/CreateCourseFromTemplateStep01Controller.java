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
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2025-11-04<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateCourseFromTemplateStep01Controller extends StepFormBasicController {

	private final AuthorListController authorListCtrl;
	private CreateCourseFromTemplateContext context;

	@Autowired
	private RepositoryService repositoryService;

	public CreateCourseFromTemplateStep01Controller(UserRequest ureq, WindowControl wControl, Form rootForm,
													StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "select_course_step");

		if (runContext.get(CreateCourseFromTemplateContext.KEY) instanceof CreateCourseFromTemplateContext context) {
			this.context = context;
		}
		
		Roles roles = ureq.getUserSession().getRoles();
		AuthorListConfiguration config = AuthorListConfiguration.selectRessource("templates-v1", "CourseModule");
		config.setImportRessources(false);
		config.setCreateRessources(false);
		config.setAllowedRuntimeTypes(List.of(RepositoryEntryRuntimeType.template));
		SearchAuthorRepositoryEntryViewParams searchParams = new SearchAuthorRepositoryEntryViewParams(getIdentity(), roles);
		searchParams.setCanCopy(true);
		searchParams.addResourceTypes("CourseModule");
		searchParams.setRuntimeTypes(List.of(RepositoryEntryRuntimeType.template));
		authorListCtrl = new AuthorListController(ureq, wControl, rootForm, searchParams, config);
		listenTo(authorListCtrl);

		ContextEntry myCoursesEntry = BusinessControlFactory.getInstance()
				.createContextEntry(OresHelper.createOLATResourceableType("MyCourses"));
		authorListCtrl.activate(ureq, List.of(myCoursesEntry), null);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("authorList", authorListCtrl.getInitialFormItem());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof AuthoringEntryRowSelectionEvent rowSelectionEvent) {
			authorListCtrl.setSelectedRow(rowSelectionEvent.getRow());
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected boolean validateFormItem(UserRequest ureq, FormItem item) {
		boolean allOk = super.validateFormItem(ureq, item);
		
		if (authorListCtrl.getMultiSelectedRows().isEmpty() && context.getTemplateRepositoryEntry() == null) {
			authorListCtrl.getInitialFormItem().setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			authorListCtrl.getInitialFormItem().clearError();
		}
		
		return allOk;
	}

	@Override
	protected void formNext(UserRequest ureq) {
		List<AuthoringEntryRow> rows = authorListCtrl.getMultiSelectedRows();
		authorListCtrl.getInitialFormItem().clearError();
		if (rows.size() == 1) {
			RepositoryEntry entry = repositoryService.loadByKey(rows.get(0).getKey());
			context.setTemplateRepositoryEntry(entry);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else {
			authorListCtrl.getInitialFormItem().setErrorKey("form.legende.mandatory");
		}
	}
}
