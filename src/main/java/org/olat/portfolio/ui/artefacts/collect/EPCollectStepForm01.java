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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.portfolio.ui.artefacts.collect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;

/**
 * Description:<br>
 * controller to provide tag-suggestion and let user select tags for this
 * artefact
 * 
 * <P>
 * Initial Date: 27.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPCollectStepForm01 extends StepFormBasicController {

	private AbstractArtefact artefact;
	private EPFrontendManager ePFMgr;
	private TextBoxListElement tagC;

	@SuppressWarnings("unused")
	public EPCollectStepForm01(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, int layout,
			String customLayoutPageName, AbstractArtefact artefact) {
		super(ureq, wControl, rootForm, runContext, FormBasicController.LAYOUT_CUSTOM, "step01tagging");
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");

		this.artefact = artefact;
		initForm(this.flc, this, ureq);
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	@SuppressWarnings("unused")
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Map<String, String> tagLM = new HashMap<String, String>();
		Collection<String> itemsToUse = null;
		Collection<String> initialItems = ePFMgr.getArtefactTags(artefact);
		@SuppressWarnings("unchecked")
		Collection<String> setTags = (List<String>) getFromRunContext("artefactTagsList");
		if (setTags != null) {
			// set some tags in wizzard already, use those
			itemsToUse = setTags;
		} else if (initialItems != null) {
			itemsToUse = initialItems;
		}
		if (itemsToUse != null) {
			for (String tag : itemsToUse) {
				tagLM.put(tag, tag);
			}
		}
		tagC = uifactory.addTextBoxListElement("artefact.tags", null, "tag.input.hint", tagLM, formLayout, getTranslator());
		tagC.setNoFormSubmit(true);
		tagC.addActionListener(this, FormEvent.ONCHANGE);
		Map<String, String> allUsersTags = ePFMgr.getUsersMostUsedTags(getIdentity(), 50);
		tagC.setAutoCompleteContent(allUsersTags);
		
		// show a list of the 50 most used tags to be clickable
		List<FormLink> userTagLinks = new ArrayList<FormLink>();
		int i=0;
		for (Iterator<Entry<String, String>> iterator = allUsersTags.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, String> entry = iterator.next();
			String tag = entry.getKey();
			FormLink tagLink = uifactory.addFormLink("tagU" +i, tag, null, formLayout, Link.NONTRANSLATED);
			tagLink.setUserObject(entry.getValue());
			userTagLinks.add(tagLink);				
			i++;
		}
		this.flc.contextPut("userTagLinks", userTagLinks);
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.components.form.flexible.FormItem, org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source==tagC){
			FormEvent event2 = event;
			logInfo("got", event2.toString());
		} else if (source instanceof FormLink){
			FormLink link = (FormLink) source;
			if (link.getName().startsWith("tag")){
				List<String> actualTagList = tagC.getValueList();
				String tag = (String) link.getUserObject();
				List<String> setTags = new ArrayList<String>(); 
				if (containsRunContextKey("artefactTagsList")){
					setTags = (List<String>) getFromRunContext("artefactTagsList");
				} 
				setTags.add(tag);
				// merge actual tags with presets
				if (actualTagList.size()!=0) setTags.addAll(actualTagList);
				removeDuplicate(setTags);
				addToRunContext("artefactTagsList", setTags);
				//refresh gui
				this.flc.setDirty(true);
				initForm(ureq);
			}
		}
	}
	
  private static void removeDuplicate(List<String> arlList)  {
   HashSet<String> h = new HashSet<String>(arlList);
   arlList.clear();
   arlList.addAll(h);
  }

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		List<String> actualTagList = tagC.getValueList();
		if (!containsRunContextKey("artefactTagsList")){
			//only add on first run, as it will get overwritten later on!			
			addToRunContext("artefactTagsList", actualTagList);
		} else {
			// try to update on changes, but not with empty list -> this is the case while validating other steps
			if (actualTagList.size() != 0) {
				addToRunContext("artefactTagsList", actualTagList);
			}
			
		}
		// force repaint when navigating back and forth
		this.flc.setDirty(true);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//nothing
	}

}
