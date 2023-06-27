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
package org.olat.modules.cemedia.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.portfolio.ui.model.MediaRow;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeleteMediaController extends FormBasicController {
	
	private List<MediaRow> rowsToDelete;
	
	@Autowired
	private MediaService mediaService;
	
	public ConfirmDeleteMediaController(UserRequest ureq, WindowControl wControl, List<MediaRow> rowsToDelete) {
		super(ureq, wControl, "confirm_delete_media");
		this.rowsToDelete = rowsToDelete;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			String msg;
			if(rowsToDelete.size() == 1) {
				msg = translate("delete.media.confirm.descr",
						StringHelper.escapeHtml(rowsToDelete.get(0).getTitle()));
			} else {
				msg = translate("delete.media.confirm.plural", Integer.toString(rowsToDelete.size()));
			}
			layoutCont.contextPut("msg", msg);
		}
		
		uifactory.addFormSubmitButton("delete.media", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(MediaRow rowToDelete:rowsToDelete) {
			Media media = mediaService.getMediaByKey(rowToDelete.getKey());
			mediaService.deleteMedia(media);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
