/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
* <p>
* Initial code contributed and copyrighted by<br>
* 2012 by frentix GmbH, http://www.frentix.com
* <p>
*/

package org.olat.admin.sysinfo;

import java.util.Date;

import org.olat.admin.sysinfo.manager.CustomStaticFolderManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Apr 30, 2004
 *
 * @author Mike Stock
 * @author Sergio Trentini
 * @author Florian Gnägi
 */
public class InfoMsgForm extends FormBasicController {
	
	private RichTextElement msg;
	private DateChooser start;
	private SysInfoMessage sysInfoMessage;
	private SingleSelection publication;
	
	@Autowired
	private CustomStaticFolderManager staticFolderMgr;
	
	/**
	 * @param name
	 * @param infomsg
	 */
	public InfoMsgForm(UserRequest ureq, WindowControl wControl, SysInfoMessage sysInfoMessage) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.sysInfoMessage = sysInfoMessage;
		initForm(ureq);
	}
	
	/**
	 * @return the info message
	 */
	public String getInfoMsg() {
		// use raw value to circumvent XSS filtering of script tags
		return msg.getRawValue();
	}
	
	public Date getStart() {
		if (publication.isSelected(2)) {
			return start.getDate();			
		} else {
			return null;
		}
	}

	public Date getEnd() {
		if (publication.isSelected(2)) {
			return start.getSecondDate();
		} else {
			return null;
		}
	}
	
	public boolean getClearOnReboot() {
		return publication.isSelected(0);
	}
	
	public void reset() {
		msg.setValue("");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source == publication) {
			updateFormDependencies();
		}		
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		msg = uifactory.addRichTextElementForStringData("msg", "infomsg", sysInfoMessage.getMessage(), 20, 70, true, staticFolderMgr.getRootContainer(), null, formLayout, ureq.getUserSession(), getWindowControl());
		msg.setMaxLength(1024);
		
		String[] lcKeys = new String[] {"publication.clear", "publication.always", "publication.timed"};
		String[] lcValues = new String[] {translate("publication.clear"), translate("publication.always"), translate("publication.timed")};
		String[] lcDesc = new String[] {translate("publication.clear.desc"), translate("publication.always.desc"), translate("publication.timed.desc")};
		publication = uifactory.addCardSingleSelectHorizontal("publication", formLayout, lcKeys, lcValues, lcDesc, null);
		if (sysInfoMessage.isClearOnRestart()){
			publication.select("publication.clear", true);
		} else if (sysInfoMessage.getStart() != null || sysInfoMessage.getEnd() != null) {
			publication.select("publication.timed", true);			
		} else {
			publication.select("publication.always", true);
		}
		publication.addActionListener(FormEvent.ONCHANGE);
		
		FormLayoutContainer dateLayout = FormLayoutContainer.createHorizontalFormLayout("msg.active", getTranslator());
		formLayout.add(dateLayout);
		
		start = uifactory.addDateChooser("msg.beginning", sysInfoMessage.getStart(), dateLayout);
		start.setDateChooserTimeEnabled(true);
		start.setSecondDate(true);
		start.setSecondDate(sysInfoMessage.getEnd());
		start.setSeparator("msg.ending");

		RichTextConfiguration richTextConfig = msg.getEditorConfiguration();
		// manually enable the source edit button
		richTextConfig.enableCode();
		//allow script tags...
		richTextConfig.setInvalidElements(RichTextConfiguration.INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE_WITH_SCRIPT);
		richTextConfig.setExtendedValidElements("script[src,type,defer]");
		// add style buttons to make alert style available
		richTextConfig.setContentCSSFromTheme(getWindowControl().getWindowBackOffice().getWindow().getGuiTheme());
		String path = Settings.getServerContextPath() + "/raw/static/";
		richTextConfig.setLinkBrowserAbsolutFilePath(path);
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("submit", "submit", buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		
		updateFormDependencies();
	}
	
	private void updateFormDependencies() {
		start.setVisible(publication.isSelected(2));
	}
}