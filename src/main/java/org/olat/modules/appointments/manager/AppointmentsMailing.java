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
package org.olat.modules.appointments.manager;

import static java.util.Collections.singletonList;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.Participation;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.ui.AppointmentsMainController;
import org.olat.modules.appointments.ui.AppointmentsUIFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 15 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class AppointmentsMailing {
	
	private static final Logger log = Tracing.createLoggerFor(AppointmentsMailing.class);
	
	@Autowired
	private ParticipationDAO participationDao;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MailManager mailManager;
	
	void sendAppointmentConfirmed(Appointment appointment) {
		if (appointment == null) return;
		
		ParticipationSearchParams participationParams = new ParticipationSearchParams();
		participationParams.setAppointments(singletonList(appointment));
		participationParams.setFetchIdentities(true);
		participationParams.setFetchUser(true);
		participationDao.loadParticipations(participationParams).stream()
				.map(Participation::getIdentity)
				.forEach(identity -> sendStatusEmail(appointment, identity, "mail.confirmed.subject", "mail.confirmed.body"));
	}

	void sendAppointmentUnconfirmed(Appointment appointment) {
		if (appointment == null) return;
		
		ParticipationSearchParams participationParams = new ParticipationSearchParams();
		participationParams.setAppointments(singletonList(appointment));
		participationParams.setFetchIdentities(true);
		participationParams.setFetchUser(true);
		participationDao.loadParticipations(participationParams).stream()
				.map(Participation::getIdentity)
				.forEach(identity -> sendStatusEmail(appointment, identity, "mail.unconfirmed.subject", "mail.unconfirmed.body"));
	}

	void sendAppointmentDeleted(List<Appointment> appointments) {
		if (appointments == null || appointments.isEmpty()) return;
		
		ParticipationSearchParams participationParams = new ParticipationSearchParams();
		participationParams.setAppointments(appointments);
		participationParams.setFetchIdentities(true);
		participationParams.setFetchUser(true);
		participationDao.loadParticipations(participationParams).stream()
				.forEach(participation -> sendStatusEmail(participation.getAppointment(), participation.getIdentity(),
						"mail.deleted.subject", "mail.deleted.body"));
	}
	
	private void sendStatusEmail(Appointment appointment, Identity identity, String i18nSubject, String i18nBody) {
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(AppointmentsMainController.class, locale);
				
		Topic topic = appointment.getTopic();
		String subject = translator.translate(i18nSubject, new String[] {
				topic.getTitle()
		});
		String body = translator.translate(i18nBody, new String[] {
				userManager.getUserDisplayName(identity.getKey()),
				createFormatedAppointments(singletonList(appointment), translator)
		});
		
		MailerResult result = new MailerResult();
		MailBundle bundle = new MailBundle();
		bundle.setToId(identity);
		bundle.setContent(subject, body);
		bundle.setContext(getMailContext(topic));
		
		result = mailManager.sendMessage(bundle);
		if (!result.isSuccessful()) {
			log.warn(MessageFormat.format("Sending status changed for appointment [key={0}, status={1}] to {2} failed: {3}",
					appointment.getKey(), appointment.getStatus(), identity, result.getErrorMessage()));
		}
	}
	
	void sendAppointmentsDeleted(List<Appointment> appointments, List<Organizer> organizers) {
		if (appointments == null || appointments.isEmpty() || organizers == null || organizers.isEmpty()) return;
		
		organizers.forEach(organizer -> sendAppointmentsDeleted(appointments, organizer));
	}

	private void sendAppointmentsDeleted(List<Appointment> appointments, Organizer organizer) {
		if (appointments == null || appointments.isEmpty()) return;
		
		Identity identity = organizer.getIdentity();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(AppointmentsMainController.class, locale);
				
		String subject = translator.translate("mail.appointments.deleted.subject");
		String body = translator.translate("mail.appointments.deleted.body", new String[] {
				userManager.getUserDisplayName(identity.getKey()),
				createFormatedAppointments(appointments, translator)
		});
		
		MailerResult result = new MailerResult();
		MailBundle bundle = new MailBundle();
		bundle.setToId(identity);
		bundle.setContent(subject, body);
		bundle.setContext(getMailContext(appointments.get(0).getTopic()));
		
		result = mailManager.sendMessage(bundle);
		if (!result.isSuccessful()) {
			log.warn(MessageFormat.format("Sending appointments deleted [keys={0}] to {1} failed: {2}",
					appointments.stream().map(Appointment::getKey).collect(Collectors.toList()),
					organizer.getIdentity(), result.getErrorMessage()));
		}
	}
	
	void sendParticipationCreated(Participation participation) {
		ParticipationSearchParams participationParams = new ParticipationSearchParams();
		participationParams.setParticipation(participation);
		participationParams.setFetchAppointments(true);
		participationParams.setFetchTopics(true);
		participationParams.setFetchIdentities(true);
		participationParams.setFetchUser(true);
		List<Participation> reloadedParticipations = participationDao.loadParticipations(participationParams);
		if (reloadedParticipations.isEmpty()) return;
		
		Participation reloadedParticipation = reloadedParticipations.get(0);
		Appointment appointment = reloadedParticipation.getAppointment();
		Identity identity = reloadedParticipation.getIdentity();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(AppointmentsMainController.class, locale);
				
		String subject = translator.translate("mail.participation.created.subject", 
				new String[] {appointment.getTopic().getTitle() });
		String body = translator.translate("mail.participation.created.body", new String[] {
				userManager.getUserDisplayName(identity.getKey()),
				createFormatedAppointments(Collections.singletonList(appointment), translator)
		});
		
		MailerResult result = new MailerResult();
		MailBundle bundle = new MailBundle();
		bundle.setToId(identity);
		bundle.setContent(subject, body);
		bundle.setContext(getMailContext(appointment.getTopic()));
		
		result = mailManager.sendMessage(bundle);
		if (!result.isSuccessful()) {
			log.warn(MessageFormat.format("Sending participation created [keys={0}] to {1} failed: {2}",
					reloadedParticipation.getKey(), identity, result.getErrorMessage()));
		}
	}
	
	void sendParticipationDeleted(Participation participation) {
		ParticipationSearchParams participationParams = new ParticipationSearchParams();
		participationParams.setParticipation(participation);
		participationParams.setFetchAppointments(true);
		participationParams.setFetchTopics(true);
		participationParams.setFetchIdentities(true);
		participationParams.setFetchUser(true);
		List<Participation> reloadedParticipations = participationDao.loadParticipations(participationParams);
		if (reloadedParticipations.isEmpty()) return;
		
		Participation reloadedParticipation = reloadedParticipations.get(0);
		Appointment appointment = reloadedParticipation.getAppointment();
		Identity identity = reloadedParticipation.getIdentity();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(AppointmentsMainController.class, locale);
				
		String subject = translator.translate("mail.participation.deleted.subject", 
				new String[] {appointment.getTopic().getTitle() });
		String body = translator.translate("mail.participation.deleted.body", new String[] {
				userManager.getUserDisplayName(identity.getKey()),
				createFormatedAppointments(Collections.singletonList(appointment), translator)
		});
		
		MailerResult result = new MailerResult();
		MailBundle bundle = new MailBundle();
		bundle.setToId(identity);
		bundle.setContent(subject, body);
		bundle.setContext(getMailContext(appointment.getTopic()));
		
		result = mailManager.sendMessage(bundle);
		if (!result.isSuccessful()) {
			log.warn(MessageFormat.format("Sending participation deleted [keys={0}] to {1} failed: {2}",
					reloadedParticipation.getKey(), identity, result.getErrorMessage()));
		}
	}
	
	void sendRebook(Appointment toAppointment, List<Participation> fromParticipations) {
		if (toAppointment == null || fromParticipations == null || fromParticipations.isEmpty()) return;
		
		ParticipationSearchParams participationParams = new ParticipationSearchParams();
		participationParams.setParticipations(fromParticipations);
		participationParams.setFetchAppointments(true);
		participationParams.setFetchTopics(true);
		participationParams.setFetchIdentities(true);
		participationParams.setFetchUser(true);
		participationDao.loadParticipations(participationParams).stream()
				.forEach(participation -> sendRebook(toAppointment, participation));
	}
	
	void sendRebook(Appointment toAppointment, Participation fromParticipation) {
		Identity identity = fromParticipation.getIdentity();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(AppointmentsMainController.class, locale);
				
		String subject = translator.translate("mail.rebooked.subject");
		String body = translator.translate("mail.rebooked.body", new String[] {
				userManager.getUserDisplayName(identity.getKey()),
				createFormatedAppointments(singletonList(fromParticipation.getAppointment()), translator),
				createFormatedAppointments(singletonList(toAppointment), translator)
		});
		
		MailerResult result = new MailerResult();
		MailBundle bundle = new MailBundle();
		bundle.setToId(identity);
		bundle.setContent(subject, body);
		bundle.setContext(getMailContext(toAppointment.getTopic()));
		
		result = mailManager.sendMessage(bundle);
		if (!result.isSuccessful()) {
			log.warn(MessageFormat.format("Sending rebook appointment [from key={0}, to key={1}] to {2} failed: {3}",
					fromParticipation.getAppointment().getKey(), toAppointment.getKey(),
					fromParticipation.getIdentity(), result.getErrorMessage()));
		}
	}
	
	private String createFormatedAppointments(List<Appointment> appointments, Translator translator) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < appointments.size(); i++) {
			Appointment appointment = appointments.get(i);
			appendFormatedAppointment(sb, appointment, translator);
			if (i > 0) {
				sb.append("<br>");
			}
		}
		return sb.toString();
	}

	private void appendFormatedAppointment(StringBuilder sb, Appointment appointment, Translator translator) {
		sb.append(translator.translate("mail.topic", new String[] { appointment.getTopic().getTitle() }));
		sb.append("<br>");
		if (DateUtils.isSameDate(appointment.getStart(), appointment.getEnd())) {
			DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, translator.getLocale());
			sb.append(translator.translate("mail.day", new String[] { dateFormat.format(appointment.getStart()) }));
			sb.append("<br>");
		} else {
			DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, translator.getLocale());
			sb.append(translator.translate("mail.start", new String[] { dateFormat.format(appointment.getStart()) }));
			sb.append("<br>");
			sb.append(translator.translate("mail.end", new String[] { dateFormat.format(appointment.getEnd()) }));
			sb.append("<br>");
		}
		String description = appointment.getTopic().getDescription();
		if (StringHelper.containsNonWhitespace(description)) {
			sb.append(translator.translate("mail.description", new String[] { description }));
			sb.append("<br>");
		}
		String details = appointment.getDetails();
		if (StringHelper.containsNonWhitespace(details)) {
			sb.append(translator.translate("mail.details", new String[] { details }));
			sb.append("<br>");
		}
		String location = AppointmentsUIFactory.getDisplayLocation(translator, appointment);
		if (StringHelper.containsNonWhitespace(location)) {
			sb.append(translator.translate("mail.location", new String[] { location }));
			sb.append("<br>");
		}
	}

	private MailContextImpl getMailContext(Topic topic) {
		return new MailContextImpl("[RepositoryEntry:" + topic.getEntry().getKey() + "]");
	}

}
