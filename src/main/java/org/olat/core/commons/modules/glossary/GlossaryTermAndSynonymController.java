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

package org.olat.core.commons.modules.glossary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Part of the tabedPane, used to edit the glossTerm and glossSynonyms
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GlossaryTermAndSynonymController extends FormBasicController {

	private GlossaryItem glossaryItem;
	private TextElement glossaryTermField;
	private TextElement newSynonymField;
	private List<String> glossItemSynonyms;
	private List<TextElement> synonymTextElementList;
	private VFSContainer glossaryFolder;
	private GlossaryItem duplicateGlossItem;
	private static final String CMD_DELETE_SYNONYM = "delete.synonym.";
	private static final String SYNONYM_TEXT_ELEMENT = "synonym.";
	private final boolean add;
	
	@Autowired
	private GlossaryItemManager glossaryItemManager;

	protected GlossaryTermAndSynonymController(UserRequest ureq, WindowControl control, GlossaryItem glossaryItem, VFSContainer glossaryFolder,
			boolean add) {
		super(ureq, control, FormBasicController.LAYOUT_VERTICAL);
		this.add = add;
		this.glossaryItem = glossaryItem;
		this.glossaryFolder = glossaryFolder;
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String glossTerm = glossaryTermField.getValue().trim();
		if (StringHelper.containsNonWhitespace(glossTerm)) {
			if (!glossTerm.equals(glossaryItem.getGlossTerm()) && !glossaryItem.getGlossFlexions().isEmpty()) {
				showWarning("flexions.warn.after.changed.term");
			}
			glossaryItem.setGlossTerm(glossTerm);
		}
		
		// save all changes made in existing synonyms
		int oldSynonymCount = glossItemSynonyms.size();
		glossItemSynonyms = new ArrayList<>();
		for (int i = 0; i < oldSynonymCount; i++) {
			String textElementValue = synonymTextElementList.get(i).getValue().trim();
			if (StringHelper.containsNonWhitespace(textElementValue)) {
				glossItemSynonyms.add(textElementValue);
			}
		}
		String newSynonym = newSynonymField.getValue().trim();
		if (StringHelper.containsNonWhitespace(newSynonym)) {
			glossItemSynonyms.add(newSynonym);
		}

		// remove duplicates and sort
		removeDuplicate(glossItemSynonyms);
		Collections.sort(glossItemSynonyms);

		// update synonym-list and re-initialize
		glossaryItem.setGlossSynonyms(glossItemSynonyms);
		createOrUpdateSynonymLayout(this.flc, glossItemSynonyms);
		
		Revision revision = new Revision();
		revision.setAuthor(new Author(getIdentity()));
		revision.setJavaDate(new Date());
		if(add) {
			revision.setRevisionflag("added");
		} else {
			revision.setRevisionflag("changed");
		}
		
		glossaryItem.getRevHistory().add(revision);
		
		if (!checkForDuplicatesInGlossary()){
			showError("term.error.alreadyused", duplicateGlossItem.getGlossTerm());
			glossaryTermField.setErrorKey("term.error.alreadyused", new String[]{duplicateGlossItem.getGlossTerm()} );
		} else fireEvent(ureq, new Event("termOK"));
	}
	
	/**
	 * looks up complete glossary for same flexion, synonym or term
	 * 
	 * @return
	 */
	private boolean checkForDuplicatesInGlossary(){
		List<String> allOfThisItem = glossaryItem.getAllStringsToMarkup();		
		List<GlossaryItem> glossaryItemList = glossaryItemManager.getGlossaryItemListByVFSItem(glossaryFolder);
		int foundItselfCounter = 0;
		int foundAnother = 0;
		for (Iterator<GlossaryItem> iterator = glossaryItemList.iterator(); iterator.hasNext();) {
			GlossaryItem tmpItem = iterator.next();
			List<String> currentAllList = tmpItem.getAllStringsToMarkup();
			if (tmpItem.equals(glossaryItem) ){
				if (! Collections.disjoint(allOfThisItem, currentAllList)) foundItselfCounter++;
				else foundAnother++;
			}
			if (foundItselfCounter > 1 || foundAnother != 0 ) {
				duplicateGlossItem = tmpItem;
				return false;		
			}
			if (!tmpItem.equals(glossaryItem) ){
				if (! Collections.disjoint(allOfThisItem, currentAllList) ) {
					duplicateGlossItem = tmpItem;
					return false;		
				}
			} 
		}
		return true;
	}

	@Override
	protected void formNOK(UserRequest ureq) {
		// disable other tabs
		fireEvent(ureq, new Event("termNOK"));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			// a delete link has been pressed, remove textElement and synonym
			FormLink delButton = (FormLink) source;
			String synonymToDelete = (String) delButton.getUserObject();
			glossItemSynonyms.remove(synonymToDelete);
			createOrUpdateSynonymLayout(flc, glossItemSynonyms);
		}  
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("term.and.synonyms.title");
		setFormDescription("term.and.synonyms.intro");

		glossaryTermField = uifactory.addTextElement("glossTerm.inputfield", "glossary.form.glossaryKey", 200, glossaryItem.getGlossTerm(),
				formLayout);
		glossaryTermField.setNotEmptyCheck("glossary.form.error.notEmpty");
		glossaryTermField.setMandatory(true);

		uifactory.addStaticTextElement("space", null, "&nbsp;", formLayout);//null > no label

		
		glossItemSynonyms = glossaryItem.getGlossSynonyms();
		createOrUpdateSynonymLayout(formLayout, glossItemSynonyms);
	}

	private void createOrUpdateSynonymLayout(FormItemContainer formLayout, List<String> glossItemSynonymsToUse){
		FormUIFactory formUIf = FormUIFactory.getInstance();
		FormItem synLay = formLayout.getFormComponent("synonymLayout");
		if (synLay != null) {
			formLayout.remove("synonymLayout");
		}
		FormLayoutContainer tmpLayout = FormLayoutContainer.createCustomFormLayout("synonymLayout", getTranslator(), velocity_root
				+ "/editSynonym.html");
		formLayout.add(tmpLayout);
		tmpLayout.setLabel("glossary.term.synonym", null);
				
		Collections.sort(glossItemSynonymsToUse);
		tmpLayout.contextPut("glossItemSynonyms", glossItemSynonymsToUse);

		// add input fields with existing synonyms
		synonymTextElementList = new ArrayList<>(glossItemSynonymsToUse.size());
		for (int synNum = 1; synNum < glossItemSynonymsToUse.size() + 1; synNum++) {
			TextElement tmpSynonymTE = formUIf.addTextElement(SYNONYM_TEXT_ELEMENT + synNum, null, 100, glossItemSynonymsToUse.get(synNum - 1), tmpLayout);
			synonymTextElementList.add(tmpSynonymTE);
		}

		// add delete-links for existing synonyms
		for (int linkNum = 1; linkNum < glossItemSynonymsToUse.size() + 1; linkNum++) {
			FormLink tmpRemoveButton = formUIf.addFormLink(CMD_DELETE_SYNONYM + linkNum, tmpLayout, Link.BUTTON_XSMALL);
			tmpRemoveButton.setUserObject(glossItemSynonymsToUse.get(linkNum - 1));
			tmpRemoveButton.setI18nKey("synonym.link.delete");
		}

		// add input field for new synonym
		newSynonymField = formUIf.addTextElement("synonym.inputfield", "glossary.term.synonym", 100, "", tmpLayout);
		newSynonymField.setVisible(true);
		newSynonymField.setLabel("synonym.inputfield", null);		
		
		formUIf.addFormSubmitButton("form.submit", tmpLayout);
	}

	private static void removeDuplicate(List<String> arlList) {
		Set<String> h = new HashSet<>(arlList);
		arlList.clear();
		arlList.addAll(h);
	}
}
