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
package org.olat.modules.reminder.rule;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.modules.reminder.IdentitiesProviderRuleSPI;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.ui.RepositoryEntryLifecycleAfterValidRuleEditor;
import org.olat.modules.reminder.ui.RepositoryEntryRoleEditor;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 07.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryRoleRuleSPI implements IdentitiesProviderRuleSPI  {

	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	@Override
	public int getSortValue() {
		return 101;
	}
	
	@Override
	public String getLabelI18nKey() {
		return "rule.course.role";
	}
	
	@Override
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			Translator translator = Util.createPackageTranslator(RepositoryEntryLifecycleAfterValidRuleEditor.class, locale);
			String currentRoles = r.getRightOperand();
			
			try {
				Roles.valueOf(currentRoles);
			} catch (Exception e) {
				return null;
			}
			
			return translator.translate("rule.course.role." + currentRoles);
		}
		return null;
	}

	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new RepositoryEntryRoleEditor(rule);
	}
	
	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		return rule.clone();
	}

	@Override
	public List<Identity> evaluate(RepositoryEntry entry, ReminderRule rule) {
		List<Identity> identities = null;
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			String roles = r.getRightOperand();
			if(StringHelper.containsNonWhitespace(roles)) {
				switch(Roles.valueOf(roles)) {
					case owner:
						identities = repositoryEntryRelationDao.getMembers(entry, RepositoryEntryRelationType.entryAndCurriculums,
								GroupRoles.owner.name());
						break;
					case coach:
						identities = repositoryEntryRelationDao.getMembers(entry, RepositoryEntryRelationType.all,
								GroupRoles.coach.name());
						break;
					case participant:
						identities = repositoryEntryRelationDao.getMembers(entry, RepositoryEntryRelationType.all,
								GroupRoles.participant.name());
						break;
					case participantAndCoach:
						identities = repositoryEntryRelationDao.getMembers(entry, RepositoryEntryRelationType.all,
								GroupRoles.coach.name(), GroupRoles.participant.name());
						break;
					case ownerAndCoach:
						identities = repositoryEntryRelationDao.getMembers(entry, RepositoryEntryRelationType.all,
								GroupRoles.coach.name(), GroupRoles.owner.name());
						break;
					case all:
						identities = repositoryEntryRelationDao.getMembers(entry, RepositoryEntryRelationType.all,
								GroupRoles.owner.name(), GroupRoles.coach.name(), GroupRoles.participant.name());
						break;
				}
			}
		}

		return identities == null ? Collections.<Identity>emptyList() : identities;
	}
	
	public enum Roles {
		owner,
		coach,
		participant,
		participantAndCoach,
		ownerAndCoach,
		all
	}
}