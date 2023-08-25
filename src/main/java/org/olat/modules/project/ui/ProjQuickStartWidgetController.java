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
import java.util.Set;

import org.olat.core.commons.editor.htmleditor.HTMLEditorConfig;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjFileSearchParams;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
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
	
	private DropdownItem addDropdown;
	private FormLink noteCreateLink;
	private FormLink appointmentCreateLink;
	private FormLink toDoCreateLink;
	private FormLink decisionCreateLink;
	private FormLink fileUploadLink;
	private FormLink fileCreateLink;
	private FormLink calendarLink;
	private FormLink toDoTasksLink;
	private FormLink decisionsLink;
	private FormLink notesLink;
	private FormLink filesLink;
	
	private CloseableModalController cmc;
	private ProjNoteEditController noteCreateCtrl;
	private ProjAppointmentEditController appointmentCreateCtrl;
	private ProjToDoEditController toDoCreateCtrl;
	private ProjDecisionEditController decisionCreateCtrl;
	private ProjFileCreateController fileCreateCtrl;
	private ProjFileUploadController fileUploadCtrl;

	private final ProjProject project;
	private final ProjProjectSecurityCallback secCallback;
	private Formatter formatter;
	private Map<Long, ProjArtefact> keyToArtefact;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private DocEditorService docEditorService;

	protected ProjQuickStartWidgetController(UserRequest ureq, WindowControl wControl, ProjProject project,
			ProjProjectSecurityCallback secCallback) {
		super(ureq, wControl, "quick_start_widget");
		this.project = project;
		this.secCallback = secCallback;
		this.formatter = Formatter.getInstance(getLocale());
		
		initForm(ureq);
		reload(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (secCallback.canCreateNotes()) {
			noteCreateLink = uifactory.addFormLink("note.create", formLayout, Link.BUTTON);
			noteCreateLink.setIconLeftCSS("o_icon o_icon_add");
		}
		
		addDropdown = uifactory.addDropdownMenu("create.dropdown", null, null, formLayout, getTranslator());
		addDropdown.setOrientation(DropdownOrientation.right);
		addDropdown.setEmbbeded(true);
		
		if (secCallback.canCreateAppointments()) {
			appointmentCreateLink = uifactory.addFormLink("appointment.create", formLayout, Link.LINK);
			addDropdown.addElement(appointmentCreateLink);
		}
		if (secCallback.canCreateToDos()) {
			toDoCreateLink = uifactory.addFormLink("todo.create", formLayout, Link.LINK);
			addDropdown.addElement(toDoCreateLink);
		}
		if (secCallback.canCreateDecisions()) {
			decisionCreateLink = uifactory.addFormLink("decision.create", formLayout, Link.LINK);
			addDropdown.addElement(decisionCreateLink);
		}
		if (secCallback.canCreateFiles()) {
			fileUploadLink = uifactory.addFormLink("file.upload", formLayout, Link.LINK);
			addDropdown.addElement(fileUploadLink);
			fileCreateLink = uifactory.addFormLink("file.create", formLayout, Link.LINK);
			addDropdown.addElement(fileCreateLink);
		}
		
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

	public void reload(UserRequest ureq) {
		keyToArtefact = new HashMap<>(6);
		List<QuickSearchItem> items = new ArrayList<>(6);
		flc.contextPut("items", items);

		ProjArtefactItems artefactItems = projectService.getQuickStartArtefactItems(project, getIdentity());
		List<ProjFile> files = artefactItems.getFiles();
		if (files != null && !files.isEmpty()) {
			for (ProjFile file : files) {
				ProjArtefact artefact = file.getArtefact();
				keyToArtefact.put(artefact.getKey(), artefact);
				
				VFSMetadata vfsMetadata = file.getVfsMetadata();
				VFSItem vfsItem = vfsRepositoryService.getItemFor(vfsMetadata);
				boolean openInNewWindow = vfsItem instanceof VFSLeaf vfsLeaf 
						&& docEditorService.hasEditor(getIdentity(), ureq.getUserSession().getRoles(), vfsLeaf, vfsMetadata, Mode.VIEW);
				
				QuickSearchItem item = new QuickSearchItem(
						artefact.getKey(),
						file.getVfsMetadata().getFileLastModified(),
						getChanged(file.getVfsMetadata().getFileLastModified()),
						CSSHelper.createFiletypeIconCssClassFor(file.getVfsMetadata().getFilename()),
						StringHelper.escapeHtml(ProjectUIFactory.getDisplayName(file)),
						ProjectBCFactory.getFileUrl(file),
						openInNewWindow);
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
						StringHelper.escapeHtml(ProjectUIFactory.getDisplayName(getTranslator(), note)),
						ProjectBCFactory.getNoteUrl(note),
						false);
				items.add(item);
			}
		}
		items.sort((i1, i2) -> i2.getChangeDate().compareTo(i1.getChangeDate()));
	}

	private String getChanged(Date lastModified) {
		return translate("quick.start.changed", formatter.formatDateAndTime(lastModified));
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (fileUploadCtrl == source) {
			if (event == Event.DONE_EVENT) {
				reload(ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (fileCreateCtrl == source) {
			if (event == Event.DONE_EVENT) {
				reload(ureq);
				fireEvent(ureq, FormEvent.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (toDoCreateCtrl == source) {
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, FormEvent.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (decisionCreateCtrl == source) {
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, FormEvent.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (noteCreateCtrl == source) {
			if (event == Event.DONE_EVENT) {
				reload(ureq);
				fireEvent(ureq, FormEvent.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (appointmentCreateCtrl == source) {
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, FormEvent.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(appointmentCreateCtrl);
		removeAsListenerAndDispose(decisionCreateCtrl);
		removeAsListenerAndDispose(fileUploadCtrl);
		removeAsListenerAndDispose(fileCreateCtrl);
		removeAsListenerAndDispose(toDoCreateCtrl);
		removeAsListenerAndDispose(noteCreateCtrl);
		removeAsListenerAndDispose(cmc);
		appointmentCreateCtrl = null;
		decisionCreateCtrl = null;
		fileUploadCtrl = null;
		fileCreateCtrl = null;
		toDoCreateCtrl = null;
		noteCreateCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String key = ureq.getParameter("select_artefact");
			if (StringHelper.containsNonWhitespace(key) && StringHelper.isLong(key)) {
				Long artefactKey = Long.valueOf(key);
				ProjArtefact artefact = keyToArtefact.get(artefactKey);
				if (artefact != null) {
					if (ProjFile.TYPE.equals(artefact.getType())) {
						doOpenOrDownload(ureq, artefactKey);
					} else {
						fireEvent(ureq, new OpenArtefactEvent(artefact));
					}
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
		} else if (source == fileUploadLink) {
			doUploadFile(ureq);
		} else if (source == fileCreateLink){
			doCreateFile(ureq);
		} else if (source == toDoCreateLink){
			doCreateToDo(ureq);
		} else if (source == decisionCreateLink){
			doCreateDecision(ureq);
		} else if (source == noteCreateLink){
			doCreateNote(ureq);
		} else if (source == appointmentCreateLink){
			doCreateAppointment(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	protected void doUploadFile(UserRequest ureq) {
		if (guardModalController(fileUploadCtrl)) return;
		
		fileUploadCtrl = new ProjFileUploadController(ureq, getWindowControl(), project);
		listenTo(fileUploadCtrl);
		
		String title = translate("file.upload");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), fileUploadCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateFile(UserRequest ureq) {
		if (guardModalController(fileCreateCtrl)) return;
		
		fileCreateCtrl = new ProjFileCreateController(ureq, getWindowControl(), project);
		listenTo(fileCreateCtrl);
		
		String title = translate("file.create");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), fileCreateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateToDo(UserRequest ureq) {
		if (guardModalController(toDoCreateCtrl)) return;
			
		toDoCreateCtrl = new ProjToDoEditController(ureq, getWindowControl(), project, false);
		listenTo(toDoCreateCtrl);
		
		String title = translate("todo.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), toDoCreateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	protected void doCreateDecision(UserRequest ureq) {
		if (guardModalController(decisionCreateCtrl)) return;
		
		decisionCreateCtrl = new ProjDecisionEditController(ureq, getWindowControl(), project, Set.of(getIdentity()), false);
		listenTo(decisionCreateCtrl);
		
		String title = translate("decision.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), decisionCreateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateNote(UserRequest ureq) {
		if (guardModalController(noteCreateCtrl)) return;
		
		ProjNote note = projectService.createNote(getIdentity(), project);
		noteCreateCtrl = new ProjNoteEditController(ureq, getWindowControl(), note, Set.of(getIdentity()), true, false);
		listenTo(noteCreateCtrl);
		
		String title = translate("note.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), noteCreateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateAppointment(UserRequest ureq) {
		if (guardModalController(appointmentCreateCtrl)) return;
		
		appointmentCreateCtrl = new ProjAppointmentEditController(ureq, getWindowControl(), project, Set.of(getIdentity()), false, new Date());
		listenTo(appointmentCreateCtrl);
		
		String title = translate("appointment.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), appointmentCreateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenOrDownload(UserRequest ureq, Long artefactKey) {
		ProjFileSearchParams searchParams = new ProjFileSearchParams();
		searchParams.setArtefactKeys(List.of(artefactKey));
		searchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjFile> files = projectService.getFiles(searchParams);
		
		if (!files.isEmpty()) {
			ProjFile file = files.get(0);
			VFSMetadata vfsMetadata = file.getVfsMetadata();
			VFSContainer projectContainer = projectService.getProjectContainer(project);
			VFSItem vfsItem = projectContainer.resolve(file.getVfsMetadata().getFilename());
			if (vfsItem instanceof VFSLeaf vfsLeaf) {
				Roles roles = ureq.getUserSession().getRoles();
				
				if (secCallback.canEditFile(file) && docEditorService.hasEditor(getIdentity(), roles, vfsLeaf, vfsMetadata, Mode.EDIT)) {
					doOpenFile(ureq, file, vfsLeaf, Mode.EDIT);
				} else if (docEditorService.hasEditor(getIdentity(), roles, vfsLeaf, vfsMetadata, Mode.VIEW)) {
					doOpenFile(ureq, file, vfsLeaf, Mode.VIEW);
				} else {
					doDownload(ureq, file.getArtefact(), vfsLeaf);
				}
			}
		}
	}
	
	private void doOpenFile(UserRequest ureq, ProjFile file, VFSLeaf vfsLeaf, Mode mode) {
		VFSContainer projectContainer = projectService.getProjectContainer(project);
		HTMLEditorConfig htmlEditorConfig = HTMLEditorConfig.builder(projectContainer, vfsLeaf.getName())
				.withAllowCustomMediaFactory(false)
				.withDisableMedia(true)
				.build();
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withMode(mode)
				.withFireSavedEvent(true)
				.addConfig(htmlEditorConfig)
				.build(vfsLeaf);
		 
		String url = docEditorService.prepareDocumentUrl(ureq.getUserSession(), configs);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
		
		if (Mode.EDIT == mode) {
			reload(ureq);
		} else {
			projectService.createActivityRead(getIdentity(), file.getArtefact());
		}
	}
	
	private void doDownload(UserRequest ureq, ProjArtefact artefact, VFSLeaf vfsLeaf) {
		VFSMediaResource resource = new VFSMediaResource(vfsLeaf);
		resource.setDownloadable(true);
		ureq.getDispatchResult().setResultingMediaResource(resource);
		projectService.createActivityDownload(getIdentity(), artefact);
	}
	
	
	public static final class QuickSearchItem {
		
		private final Long artefactKey;
		private final Date changeDate;
		private final String changed;
		private final String iconCss;
		private final String displayName;
		private final String url;
		private final boolean openInNewWindow;
		
		public QuickSearchItem(Long artefactKey, Date changeDate, String changed, String iconCss, String displayName, String url, boolean openInNewWindow) {
			this.artefactKey = artefactKey;
			this.changeDate = changeDate;
			this.changed = changed;
			this.iconCss = iconCss;
			this.displayName = displayName;
			this.url = url;
			this.openInNewWindow = openInNewWindow;
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

		public boolean isOpenInNewWindow() {
			return openInNewWindow;
		}
		
	}

}
