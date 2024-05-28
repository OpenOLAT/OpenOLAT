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
package org.olat.modules.reminder.rule;

import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RepositoryEntryRuleSPI;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.ui.RepositoryEntryStatusEditor;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class RepositoryEntryStatusRuleSPI implements RepositoryEntryRuleSPI {
	
	public static final Status[] ALL_STATUS = Status.values();
	public enum Status {
		preparation,
		review,
		coachpublished,
		published,
		notPreparation,
		notReview,
		notCoachpublished,
		notPublished,
	}

	@Override
	public String getLabelI18nKey() {
		return "rule.repository.status";
	}

	@Override
	public int getSortValue() {
		return 2;
	}

	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		return rule.clone();
	}
	
	@Override
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl reminderRule) {
			Translator translator = Util.createPackageTranslator(RepositoryEntryStatusEditor.class, locale);
			String status = reminderRule.getRightOperand();
			
			try {
				Status.valueOf(status);
			} catch (Exception e) {
				return null;
			}
			
			return translator.translate("rule.repository.status.text." + status);
		}
		return null;
	}

	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new RepositoryEntryStatusEditor(rule);
	}

	@Override
	public boolean evaluate(RepositoryEntry entry, ReminderRule rule) {
		if (rule instanceof ReminderRuleImpl reminderRule) {
			try {
				Status status = Status.valueOf(reminderRule.getRightOperand());
				return evaluateStatus(status, entry.getEntryStatus());
			} catch (Exception e) {
				//
			}
		}
		return false;
	}

	boolean evaluateStatus(Status status, RepositoryEntryStatusEnum repositoryEntryStatus) {
		return switch (status) {
		case preparation -> RepositoryEntryStatusEnum.preparation == repositoryEntryStatus;
		case review -> RepositoryEntryStatusEnum.review == repositoryEntryStatus;
		case coachpublished -> RepositoryEntryStatusEnum.coachpublished == repositoryEntryStatus;
		case published -> RepositoryEntryStatusEnum.published == repositoryEntryStatus;
		case notPreparation -> RepositoryEntryStatusEnum.preparation != repositoryEntryStatus;
		case notReview -> RepositoryEntryStatusEnum.review != repositoryEntryStatus;
		case notCoachpublished -> RepositoryEntryStatusEnum.coachpublished != repositoryEntryStatus;
		case notPublished -> RepositoryEntryStatusEnum.published != repositoryEntryStatus;
		default -> false;
		};
	}

}
