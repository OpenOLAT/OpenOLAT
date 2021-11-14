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
package org.olat.user.ui.data;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.WebappHelper;
import org.olat.user.UserDataExport;
import org.olat.user.UserDataExportService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserDataController extends FormBasicController {
	
	private final Identity dataIdentity;
	
	private FormLink downloadButton;
	
	@Autowired
	private UserDataExportService userDataExportService;
	
	public UserDataController(UserRequest ureq, WindowControl wControl, Identity dataIdentity) {
		super(ureq, wControl, "user_data");
		this.dataIdentity = dataIdentity;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			UserDataExport data = userDataExportService.getCurrentData(dataIdentity);
			layoutCont.contextPut("data", Boolean.valueOf(data != null));
			layoutCont.contextPut("mailSupport", WebappHelper.getMailConfig("mailSupport"));
			if(data == null) {
				layoutCont.contextPut("dataStatus", UserDataExport.ExportStatus.none.name());
			} else {
				layoutCont.contextPut("dataStatus", data.getStatus().name());
			}
			
			downloadButton = uifactory.addFormLink("download.data", layoutCont, Link.BUTTON);
			downloadButton.setIconLeftCSS("o_icon o_icon_download");
			downloadButton.setVisible(data != null && data.getStatus() == UserDataExport.ExportStatus.ready);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(downloadButton == source) {
			doDownload(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doDownload(UserRequest ureq) {
		MediaResource resource = userDataExportService.getDownload(dataIdentity);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
}
