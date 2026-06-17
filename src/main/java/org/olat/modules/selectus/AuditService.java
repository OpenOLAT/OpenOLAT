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
package org.olat.modules.selectus;

import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationAssignment;
import org.olat.modules.selectus.model.ApplicationAssignmentLight;
import org.olat.modules.selectus.model.ApplicationComment;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.PublicFeedback;
import org.olat.modules.selectus.model.RecruitingAuditLog;
import org.olat.modules.selectus.model.RecruitingAuditLogLight;
import org.olat.modules.selectus.model.RecruitingAuditLogUserSettings;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.log.RecruitingAuditLogSearchParameters;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.ReviewResponse;

/**
 * 
 * Initial date: 20 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AuditService {
	
	public RecruitingAuditLogUserSettings getOrCreateRecruitingAuditLogUserSettings(Identity identity);
	
	public RecruitingAuditLogUserSettings updateRecruitingAuditLogUserSettings(RecruitingAuditLogUserSettings settings);
	
	public String toAuditXml(Position position);
	
	public String toAuditXml(PositionReviewDefinition reviewDefinition);
	
	public String toAuditXml(Application application);
	
	public String toAuditXml(Identity identity);
	
	public String toAuditXml(Reference reference);
	
	public String toAuditXml(ApplicationComment comment);
	
	public String toAuditXml(ApplicationAssignment assignment);
	
	public String toAuditXml(ApplicationAssignmentLight assignment);
	
	public String toAuditXml(List<ReviewResponse> responses);
	
	public String toAuditXml(PublicFeedback feedback);
	
	public String toAuditXml(ApplicationFeedback feedback);
	
	public void auditPositionLog(RecruitingAuditLog.Action action, RecruitingAuditLog.ActionTarget target,
			String before, String after, String messageI18n, String[] args, Translator translator,
			PositionRef position, Identity doer);
	
	public void auditApplicationLog(RecruitingAuditLog.Action action, RecruitingAuditLog.ActionTarget target,
			String before, String after, String messageI18n, String[] args, Translator translator,
			PositionRef position, ApplicationRef application, Identity doer);
	
	public void auditApplicationDecisionLog(RecruitingAuditLog.Action action, RecruitingAuditLog.ActionTarget target,
			Integer before, Integer after, String messageI18n, String[] args, Translator translator,
			PositionRef position, ApplicationRef application, Identity doer);

	public void auditCommitteeLog(RecruitingAuditLog.Action action, RecruitingAuditLog.ActionTarget target,
			String messageI18n, String[] args, Translator translator, PositionRef position, Identity member, Identity doer);
	
	public void auditCommitteeLog(RecruitingAuditLog.Action action, RecruitingAuditLog.ActionTarget target, String before, String after,
			String messageI18n, String[] args, Translator translator, PositionRef position, Identity member, Identity doer);
	
	public void auditRatingLog(RecruitingAuditLog.Action action, RecruitingAuditLog.ActionTarget target, String before, String after,
			String messageI18n, String[] args, Translator translator, PositionRef position, ApplicationRef application,
			UserRating userRating, Identity doer);
	
	public void auditRefereeLog(RecruitingAuditLog.Action action, RecruitingAuditLog.ActionTarget target, String before, String after,
			String messageI18n, String[] args, Translator translator, PositionRef position, ApplicationRef application,
			Reference reference, Identity doer);
	
	public void auditReviewLog(RecruitingAuditLog.Action action, String before, String after,
			String messageI18n, String[] args, Translator translator, PositionRef position, ApplicationRef application,
			Identity doer);
	
	public void auditCommentLog(RecruitingAuditLog.Action action, String before, String after,
			String messageI18n, String[] args, Translator translator, PositionRef position, ApplicationRef application,
			ApplicationComment comment, Identity doer);
	
	public void auditAssignmentLog(RecruitingAuditLog.Action action, String before, String after,
			String messageI18n, String[] args, Translator translator,
			PositionRef position, ApplicationRef application, Identity member, Identity doer);
	
	public void auditPublicFeedbackLog(RecruitingAuditLog.Action action, String before, String after,
			String messageI18n, String[] args, Translator translator,
			PositionRef position, ApplicationRef application, Identity doer);
	
	public void auditPublicFeedbackLinkLog(RecruitingAuditLog.Action action, String before, String after,
			String messageI18n, String[] args, Translator translator,
			PositionRef position, ApplicationRef application, Identity doer);
	
	public void auditFeedbackMemberLog(RecruitingAuditLog.Action action, String before, String after,
			String messageI18n, String[] args, Translator translator, PositionRef position, ApplicationRef application,
			ApplicationFeedback feedback, Identity doer);
	
	public void auditFeedbackLog(RecruitingAuditLog.Action action, String before, String after,
			String messageI18n, String[] args, Translator translator, PositionRef position, ApplicationRef application,
			ApplicationFeedback feedback, Identity doer);
	

	public int countLogs(Identity identity, Roles roles, RecruitingAuditLogSearchParameters params);
	
	public List<RecruitingAuditLog> getLogs(Identity identity, Roles roles, RecruitingAuditLogSearchParameters params);
	
	public List<RecruitingAuditLogLight> getLightLogs(Identity identity, Roles roles, RecruitingAuditLogSearchParameters params);
	
	public Set<Long> getReadAuditLogs(Identity identity);
	
	public void markAsRead(List<Long> logs, Identity identity);
	
	public void markAsUnread(List<Long> logs, Identity identity);
	
	public void sendNotifications();
	
	public enum NotificationIntervals {
		never(0),
		monthly(720),
		weekly(168),
		daily(24),
		halfdaily(12),
		fourhourly(4),
		twohourly(2);
		
		private final int hours;
		
		private NotificationIntervals(int hours) {
			this.hours = hours;
		}
		
		public int hours() {
			return hours;
		}
		
		public static boolean valid(String val) {
			boolean allOk = false;
			if(StringHelper.containsNonWhitespace(val)) {
				for(NotificationIntervals interval:NotificationIntervals.values()) {
					if(interval.name().equals(val)) {
						allOk = true;
					}
				}
			}
			return allOk;
		}
	}
}
