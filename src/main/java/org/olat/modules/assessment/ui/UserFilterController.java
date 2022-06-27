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
package org.olat.modules.assessment.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.assessment.ui.event.UserFilterEvent;

/**
 * 
 * Initial date: 19.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserFilterController extends FormBasicController {

	private static final String OTHER_USER = "other.users";
	private static final String ANONYMOUS = "anonymousUsers";
	
	private MultipleSelectionElement restrictionEl;
	
	private final boolean canAnonymous;
	private final boolean canOtherUsers;
	private final boolean initialOtherUser;
	private final boolean initialAnonymous;

	public UserFilterController(UserRequest ureq, WindowControl wControl, boolean canOtherUsers, boolean canAnonymous,
			boolean initialOtherUser, boolean initialAnonymous) {
		super(ureq, wControl, "user_filter");
		this.canAnonymous = canAnonymous;
		this.canOtherUsers = canOtherUsers;
		this.initialOtherUser = initialOtherUser;
		this.initialAnonymous = initialAnonymous;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<String> allowKeyList = new ArrayList<>(2);
		if(canOtherUsers) {
			allowKeyList.add(OTHER_USER);
		}
		if(canAnonymous) {
			allowKeyList.add(ANONYMOUS);
		}

		String[] allowKeys = allowKeyList.toArray(new String[allowKeyList.size()]);
		String[] allowValues = new String[allowKeys.length];
		for(int i=allowKeys.length; i-->0 ; ) {
			allowValues[i] = translate("filter.".concat(allowKeys[i]));
		}
		restrictionEl = uifactory.addCheckboxesHorizontal("user.restrictions", null, formLayout, allowKeys, allowValues);
		restrictionEl.setDomReplacementWrapperRequired(false);
		restrictionEl.addActionListener(FormEvent.ONCHANGE);
		if(initialOtherUser && allowKeyList.contains(OTHER_USER)) {
			restrictionEl.select(OTHER_USER, true);
		}
		if(initialAnonymous && allowKeyList.contains(ANONYMOUS)) {
			restrictionEl.select(ANONYMOUS, true);
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(restrictionEl == source) {
			Collection<String> selectedKeys = restrictionEl.getSelectedKeys();
			boolean withOtherUsers = selectedKeys.contains(OTHER_USER);
			boolean withAnonymousUsers = selectedKeys.contains(ANONYMOUS);
			fireEvent(ureq, new UserFilterEvent(withOtherUsers, withAnonymousUsers));
		}
		super.formInnerEvent(ureq, source, event);
	}
}