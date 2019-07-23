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
package org.olat.portfolio.ui.artefacts.collect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.olat.core.util.StringHelper;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.springframework.beans.factory.annotation.Autowired;

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
	private TextBoxListElement tagListElement;
	@Autowired
	private EPFrontendManager ePFMgr;

	private static final String RUNCTX_TAGLIST_KEY = "artefactTagsList"; 
	
	public EPCollectStepForm01(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact) {
		super(ureq, wControl, "step01tagging");
		this.artefact = artefact;
		initForm(ureq);
	}
	
	public EPCollectStepForm01(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, AbstractArtefact artefact) {
		super(ureq, wControl, rootForm, runContext, FormBasicController.LAYOUT_CUSTOM, "step01tagging");
		this.artefact = artefact;
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		tagListElement = uifactory.addTextBoxListElement("artefact.tags", null, "tag.input.hint", getTagMapToDisplay(), formLayout, getTranslator());
		tagListElement.setElementCssClass("o_sel_ep_tagsinput");
		Map<String, String> allUsersTags = ePFMgr.getUsersMostUsedTags(getIdentity(), 50);
		tagListElement.setAutoCompleteContent(allUsersTags);
		tagListElement.setAllowDuplicates(false);
		
		// show a list of the 50 most used tags
		List<FormLink> userTagLinks = new ArrayList<>();
		int i = 0;
		for (Iterator<Entry<String, String>> iterator = allUsersTags.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, String> entry = iterator.next();
			String tag = StringHelper.escapeHtml(entry.getKey());
			FormLink tagLink = uifactory.addFormLink("tagU" + i, tag, null, formLayout, Link.NONTRANSLATED);
			tagLink.setUserObject(entry.getValue());
			userTagLinks.add(tagLink);
			i++;
		}
		flc.contextPut("userTagLinks", userTagLinks);
		
		if (!isUsedInStepWizzard()) {
			// add form buttons
			uifactory.addFormSubmitButton("stepform.submit", formLayout);
		}
	}

	/**
	 * returns a Map holding the tags for the TextBoxListComponent<br />
	 * The map will contain the tags that...
	 * <ul>
	 * <li>... are pre-set already through the wizard (user navigated back and
	 * forth)</li>
	 * <li>... are pre-set in the artefact (if artefact gets edited)</li>
	 * </ul>
	 * 
	 * @return
	 */
	private Map<String, String> getTagMapToDisplay() {

		Map<String, String> tagMap = new HashMap<>();
		Collection<String> tagCollection = null;
		Collection<String> preSetArtefactTags = ePFMgr.getArtefactTags(artefact);

		@SuppressWarnings("unchecked")
		Collection<String> runContextTags = isUsedInStepWizzard() ? (List<String>) getFromRunContext(RUNCTX_TAGLIST_KEY) : null;
		if (runContextTags != null) {
			// there are already tags in runContext, use those
			tagCollection = runContextTags;
		} else if (preSetArtefactTags != null) {
			tagCollection = preSetArtefactTags;
		}

		// now, if there are tags, put them in a map
		if (tagCollection != null) {
			for (String tag : tagCollection) {
				tagMap.put(tag, tag);
			}
		}

		return tagMap;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.form.flexible.FormItem,
	 *      org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {	
		if (source == tagListElement) {
			// nothing to do here, update dataModel on FormOK
		} else if (source instanceof FormLink) {
			
			// user clicked on a tag in the "50 most used tags"-list
			FormLink link = (FormLink) source;
			if (link.getName().startsWith("tag")) {
				List<String> currentTagsInComponent = tagListElement.getValueList();
				String newTagFromLink = (String) link.getUserObject();
				newTagFromLink = StringHelper.escapeHtml(newTagFromLink);
				newTagFromLink = StringHelper.escapeJavaScript(newTagFromLink);
				currentTagsInComponent.add(newTagFromLink);
				if(isUsedInStepWizzard()) {
					addToRunContext(RUNCTX_TAGLIST_KEY, currentTagsInComponent);
				}
				// refresh gui
				flc.setDirty(true);
				initForm(ureq);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		if(isUsedInStepWizzard()) {
			List<String> actualTagList = tagListElement.getValueList();
			if (actualTagList.size() != 0) {
				addToRunContext(RUNCTX_TAGLIST_KEY, actualTagList);
			}	
			// force repaint when navigating back and forth
			flc.setDirty(true);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else {
			List<String> tags = tagListElement.getValueList();
			ePFMgr.setArtefactTags(ureq.getIdentity(), artefact, tags);
			fireEvent(ureq, StepsEvent.DONE_EVENT);
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing
	}

}
