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
package org.olat.modules.cemedia.ui.medias;

import java.util.List;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StandardEditMediaController extends FormBasicController {
	
	private TextElement titleEl;
	private TagSelection tagsEl;
	private RichTextElement descriptionEl;

	private Media mediaReference;
	
	@Autowired
	private MediaService mediaService;
	
	public StandardEditMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MediaCenterController.class, getLocale(), getTranslator()));
		mediaReference = media;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = mediaReference == null ? null : mediaReference.getTitle();
		titleEl = uifactory.addTextElement("artefact.title", "artefact.title", 255, title, formLayout);
		titleEl.setMandatory(true);
		
		String desc = mediaReference == null ? null : mediaReference.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringData("artefact.descr", "artefact.descr", desc, 4, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		descriptionEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		
		List<TagInfo> tagsInfos = mediaService.getTagInfos(mediaReference, getIdentity(), false);
		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), tagsInfos);
		tagsEl.setHelpText(translate("categories.hint"));
		tagsEl.setElementCssClass("o_sel_ep_tagsinput");
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", "save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
		
	@Override
	protected void formOK(UserRequest ureq) {
		mediaReference.setTitle(titleEl.getValue());
		mediaReference.setDescription(descriptionEl.getValue());
		mediaReference = mediaService.updateMedia(mediaReference);

		List<String> updatedTags = tagsEl.getDisplayNames();
		mediaService.updateTags(getIdentity(), mediaReference, updatedTags);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
