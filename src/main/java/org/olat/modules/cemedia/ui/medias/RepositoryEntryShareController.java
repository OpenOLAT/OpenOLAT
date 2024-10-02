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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryShareController extends FormBasicController {
	
	private MultipleSelectionElement editableEl;
	
	private final Media media;
	private final RepositoryEntry repositoryEntry;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaService mediaService;
	
	public RepositoryEntryShareController(UserRequest ureq, WindowControl wControl, Media media, RepositoryEntry repositoryEntry) {
		super(ureq, wControl, "share_confirmation", Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale()));
		this.media = media;
		this.repositoryEntry = repositoryEntry;
		initForm(ureq);
	}
	
	public Media getMedia() {
		return media;
	}

	public String getTitle() {
		return translate("share.confirm.title." + media.getType());
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String type = media.getType();
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("msg", translate("share.confirm.description." + type));
		}
		
		SelectionValues shareKV = new SelectionValues();
		shareKV.add(SelectionValues.entry("true", translate("share.confirm.editable." + type)));
		editableEl = uifactory.addCheckboxesHorizontal("share.confirm.editable", null, formLayout, shareKV.keys(), shareKV.values());
		
		uifactory.addFormSubmitButton("share.confirm", formLayout);
		uifactory.addFormCancelButton("no", formLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		boolean editable = editableEl.isAtLeastSelected(1);
		mediaService.addRelation(media, editable, repositoryEntry);
		dbInstance.commitAndCloseSession();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
