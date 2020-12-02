/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.repository.ui.catalog;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.repository.CatalogEntry;
import org.olat.repository.CatalogEntry.Style;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.manager.CatalogManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Description: <br>
 * The form allows to edit, create respective, a new catalog entry, which is
 * either a category or an alias for the linked repository entry. Further it is
 * abused as input form for import feature within the catalog.
 * <p>
 * 
 * Initial Date: Oct 3, 2004 <br>
 * @author patrick
 */

public class CatalogEntryEditController extends FormBasicController {
	
	private static final int picUploadlimitKB = 5024;
	private static final Set<String> mimeTypes = new HashSet<>();
	static {
		mimeTypes.add("image/gif");
		mimeTypes.add("image/jpg");
		mimeTypes.add("image/jpeg");
		mimeTypes.add("image/png");
	}
	
	private static final String[] styleKeys = new String[]{
		Style.tiles.name(), Style.list.name(), Style.compact.name()
	};
	private static final String[] sortSelectKeys = new String[]{
			"add.default", "add.alphabetically", "add.top", "add.bottom"
	};

	private TextElement nameEl;
	private TextElement shortTitleEl;
	private SingleSelection styleEl;
	private RichTextElement descriptionEl;
	private FileElement fileUpload;

	private SingleSelection nodesSortSelect;
	private SingleSelection entriesSortSelect;

	private CatalogEntry parentEntry;
	private CatalogEntry catalogEntry;
	
	@Autowired
	private CatalogManager catalogManager;
	@Autowired
	private RepositoryModule repositoryModule;
	
	public CatalogEntryEditController(UserRequest ureq, WindowControl wControl, CatalogEntry entry) {
		this(ureq, wControl, entry, null);
	}
	
	public CatalogEntryEditController(UserRequest ureq, WindowControl wControl, CatalogEntry entry, CatalogEntry parentEntry) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryManager.class, ureq.getLocale()));
		
		this.catalogEntry = entry;
		this.parentEntry = parentEntry;
		initForm (ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormStyle("o_catalog");
		String name = catalogEntry == null ? "" : catalogEntry.getName();
		nameEl = uifactory.addTextElement("name", "entry.category", 200, name, formLayout);
		nameEl.setElementCssClass("o_sel_cat_name");
		nameEl.setMandatory(true);
		nameEl.setNotEmptyCheck("form.legende.mandatory");
		
		String shortTitle = catalogEntry == null ? "" : catalogEntry.getShortTitle() != null ? catalogEntry.getShortTitle() : "";
		// max longer than actual max because html input maxsize does not normalize (ü count as 2 characters)
		shortTitleEl = uifactory.addTextElement("shortTitle", "entry.shorttitle", 40, shortTitle, formLayout); 
		shortTitleEl.setElementCssClass("o_sel_cat_short_title");
		shortTitleEl.setMandatory(true);
		shortTitleEl.setNotEmptyCheck("form.legende.mandatory");
		shortTitleEl.setHelpText(translate("catalog.popup.edit.shorttitle.desc"));
		
		String desc = catalogEntry == null ? "" : catalogEntry.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("description", "entry.description", desc, 10, -1, formLayout, getWindowControl());
		
		String[] styleValues = new String[]{ translate("tiles"), translate("list"), translate("list.compact")};
		styleEl = uifactory.addDropdownSingleselect("style", "style", formLayout, styleKeys, styleValues, null);
		Style style = catalogEntry == null ? null : catalogEntry.getStyle();
		if(style != null) {
			for(String styleKey:styleKeys) {
				if(styleKey.equals(style.name())) {
					styleEl.select(styleKey, true);
				}
			}
		}
		if(!styleEl.isOneSelected()) {
			styleEl.select(styleKeys[0], true);
		}

		String[] translatedNodeKeys = TranslatorHelper.translateAll(getTranslator(), sortSelectKeys);
		String[] translatedEntryKeys = TranslatorHelper.translateAll(getTranslator(), sortSelectKeys);

		translatedNodeKeys[0] += " (" + translate(sortSelectKeys[repositoryModule.getCatalogAddCategoryPosition() + 1]) + ")";
		translatedEntryKeys[0] += " (" + translate(sortSelectKeys[repositoryModule.getCatalogAddEntryPosition() + 1]) + ")";

		nodesSortSelect = uifactory.addDropdownSingleselect("sort.nodes", formLayout, sortSelectKeys, translatedNodeKeys);
		entriesSortSelect = uifactory.addDropdownSingleselect("sort.entries", formLayout, sortSelectKeys, translatedEntryKeys);

		VFSLeaf img = catalogEntry == null || catalogEntry.getKey() == null ? null : catalogManager.getImage(catalogEntry);
		fileUpload = uifactory.addFileElement(getWindowControl(), getIdentity(), "entry.pic", "entry.pic", formLayout);
		fileUpload.setMaxUploadSizeKB(picUploadlimitKB, null, null);
		fileUpload.addActionListener(FormEvent.ONCHANGE);
		fileUpload.setPreview(ureq.getUserSession(), true);
		fileUpload.setCropSelectionEnabled(true);
		fileUpload.setDeleteEnabled(true);
		if(img instanceof LocalFileImpl) {
			fileUpload.setInitialFile(((LocalFileImpl)img).getBasefile());
		}
		fileUpload.limitToMimeType(mimeTypes, "cif.error.mimetype", new String[]{ mimeTypes.toString()} );

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		buttonLayout.setElementCssClass("o_sel_catalog_entry_form_buttons");
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());

		if (catalogEntry.getCategoryAddPosition() != null) {
			nodesSortSelect.select(sortSelectKeys[catalogEntry.getCategoryAddPosition() + 1], true);
		}
		if (catalogEntry.getEntryAddPosition() != null) {
			entriesSortSelect.select(sortSelectKeys[catalogEntry.getEntryAddPosition() + 1], true);
		}
	}
	
	public CatalogEntry getEditedCatalogEntry() {
		return catalogEntry;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public void setElementCssClass(String cssClass) {
		flc.setElementCssClass(cssClass);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateTextInput(nameEl, 100);
		allOk &= validateTextInput(shortTitleEl, 20);
		
		styleEl.clearError();
		if(!styleEl.isOneSelected()) {
			styleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}
	
	private boolean validateTextInput(TextElement textElement, int lenght) {		
		textElement.clearError();
		if(StringHelper.containsNonWhitespace(nameEl.getValue())) {
			if(textElement.getValue().length() > lenght) {
				textElement.setErrorKey("input.toolong", new String[]{ String.valueOf(lenght)});
				return false;
			}
		} else {
			textElement.setErrorKey("form.legende.mandatory", null);
			return false;
		}
		
		return true;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == fileUpload) {
			if(FileElementEvent.DELETE.equals(event.getCommand())) {
				VFSLeaf img = catalogManager.getImage(catalogEntry);
				if(fileUpload.getUploadFile() != null && fileUpload.getUploadFile() != fileUpload.getInitialFile()) {
					fileUpload.reset();
					if(img != null) {
						fileUpload.setInitialFile(((LocalFileImpl)img).getBasefile());
					}
				} else if(img != null) {
					catalogManager.deleteImage(catalogEntry);
					fileUpload.setInitialFile(null);
				}
				flc.setDirty(true);
				fileUpload.clearError();
			} else if (fileUpload.isUploadSuccess()) {
				flc.setDirty(true);
				fileUpload.clearError();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(catalogEntry.getKey() != null) {
			catalogEntry = catalogManager.getCatalogEntryByKey(catalogEntry.getKey());
		}
		catalogEntry.setName(nameEl.getValue());
		if(styleEl.isOneSelected()) {
			catalogEntry.setStyle(Style.valueOf(styleEl.getSelectedKey()));
		} else {
			catalogEntry.setStyle(null);
		}
		catalogEntry.setDescription(descriptionEl.getValue());
		catalogEntry.setShortTitle(shortTitleEl.getValue());

		if (!nodesSortSelect.getSelectedKey().equals(sortSelectKeys[0])) {
			catalogEntry.setCategoryAddPosition(Arrays.asList(sortSelectKeys).indexOf(nodesSortSelect.getSelectedKey()) - 1);
		} else {
			catalogEntry.setCategoryAddPosition(null);
		}
		if (!entriesSortSelect.getSelectedKey().equals(sortSelectKeys[0])) {
			catalogEntry.setEntryAddPosition(Arrays.asList(sortSelectKeys).indexOf(entriesSortSelect.getSelectedKey()) - 1);
		} else {
			catalogEntry.setEntryAddPosition(null);
		}
		
		if(catalogEntry.getKey() == null) {
			//a new one
			catalogEntry.setRepositoryEntry(null);
			catalogEntry = catalogManager.addCatalogCategory(catalogEntry, parentEntry);
		} else {
			catalogEntry = catalogManager.updateCatalogEntry(catalogEntry);
		}
		
		File uploadedFile = fileUpload.getUploadFile();
		if(uploadedFile != null) {
			VFSContainer tmpHome = new LocalFolderImpl(new File(WebappHelper.getTmpDir()));
			VFSContainer tmpContainer = tmpHome.createChildContainer(UUID.randomUUID().toString());
			VFSLeaf newFile = fileUpload.moveUploadFileTo(tmpContainer, true);//give it it's real name and extension
			if(newFile != null) {
				boolean ok = catalogManager.setImage(newFile, catalogEntry);
				if (!ok) {
					showError("error.download.image");
				}
			} else {
				logError("Cannot move and or crop: " + fileUpload.getUploadFileName() + " ( " + fileUpload.getUploadMimeType() + " )", null);
				showError("error.download.image");
			}
			tmpContainer.deleteSilently();
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}

