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
package org.olat.course.archiver;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.modules.bc.FolderManager;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.model.SearchExportMetadataParameters;
import org.olat.core.commons.services.export.ui.ExportRow;
import org.olat.core.commons.services.export.ui.ExportsListController;
import org.olat.core.commons.services.export.ui.ExportsListSettings;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.scope.FormScopeSelection;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseModule;
import org.olat.course.archiver.wizard.CourseArchiveContext;
import org.olat.course.archiver.wizard.CourseArchiveFinishStepCallback;
import org.olat.course.archiver.wizard.CourseArchive_1_ArchiveTypeStep;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseArchiveListController extends ExportsListController implements Activateable2 {
	
	public static final String COURSE_ARCHIVE_SUB_IDENT = "ooo-course-archive-ooo";
	private static final String ALL_ARCHIVES = "All";
	private static final String COMPLETE_ARCHIVES = "Complete";
	private static final String PARTIAL_ARCHIVES = "Partial";

	private FormLink newArchiveButton;
	private FormScopeSelection archiveScopes;
	
	private final RepositoryEntry entry;
	
	private StepsMainRunController courseArchiveWizard;
	
	@Autowired
	private WebDAVModule webDAVModule;
	@Autowired
	private CourseModule courseModule;
	@Autowired
	private RepositoryService repositoryService;
	
	public CourseArchiveListController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,  boolean admin) {
		super(ureq, wControl, entry, COURSE_ARCHIVE_SUB_IDENT, admin, new ExportsListSettings(false));
		this.entry = entry;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("off_info", translate("course.archive.infos", courseModule.getCourseArchiveRetention().toString()));
			layoutCont.contextPut("off_info_help_url", "manual_user/learningresources/Data_archiving");
			
			if (webDAVModule.isEnabled() && webDAVModule.isLinkEnabled()) {
				layoutCont.contextPut("webdavhttp", FolderManager.getWebDAVHttp());
				layoutCont.contextPut("webdavhttps", FolderManager.getWebDAVHttps());
			}
		}
		
		List<Scope> scopes = new ArrayList<>(4);
		scopes.add(ScopeFactory.createScope(ALL_ARCHIVES, translate("course.archive.all"), null));
		scopes.add(ScopeFactory.createScope(COMPLETE_ARCHIVES, translate("course.archive.complete"), null));
		scopes.add(ScopeFactory.createScope(PARTIAL_ARCHIVES, translate("course.archive.partial"), null));
		archiveScopes = uifactory.addScopeSelection("archive.scopes", null, formLayout, scopes);

		newArchiveButton = uifactory.addFormLink("course.archive.new", formLayout, Link.BUTTON);
		newArchiveButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");

		super.initForm(formLayout, listener, ureq);
	}
	
	@Override
	protected void initTable(FlexiTableElement tableElement) {
		super.initTable(tableElement);
		
		tableElement.setEmptyTableSettings("course.archive.empty", null, "o_icon_coursearchive", "course.archive.empty.action", "o_icon_add", false);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(courseArchiveWizard == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					postDoNewArchive();
				}
				cleanUp();
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeControllerListener(courseArchiveWizard);
		courseArchiveWizard = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == archiveScopes) {
			loadModel();
		} else if(source == newArchiveButton
				|| event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
			doNewArchive(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	public SearchExportMetadataParameters getSearchParams() {
		SearchExportMetadataParameters params = super.getSearchParams();
		String selectedKey = archiveScopes.getSelectedKey();
		if(COMPLETE_ARCHIVES.equals(selectedKey)) {
			params.setArchiveTypes(List.of(ArchiveType.COMPLETE));
		} else if(PARTIAL_ARCHIVES.equals(selectedKey)) {
			params.setArchiveTypes(List.of(ArchiveType.PARTIAL));
		} else {
			params.setArchiveTypes(List.of(ArchiveType.COMPLETE, ArchiveType.PARTIAL));
		}
		return params;
	}
	
	protected void doConfirmCancel(UserRequest ureq, ExportRow row) {
		String[] args = { StringHelper.escapeHtml(row.getTitle()) };
		String title = translate("confirm.course.cancel.title", args);
		String text = translate("confirm.course.cancel.text", args);		
		confirmCancelCtrl = activateYesNoDialog(ureq, title, text, confirmCancelCtrl);
		confirmCancelCtrl.setUserObject(row);
	}

	private void doNewArchive(UserRequest ureq) {
		removeAsListenerAndDispose(courseArchiveWizard);
		
		Roles roles = ureq.getUserSession().getRoles();
		CourseArchiveContext context = CourseArchiveContext.defaultValues(entry, getIdentity(), roles, repositoryService);
		
		Step start = new CourseArchive_1_ArchiveTypeStep(ureq, context);
		CourseArchiveFinishStepCallback finish = new CourseArchiveFinishStepCallback(context);
		courseArchiveWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("wizard.course.archive.title"), "");
		listenTo(courseArchiveWizard);
		getWindowControl().pushAsModalDialog(courseArchiveWizard.getInitialComponent());
	}
	
	private void postDoNewArchive() {
		loadModel();
		String title = translate("post.create.archive.wizard.title");
		String text = translate("post.create.archive.wizard.desc");
		getWindowControl().setInfo(title, text);
	}
}
