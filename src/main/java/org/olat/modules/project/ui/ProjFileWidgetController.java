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
package org.olat.modules.project.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.project.ProjFileSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectStatus;

/**
 * 
 * Initial date: 12 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjFileWidgetController extends ProjFileListController {
	
	private static final Integer NUM_LAST_MODIFIED = 6;
	
	private FormLink titleLink;
	private FormLink uploadLink;
	private FormLink createLink;
	private FormLink showAllLink;

	public ProjFileWidgetController(UserRequest ureq, WindowControl wControl, ProjProject project,
			ProjProjectSecurityCallback secCallback, Date lastVisitDate) {
		super(ureq, wControl, "file_widget", project, secCallback, lastVisitDate);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		titleLink = uifactory.addFormLink("file.widget.title", formLayout);
		titleLink.setIconRightCSS("o_icon o_icon_start");
		titleLink.setElementCssClass("o_link_plain");
		
		String url = ProjectBCFactory.getFilesUrl(project);
		titleLink.setUrl(url);
		
		uploadLink = uifactory.addFormLink("file.upload", "", null, formLayout, Link.LINK + Link.NONTRANSLATED);
		uploadLink.setIconLeftCSS("o_icon o_icon_upload");
		uploadLink.setElementCssClass("o_link_plain");
		uploadLink.setTitle(translate("file.upload"));
		uploadLink.setVisible(secCallback.canCreateFiles());
		
		createLink = uifactory.addFormLink("file.create", "", null, formLayout, Link.LINK + Link.NONTRANSLATED);
		createLink.setIconLeftCSS("o_icon o_icon_add");
		createLink.setElementCssClass("o_link_plain");
		createLink.setTitle(translate("file.create"));
		createLink.setVisible(secCallback.canCreateFiles());
		
		showAllLink = uifactory.addFormLink("file.show.all", "", null, formLayout, Link.LINK + Link.NONTRANSLATED);
		
		super.initForm(formLayout, listener, ureq);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == titleLink || source == showAllLink) {
			fireEvent(ureq, ProjProjectDashboardController.SHOW_ALL);
		} else if (source == uploadLink) {
			doUploadFile(ureq);
		} else if (source == createLink) {
			doCreateFile(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean isFullTable() {
		return false;
	}

	@Override
	protected Integer getNumLastModified() {
		return NUM_LAST_MODIFIED;
	}

	@Override
	protected void onModelLoaded() {
		ProjFileSearchParams searchParams = new ProjFileSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		long count = projectService.getFilesCount(searchParams);
		
		showAllLink.setI18nKey(translate("file.show.all", String.valueOf(count)));
		showAllLink.setVisible(count > 0);
	}

}
