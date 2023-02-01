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

import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.project.ProjNoteRef;
import org.olat.modules.project.ProjNoteSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.event.OpenNoteEvent;

/**
 * 
 * Initial date: 19 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjNoteWidgetController extends ProjNoteListController {
	
	private static final Integer NUM_LAST_MODIFIED = 6;
	
	private FormLink titleLink;
	private FormLink createLink;
	private FormLink showAllLink;
	
	public ProjNoteWidgetController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			ProjProject project, ProjProjectSecurityCallback secCallback, Date lastVisitDate, MapperKey avatarMapperKey) {
		super(ureq, wControl, stackPanel, "note_widget", project, secCallback, lastVisitDate, avatarMapperKey);
		initForm(ureq);
		loadModel(ureq, true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		titleLink = uifactory.addFormLink("note.widget.title", formLayout);
		titleLink.setIconRightCSS("o_icon o_icon_start");
		titleLink.setElementCssClass("o_link_plain");
		
		String url = ProjectBCFactory.getNotesUrl(project);
		titleLink.setUrl(url);
		
		createLink = uifactory.addFormLink("note.create", "", null, formLayout, Link.LINK + Link.NONTRANSLATED);
		createLink.setIconLeftCSS("o_icon o_icon_add");
		createLink.setElementCssClass("o_link_plain");
		createLink.setTitle(translate("note.create"));
		createLink.setVisible(secCallback.canCreateNotes());
		
		showAllLink = uifactory.addFormLink("note.show.all", "", null, formLayout, Link.LINK + Link.NONTRANSLATED);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == titleLink || source == showAllLink) {
			fireEvent(ureq, ProjProjectDashboardController.SHOW_ALL);
		} else if (source == createLink) {
			doCreateNote(ureq);
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
		ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		long count = projectService.getNotesCount(searchParams);
		
		showAllLink.setI18nKey(translate("note.show.all", String.valueOf(count)));
		showAllLink.setVisible(count > 0);
	}

	@Override
	protected void doSelectNote(UserRequest ureq, ProjNoteRef noteRef, boolean edit) {
		fireEvent(ureq, new OpenNoteEvent(noteRef, edit));
	}

}
