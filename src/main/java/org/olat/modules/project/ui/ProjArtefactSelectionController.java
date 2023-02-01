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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactRef;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjArtefactSelectionController extends FormBasicController {
	
	private TextElement quickSearchEl;
	private FormLink quickSearchButton;
	private FormLink resetQuickSearchButton;
	
	private MultipleSelectionElement fileEl;
	private MultipleSelectionElement noteEl;
	
	private final ProjArtefact artefact;
	private final ProjArtefactItems artefactItems;
	private final SelectionValues fileSV;
	private final SelectionValues noteSV;
	private final Set<String> fileSelectedKeys = new HashSet<>();
	private final Set<String> noteSelectedKeys = new HashSet<>();

	@Autowired
	private ProjectService projectService;
	
	public ProjArtefactSelectionController(UserRequest ureq, WindowControl wControl, ProjArtefact artefact) {
		super(ureq, wControl, "artefact_selection");
		this.artefact = artefact;
		
		List<ProjArtefact> linkedArtefacts = projectService.getLinkedArtefacts(artefact);
		Collection<ProjArtefactRef> excludedArtefacts = new HashSet<>(linkedArtefacts.size() + 1);
		excludedArtefacts.addAll(linkedArtefacts);
		excludedArtefacts.add(artefact);
		
		ProjArtefactSearchParams searchParams = new ProjArtefactSearchParams();
		searchParams.setProject(artefact.getProject());
		searchParams.setExcludedArtefacts(excludedArtefacts);
		artefactItems = projectService.getArtefactItems(searchParams);
		
		fileSV = new SelectionValues();
		if (artefactItems.getFiles() != null) {
			artefactItems.getFiles().forEach(file -> fileSV.add(createSVEntry(
					file.getArtefact(),
					ProjectUIFactory.getDisplayName(file),
					"o_icon_proj_file")));
		}
		noteSV = new SelectionValues();
		if (artefactItems.getNotes() != null) {
			artefactItems.getNotes().forEach(note -> noteSV.add(createSVEntry(
					note.getArtefact(),
					ProjectUIFactory.getDisplayName(getTranslator(), note),
					"o_icon_proj_note")));
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		quickSearchEl = uifactory.addTextElement("quicksearch", null, 32, "", formLayout);
		quickSearchEl.setDomReplacementWrapperRequired(false);
		quickSearchEl.addActionListener(FormEvent.ONKEYUP);
		
		quickSearchButton = uifactory.addFormLink("quickSearchButton", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		quickSearchButton.setIconLeftCSS("o_icon o_icon_search");
		quickSearchButton.setDomReplacementWrapperRequired(false);
		
		resetQuickSearchButton = uifactory.addFormLink("resetQuickSearch", "", null, formLayout, Link.LINK | Link.NONTRANSLATED);
		resetQuickSearchButton.setElementCssClass("btn o_reset_filter_search");
		resetQuickSearchButton.setIconLeftCSS("o_icon o_icon_remove_filters");
		resetQuickSearchButton.setDomReplacementWrapperRequired(false);
		
		FormLayoutContainer listsCont = FormLayoutContainer.createVerticalFormLayout("lists", getTranslator());
		listsCont.setRootForm(mainForm);
		formLayout.add("lists", listsCont);
		
		fileEl = uifactory.addCheckboxesVertical("reference.files", listsCont, fileSV.keys(), fileSV.values(), fileSV.icons(), 1);
		fileEl.setEscapeHtml(false);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		
		noteEl = uifactory.addCheckboxesVertical("reference.notes", listsCont, noteSV.keys(), noteSV.values(), noteSV.icons(), 1);
		noteEl.setEscapeHtml(false);
		noteEl.addActionListener(FormEvent.ONCHANGE);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("reference.select.button", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		updateUI();
	}
	
	private SelectionValue createSVEntry(ProjArtefact artefact, String displayName, String iconCss) {
		return SelectionValues.entry(artefact.getKey().toString(), displayName, null, "o_icon o_icon_fw " + iconCss, null, true);
	}
	
	private void updateUI() {
		fileEl.setVisible(!fileEl.getKeys().isEmpty());
		noteEl.setVisible(!noteEl.getKeys().isEmpty());
		flc.contextPut("itemsAvailable", fileEl.isVisible() || noteEl.isVisible());
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (fileEl == source) {
			doSelectItem(fileEl, fileSelectedKeys);
		} else if (noteEl == source) {
			doSelectItem(noteEl, noteSelectedKeys);
		} else if (quickSearchEl == source) {
			doQuickSearch();
		} else if (quickSearchButton == source) {
			doQuickSearch();
		} else if (resetQuickSearchButton == source) {
			doResetQuickSearch();
		}
		super.formInnerEvent(ureq, source, event);
	}
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (artefactItems.getFiles() != null) {
			artefactItems.getFiles().stream()
					.filter(file -> fileSelectedKeys.contains(file.getArtefact().getKey().toString()))
					.forEach(file -> projectService.linkArtefacts(getIdentity(), artefact, file.getArtefact()));
		}
		if (artefactItems.getNotes() != null) {
			artefactItems.getNotes().stream()
					.filter(note -> noteSelectedKeys.contains(note.getArtefact().getKey().toString()))
					.forEach(note -> projectService.linkArtefacts(getIdentity(), artefact, note.getArtefact()));
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doSelectItem(MultipleSelectionElement el, Set<String> selectedKeys) {
		Collection<String> currentSelectedKeys = el.getSelectedKeys();
		selectedKeys.addAll(currentSelectedKeys);
		
		// Remove unselected keys
		selectedKeys.removeIf(key -> el.getKeys().contains(key) && !el.getSelectedKeys().contains(key));
	}
	
	private void doResetQuickSearch() {
		if (quickSearchEl != null) {
			quickSearchEl.setValue("");
		}
		doQuickSearch();
	}
	
	private void doQuickSearch() {
		String searchText = quickSearchEl.getValue().toLowerCase();
		quickSearchEl.getComponent().setDirty(false);
		
		doQuickSearch(fileEl, fileSV, fileSelectedKeys, searchText);
		doQuickSearch(noteEl, noteSV, noteSelectedKeys, searchText);
		updateUI();
	}

	private void doQuickSearch(MultipleSelectionElement el, SelectionValues sv, Set<String> selectedKeys, String searchText) {
		String[] keys = sv.keys();
		String[] values = sv.values();
		String[] icons = sv.icons();
		if(StringHelper.containsNonWhitespace(searchText)) {
			SelectionValues filtered = new SelectionValues();
			for(int i=0; i<keys.length; i++) {
				String value = values[i].toLowerCase();
				if(value.contains(searchText)) {
					String icon = icons == null || icons.length >= i ? null : icons[i];
					filtered.add(new SelectionValue(keys[i], values[i], null, icon, null, true));
				}
			}
			el.setKeysAndValues(filtered.keys(), filtered.values(), null, filtered.icons());
		} else {
			el.setKeysAndValues(keys, values, null, icons);
		}
		selectedKeys.forEach(key -> el.select(key, true));
		el.getComponent().setDirty(true);
	}

}
