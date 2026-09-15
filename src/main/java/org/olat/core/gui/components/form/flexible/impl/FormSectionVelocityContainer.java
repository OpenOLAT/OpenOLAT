/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.form.flexible.impl;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.prefs.Preferences;

/**
 * Catches the background command a collapsible {@link FormSection} fires on
 * expand/collapse and persists it in the GUI preferences. No form evaluation,
 * no dirty marking - the client has already toggled the section visually.
 *
 * Same pattern as {@code org.olat.core.gui.components.panel.InfoPanel#doDispatchRequest}.
 *
 * Initial date: 2026-09-10<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
class FormSectionVelocityContainer extends FormVelocityContainer {

	private final FormSection section;

	public FormSectionVelocityContainer(String id, String componentName, String pagePath, FormSection section, Translator trans) {
		super(id, componentName, pagePath, section, trans);
		this.section = section;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		if ("expanded".equals(cmd) || "collapsed".equals(cmd)) {
			boolean collapsed = "collapsed".equals(cmd);
			section.setCollapsed(collapsed);

			String persistedStatusId = section.getPersistedStatusId();
			if (persistedStatusId != null) {
				Preferences prefs = ureq.getUserSession().getGuiPreferences();
				prefs.putAndSave(FormSection.class, persistedStatusId, Boolean.valueOf(collapsed));
			}
		}
	}

}
