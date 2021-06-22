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
package org.olat.course.reminder.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.manager.ReminderRulesXStream;
import org.olat.modules.reminder.model.ImportExportReminder;
import org.olat.modules.reminder.model.ImportExportReminders;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.model.ReminderRules;

/**
 * 
 * Initial date: 22 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReminderRulesXStreamTest {
	
	@Test
	public void readImportExportRemindersList() throws IOException {
		InputStream listStream = ReminderRulesXStreamTest.class.getResourceAsStream("Reminders.xml");
		ImportExportReminders reminderRules = ReminderRulesXStream.fromXML(listStream);
		Assert.assertNotNull(reminderRules);
		listStream.close();
		
		Assert.assertNotNull(reminderRules);
		Assert.assertEquals(2, reminderRules.getReminders().size());
	}
	
	@Test
	public void writeImportExportRemindersList() throws IOException {
		ImportExportReminder reminder = new ImportExportReminder();
		reminder.setConfiguration("My configuration");
		reminder.setDescription("My description");
		reminder.setEmailBody("The body");
		reminder.setEmailSubject("Subject");
		
		ImportExportReminders reminders = new ImportExportReminders();
		List<ImportExportReminder> reminderList = new ArrayList<>();
		reminderList.add(reminder);
		reminders.setReminders(reminderList);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ReminderRulesXStream.toXML(reminders, out);
		String xml = new String(out.toByteArray());
		Assert.assertNotNull(xml);
		
		InputStream in = new ByteArrayInputStream(out.toByteArray());
		ImportExportReminders exportedReminders = ReminderRulesXStream.fromXML(in);
		Assert.assertNotNull(exportedReminders);
		Assert.assertEquals(1, exportedReminders.getReminders().size());
		
		out.close();
		in.close();
	}
	
	@Test
	public void readReminderRules() throws IOException  {
		InputStream listStream = ReminderRulesXStreamTest.class.getResourceAsStream("Reminders.xml");
		ImportExportReminders reminderRules = ReminderRulesXStream.fromXML(listStream);
		listStream.close();

		Assert.assertEquals(2, reminderRules.getReminders().size());
		
		String reminderRuleXml = reminderRules.getReminders().get(0).getConfiguration();
		ReminderRules rules = ReminderRulesXStream.toRules(reminderRuleXml);
		Assert.assertNotNull(rules);
		List<ReminderRule> ruleList = rules.getRules();
		Assert.assertNotNull(ruleList);
		Assert.assertEquals(1, ruleList.size());
		ReminderRule rule = ruleList.get(0);
		Assert.assertNotNull(rule);
		Assert.assertEquals("PassedRuleSPI", rule.getType());
	}
	
	@Test
	public void writeReminderRule() throws IOException  {
		ReminderRules rules = new ReminderRules();
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType("RuleType");
		rule.setLeftOperand("Left");
		rule.setOperator("Op");
		rule.setRightOperand("Right");
		rules.getRules().add(rule);
		
		String xml = ReminderRulesXStream.toXML(rules);
		Assert.assertNotNull(xml);
		ReminderRules exportedRules = ReminderRulesXStream.toRules(xml);
		Assert.assertNotNull(exportedRules);
		List<ReminderRule> ruleList = exportedRules.getRules();
		Assert.assertNotNull(ruleList);
		Assert.assertEquals(1, ruleList.size());
		ReminderRuleImpl exportedRule = (ReminderRuleImpl)ruleList.get(0);
		Assert.assertNotNull(exportedRule);
		Assert.assertEquals("RuleType", exportedRule.getType());
		Assert.assertEquals("Left", exportedRule.getLeftOperand());
		Assert.assertEquals("Op", exportedRule.getOperator());
		Assert.assertEquals("Right", exportedRule.getRightOperand());
	}
}
