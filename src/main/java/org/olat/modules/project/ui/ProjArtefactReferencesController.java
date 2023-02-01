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
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.event.OpenArtefactEvent;
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
	private FormLink noteCreateLink;
	
	private CloseableModalController cmc;
	private ProjArtefactSelectionController selectCtrl;
	private ProjFileUploadController fileUploadCtrl;
	private ProjFileCreateController fileCreateCtrl;
	private ProjNoteEditController noteCreateCtrl;
	private ProjConfirmationController deleteConfirmationCtrl;
	
	private final ProjArtefact artefact;
	private final boolean withOpenInSameWindow;

	@Autowired
	protected ProjectService projectService;

	public ProjArtefactReferencesController(UserRequest ureq, WindowControl wControl, Form mainForm,
			ProjArtefact artefact, boolean withOpenInSameWindow) {
		super(ureq, wControl, LAYOUT_CUSTOM, "references", mainForm);
		this.artefact = artefact;
		this.withOpenInSameWindow = withOpenInSameWindow;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer topButtons = FormLayoutContainer.createButtonLayout("topButtons", getTranslator());
		topButtons.setRootForm(mainForm);
		formLayout.add("topButtons", topButtons);
		topButtons.setElementCssClass("o_button_group o_button_group_right");
		
		selectLink = uifactory.addFormLink("reference.select", topButtons, Link.BUTTON);
		
		addDropdown = uifactory.addDropdownMenu("reference.dropdown", null, topButtons, getTranslator());
		addDropdown.setOrientation(DropdownOrientation.right);
		addDropdown.setEmbbeded(true);
		
		fileUploadLink = uifactory.addFormLink("reference.file.upload", formLayout, Link.LINK);
		addDropdown.addElement(fileUploadLink);
		fileCreateLink = uifactory.addFormLink("reference.file.create", formLayout, Link.LINK);
		addDropdown.addElement(fileCreateLink);
		noteCreateLink = uifactory.addFormLink("reference.note.create", formLayout, Link.LINK);
		addDropdown.addElement(noteCreateLink);
		
		loadArtefacts();
	}

	private void loadArtefacts() {
		ProjArtefactItems artefacts = projectService.getLinkedArtefactItems(artefact);
		
		List<ArtefactRow> artefactRows = new ArrayList<>();
		List<ProjFile> files = artefacts.getFiles();
		if (files != null && !files.isEmpty()) {
			List<ArtefactRow> rows = new ArrayList<>(files.size());
			for (ProjFile file : files) {
				ArtefactRow artefactRow = new ArtefactRow(file.getKey(), file.getArtefact());
				String iconCss = CSSHelper.createFiletypeIconCssClassFor(file.getVfsMetadata().getFilename());
				forgeRow(artefactRow, iconCss, ProjectUIFactory.getDisplayName(file), ProjectBCFactory.getFileUrl(file));
				rows.add(artefactRow);
			}
			rows.sort((r1, r2) -> r1.getDisplayName().compareToIgnoreCase(r2.getDisplayName()));
			artefactRows.addAll(rows);
		}
		List<ProjNote> notes = artefacts.getNotes();
		if (notes != null && !notes.isEmpty()) {
			List<ArtefactRow> rows = new ArrayList<>(notes.size());
			for (ProjNote note : notes) {
				ArtefactRow artefactRow = new ArtefactRow(note.getKey(), note.getArtefact());
				forgeRow(artefactRow, "o_icon_proj_note", ProjectUIFactory.getDisplayName(getTranslator(), note), ProjectBCFactory.getNoteUrl(note));
				rows.add(artefactRow);
			}
			rows.sort((r1, r2) -> r1.getDisplayName().compareToIgnoreCase(r2.getDisplayName()));
			artefactRows.addAll(rows);
		}
		
		flc.contextPut("rows", artefactRows);
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
		link.setIconLeftCSS("o_icon o_icon-lg " + row.getIconCss());
		link.setI18nKey(row.getDisplayName());
		link.setUrl(url);
		link.setUserObject(row);
		row.setDisplayNameLink(link);
	}
	
	private void forgeOpenInNewWindowLink(ArtefactRow row) {
		FormLink link = uifactory.addFormLink("open_" + row.getArtefactKey(), "open", "", null, flc, Link.NONTRANSLATED);
		link.setIconLeftCSS("o_icon o_icon-lg o_icon_content_popup");
		link.setNewWindow(true, true, false);
		link.setUserObject(row);
		row.setOpenInNewWindowLink(link);
	}
	
	private void forgeDeleteLink(ArtefactRow row) {
		FormLink link = uifactory.addFormLink("del_" + row.getArtefactKey(), "delete", "", null, flc, Link.NONTRANSLATED);
		link.setIconLeftCSS("o_icon o_icon-lg o_icon_proj_project_status_deleted");
		link.setUserObject(row);
		row.setDeleteLink(link);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (selectCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadArtefacts();
			}
			cmc.deactivate();
			cleanUp();
		} else if (fileUploadCtrl == source) {
			if (event == Event.DONE_EVENT) {
				linkFile(fileUploadCtrl.getFile());
				loadArtefacts();
			}
			cmc.deactivate();
			cleanUp();
		} else if (fileCreateCtrl == source) {
			if (event == Event.DONE_EVENT) {
				linkFile(fileCreateCtrl.getFile());
				loadArtefacts();
			}
			cmc.deactivate();
			cleanUp();
		} else if (noteCreateCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadArtefacts();
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
		removeAsListenerAndDispose(fileUploadCtrl);
		removeAsListenerAndDispose(fileCreateCtrl);
		removeAsListenerAndDispose(noteCreateCtrl);
		removeAsListenerAndDispose(selectCtrl);
		removeAsListenerAndDispose(cmc);
		deleteConfirmationCtrl = null;
		fileUploadCtrl = null;
		fileCreateCtrl = null;
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
		} else if (source == noteCreateLink){
			doCreateNote(ureq);
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
	
	private void doSelect(UserRequest ureq) {
		if (guardModalController(fileUploadCtrl)) return;
		
		selectCtrl = new ProjArtefactSelectionController(ureq, getWindowControl(), artefact);
		listenTo(selectCtrl);
		
		String title = translate("reference.select");
		cmc = new CloseableModalController(getWindowControl(), "close", selectCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doUploadFile(UserRequest ureq) {
		if (guardModalController(fileUploadCtrl)) return;
		
		fileUploadCtrl = new ProjFileUploadController(ureq, getWindowControl(), artefact.getProject());
		listenTo(fileUploadCtrl);
		
		String title = translate("file.upload");
		cmc = new CloseableModalController(getWindowControl(), "close", fileUploadCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateFile(UserRequest ureq) {
		if (guardModalController(fileCreateCtrl)) return;
		
		fileCreateCtrl = new ProjFileCreateController(ureq, getWindowControl(), artefact.getProject());
		listenTo(fileCreateCtrl);
		
		String title = translate("file.create");
		cmc = new CloseableModalController(getWindowControl(), "close", fileCreateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void linkFile(ProjFile file) {
		if (file != null) {
			projectService.linkArtefacts(getIdentity(), artefact, file.getArtefact());
		}
	}
	
	private void doCreateNote(UserRequest ureq) {
		if (guardModalController(noteCreateCtrl)) return;
		
		ProjNote note = projectService.createNote(getIdentity(), artefact.getProject());
		projectService.linkArtefacts(getIdentity(), artefact, note.getArtefact());
		noteCreateCtrl = new ProjNoteEditController(ureq, getWindowControl(), note, Set.of(getIdentity()), true, false);
		listenTo(noteCreateCtrl);
		
		String title = translate("note.edit");
		cmc = new CloseableModalController(getWindowControl(), "close", noteCreateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpen(UserRequest ureq, ArtefactRow row, boolean openInNewWindow) {
		String url = ProjectBCFactory.getArtefactUrl(artefact.getProject(), row.getArtefact().getType(), row.getKey());
		if (openInNewWindow) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
		} else {
			fireEvent(ureq, new OpenArtefactEvent(row.getArtefact()));
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, ArtefactRow row) {
		if (guardModalController(deleteConfirmationCtrl)) return;
		
		
		String message = translate("reference.delete.message", row.getDisplayName());
		deleteConfirmationCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				"reference.delete.confirm", "reference.delete.button");
		deleteConfirmationCtrl.setUserObject(row);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", deleteConfirmationCtrl.getInitialComponent(),
				true, translate("reference.delete.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(ArtefactRow row) {
		projectService.unlinkArtefacts(getIdentity(), artefact, row.getArtefact());
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
