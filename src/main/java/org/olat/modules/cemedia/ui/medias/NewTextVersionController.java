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

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NewTextVersionController extends FormBasicController {
	
	private TextElement contentEl;
	
	private Media media;
	private final MediaHandler handler;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaService mediaService;
	
	public NewTextVersionController(UserRequest ureq, WindowControl wControl, Media media,
			MediaHandler handler) {
		super(ureq, wControl, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale()));
		this.media = media;
		this.handler = handler;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		contentEl = uifactory.addRichTextElementForStringData("content", "content", null, 10, 6, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		contentEl.setElementCssClass("o_sel_media_content");

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("add.version." + handler.getType(), buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String content = contentEl.getValue();
		media = mediaService.getMediaByKey(media.getKey());
		mediaService.addVersion(media, content);
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
