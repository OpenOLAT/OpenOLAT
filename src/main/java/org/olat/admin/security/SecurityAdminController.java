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
package org.olat.admin.security;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 23.12.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SecurityAdminController extends FormBasicController {
	
	private MultipleSelectionElement wikiEl, topFrameEl, forceDownloadEl;

	private final FolderModule folderModule;
	private final BaseSecurityModule securityModule;
	
	public SecurityAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		folderModule = CoreSpringFactory.getImpl(FolderModule.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("sec.title");
		setFormDescription("sec.description");
		setFormContextHelp("Security");

		String[] keys = new String[]{ "on" };
		String[] values = new String[]{ "" };
		
		// on: force top top frame (more security); off: allow in frame (less security)
		topFrameEl = uifactory.addCheckboxesHorizontal("sec.topframe", "sec.topframe", formLayout, keys, values);
		topFrameEl.select("on", securityModule.isForceTopFrame());
		topFrameEl.addActionListener(FormEvent.ONCHANGE);
		topFrameEl.setEnabled(false);
		topFrameEl.setExampleKey("sec.top.frame.explanation", null);
		
		// on: block wiki (more security); off: do not block wiki (less security)
		wikiEl = uifactory.addCheckboxesHorizontal("sec.wiki", "sec.wiki", formLayout, keys, values);
		wikiEl.select("off", securityModule.isWikiEnabled());
		wikiEl.addActionListener(FormEvent.ONCHANGE);

		// on: force file download in folder component (more security); off: allow execution of content (less security)
		forceDownloadEl = uifactory.addCheckboxesHorizontal("sec.download", "sec.force.download", formLayout, keys, values);
		forceDownloadEl.select("on", folderModule.isForceDownload());
		forceDownloadEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(topFrameEl == source) {
			boolean enabled = topFrameEl.isAtLeastSelected(1);
			securityModule.setForceTopFrame(enabled);
		} else if(wikiEl == source) {
			boolean enabled = wikiEl.isAtLeastSelected(1);
			securityModule.setWikiEnabled(!enabled);
			// update collaboration tools list
			CollaborationToolsFactory.getInstance().initAvailableTools();
		} else if(forceDownloadEl == source) {
			boolean enabled = forceDownloadEl.isAtLeastSelected(1);
			folderModule.setForceDownload(enabled);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}