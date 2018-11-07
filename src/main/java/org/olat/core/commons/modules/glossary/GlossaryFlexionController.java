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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.olat.core.commons.modules.glossary.morphService.MorphologicalService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Shows existings flexions and manages the process of requesting flexions and
 * selecting them.
 * 
 * <P>
 * Initial Date: 07.12.2008 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GlossaryFlexionController extends FormBasicController {

	private GlossaryItem glossaryItem;
	private FormItem flexButton;
	private MultipleSelectionElement existingFlexions;
	private MorphologicalService morphService;
	private List<String> flexionsMSResult = null;
	private FormLink selectAllLink;
	private FormLink deselectAllLink;
	private String morphServicePresetIdent;
	private SingleSelection selectMS;
	private Properties glossProps;
	private VFSContainer glossaryFolder;
	
	@Autowired
	private GlossaryItemManager glossaryItemManager;

	protected GlossaryFlexionController(UserRequest ureq, WindowControl control, GlossaryItem glossaryItem, VFSContainer glossaryFolder) {
		super(ureq, control, "editFlexion");
		this.glossaryItem = glossaryItem;
		this.glossaryFolder = glossaryFolder;
		
		glossProps = glossaryItemManager.getGlossaryConfig(glossaryFolder);
		String configuredMS = glossProps.getProperty(GlossaryItemManager.MS_KEY);
		// a MS was configured for this glossary, check if its enabled as bean in global glossary-config
		List<MorphologicalService> morphServices = GlossaryModule.getMorphologicalServices();
		for (Iterator<MorphologicalService> iterator = morphServices.iterator(); iterator.hasNext();) {
			MorphologicalService fsMgr = iterator.next();
			// if none was preselected, just use the first in list
			if (morphService == null) morphService = fsMgr; 
			if (fsMgr.getMorphServiceIdentifier().equals(configuredMS)){
				morphService = fsMgr;
				morphServicePresetIdent = fsMgr.getMorphServiceIdentifier();
			}
		}
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.form.flexible.FormItem,
	 *      org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == flexButton) {
			String glossaryTerm = glossaryItem.getGlossTerm();
			if (StringHelper.containsNonWhitespace(glossaryTerm)) {
				String partOfSpeech = morphService.assumePartOfSpeech(glossaryTerm);
				flexionsMSResult = morphService.getFlexions(partOfSpeech, glossaryTerm);
				String replyStatus = morphService.getReplyStatus();
				if (replyStatus.equals(MorphologicalService.STATUS_ERROR)) {
					showError("flexions.error");
					flexionsMSResult = null;
				} else {
					if (replyStatus.equals(MorphologicalService.STATUS_GUESSED)) {
						showInfo("flexions.guessed");
					}

					if (flexionsMSResult.isEmpty()){
						showError("flexions.answer.error");
						logError("Check reply from flexion service, there is a reply, but with an empty list! Contact flexion-service provider.", null);
						flexionsMSResult = null;
					}
					// update flexion checkboxes-list by re-initialising
					this.flc.setDirty(true);
					fireEvent(ureq, FormEvent.RESET);
					initForm(ureq);
				}
			} else {
				showWarning("flexions.impossible.without.term");
				flexionsMSResult = null;
			}
			if (Settings.isDebuging()) {
				logDebug("Flexion GET triggered!", null);
			}
		} else if (source == selectAllLink) {
			existingFlexions.selectAll();
			saveSelectedFlexions();
			this.flc.contextPut("existing.flexions.checkboxes", existingFlexions);
		} else if (source == deselectAllLink) {
			existingFlexions.uncheckAll();
			saveSelectedFlexions();
			this.flc.contextPut("existing.flexions.checkboxes", existingFlexions);
		} else if (source == existingFlexions){
			saveSelectedFlexions();
		} else if (source == selectMS){
			// change the to be used morphological service for this item. 
			List<MorphologicalService> morphServices = GlossaryModule.getMorphologicalServices();
			for (Iterator<MorphologicalService> iterator = morphServices.iterator(); iterator.hasNext();) {
				MorphologicalService fsMgr = iterator.next();
				if (fsMgr.getMorphServiceIdentifier().equals(selectMS.getSelectedKey())){
					morphServicePresetIdent = fsMgr.getMorphServiceIdentifier(); // keep selection when recalling initForm later on
					morphService = fsMgr;					
				}
			}
			if (!selectMS.getSelectedKey().equals(glossProps.getProperty(GlossaryItemManager.MS_KEY))){
				// change occurred, persist this
				glossProps.setProperty(GlossaryItemManager.MS_KEY, selectMS.getSelectedKey());
				glossaryItemManager.setGlossaryConfig(glossaryFolder, glossProps);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// done in innerEvent
	}

	/**
	 * save only flexions which are active:
	 */
	private void saveSelectedFlexions() {
		if (existingFlexions != null) {
			Collection<String> choosedFlexions = existingFlexions.getSelectedKeys();
			ArrayList<String> glossItemFlexionsToSave = new ArrayList<>(choosedFlexions.size());
			glossItemFlexionsToSave.addAll(choosedFlexions);
			glossaryItem.setGlossFlexions(glossItemFlexionsToSave);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("flexions.title");
    
		// let the user choose another service for this entry. helpful for language-mixed glossaries
		List<MorphologicalService> morphServices = GlossaryModule.getMorphologicalServices();
		String[] msKeys = new String[morphServices.size()];
		String[] msValues = new String[morphServices.size()];
		String[] msCSS = new String[morphServices.size()];
		if (morphServices != null && !morphServices.isEmpty()){
			int i=0;
			for (Iterator<MorphologicalService> iterator = morphServices.iterator(); iterator.hasNext();) {
				MorphologicalService fsMgr = iterator.next();
				msKeys[i] = fsMgr.getMorphServiceIdentifier();
				msValues[i] = fsMgr.getMorphServiceDescriptor();
				i++;
			}
			selectMS = uifactory.addDropdownSingleselect("morph.service", formLayout, msKeys, msValues, msCSS);
			if (Arrays.asList(msKeys).contains(morphServicePresetIdent)){
				selectMS.select(morphServicePresetIdent, true);
			}
			selectMS.addActionListener(FormEvent.ONCHANGE);
			
			flexButton = uifactory.addFormLink("flexions.get.button", formLayout, Link.BUTTON);
		}		
		
		//combining flexion list from already existing and newly fetched
		List<String> glossItemFlexions = new ArrayList<>(glossaryItem.getGlossFlexions());
		if (!glossItemFlexions.isEmpty() || flexionsMSResult != null) {
			String[] existingKeys = ArrayHelper.toArray(glossItemFlexions);
			if (flexionsMSResult != null) glossItemFlexions.addAll(flexionsMSResult);
			removeDuplicate(glossItemFlexions);
			Collections.sort(glossItemFlexions);
			String[] flexionKeys = ArrayHelper.toArray(glossItemFlexions);
			String[] flexionValues = new String[flexionKeys.length];
			for (int i = 0; i < flexionKeys.length; i++) {
				flexionValues[i] = glossItemFlexions.get(i);
			}
			existingFlexions = uifactory.addCheckboxesVertical("existing.flexions.checkboxes", null, formLayout,
					flexionKeys, flexionValues, 1);
			existingFlexions.addActionListener(FormEvent.ONCLICK);
			for (String flexKey : existingKeys) {
				existingFlexions.select(flexKey, true);
			}

			selectAllLink = uifactory.addFormLink("flexions.select.all", formLayout, Link.LINK);
			deselectAllLink = uifactory.addFormLink("flexions.select.none", formLayout, Link.LINK);
		}
	}

	/**
	 * internal method to remove Duplicates from list
	 * @param arlList
	 */
	private static void removeDuplicate(List<String> arlList) {
		Set<String> h = new HashSet<>(arlList);
		arlList.clear();
		arlList.addAll(h);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}

}
