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
package org.olat.course.reminder.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.reminder.ReminderModule;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.RuleSPI;
import org.olat.modules.reminder.ui.ReminderAdminController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RulesViewController extends BasicController {
	
	private final RepositoryEntry entry;

	@Autowired
	private ReminderService reminderService;
	@Autowired
	private ReminderModule reminderModule;

	protected RulesViewController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String reminderConfiguration) {
		super(ureq, wControl);
		this.entry = entry;
		setTranslator(Util.createPackageTranslator(ReminderAdminController.class, getLocale(), getTranslator()));
		
		VelocityContainer mainVC = createVelocityContainer("rules_view");
		
		if(StringHelper.containsNonWhitespace(reminderConfiguration)) {
			List<ReminderRule> rules = reminderService.toRules(reminderConfiguration).getRules();
			if(rules != null && !rules.isEmpty()) {
				ReminderRule rule = rules.get(0);
				mainVC.contextPut("mainRule", getRuleAsText(rule));
				
				if (rules.size() > 1) {
					List<String> additionalRules = new ArrayList<>(rules.size() - 1);
					for (int i = 1; i < rules.size(); i++) {
						rule = rules.get(i);
						if(rule != null) {
							additionalRules.add(getRuleAsText(rule));
						}
					}
					mainVC.contextPut("additionalRules", additionalRules);
				}
			}
		}
		
		putInitialPanel(mainVC);
	}
	
	private String getRuleAsText(ReminderRule rule) {
		RuleSPI ruleSpy = reminderModule.getRuleSPIByType(rule.getType());
		String ruleText = ruleSpy.getStaticText(rule, entry, getLocale());
		return StringHelper.containsNonWhitespace(ruleText)? ruleText: translate("error.rule.no.text");
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
