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
package org.olat.modules.topicbroker.ui;

import java.util.Date;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBBrokerStatus;
import org.olat.modules.topicbroker.TBCustomFieldType;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBSelectionStatus;
import org.olat.modules.topicbroker.TBTopic;

/**
 * 
 * Initial date: 27 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBUIFactory {
	
	public static int getRequiredEnrollments(TBBroker broker, TBParticipant participant) {
		int maxEnrollments = broker.getRequiredEnrollments();
		if (participant != null && participant.getRequiredEnrollments() != null) {
			maxEnrollments = participant.getRequiredEnrollments();
		}
		return maxEnrollments;
	}
	
	public static TBBrokerStatus getBrokerStatus(TBBroker broker) {
		if (broker.getEnrollmentDoneDate() != null) {
			return TBBrokerStatus.enrollmentDone;
		}
		if (broker.getSelectionStartDate() != null && broker.getSelectionStartDate().before(new Date())) {
			return TBBrokerStatus.selectionInProgess;
		}
		return TBBrokerStatus.notStarted;
	}

	public static String getTranslatedStatus(Translator translator, TBBrokerStatus status) {
		if (status != null) {
			switch (status) {
			case notStarted: return translator.translate("broker.status.not.started");
			case selectionInProgess: return translator.translate("broker.status.selection.in.progress");
			case enrollmentDone: return translator.translate("broker.status.enrollment.done");
			default:
			}
		}
		return null;
	}

	public static String getStatusIconCss(TBBrokerStatus status) {
		if (status != null) {
			switch (status) {
			case notStarted: return "o_icon_tb_broker_not_started";
			case selectionInProgess: return "o_icon_tb_broker_selection_in_progress";
			case enrollmentDone: return "o_icon_tb_broker_enrollment_done";
			default:
			}
		}
		return null;
	}

	public static String getLabelCss(TBBrokerStatus status) {
		if (status != null) {
			switch (status) {
			case notStarted: return "o_tb_label_broker_not_started";
			case selectionInProgess: return "o_tb_label_broker_selection_in_progress";
			case enrollmentDone: return "o_tb_label_broker_enrollment_done";
			default:
			}
		}
		return null;
	}
	
	public static String getLabel(Translator translator, TBBroker broker) {
		TBBrokerStatus status = getBrokerStatus(broker);
		return "<span class=\"o_labeled " + getLabelCss(status) 
				+ "\"><i class=\"o_icon " + getStatusIconCss(status)
				+ "\"> </i> "+ getTranslatedStatus(translator, status) + "</span>";
	}
	
	public static TBSelectionStatus getSelectionStatus(TBBroker broker, int requiredEnrollments,
			int numEnrollments, boolean selected, boolean enrolled, int sortOrder) {
		if (!selected) {
			return TBSelectionStatus.fillIn;
		}
		
		if (enrolled) {
			return TBSelectionStatus.enrolled;
		}
		
		if (sortOrder > broker.getMaxSelections()) {
			return TBSelectionStatus.extra;
		}
		
		if (numEnrollments >= requiredEnrollments) { // fully enrolled
			return TBSelectionStatus.notEnrolled;
		}
		
		if (broker.getEnrollmentDoneDate() != null) {
			return TBSelectionStatus.waitingList;
		}
		
		return TBSelectionStatus.selected;
	}

	public static String getTranslatedStatus(Translator translator, TBSelectionStatus status) {
		if (status != null) {
			switch (status) {
			case selected: return translator.translate("selection.status.selected");
			case enrolled: return translator.translate("selection.status.enrolled");
			case waitingList: return translator.translate("selection.status.waiting.list");
			case notEnrolled: return translator.translate("selection.status.not.enrolled");
			case extra: return translator.translate("selection.status.extra");
			default:
			}
		}
		return null;
	}

	public static String getStatusIconCss(TBSelectionStatus status) {
		if (status != null) {
			switch (status) {
			case selected: return "o_icon_tb_selection_selected";
			case enrolled: return "o_icon_tb_selection_enrolled";
			case waitingList: return "o_icon_tb_selection_waiting";
			case notEnrolled: return "o_icon_tb_selection_not_enrolled";
			case extra: return "o_icon_tb_selection_extra";
			default:
			}
		}
		return null;
	}

	public static String getLabelLightCss(TBSelectionStatus status) {
		if (status != null) {
			switch (status) {
			case selected: return "o_tb_label_light_selected";
			case enrolled: return "o_tb_label_light_enrolled";
			case waitingList: return "o_tb_label_light_waiting";
			case notEnrolled: return "o_tb_label_light_not_enrolled";
			case extra: return "o_tb_label_light_extra";
			case fillIn: return "o_tb_label_light_fillin";
			default:
			}
		}
		return null;
	}

	public static String getPriorityCss(TBSelectionStatus status) {
		if (status != null) {
			switch (status) {
			case selected: return "o_tb_priority_selected";
			case enrolled: return "o_tb_priority_enrolled";
			case waitingList: return "o_tb_priority_waiting";
			case notEnrolled: return "o_tb_priority_not_enrolled";
			case extra: return "o_tb_priority_extra";
			case fillIn: return "o_tb_priority_fillin";
			default:
			}
		}
		return null;
	}
	
	public static final String getPriorityLabel(Translator translator, TBSelectionStatus status, int sortOrder) {
		String label = "<div class=\"o_tb_priority_label\">";
		label += "<div class=\"";
		label += getPriorityCss(status);
		label += "\">";
		if (TBSelectionStatus.extra == status) {
			label += translator.translate("selection.extra.abbr");
		} else {
			label += sortOrder;
		}
		label += "</div>";
		label += "</div>";
		return label;
	}

	public static String getTitleAbbr(String title) {
		String abbr = null;
		if (StringHelper.containsNonWhitespace(title)) {
			abbr = title.replace(" ", "");
			abbr = Formatter.truncateOnly(title, 3);
			abbr = abbr.toUpperCase();
		}
		return abbr;
	}
	
	public static String getParticipantRange(Translator translator, TBTopic topic) {
		String participants = "<i class=\"o_icon o_icon_tb_participants\"></i> ";
		if (topic.getMinParticipants() > 0) {
			participants += translator.translate("topic.participants.label.min.max",
					String.valueOf(topic.getMinParticipants()), String.valueOf(topic.getMaxParticipants()));
		} else {
			participants += translator.translate("topic.participants.label.max",
					String.valueOf(topic.getMaxParticipants()));
		}
		return participants;
	}
	
	public static String getTranslatedType(Translator translator, TBCustomFieldType type) {
		if (type != null) {
			switch (type) {
			case text: return translator.translate("custom.field.def.type.text");
			case file: return translator.translate("custom.field.def.type.file");
			default:
			}
		}
		return null;
	}
	
	public static String getConfigInfos(Translator translator, TBBroker broker, boolean withMethod) {
		String infos = "<ul class=\"list-unstyled\">";
		
		infos += createInfo("o_icon_topicbroker",
				translator.translate("config.overview.max.selections",
						String.valueOf(broker.getMaxSelections())));
		
		String requiredEnrollmentsI18n = broker.isParticipantCanEditRequiredEnrollments()
				? "config.overview.required.enrollments.editable"
				: "config.overview.required.enrollments";
		infos += createInfo("o_icon_tb_broker_enrollment_done",
				translator.translate(requiredEnrollmentsI18n,
				String.valueOf(broker.getRequiredEnrollments())));
		
		if (withMethod) {
			String methodAuto = broker.isAutoEnrollment()
					? translator.translate("config.overview.method.auto")
					: translator.translate("config.overview.method.manually");
			infos += createInfo("o_icon_tb_method",
					translator.translate("config.overview.method",
					translator.translate("strategy.fair"), methodAuto));
			
		}
		infos += createInfo("o_icon_calendar", translator.translate("config.overview.selection.period", 
				Formatter.getInstance(translator.getLocale()).formatDateAndTime(broker.getSelectionStartDate()),
				Formatter.getInstance(translator.getLocale()).formatDateAndTime(broker.getSelectionEndDate())));
		
		if (broker.isParticipantCanWithdraw()) {
			infos += createInfo("o_icon_tb_withdraw",
					translator.translate("config.overview.withdraw.deadline",
					Formatter.getInstance(translator.getLocale()).formatDateAndTime(broker.getWithdrawEndDate())));
		}
		
		infos += "</ul>";
		return infos;
	}
	
	public static String createInfo(String iconCss, String info) {
		return "<li><span><i class=\"o_icon o o_icon-fw " + iconCss + "\"></i> " + info + "</span></li>";
	}

	public static String formatPrettyText(String text, Integer truncate) {
		String formattedText = text;
		if (truncate != null) {
			formattedText = Formatter.truncate(formattedText, truncate);
		}
		formattedText = StringHelper.escapeHtml(formattedText);
		formattedText = Formatter.formatURLsAsLinks(formattedText, false);
		formattedText = Formatter.formatMailsAsLinks(formattedText, false);
		formattedText = Formatter.formatEmoticonsAsImages(formattedText);
		formattedText = formattedText.replace("\n", "<br>");
		return formattedText;
	}

}
