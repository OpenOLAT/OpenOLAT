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
import org.olat.modules.contacttracing.ContactTracingRegistration;

/**
 * Initial date: 22.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingHelper {

	public static String getLocationsDetails(Translator translator, ContactTracingLocation location, ContactTracingRegistration registration) {
		StringBuilder locationDetailsBuilder = new StringBuilder();

		locationDetailsBuilder
				.append("<p>")
				.append("<table class='o_ct_location_details_table'>")
				.append("<tr>");

		// TODO CSS STYLE padding-right: 30px;
		if (StringHelper.containsNonWhitespace(location.getTitle())) {
			locationDetailsBuilder.append("<th scope='row'").append("<b>").append(translator.translate("contact.tracing.location")).append("</th>")
				.append("<td>").append(location.getTitle()).append("</td>");
		} else {
			locationDetailsBuilder.append("<th colspan='2' style='padding-right: 30px;'>").append(translator.translate("contact.tracing.location")).append("</th>");
		}

		locationDetailsBuilder.append("</tr>");

		if (StringHelper.containsNonWhitespace(location.getTitle())) {
			locationDetailsBuilder.append("<tr>");
			locationDetailsBuilder.append("<th scope='row'>").append(translator.translate("contact.tracing.cols.reference")).append("</th>");
			locationDetailsBuilder.append("<td>").append(location.getReference()).append("</td>");
			locationDetailsBuilder.append("</tr>");
		}

		if (StringHelper.containsNonWhitespace(location.getBuilding())) {
			locationDetailsBuilder.append("<tr>");
			locationDetailsBuilder.append("<th scope='row'>").append(translator.translate("contact.tracing.cols.building")).append("</th>");
			locationDetailsBuilder.append("<td>").append(location.getBuilding()).append("</td>");
			locationDetailsBuilder.append("</tr>");
		}
		if (StringHelper.containsNonWhitespace(location.getRoom())) {
			locationDetailsBuilder.append("<tr>");
			locationDetailsBuilder.append("<th scope='row'>").append(translator.translate("contact.tracing.cols.room")).append("</th>");
			locationDetailsBuilder.append("<td>").append(location.getRoom()).append("</td>");
			locationDetailsBuilder.append("</tr>");
		}

		if (StringHelper.containsNonWhitespace(location.getSector())) {
			locationDetailsBuilder.append("<tr>");
			locationDetailsBuilder.append("<th scope='row'>").append(translator.translate("contact.tracing.cols.sector")).append("</th>");
			locationDetailsBuilder.append("<td>").append(location.getSector()).append("</td>");
			locationDetailsBuilder.append("</tr>");
		}
		if (StringHelper.containsNonWhitespace(location.getTable())) {
			locationDetailsBuilder.append("<tr>");
			locationDetailsBuilder.append("<th scope='row'>").append(translator.translate("contact.tracing.cols.table")).append("</th>");
			locationDetailsBuilder.append("<td>").append(location.getTable()).append("</td>");
			locationDetailsBuilder.append("</tr>");
		}
		if (registration != null && StringHelper.containsNonWhitespace(registration.getSeatNumber())) {
			locationDetailsBuilder.append("<tr>");
			locationDetailsBuilder.append("<th scope='row'>").append(translator.translate("contact.tracing.cols.seat.number")).append("</th>");
			locationDetailsBuilder.append("<td>").append(registration.getSeatNumber()).append("</td>");
			locationDetailsBuilder.append("</tr>");
		}

		locationDetailsBuilder.append("</table>").append("</p>");

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

	public static String getMailBody(Translator translator, int retentionPeriod, ContactTracingLocation location, ContactTracingRegistration registration) {
		StringBuilder subjectBuilder = new StringBuilder();

		if (registration != null &&
				StringHelper.containsNonWhitespace(registration.getFirstName()) &&
				StringHelper.containsNonWhitespace(registration.getLastName())) {
				subjectBuilder.append(translator.translate("contact.tracing.registration.confirmation.mail.greeting.personal", new String[] {
						registration.getFirstName(), registration.getLastName()}));
		} else {
			subjectBuilder.append(translator.translate("contact.tracing.registration.confirmation.mail.greeting.anonymous"));
		}

		subjectBuilder.append("<br>")
				.append(translator.translate("contact.tracing.registration.confirmation.mail.body", new String[] {String.valueOf(retentionPeriod)}))
				.append(getLocationsDetails(translator, location, registration)).append("<br>")
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

	public static String getName(ContactTracingRegistration registration) {
		StringBuilder nameBuilder = new StringBuilder();

		if (StringHelper.containsNonWhitespace(registration.getFirstName()) || StringHelper.containsNonWhitespace(registration.getLastName())) {
			if (StringHelper.containsNonWhitespace(registration.getFirstName())) {
				nameBuilder.append(registration.getFirstName());
			}

			if (StringHelper.containsNonWhitespace(registration.getFirstName()) && StringHelper.containsNonWhitespace(registration.getLastName())) {
				nameBuilder.append(" ");
			}

			if (StringHelper.containsNonWhitespace(registration.getFirstName())) {
				nameBuilder.append(registration.getLastName());
			}
		}

		return nameBuilder.toString();
	}
}
