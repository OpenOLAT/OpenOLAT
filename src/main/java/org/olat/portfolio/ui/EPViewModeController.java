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
package org.olat.portfolio.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.portfolio.manager.EPFrontendManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * switch between artefact-preview and the table view mode,
 * sends an event on changes, persist setting in user-properties
 * 
 * <P>
 * Initial Date:  16.11.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPViewModeController extends FormBasicController {

	public static final String VIEWMODE_TABLE = "table";
	public static final String VIEWMODE_DETAILS = "details";
	public static final String VIEWMODE_CONTEXT_ARTEFACTPOOL = "artefact";
	public static final String VIEWMODE_CONTEXT_MAP = "map";
	public static final String VIEWMODE_CHANGED_EVENT_CMD = "viewModeChangedEventCommand";

	private FormLink tableLink, detailsLink;
	
	@Autowired
	private EPFrontendManager ePFMgr;
	private String userPrefsMode;
	private String context;
	
	public EPViewModeController(UserRequest ureq, WindowControl wControl, String context) {
		super(ureq, wControl, "view_mode");
		this.context = context;
		userPrefsMode = ePFMgr.getUsersPreferedArtefactViewMode(getIdentity(), context);
		initForm(ureq);
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer, org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tableLink = uifactory.addFormLink("view.mode.table", VIEWMODE_TABLE, "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		tableLink.setIconRightCSS("o_icon o_icon_table o_icon-lg");
		detailsLink = uifactory.addFormLink("view.mode.details", VIEWMODE_DETAILS, "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		detailsLink.setIconRightCSS("o_icon o_icon_table_large o_icon-lg");
	}
	
	public void selectDetails() {
		tableLink.setElementCssClass(null);
		detailsLink.setElementCssClass("active");
	}
	
	public void selectTable() {
		tableLink.setElementCssClass("active");
		detailsLink.setElementCssClass(null);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.components.form.flexible.FormItem, org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableLink || source == detailsLink){
			FormLink link = (FormLink)source;
			if (!link.getCmd().equals(userPrefsMode)){
				String newUserPrefsMode = link.getCmd();
				ePFMgr.setUsersPreferedArtefactViewMode(getIdentity(), newUserPrefsMode, context);
				userPrefsMode = newUserPrefsMode;
				fireEvent(ureq, new Event(VIEWMODE_CHANGED_EVENT_CMD));
			}
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to persist, see formInnerEvent
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing
	}
}