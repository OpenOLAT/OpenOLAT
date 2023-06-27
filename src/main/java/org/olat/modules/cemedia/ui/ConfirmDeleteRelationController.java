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
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;
import org.olat.modules.cemedia.model.MediaShare;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeleteRelationController extends FormBasicController {
	
	private final Media media;
	private final MediaShare share;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private MediaService mediaService;
	
	public ConfirmDeleteRelationController(UserRequest ureq, WindowControl wControl, Media media, MediaShare share) {
		super(ureq, wControl, "confirm_delete_share");
		this.share = share;
		this.media = media;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			String msg = null;
			if(share.getType() == MediaToGroupRelationType.USER) {
				String fullname = userManager.getUserDisplayName(share.getUser());
				msg = translate("confirm.remove.relation.user",
						StringHelper.escapeHtml(fullname));
			} else if(share.getType() == MediaToGroupRelationType.BUSINESS_GROUP) {
				msg = translate("confirm.remove.relation.group",
						StringHelper.escapeHtml(share.getBusinessGroup().getName()));
			}else if(share.getType() == MediaToGroupRelationType.ORGANISATION) {
				msg = translate("confirm.remove.relation.organisation",
						StringHelper.escapeHtml(share.getOrganisation().getDisplayName()));
			}
			layoutCont.contextPut("msg", msg);
		}
		
		uifactory.addFormSubmitButton("delete.share", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(share.getType() == MediaToGroupRelationType.USER) {
			mediaService.removeRelation(media, share.getUser());
		} else if(share.getType() == MediaToGroupRelationType.BUSINESS_GROUP) {
			mediaService.removeRelation(media, share.getBusinessGroup());
		} else if(share.getType() == MediaToGroupRelationType.ORGANISATION) {
			mediaService.removeRelation(media, share.getOrganisation());
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
