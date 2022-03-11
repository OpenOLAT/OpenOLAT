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
package org.olat.instantMessaging.ui;

import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RosterForm extends FormBasicController {

	private TextElement nickNameEl;
	private SingleSelection toggle;
	
	private final Roster buddyList;
	private final String fullName;
	private final boolean defaultAnonym;
	private final boolean offerAnonymMode;
	private final MapperKey avatarMapperKey;
	private static final String[] anonKeys = new String[]{ "name", "anon"};
	
	@Autowired
	private UserManager userManager;

	public RosterForm(UserRequest ureq, WindowControl wControl, Roster buddyList, boolean defaultAnonym, boolean offerAnonymMode,
			RosterFormDisplay rosterDisplay, MapperKey avatarMapperKey) {
		super(ureq, wControl, rosterDisplay == RosterFormDisplay.supervised ? "roster_supervised" : "roster");

		this.defaultAnonym = defaultAnonym;
		this.offerAnonymMode = offerAnonymMode;
		this.buddyList = buddyList;
		this.avatarMapperKey = avatarMapperKey;
		fullName = userManager.getUserDisplayName(getIdentity());

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutcont = (FormLayoutContainer)formLayout;
			layoutcont.contextPut("avatarBaseURL", avatarMapperKey.getUrl());
		}
		// for simplicity we initialize the form even when the anonymous mode is disabled
		// and just hide the form elements in the GUI
		String[] theValues = new String[]{ translate("yes"), translate("no") };
		toggle = uifactory.addRadiosHorizontal("toggle", "toogle.anonymous", formLayout, anonKeys, theValues);
		toggle.setLabel("anonymous",null);

		if(defaultAnonym) {
			toggle.select("anon", true);
		} else {
			toggle.select("name", true);
		}
		toggle.addActionListener(FormEvent.ONCLICK);

		String nickName = generateNickname();
		nickNameEl = uifactory.addTextElement("nickname", "", 20, nickName, formLayout);
		nickNameEl.setDisplaySize(18);
		nickNameEl.setVisible(defaultAnonym);
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("roster", buddyList);
			// hide the form elements in the GUI when no anonym mode is possible
			layoutCont.contextPut("offerAnonymMode", Boolean.valueOf(offerAnonymMode));
		}
	}
	
	private static final String[] anonymPrefix = new String[] {
		"John Doe",
		"M. Mustermann",
		"Juan Lopez",
		"M. Dupont",
		"Anonym"
	};
	
	private String generateNickname() {
		String prefix = anonymPrefix[(int)(Math.random() * (anonymPrefix.length - 1)) ];
		return prefix + " - "+ (int)(Math.random() * getIdentity().getKey());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		fireEvent(ureq, Event.CHANGED_EVENT);
		nickNameEl.setVisible(isUseNickName());
	}
	
	protected String getNickName() {
		if(isUseNickName()) {
			return nickNameEl.getValue();
		}
		return fullName;
	}
	
	protected boolean isUseNickName() {
		return toggle.isSelected(1);
	}
	
	protected void updateModel() {
		flc.setDirty(true);
	}
}
