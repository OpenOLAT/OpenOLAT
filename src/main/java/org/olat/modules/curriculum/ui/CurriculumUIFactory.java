/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.CurriculumAutomationRule;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * 
 * Initial date: 12 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumUIFactory {
	
	private CurriculumUIFactory() {
		//
	}
	
	public static final String getMembershipLabelCssClass(GroupMembershipStatus status) {
		return switch(status) {
			case reservation -> "o_gmembership_status_reservation";
			case active -> "o_gmembership_status_active";
			case cancel -> "o_gmembership_status_cancel";
			case cancelWithFee -> "o_gmembership_status_cancelwithfee";
			case declined -> "o_gmembership_status_declined";
			case resourceDeleted ->"o_gmembership_status_resourcedeleted";
			case finished ->"o_gmembership_status_finished";
			case removed -> "o_gmembership_status_removed";
			default -> null;
		};
	}
	
	public static final String getMembershipIconCssClass(GroupMembershipStatus status) {
		return switch(status) {
			case reservation -> "o_membership_status_pending";
			case active -> "o_membership_status_active";
			case cancel -> "o_membership_status_cancel";
			case cancelWithFee -> "o_membership_status_cancelwithfee";
			case declined -> "o_membership_status_declined";
			case resourceDeleted -> "o_membership_status_resourcedeleted";
			case finished -> "o_membership_status_finished";
			case removed -> "o_membership_status_removed";
			default -> null;
		};
	}

	public static String translateAutomationStatus(Translator translator, String statusString) {
		if (statusString == null) {
			return null;
		}
		if (CurriculumElementStatus.isValueOf(statusString)) {
			return translator.translate("status." + statusString);
		}
		if (RepositoryEntryStatusEnum.isValid(statusString)) {
			return translator.translate(RepositoryEntryStatusEnum.valueOf(statusString).i18nKey());
		}
		return statusString;
	}

	public static String translateAutomationCondition(Translator translator, CurriculumAutomationRule rule) {
		if (rule.getDependingOn() == AutomationDependingOn.STATUS) {
			return translateAutomationStatusCondition(translator, rule.getDependingOnStatus());
		}
		boolean after = rule.getDirection() == OffsetDirection.AFTER;
		boolean endRef = CurriculumAutomationRule.REFERENCE_END.equals(rule.getReference())
				|| (rule.getReference() == null && after);
		String anchor = translator.translate(endRef
				? "automation.condition.anchor.end" : "automation.condition.anchor.begin");
		if (rule.getUnit() == null || rule.getUnit() == AutomationUnit.SAME_DAY) {
			return translator.translate("relative.date.display.same.day", new String[] { anchor });
		}
		if (rule.getValue() == null) {
			return "-";
		}
		String base = "relative.date.unit." + rule.getUnit().name().toLowerCase().replaceAll("s$", "");
		String unit = translator.translate(rule.getValue() == 1 ? base : base + "s");
		String key = after ? "relative.date.display.after" : "relative.date.display.before";
		return translator.translate(key, new String[] { String.valueOf(rule.getValue()), unit, anchor });
	}

	private static String translateAutomationStatusCondition(Translator translator, Set<String> statuses) {
		if (statuses == null || statuses.isEmpty()) {
			return "-";
		}
		String sep = " " + translator.translate("automation.condition.status.or") + " ";
		String joined = Arrays.stream(CurriculumElementStatus.values())
				.filter(s -> statuses.contains(s.name()))
				.map(s -> "\"" + translateAutomationStatus(translator, s.name()) + "\"")
				.collect(Collectors.joining(sep));
		return translator.translate("automation.condition.status", new String[] { joined });
	}

}
