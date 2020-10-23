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
package org.olat.modules.contacttracing.ui;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.contacttracing.ContactTracingLocation;

/**
 * Initial date: 22.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingHelper {

	public static String getLocationsDetails(Translator translator, ContactTracingLocation location) {
		StringBuilder locationDetailsBuilder = new StringBuilder();

		locationDetailsBuilder
				.append("<p>")
				.append("<b>").append(translator.translate("contact.tracing.location")).append(" ").append(location.getReference()).append("</b><br>");

		if (StringHelper.containsNonWhitespace(location.getTitle())) {
			locationDetailsBuilder.append(location.getTitle()).append("<br>");
		}

		if (StringHelper.containsNonWhitespace(location.getBuilding()) || StringHelper.containsNonWhitespace(location.getRoom())) {
			if (StringHelper.containsNonWhitespace(location.getBuilding())) {
				locationDetailsBuilder.append(location.getBuilding());
			}
			if (StringHelper.containsNonWhitespace(location.getBuilding()) && StringHelper.containsNonWhitespace(location.getRoom())) {
				locationDetailsBuilder.append(" - ");
			}
			if (StringHelper.containsNonWhitespace(location.getRoom())) {
				locationDetailsBuilder.append(location.getBuilding());
			}
			locationDetailsBuilder.append("<br>");
		}

		if (StringHelper.containsNonWhitespace(location.getSector()) || StringHelper.containsNonWhitespace(location.getTable())) {
			if (StringHelper.containsNonWhitespace(location.getSector())) {
				locationDetailsBuilder.append(location.getSector());
			}
			if (StringHelper.containsNonWhitespace(location.getSector()) && StringHelper.containsNonWhitespace(location.getTable())) {
				locationDetailsBuilder.append(" - ");
			}
			if (StringHelper.containsNonWhitespace(location.getTable())) {
				locationDetailsBuilder.append(location.getTable());
			}
		}

		locationDetailsBuilder.append("</p>");

		return locationDetailsBuilder.toString();
	}

	public static String getMailSubject(Translator translator, ContactTracingLocation location) {
		StringBuilder subjectBuilder = new StringBuilder()
				.append(translator.translate("contact.tracing.registration.confirmation.mail.subject"))
				.append(" - ");

		if (StringHelper.containsNonWhitespace(location.getReference())) {
			subjectBuilder.append(location.getReference());
		} else if (StringHelper.containsNonWhitespace(location.getTitle())) {
			subjectBuilder.append(location.getTitle());
		} else {
			subjectBuilder.append(location.getQrId());
		}

		return subjectBuilder.toString();
	}

	public static String getMailBody(Translator translator, ContactTracingLocation location, FormItem firstNameEl, FormItem lastNameEl) {
		StringBuilder subjectBuilder = new StringBuilder();

		if (firstNameEl instanceof TextElement && lastNameEl instanceof TextElement &&
				StringHelper.containsNonWhitespace(((TextElement) firstNameEl).getValue()) &&
				StringHelper.containsNonWhitespace(((TextElement) lastNameEl).getValue())) {
				subjectBuilder.append(translator.translate("contact.tracing.registration.confirmation.mail.greeting.personal", new String[] {
						((TextElement) firstNameEl).getValue(), ((TextElement) lastNameEl).getValue()}));
		} else {
			subjectBuilder.append(translator.translate("contact.tracing.registration.confirmation.mail.greeting.anonymous"));
		}

		subjectBuilder.append("<br>")
				.append(translator.translate("contact.tracing.registration.confirmation.mail.body"))
				.append(getLocationsDetails(translator, location)).append("<br>")
				.append(translator.translate("contact.tracing.registration.confirmation.mail.footer"));

		return subjectBuilder.toString();
	}

	public static String getMailAddress(FormItem emailItem, FormItem institutionalEmailItem, FormItem genericEmailItem) {
		if (emailItem instanceof TextElement) {
			if (StringHelper.containsNonWhitespace(((TextElement) emailItem).getValue())) {
				return ((TextElement) emailItem).getValue();
			}
		} else if (institutionalEmailItem instanceof TextElement) {
			if (StringHelper.containsNonWhitespace(((TextElement) institutionalEmailItem).getValue())) {
				return ((TextElement) institutionalEmailItem).getValue();
			}
		} else if (genericEmailItem instanceof TextElement) {
			if (StringHelper.containsNonWhitespace(((TextElement) genericEmailItem).getValue())) {
				return ((TextElement) genericEmailItem).getValue();
			}
		}

		return "No valid mail provided!";
	}
}
