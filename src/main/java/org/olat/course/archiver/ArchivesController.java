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
import org.olat.core.commons.services.export.ui.ExportsListController;
import org.olat.core.commons.services.export.ui.ExportsListSettings;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.scope.FormScopeSelection;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ArchivesController extends ExportsListController {
	
	private static final String ALL_ARCHIVES = "All";
	private static final String COMPLETE_ARCHIVES = "Complete";
	private static final String PARTIAL_ARCHIVES = "Partial";
	
	private FormScopeSelection archiveScopes;

	@Autowired
	private WebDAVModule webDAVModule;
	
	public ArchivesController(UserRequest ureq, WindowControl wControl, boolean admin) {
		super(ureq, wControl, null, CourseArchiveListController.COURSE_ARCHIVE_SUB_IDENT,
				admin, new ExportsListSettings(false), "my_export_list");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont
				&& webDAVModule.isEnabled() && webDAVModule.isLinkEnabled()) {
			layoutCont.contextPut("webdavhttp", FolderManager.getWebDAVHttp());
			layoutCont.contextPut("webdavhttps", FolderManager.getWebDAVHttps());
		}
		
		List<Scope> scopes = new ArrayList<>(4);
		scopes.add(ScopeFactory.createScope(ALL_ARCHIVES, translate("course.archive.all"), null));
		scopes.add(ScopeFactory.createScope(COMPLETE_ARCHIVES, translate("course.archive.complete"), null));
		scopes.add(ScopeFactory.createScope(PARTIAL_ARCHIVES, translate("course.archive.partial"), null));
		archiveScopes = uifactory.addScopeSelection("archive.scopes", null, formLayout, scopes);

		super.initForm(formLayout, listener, ureq);
	}
	
	@Override
	protected void initTable(FlexiTableElement tableElement) {
		super.initTable(tableElement);
		
		tableElement.setEmptyTableSettings("course.my.archive.empty", null, "o_icon_coursearchive", null, null, false);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(archiveScopes == source) {
			loadModel();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	public SearchExportMetadataParameters getSearchParams() {
		SearchExportMetadataParameters params = super.getSearchParams();
		if(!isAdministrator) {
			params.setOnlyAdministrators(Boolean.FALSE);
		}
		
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
	
	
}
