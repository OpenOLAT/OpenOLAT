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
package org.olat.course.nodes.co;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.HashSet;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 6 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class COToolRecipientsController extends FormBasicController {
	
	public enum Recipients {
		owners("tool.recipients.owners"),
		coaches("tool.recipients.coaches"),
		participants("tool.recipients.participants");
		
		private final String i18nKey;

		private Recipients(String i18nKey) {
			this.i18nKey = i18nKey;
			
		}

		public String getI18nKey() {
			return i18nKey;
		}
	}
	
	private MultipleSelectionElement recipientsEl;

	private final Config config;
	
	public COToolRecipientsController(UserRequest ureq, WindowControl wControl, Config config) {
		super(ureq, wControl);
		this.config = config;
		initForm(ureq);
	}
	
	public Set<Recipients> getSelectedRecipients() {
		Set<Recipients> recipients = new HashSet<>();
		for (String key : recipientsEl.getSelectedKeys()) {
			recipients.add(Recipients.valueOf(key));
		}
		return recipients;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("tool.title");
		
		SelectionValues recipientKV = new SelectionValues();
		for (Recipients recipient : Recipients.values()) {
			recipientKV.add(entry(recipient.name(), translate(recipient.getI18nKey())));
		}
		recipientsEl = uifactory.addCheckboxesHorizontal("tool.recipients", formLayout, recipientKV.keys(), recipientKV.values());
		recipientsEl.setEnabled(config.isRecipientsEnabled());
		for (Recipients recipients : config.getInitialRecipients()) {
			recipientsEl.select(recipients.name(), true);
		}
		recipientsEl.addActionListener(FormEvent.ONCHANGE);
	}

	public void setReadOnly() {
		recipientsEl.setEnabled(false);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == recipientsEl) {
			boolean valid = validateFormLogic(ureq);
			if (valid) {
				fireEvent(ureq, FormEvent.CHANGED_EVENT);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		recipientsEl.clearError();
		if (!recipientsEl.isAtLeastSelected(1)) {
			recipientsEl.setErrorKey("tool.recipients.mandatory", null);
			allOk = false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	static class Config {
		
		private final boolean recipientsEnabled;
		private final Recipients[] initialRecipients;
		
		Config(boolean recipientsEnabled, Recipients[] recipients) {
			this.recipientsEnabled = recipientsEnabled;
			this.initialRecipients = recipients;
		}

		private boolean isRecipientsEnabled() {
			return recipientsEnabled;
		}

		private Recipients[] getInitialRecipients() {
			return initialRecipients;
		}
	}

}
