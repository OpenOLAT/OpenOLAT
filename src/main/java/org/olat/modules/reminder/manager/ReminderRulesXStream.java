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
package org.olat.modules.reminder.manager;

import java.io.InputStream;
import java.io.OutputStream;

import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.reminder.model.ImportExportReminders;
import org.olat.modules.reminder.model.ReminderRules;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Initial date: 25 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReminderRulesXStream {
	
	private static final XStream ruleXStream = XStreamHelper.createXStreamInstance();
	static {
		XStreamHelper.allowDefaultPackage(ruleXStream);
		ruleXStream.alias("rule", org.olat.modules.reminder.model.ReminderRuleImpl.class);
		ruleXStream.alias("rules", org.olat.modules.reminder.model.ReminderRules.class);
		ruleXStream.alias("reminders", org.olat.modules.reminder.model.ImportExportReminders.class);
		ruleXStream.alias("reminder", org.olat.modules.reminder.model.ImportExportReminder.class);
	}
	
	public static ReminderRules toRules(String rulesXml) {
		return (ReminderRules)ruleXStream.fromXML(rulesXml);
	}
	
	public static ReminderRules toRules(InputStream in) {
		return (ReminderRules)ruleXStream.fromXML(in);
	}
	
	public static String toXML(ReminderRules rules) {
		return ruleXStream.toXML(rules);
	}
	
	public static void toXML(ImportExportReminders reminders, OutputStream out) {
		ruleXStream.toXML(reminders, out);
	}
	
	public static ImportExportReminders fromXML(InputStream in) {
		return (ImportExportReminders)ruleXStream.fromXML(in);
	}

}
