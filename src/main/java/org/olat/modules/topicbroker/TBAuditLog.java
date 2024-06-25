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
package org.olat.modules.topicbroker;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;

/**
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public interface TBAuditLog extends CreateInfo {
	
	Long getKey();

	Action getAction();

	String getBefore();

	String getAfter();
	
	Identity getDoer();
	
	TBBroker getBroker();
	
	TBParticipant getParticipant();
	
	TBTopic getTopic();
	
	TBSelection getSelection();
	
	enum Action {
		brokerCreate,
		brokerUpdateContent,
		brokerEnrollmentStart,
		brokerEnrollmentDone,
		participantCreate,
		participantUpdateContent,
		topicCreate,
		topicUpdateContent,
		topicUpdateSortOrder,
		topicUpdateFile,
		topicDeleteFile,
		topicDeleteSoftly,
		selectionCreate,
		selectionUpdateSortOrder,
		selectionEnrollManually,
		selectionEnrollProcessMan,
		selectionEnrollProcessAuto,
		selectionWithdrawManually,
		selectionWithdrawProcessMan,
		selectionWithdrawProcessAuto,
		selectionDelete,
		
		// Process actions
		processStart,
		processEnd,
		topicSchuffle,
		topicExcludeByUnpopularity,
		evaluationStart,
		evaluationEnd,
		evaluationLevelStart,
		evaluationLevelEnd,
		evaluationLevelEndTopicsEmpty,
		evaluationTopicStart,
		evaluationTopicEnd,
		participantsOrdered,
		participantEnroll,
		participantPreEnrolled,
		participantWithdraw,
		;
	}
	
	static final class TBFileAuditLog {
		
		private String identifier;
		private String filename;
		
		public TBFileAuditLog(String identifier, String filename) {
			this.identifier = identifier;
			this.filename = filename;
		}

		public String getIdentifier() {
			return identifier;
		}
		
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}
		
		public String getFilename() {
			return filename;
		}
		
		public void setFilename(String filename) {
			this.filename = filename;
		}
		
	}
	
}
