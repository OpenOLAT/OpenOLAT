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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.event.OpenArtefactEvent;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjArtefactReferencesController extends FormBasicController {

	private FormLink selectLink;
	private DropdownItem addDropdown;
	private FormLink fileUploadLink;
	private FormLink fileCreateLink;
	private FormLink toDoCreateLink;
	private FormLink decisionCreateLink;
	private FormLink noteCreateLink;
	private FormLink appointmentCreateLink;
	
	private CloseableModalController cmc;
	private ProjArtefactSelectionController selectCtrl;
	private ProjFileUploadController fileUploadCtrl;
	private ProjFileCreateController fileCreateCtrl;
	private ProjToDoEditController toDoCreateCtrl;
	private ProjDecisionEditController decisionCreateCtrl;
	private ProjNoteEditController noteCreateCtrl;
	private ProjAppointmentEditController appointmentCreateCtrl;
	private ProjConfirmationController deleteConfirmationCtrl;
	
	private final ProjectBCFactory bcFactory;
	private final ProjProject project;
	private final ProjArtefact artefact;
	private final boolean readOnly;
	private final boolean withOpenInSameWindow;
	private final boolean autosave;
	private Set<ProjArtefact> linkedArtefacts;
	private int numRows;

	@Autowired
	protected ProjectService projectService;
	
	public ProjArtefactReferencesController(UserRequest ureq, WindowControl wControl, ProjectBCFactory bcFactory,
			ProjArtefact artefact, boolean autosave, boolean readOnly, boolean withOpenInSameWindow) {
		super(ureq, wControl, "references");
		setTranslator(Util.createPackageTranslator(ToDoUIFactory.class, getLocale(), getTranslator()));
		this.bcFactory = bcFactory;
		this.project = artefact.getProject();
		this.artefact = artefact;
		this.autosave = autosave;
		this.readOnly = readOnly;
		this.withOpenInSameWindow = withOpenInSameWindow;
		
		initForm(ureq);
	}

	public ProjArtefactReferencesController(UserRequest ureq, WindowControl wControl, Form mainForm,
			ProjectBCFactory bcFactory, ProjProject project, ProjArtefact artefact, boolean autosave, boolean readOnly,
			boolean withOpenInSameWindow) {
		super(ureq, wControl, LAYOUT_CUSTOM, "references", mainForm);
		setTranslator(Util.createPackageTranslator(ToDoUIFactory.class, getLocale(), getTranslator()));
		this.bcFactory = bcFactory;
		this.project = project;
		this.artefact = artefact;
		this.autosave = autosave;
		this.readOnly = readOnly;
		this.withOpenInSameWindow = withOpenInSameWindow;
		
		initForm(ureq);
	}
	
	public int getNumReferences() {
		return numRows;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (!readOnly) {
			FormLayoutContainer topButtons = FormLayoutContainer.createButtonLayout("topButtons", getTranslator());
			topButtons.setRootForm(mainForm);
			formLayout.add("topButtons", topButtons);
			topButtons.setElementCssClass("o_button_group o_button_group_right");
			
			selectLink = uifactory.addFormLink("reference.select", topButtons, Link.BUTTON);
			
			addDropdown = uifactory.addDropdownMenu("reference.dropdown", null, topButtons, getTranslator());
			addDropdown.setOrientation(DropdownOrientation.right);
			addDropdown.setEmbbeded(true);
			
			appointmentCreateLink = uifactory.addFormLink("reference.appointment.create", formLayout, Link.LINK);
			addDropdown.addElement(appointmentCreateLink);
			toDoCreateLink = uifactory.addFormLink("reference.todo.create", formLayout, Link.LINK);
			addDropdown.addElement(toDoCreateLink);
			decisionCreateLink = uifactory.addFormLink("reference.decision.create", formLayout, Link.LINK);
			addDropdown.addElement(decisionCreateLink);
			noteCreateLink = uifactory.addFormLink("reference.note.create", formLayout, Link.LINK);
			addDropdown.addElement(noteCreateLink);
			fileCreateLink = uifactory.addFormLink("reference.file.create", formLayout, Link.LINK);
			addDropdown.addElement(fileCreateLink);
			fileUploadLink = uifactory.addFormLink("reference.file.upload", formLayout, Link.LINK);
			addDropdown.addElement(fileUploadLink);
		}
		
		linkedArtefacts = artefact != null? new HashSet<>(projectService.getLinkedArtefacts(artefact)): new HashSet<>(2);
		loadArtefacts();
	}

	private void loadArtefacts() {
		ArrayList<ArtefactRow> artefactRows = new ArrayList<>();
		
		if (!linkedArtefacts.isEmpty()) {
			ProjArtefactSearchParams searchParams = new ProjArtefactSearchParams();
			searchParams.setArtefacts(linkedArtefacts);
			ProjArtefactItems artefacts = projectService.getArtefactItems(searchParams);
			
			List<ProjAppointment> appointments = artefacts.getAppointments();
			if (appointments != null && !appointments.isEmpty()) {
				List<ArtefactRow> rows = new ArrayList<>(appointments.size());
				for (ProjAppointment appointment : appointments) {
					rows.add(createRow(appointment));
				}
				rows.sort((r1, r2) -> r1.getDisplayName().compareToIgnoreCase(r2.getDisplayName()));
				artefactRows.addAll(rows);
			}
			
			List<ProjToDo> toDos = artefacts.getToDos();
			if (toDos != null && !toDos.isEmpty()) {
				List<ArtefactRow> rows = new ArrayList<>(toDos.size());
				for (ProjToDo toDo : toDos) {
					rows.add(createRow(toDo));
				}
				rows.sort((r1, r2) -> r1.getDisplayName().compareToIgnoreCase(r2.getDisplayName()));
				artefactRows.addAll(rows);
			}
			
			List<ProjDecision> decisions = artefacts.getDecisions();
			if (decisions != null && !decisions.isEmpty()) {
				List<ArtefactRow> rows = new ArrayList<>(decisions.size());
				for (ProjDecision decision : decisions) {
					rows.add(createRow(decision));
				}
				rows.sort((r1, r2) -> r1.getDisplayName().compareToIgnoreCase(r2.getDisplayName()));
				artefactRows.addAll(rows);
			}
			
			List<ProjNote> notes = artefacts.getNotes();
			if (notes != null && !notes.isEmpty()) {
				List<ArtefactRow> rows = new ArrayList<>(notes.size());
				for (ProjNote note : notes) {
					rows.add(createRow(note));
				}
				rows.sort((r1, r2) -> r1.getDisplayName().compareToIgnoreCase(r2.getDisplayName()));
				artefactRows.addAll(rows);
			}
			
			List<ProjFile> files = artefacts.getFiles();
			if (files != null && !files.isEmpty()) {
				List<ArtefactRow> rows = new ArrayList<>(files.size());
				for (ProjFile file : files) {
					rows.add(createRow(file));
				}
				rows.sort((r1, r2) -> r1.getDisplayName().compareToIgnoreCase(r2.getDisplayName()));
				artefactRows.addAll(rows);
			}
		}
		
		flc.contextPut("rows", artefactRows);
		numRows = artefactRows.size();
	}

	private ArtefactRow createRow(ProjToDo toDo) {
		ArtefactRow artefactRow = new ArtefactRow(toDo.getKey(), toDo.getArtefact());
		forgeRow(artefactRow, "o_icon_todo_task", ToDoUIFactory.getDisplayName(getTranslator(), toDo.getToDoTask()), bcFactory.getToDoUrl(toDo));
		return artefactRow;
	}

	private ArtefactRow createRow(ProjDecision decision) {
		ArtefactRow artefactRow = new ArtefactRow(decision.getKey(), decision.getArtefact());
		forgeRow(artefactRow, "o_icon_proj_decision", ProjectUIFactory.getDisplayName(getTranslator(), decision), bcFactory.getDecisionUrl(decision));
		return artefactRow;
	}

	private ArtefactRow createRow(ProjNote note) {
		ArtefactRow artefactRow = new ArtefactRow(note.getKey(), note.getArtefact());
		forgeRow(artefactRow, "o_icon_proj_note", ProjectUIFactory.getDisplayName(getTranslator(), note), bcFactory.getNoteUrl(note));
		return artefactRow;
	}

	private ArtefactRow createRow(ProjFile file) {
		ArtefactRow artefactRow = new ArtefactRow(file.getKey(), file.getArtefact());
		String iconCss = CSSHelper.createFiletypeIconCssClassFor(file.getVfsMetadata().getFilename());
		forgeRow(artefactRow, iconCss, ProjectUIFactory.getDisplayName(file), bcFactory.getFileUrl(file));
		return artefactRow;
	}

	private ArtefactRow createRow(ProjAppointment appointment) {
		ArtefactRow artefactRow = new ArtefactRow(appointment.getKey(), appointment.getArtefact());
		forgeRow(artefactRow, "o_icon_proj_appointment", ProjectUIFactory.getDisplayName(getTranslator(), appointment), bcFactory.getAppointmentUrl(appointment));
		return artefactRow;
	}
	
	private void forgeRow(ArtefactRow artefactRow, String iconCss, String displayName, String url) {
		artefactRow.setIconCss(iconCss);
		artefactRow.setDisplayName(displayName);
		forgeDisplayNameLink(artefactRow, url);
		forgeOpenInNewWindowLink(artefactRow);
		forgeDeleteLink(artefactRow);
	}
	
	private void forgeDisplayNameLink(ArtefactRow row, String url) {
		if (!withOpenInSameWindow) return;
		
		FormLink link = uifactory.addFormLink("select_" + row.getArtefactKey(), "open", "", null, flc, Link.NONTRANSLATED);
		link.setIconLeftCSS("o_icon o_icon-fw " + row.getIconCss());
		link.setI18nKey(StringHelper.escapeHtml(row.getDisplayName()));
		link.setUrl(url);
		link.setUserObject(row);
		row.setDisplayNameLink(link);
	}
	
	private void forgeOpenInNewWindowLink(ArtefactRow row) {
		FormLink link = uifactory.addFormLink("open_" + row.getArtefactKey(), "open", "", null, flc, Link.BUTTON + Link.NONTRANSLATED);
		link.setIconLeftCSS("o_icon o_icon-lg o_icon_content_popup");
		link.setGhost(true);
		link.setNewWindow(true, true, false);
		link.setUserObject(row);
		row.setOpenInNewWindowLink(link);
	}
	
	private void forgeDeleteLink(ArtefactRow row) {
		if (readOnly) return;
		
		FormLink link = uifactory.addFormLink("del_" + row.getArtefactKey(), "delete", "", null, flc, Link.BUTTON + Link.NONTRANSLATED);
		link.setIconLeftCSS("o_icon o_icon-lg o_icon_proj_project_status_deleted");
		link.setGhost(true);
		link.setUserObject(row);
		row.setDeleteLink(link);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (selectCtrl == source) {
			if (event == Event.DONE_EVENT) {
				linkedArtefacts(selectCtrl.getSelectdArtefacts());
			}
			cmc.deactivate();
			cleanUp();
		} else if (fileUploadCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (fileUploadCtrl.getFile() != null) {
					linkArtefact(fileUploadCtrl.getFile().getArtefact());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (fileCreateCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (fileCreateCtrl.getFile() != null) {
					linkArtefact(fileCreateCtrl.getFile().getArtefact());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (toDoCreateCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (toDoCreateCtrl.getToDo() != null) {
					linkArtefact(toDoCreateCtrl.getToDo().getArtefact());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (decisionCreateCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (decisionCreateCtrl.getDecision() != null) {
					linkArtefact(decisionCreateCtrl.getDecision().getArtefact());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (noteCreateCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (noteCreateCtrl.getNote() != null) {
					linkArtefact(noteCreateCtrl.getNote().getArtefact());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (appointmentCreateCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (appointmentCreateCtrl.getAppointment() != null) {
					linkArtefact(appointmentCreateCtrl.getAppointment().getArtefact());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (deleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doDelete((ArtefactRow)deleteConfirmationCtrl.getUserObject());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(appointmentCreateCtrl);
		removeAsListenerAndDispose(decisionCreateCtrl);
		removeAsListenerAndDispose(fileUploadCtrl);
		removeAsListenerAndDispose(fileCreateCtrl);
		removeAsListenerAndDispose(toDoCreateCtrl);
		removeAsListenerAndDispose(noteCreateCtrl);
		removeAsListenerAndDispose(selectCtrl);
		removeAsListenerAndDispose(cmc);
		deleteConfirmationCtrl = null;
		appointmentCreateCtrl = null;
		decisionCreateCtrl = null;
		fileUploadCtrl = null;
		fileCreateCtrl = null;
		toDoCreateCtrl = null;
		noteCreateCtrl = null;
		selectCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == selectLink) {
			doSelect(ureq);
		} else if (source == fileCreateLink){
			doCreateFile(ureq);
		} else if (source == fileUploadLink){
			doUploadFile(ureq);
		} else if (source == toDoCreateLink){
			doCreateToDo(ureq);
		} else if (source == decisionCreateLink){
			doCreateDecision(ureq);
		} else if (source == noteCreateLink){
			doCreateNote(ureq);
		} else if (source == appointmentCreateLink){
			doCreateAppointment(ureq);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if ("select".equals(link.getCmd()) && link.getUserObject() instanceof ArtefactRow) {
				doOpen(ureq, (ArtefactRow)link.getUserObject(), false);
			} else if ("open".equals(link.getCmd()) && link.getUserObject() instanceof ArtefactRow) {
				doOpen(ureq, (ArtefactRow)link.getUserObject(), true);
			} else if ("delete".equals(link.getCmd()) && link.getUserObject() instanceof ArtefactRow) {
				doConfirmDelete(ureq, (ArtefactRow)link.getUserObject());
			}
		} 
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	public void save(ProjArtefact saveArtefact) {
		projectService.updateLinkedArtefacts(getIdentity(), saveArtefact, linkedArtefacts);
	}
	
	private void doSelect(UserRequest ureq) {
		if (guardModalController(fileUploadCtrl)) return;
		
		selectCtrl = new ProjArtefactSelectionController(ureq, getWindowControl(), project, artefact);
		listenTo(selectCtrl);
		
		String title = translate("reference.select");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), selectCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doUploadFile(UserRequest ureq) {
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
			
		toDoCreateCtrl = new ProjToDoEditController(ureq, getWindowControl(), bcFactory, project, false);
		listenTo(toDoCreateCtrl);
		
		String title = translate("todo.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), toDoCreateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	protected void doCreateDecision(UserRequest ureq) {
		if (guardModalController(decisionCreateCtrl)) return;
		
		decisionCreateCtrl = new ProjDecisionEditController(ureq, getWindowControl(), bcFactory, project, Set.of(getIdentity()), false);
		listenTo(decisionCreateCtrl);
		
		String title = translate("decision.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), decisionCreateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateNote(UserRequest ureq) {
		if (guardModalController(noteCreateCtrl)) return;
		
		ProjNote note = projectService.createNote(getIdentity(), project);
		noteCreateCtrl = new ProjNoteEditController(ureq, getWindowControl(), bcFactory, note, Set.of(getIdentity()), true, false);
		listenTo(noteCreateCtrl);
		
		String title = translate("note.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), noteCreateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateAppointment(UserRequest ureq) {
		if (guardModalController(appointmentCreateCtrl)) return;
		
		appointmentCreateCtrl = new ProjAppointmentEditController(ureq, getWindowControl(), bcFactory, project, Set.of(getIdentity()), false, new Date());
		listenTo(appointmentCreateCtrl);
		
		String title = translate("appointment.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), appointmentCreateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void linkedArtefacts(Set<ProjArtefact> selectdArtefacts) {
		if (!selectdArtefacts.isEmpty()) {
			selectdArtefacts.forEach(artefact2 -> linkArtefact(artefact2, false));
			loadArtefacts();
		}
	}
	
	private void linkArtefact(ProjArtefact artefact2) {
		linkArtefact(artefact2, true);
	}
	
	private void linkArtefact(ProjArtefact artefact2, boolean load) {
		linkedArtefacts.add(artefact2);
		if (autosave) {
			projectService.linkArtefacts(getIdentity(), artefact, artefact2);
		}
		if (load) {
			loadArtefacts();
		}
	}

	private void doOpen(UserRequest ureq, ArtefactRow row, boolean openInNewWindow) {
		String url = bcFactory.getArtefactUrl(project, row.getArtefact().getType(), row.getKey());
		if (openInNewWindow) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
		} else {
			fireEvent(ureq, new OpenArtefactEvent(row.getArtefact()));
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, ArtefactRow row) {
		if (guardModalController(deleteConfirmationCtrl)) return;
		
		String message = translate("reference.delete.message", StringHelper.escapeHtml(row.getDisplayName()));
		deleteConfirmationCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				"reference.delete.confirm", "reference.delete.button", true);
		deleteConfirmationCtrl.setUserObject(row);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmationCtrl.getInitialComponent(),
				true, translate("reference.delete.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(ArtefactRow row) {
		linkedArtefacts.remove(row.getArtefact());
		if (autosave) {
			projectService.unlinkArtefacts(getIdentity(), artefact, row.getArtefact());
		}
		loadArtefacts();
	}
	
	public static final class ArtefactRow {
		
		private final Long key;
		private final ProjArtefact artefact;
		private String iconCss;
		private String displayName;
		private FormLink displayNameLink;
		private FormLink openInNewWindowLink;
		private FormLink deleteLink;
		
		public ArtefactRow(Long key, ProjArtefact artefact) {
			this.key = key;
			this.artefact = artefact;
		}
		
		public Long getKey() {
			return key;
		}
		
		public ProjArtefact getArtefact() {
			return artefact;
		}
		
		public Long getArtefactKey() {
			return artefact.getKey();
		}
			
		public String getIconCss() {
			return iconCss;
		}

		public void setIconCss(String iconCss) {
			this.iconCss = iconCss;
		}

		public String getDisplayName() {
			return displayName;
		}
		
		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}
		
		public FormLink getDisplayNameLink() {
			return displayNameLink;
		}

		public void setDisplayNameLink(FormLink displayNameLink) {
			this.displayNameLink = displayNameLink;
		}
		
		public String getDisplayNameLinkName() {
			return displayNameLink != null? displayNameLink.getName(): null;
		}
		
		public FormLink getOpenInNewWindowLink() {
			return openInNewWindowLink;
		}
		
		public void setOpenInNewWindowLink(FormLink openInNewWindowLink) {
			this.openInNewWindowLink = openInNewWindowLink;
		}
		
		public String getOpenInNewWindowLinkName() {
			return openInNewWindowLink != null? openInNewWindowLink.getName(): null;
		}
		
		public FormLink getDeleteLink() {
			return deleteLink;
		}
		
		public void setDeleteLink(FormLink deleteLink) {
			this.deleteLink = deleteLink;
		}
		
		public String getDeleteLinkName() {
			return deleteLink != null? deleteLink.getName(): null;
		}
		
	}

}
