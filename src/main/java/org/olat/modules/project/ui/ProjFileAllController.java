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
package org.olat.modules.project.ui;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;

/**
 * 
 * Initial date: 13 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjFileAllController extends ProjFileListController {
	
	private FormLink uploadLink;
	private FormLink createLink;
	private DropdownItem createDropdown;
	private FormLink createVideoLink;

	public ProjFileAllController(UserRequest ureq, WindowControl wControl, ProjProject project,
			ProjProjectSecurityCallback secCallback, Date lastVisitDate) {
		super(ureq, wControl, "file_all", project, secCallback, lastVisitDate);
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uploadLink = uifactory.addFormLink("file.upload", formLayout, Link.BUTTON);
		uploadLink.setIconLeftCSS("o_icon o_icon_upload");
		uploadLink.setVisible(secCallback.canCreateFiles());
		
		createLink = uifactory.addFormLink("file.create", formLayout, Link.BUTTON);
		createLink.setIconLeftCSS("o_icon o_icon_add");
		createLink.setVisible(secCallback.canCreateFiles());
		
		createDropdown = uifactory.addDropdownMenu("file.create.dropdown", null, null, formLayout, getTranslator());
		createDropdown.setOrientation(DropdownOrientation.right);
		createDropdown.setVisible(secCallback.canCreateFiles() && false);
		
		createVideoLink = uifactory.addFormLink("file.create.video", formLayout, Link.LINK);
		createVideoLink.setIconLeftCSS("o_icon o_icon_video_record");
		createDropdown.addElement(createVideoLink);
		createDropdown.setVisible(secCallback.canCreateFiles() && false);
		
		super.initForm(formLayout, listener, ureq);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == uploadLink) {
			doUploadFile(ureq);
		} else if (source == createLink) {
			doCreateFile(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean isFullTable() {
		return true;
	}
	
	@Override
	protected Integer getNumLastModified() {
		return null;
	}

	@Override
	protected void onModelLoaded() {
		//
	}

}
