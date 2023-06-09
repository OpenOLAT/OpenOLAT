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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.event.OpenArtefactEvent;
import org.olat.modules.project.ui.event.QuickStartEvents;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjQuickStartWidgetController extends FormBasicController {
	
	private FormLink calendarLink;
	private FormLink toDoTasksLink;
	private FormLink decisionsLink;
	private FormLink notesLink;
	private FormLink filesLink;

	private final ProjProject project;
	private final ProjProjectSecurityCallback secCallback;
	private Formatter formatter;
	private Map<Long, ProjArtefact> keyToArtefact;
	
	@Autowired
	private ProjectService projectService;

	protected ProjQuickStartWidgetController(UserRequest ureq, WindowControl wControl, ProjProject project,
			ProjProjectSecurityCallback secCallback) {
		super(ureq, wControl, "quick_start_widget");
		this.project = project;
		this.secCallback = secCallback;
		this.formatter = Formatter.getInstance(getLocale());
		
		initForm(ureq);
		reload();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<String> quickStarterNames = new ArrayList<>(4);
		flc.contextPut("quickStarters", quickStarterNames);
		if (secCallback.canViewAppointments() || secCallback.canViewMilestones()) {
			calendarLink = uifactory.addFormLink("calendar.widget.title", formLayout);
			calendarLink.setIconLeftCSS("o_icon o_icon_calendar");
			calendarLink.setUrl(ProjectBCFactory.getCalendarUrl(project));
			quickStarterNames.add(calendarLink.getComponent().getComponentName());
		}
		if (secCallback.canViewToDos()) {
			toDoTasksLink = uifactory.addFormLink("todo.widget.title", formLayout);
			toDoTasksLink.setIconLeftCSS("o_icon o_icon_todo_task");
			toDoTasksLink.setUrl(ProjectBCFactory.getToDosUrl(project));
			quickStarterNames.add(toDoTasksLink.getComponent().getComponentName());
		}
		if (secCallback.canViewDecisions()) {
			decisionsLink = uifactory.addFormLink("decision.widget.title", formLayout);
			decisionsLink.setIconLeftCSS("o_icon o_icon_proj_decision");
			decisionsLink.setUrl(ProjectBCFactory.getDecisionsUrl(project));
			quickStarterNames.add(decisionsLink.getComponent().getComponentName());
		}
		if (secCallback.canViewNotes()) {
			notesLink = uifactory.addFormLink("note.widget.title", formLayout);
			notesLink.setIconLeftCSS("o_icon o_icon_proj_note");
			notesLink.setUrl(ProjectBCFactory.getNotesUrl(project));
			quickStarterNames.add(notesLink.getComponent().getComponentName());
		}
		if (secCallback.canViewFiles()) {
			filesLink = uifactory.addFormLink("file.widget.title", formLayout);
			filesLink.setIconLeftCSS("o_icon o_icon_proj_file");
			filesLink.setUrl(ProjectBCFactory.getNotesUrl(project));
			quickStarterNames.add(filesLink.getComponent().getComponentName());
		}
	}

	public void reload() {
		keyToArtefact = new HashMap<>(6);
		List<QuickSearchItem> items = new ArrayList<>(6);
		flc.contextPut("items", items);

		ProjArtefactItems artefactItems = projectService.getQuickStartArtefactItems(project, getIdentity());
		List<ProjFile> files = artefactItems.getFiles();
		if (files != null && !files.isEmpty()) {
			for (ProjFile file : files) {
				ProjArtefact artefact = file.getArtefact();
				keyToArtefact.put(artefact.getKey(), artefact);
				
				QuickSearchItem item = new QuickSearchItem(
						artefact.getKey(),
						file.getVfsMetadata().getFileLastModified(),
						getChanged(file.getVfsMetadata().getFileLastModified()),
						CSSHelper.createFiletypeIconCssClassFor(file.getVfsMetadata().getFilename()),
						ProjectUIFactory.getDisplayName(file),
						ProjectBCFactory.getFileUrl(file));
				items.add(item);
			}
		}
		List<ProjNote> notes = artefactItems.getNotes();
		if (notes != null && !notes.isEmpty()) {
			for (ProjNote note : notes) {
				ProjArtefact artefact = note.getArtefact();
				keyToArtefact.put(artefact.getKey(), artefact);
				
				QuickSearchItem item = new QuickSearchItem(
						artefact.getKey(),
						artefact.getLastModified(),
						getChanged(artefact.getLastModified()),
						"o_icon_proj_note",
						ProjectUIFactory.getDisplayName(getTranslator(), note),
						ProjectBCFactory.getNoteUrl(note));
				items.add(item);
			}
		}
		items.sort((i1, i2) -> i2.getChangeDate().compareTo(i1.getChangeDate()));
	}

	private String getChanged(Date lastModified) {
		return translate("quick.start.changed", formatter.formatDateAndTime(lastModified));
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String key = ureq.getParameter("select_artefact");
			if (StringHelper.containsNonWhitespace(key) && StringHelper.isLong(key)) {
				Long artefactKey = Long.valueOf(key);
				ProjArtefact artefact = keyToArtefact.get(artefactKey);
				if (artefact != null) {
					fireEvent(ureq, new OpenArtefactEvent(artefact));
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == calendarLink) {
			fireEvent(ureq, QuickStartEvents.CALENDAR_EVENT);
		} else if (source == toDoTasksLink) {
			fireEvent(ureq, QuickStartEvents.TODOS_EVENT);
		} else if (source == decisionsLink) {
			fireEvent(ureq, QuickStartEvents.DECISIONS_EVENT);
		} else if (source == notesLink) {
			fireEvent(ureq, QuickStartEvents.NOTES_EVENT);
		} else if (source == filesLink) {
			fireEvent(ureq, QuickStartEvents.FILES_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public static final class QuickSearchItem {
		
		private final Long artefactKey;
		private final Date changeDate;
		private final String changed;
		private final String iconCss;
		private final String displayName;
		private final String url;
		
		public QuickSearchItem(Long artefactKey, Date changeDate, String changed, String iconCss, String displayName, String url) {
			this.artefactKey = artefactKey;
			this.changeDate = changeDate;
			this.changed = changed;
			this.iconCss = iconCss;
			this.displayName = displayName;
			this.url = url;
		}

		public Long getArtefactKey() {
			return artefactKey;
		}

		public Date getChangeDate() {
			return changeDate;
		}

		public String getChanged() {
			return changed;
		}

		public String getIconCss() {
			return iconCss;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getUrl() {
			return url;
		}
		
	}

}
