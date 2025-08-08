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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBBrokerStatus;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldType;
import org.olat.modules.topicbroker.TBEnrollmentFunction;
import org.olat.modules.topicbroker.TBEnrollmentStrategyType;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBSelectionStatus;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicEnrollmentStatus;

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
	
	public static Set<Long> getFullyEnrolledParticipantKeys(TBBroker broker, List<TBSelection> selections) {
		Set<Long> fullyEnrolledParticipantKeys = new HashSet<>();
		Map<TBParticipant, List<TBSelection>> participantToEnrollments = selections.stream()
				.filter(TBSelection::isEnrolled)
				.collect(Collectors.groupingBy(TBSelection::getParticipant));
		for (Entry<TBParticipant, List<TBSelection>> pts : participantToEnrollments.entrySet()) {
			TBParticipant participant = pts.getKey();
			int numEnrollments = pts.getValue().size();
			int requiredEnrollments = TBUIFactory.getRequiredEnrollments(broker, participant);
			if (numEnrollments >= requiredEnrollments) {
				fullyEnrolledParticipantKeys.add(participant.getKey());
			}
		}
		return fullyEnrolledParticipantKeys;
	}
	
	public static TBBrokerStatus getBrokerStatus(TBBroker broker) {
		if (broker.getEnrollmentDoneDate() != null) {
			return TBBrokerStatus.enrollmentDone;
		}
		if (broker.getSelectionEndDate() != null && broker.getSelectionEndDate().before(new Date())) {
			return TBBrokerStatus.enrollmentInProgess;
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
			case enrollmentInProgess: return translator.translate("broker.status.enrollment.in.progress");
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
			case enrollmentInProgess: return "o_icon_tb_broker_enrollment_in_progress";
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
			case enrollmentInProgess: return "o_tb_label_broker_enrollment_in_progress";
			case enrollmentDone: return "o_tb_label_broker_enrollment_done";
			default:
			}
		}
		return null;
	}
	
	public static String getLabel(Translator translator, TBBroker broker) {
		return getLabel(translator, getBrokerStatus(broker));
	}
	
	public static String getLabel(Translator translator, TBBrokerStatus status) {
		return "<span class=\"o_labeled " + getLabelCss(status) 
				+ "\"><i class=\"o_icon " + getStatusIconCss(status)
				+ "\"> </i> "+ getTranslatedStatus(translator, status) + "</span>";
	}
	
	public static TBTopicEnrollmentStatus getEnrollmentStatus(Integer minParticipants, int numEnrollments) {
		return numEnrollments >= intOrZero(minParticipants)? TBTopicEnrollmentStatus.held: TBTopicEnrollmentStatus.notHeld;
	}

	public static String getTranslatedStatus(Translator translator, TBTopicEnrollmentStatus status) {
		if (status != null) {
			switch (status) {
			case held: return translator.translate("topic.enrollment.status.held");
			case notHeld: return translator.translate("topic.enrollment.status.not.held");
			default:
			}
		}
		return null;
	}
	
	public static String getStatusIconCss(TBTopicEnrollmentStatus status) {
		if (status != null) {
			switch (status) {
			case held: return "o_icon_tb_topic_held";
			case notHeld: return "o_icon_tb_topic_not_held";
			default:
			}
		}
		return null;
	}
	
	public static String getLabelLightCss(TBTopicEnrollmentStatus status) {
		if (status != null) {
			switch (status) {
			case held: return "o_tb_label_light_held";
			case notHeld: return "o_tb_label_light_not_held";
			default:
			}
		}
		return null;
	}
	
	public static String getAvailability(Translator translator, TBTopicEnrollmentStatus status, int maxParticipants,
			int numEnrollments, int numWaitingList) {
		if (TBTopicEnrollmentStatus.notHeld == status) {
			return null;
		}
		
		if (numEnrollments >= maxParticipants) {
			if (numWaitingList > 0) {
				return  translator.translate("topic.availability.full.waiting");
			}
			return translator.translate("topic.availability.full");
		}
		return translator.translate("topic.availability.seats.left", String.valueOf(maxParticipants - numEnrollments));
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
			return TBSelectionStatus.surplus;
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
			case surplus: return translator.translate("selection.status.surplus");
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
			case surplus: return "o_icon_tb_selection_surplus";
			default:
			}
		}
		return null;
	}

	public static String getLabelCss(TBSelectionStatus status) {
		if (status != null) {
			switch (status) {
			case selected: return "o_tb_label_selected";
			case enrolled: return "o_tb_label_enrolled";
			case waitingList: return "o_tb_label_waiting";
			case notEnrolled: return "o_tb_label_not_enrolled";
			case surplus: return "o_tb_label_surplus";
			case fillIn: return "o_tb_label_fillin";
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
			case surplus: return "o_tb_label_light_surplus";
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
			case surplus: return "o_tb_priority_surplus";
			case fillIn: return "o_tb_priority_fillin";
			default:
			}
		}
		return null;
	}
	
	public static final String getPriorityLabel(Translator translator, TBSelectionStatus status, int sortOrder, String topicTitle) {
		String label = "<div class=\"o_tb_priority_label\"";
		if (StringHelper.containsNonWhitespace(topicTitle)) {
			label += " title=\"";
			label += topicTitle;
			label += "\"";
		}
		label += ">";
		label += "<div class=\"";
		label += getPriorityCss(status);
		label += "\">";
		if (TBSelectionStatus.surplus == status) {
			label += translator.translate("selection.surplus.abbr");
		} else {
			label += sortOrder;
		}
		label += "</div>";
		label += "</div>";
		return label;
	}
	
	public static final String getPriorityLabelAsRow(Translator translator, TBSelectionStatus status, int sortOrder) {
		return getPriorityLabelsAsRow(List.of(getPriorityLabel(translator, status, sortOrder, null)));
	}
	
	public static final String getPriorityLabelsAsRow(List<String> formatedLabels) {
		String labels = "<div class=\"o_tb_priority_labels\">";
		for (String formatedLabel : formatedLabels) {
			labels += formatedLabel;
		}
		labels += "</div>";
		return labels;
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
		String participants = "<i class=\"o_icon o_icon-fw o_icon_num_participants\"></i> ";
		if (topic.getMinParticipants() > 0) {
			participants += translator.translate("topic.participants.label.min.max",
					String.valueOf(topic.getMinParticipants()), String.valueOf(topic.getMaxParticipants()));
		} else {
			participants += translator.translate("topic.participants.label.max",
					String.valueOf(topic.getMaxParticipants()));
		}
		return participants;
	}
	
	public static String getExecutionPeriod(Formatter formatter, TBTopic topic) {
		if (topic.getBeginDate() == null && topic.getEndDate() == null) {
			return null;
		}
		
		String executionPeriod = "<i class=\"o_icon o_icon-fw o_icon_calendar\"></i> ";
		if (topic.getBeginDate() != null) {
			executionPeriod += formatter.formatDateWithDay(topic.getBeginDate());
			if (topic.getEndDate() != null) {
				executionPeriod += " - ";
			}
		}
		if (topic.getEndDate() != null) {
			executionPeriod += formatter.formatDateWithDay(topic.getEndDate());
		}
		
		return executionPeriod;
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
	
	public static String getConfigInfos(Translator translator, TBBroker broker, boolean withMethod,
			Integer participantMaxRequiredEnrollments) {
		String infos = "<ul class=\"list-unstyled\">";
		
		infos += createInfo("o_icon_topicbroker",
				translator.translate("config.overview.max.selections",
						String.valueOf(broker.getMaxSelections())));
		
		String requiredEnrollmentsI18n = broker.isParticipantCanEditRequiredEnrollments()
				? "config.overview.required.enrollments.editable"
				: "config.overview.required.enrollments";
		String maxRequiredEnrollments = participantMaxRequiredEnrollments != null
				? String.valueOf(participantMaxRequiredEnrollments)
				: String.valueOf(broker.getRequiredEnrollments());
		infos += createInfo("o_icon_tb_broker_enrollment_done",
				translator.translate(requiredEnrollmentsI18n,
				maxRequiredEnrollments));
		
		if (withMethod) {
			String methodAuto = broker.isAutoEnrollment()
					? translator.translate("config.overview.method.auto")
					: translator.translate("config.overview.method.manually");
			String overlappingPeriodAllowed = broker.isOverlappingPeriodAllowed()
					? translator.translate("config.overview.overlapping.allowed.yes")
					: translator.translate("config.overview.overlapping.allowed.no");
			infos += createInfo("o_icon_tb_method",
					translator.translate("config.overview.method.3",
					translator.translate("strategy.fair"), methodAuto, overlappingPeriodAllowed));
			
		}
		infos += createInfo("o_icon_calendar", translator.translate("config.overview.selection.period", 
				Formatter.getInstance(translator.getLocale()).formatDateAndTime(broker.getSelectionStartDate()),
				Formatter.getInstance(translator.getLocale()).formatDateAndTime(broker.getSelectionEndDate())));
		
		if (broker.isParticipantCanWithdraw()) {
			String deadline = broker.getWithdrawEndDate() != null
					? Formatter.getInstance(translator.getLocale()).formatDateAndTime(broker.getWithdrawEndDate())
					: translator.translate("config.overview.withdraw.deadline.none");
			infos += createInfo("o_icon_tb_withdraw",
					translator.translate("config.overview.withdraw.deadline",
					deadline));
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

	public static String getImportColumns(Translator translator, List<TBCustomFieldDefinition> definitions) {
		String columns = "<ul class=\"list-unstyled\">";
		columns += "<li><strong>" + translator.translate("topic.identifier") + " *</strong></li>";
		columns += "<li><strong>" + translator.translate("topic.title") + " *</strong></li>";
		columns += "<li>" + translator.translate("topic.description") + "</li>";
		columns += "<li><strong>" + translator.translate("topic.participants.min") + " *</strong></li>";
		columns += "<li><strong>" + translator.translate("topic.participants.max") + " *</strong></li>";
		columns += "<li>" + translator.translate("topic.begin.date") + "</li>";
		columns += "<li>" + translator.translate("topic.end.date") + "</li>";
		columns += "<li>" + translator.translate("topic.group.restriction") + "</li>";
		
		if (definitions != null && !definitions.isEmpty()) {
			for (TBCustomFieldDefinition definition : definitions) {
				if (TBCustomFieldType.text == definition.getType()) {
					columns += "<li>" + StringHelper.escapeHtml(definition.getName()) + "</li>";
				}
			}
		}
		
		columns += "</ul>";
		return columns;
	}
	
	public static String getTypeIconCss(TBEnrollmentStrategyType type) {
		if (type != null) {
			switch (type) {
			case maxEnrollments: return "o_icon_tb_strategy_max_enrollments";
			case maxPriorities: return "o_icon_tb_strategy_max_priorities";
			case maxTopics: return "o_icon_tb_strategy_max_topics";
			case custom: return "o_icon_tb_strategy_custom";
			default:
			}
		}
		return null;
	}
	
	public static String getTranslatedType(Translator translator, TBEnrollmentStrategyType type) {
		if (type != null) {
			switch (type) {
			case maxEnrollments: return translator.translate("enrollment.strategy.max.enrollments");
			case maxPriorities: return translator.translate("enrollment.strategy.max.priorities");
			case maxTopics: return translator.translate("enrollment.strategy.max.topics");
			case custom: return translator.translate("enrollment.strategy.custom");
			default:
			}
		}
		return null;
	}
	
	public static String getTranslatedTypeDesc(Translator translator, TBEnrollmentStrategyType type) {
		if (type != null) {
			switch (type) {
			case maxEnrollments: return translator.translate("enrollment.strategy.max.enrollments.desc");
			case maxPriorities: return translator.translate("enrollment.strategy.max.priorities.desc");
			case maxTopics: return translator.translate("enrollment.strategy.max.topics.desc");
			case custom: return translator.translate("enrollment.strategy.custom.desc");
			default:
			}
		}
		return null;
	}
	
	public static String getTranslatedWeight(Translator translator, Integer weight) {
		if (weight != null) {
			switch (weight) {
			case 1: return translator.translate("enrollment.strategy.overview.weight.low");
			case 2: return translator.translate("enrollment.strategy.overview.weight.low.neutral");
			case 3: return translator.translate("enrollment.strategy.overview.weight.neutral");
			case 4: return translator.translate("enrollment.strategy.overview.weight.neutral.high");
			case 5: return translator.translate("enrollment.strategy.overview.weight.high");
			default:
			}
		}
		return translator.translate("enrollment.strategy.overview.weight.not.considered");
	}
	
	public static final String getTranslatedFunction(Translator translator, TBEnrollmentFunction function) {
		if (function != null) {
			switch (function) {
			case constant: return translator.translate("enrollment.strategy.function.constant");
			case linear: return translator.translate("enrollment.strategy.function.linear");
			case logarithmic: return translator.translate("enrollment.strategy.function.logarithmic");
			default:
			}
		}
		return null;
	}
	
	public static String getTranslatedBreakPoint(Translator translator, Integer breakPoint) {
		if (breakPoint == null || breakPoint < 0) {
			return translator.translate("enrollment.strategy.function.without.break.point");
		}
		
		return translator.translate("enrollment.strategy.function.priority", String.valueOf(breakPoint));
	}
	
	public static String getLogContextIconCss(TBActivityLogContext context) {
		if (context != null) {
			return switch (context) {
			case configuration -> "o_icon_tb_configuration";
			case topic -> "o_icon_tb_topics";
			case participant -> "o_icon_user";
			case enrollmentProcess -> "o_icon_tb_enroll";
			default -> null;
			};
		}
		return null;
	}
	
	public static String getTranslatedLogContext(Translator translator, TBActivityLogContext context) {
		if (context != null) {
			return switch (context) {
			case configuration -> translator.translate("activity.log.context.configuration");
			case topic -> translator.translate("activity.log.context.topic");
			case participant -> translator.translate("activity.log.context.participant");
			case enrollmentProcess -> translator.translate("activity.log.context.enrollment.process");
			default -> null;
			};
		}
		return null;
	}
	
	public static int intOrZero(Integer integer) {
		return integer != null? integer.intValue(): 0;
	}

}
