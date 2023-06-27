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
package org.olat.modules.portfolio.ui.wizard;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaInformations;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CollectArtefactController extends FormBasicController {
	
	private TagSelection tagsEl;
	private TextElement titleEl;
	private TextElement altTextEl;
	private TextElement descriptionEl;

	private Media mediaReference;
	private final Object mediaObject;
	private final MediaHandler handler;
	
	private final String businessPath;
	private final MediaInformations prefillInfos;
	
	@Autowired
	private MediaService mediaService;

	public CollectArtefactController(UserRequest ureq, WindowControl wControl, Object mediaObject, MediaHandler handler, String businessPath) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(PortfolioHomeController.class, getLocale(),
				Util.createPackageTranslator(MediaCenterController.class, getLocale(), getTranslator())));
		this.handler = handler;
		this.mediaObject = mediaObject; 
		this.businessPath = businessPath;
		
		prefillInfos = handler.getInformations(mediaObject);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_pf_collect_media_form");
		
		String title = prefillInfos == null ? "" : prefillInfos.getTitle();
		titleEl = uifactory.addTextElement("artefact.title", "artefact.title", 255, title, formLayout);
		titleEl.setElementCssClass("o_sel_pf_collect_media_title");
		titleEl.setMandatory(true);
		
		String descr = prefillInfos == null ? "" : prefillInfos.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringData("artefact.descr", "artefact.descr", descr, 8, 6, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.setElementCssClass("o_sel_pf_collect_media_description");

		String altText = mediaReference == null ? null : mediaReference.getAltText();
		altTextEl = uifactory.addTextElement("artefact.alt.text", "artefact.alt.text", 1000, altText, formLayout);
		
		List<TagInfo> tagsInfos = mediaService.getTagInfos(mediaReference);
		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), tagsInfos);
		tagsEl.setHelpText(translate("tags.hint"));
		tagsEl.setElementCssClass("o_sel_ep_tagsinput");
		
		String source = handler.getType();
		uifactory.addStaticTextElement("artefact.source", "artefact.source", source, formLayout);
		String date = Formatter.getInstance(getLocale()).formatDate(new Date());
		uifactory.addStaticTextElement("artefact.collect.date", "artefact.collect.date", date, formLayout);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", "save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(mediaReference == null) {
			String title = titleEl.getValue();
			String altText = altTextEl.getValue();
			String description = descriptionEl.getValue();
			mediaReference = handler.createMedia(title, description, altText, mediaObject, businessPath, getIdentity());
		}

		if(mediaReference != null) {
			List<String> updatedTags = tagsEl.getDisplayNames();
			mediaService.updateTags(getIdentity(), mediaReference, updatedTags);
		} else {
			showError("ERROR");
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
