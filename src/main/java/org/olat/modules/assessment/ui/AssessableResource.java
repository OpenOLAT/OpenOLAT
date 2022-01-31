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
package org.olat.modules.assessment.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 24.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AssessableResource {
	
	private final Double minScore;
	private final Double maxScore;
	private final Double cutValue;
	private final Integer maxAttempts;
	
	private final boolean hasScore;
	private final boolean hasPassed;
	private final boolean hasAttempts;
	private final boolean hasMaxAttempts;
	private final boolean hasComments;
	
	public AssessableResource(boolean hasScore, boolean hasPassed, boolean hasAttempts, boolean hasMaxAttempts, boolean hasComments,
			Double minScore, Double maxScore, Double cutValue, Integer maxAttempts) {
		this.hasScore = hasScore;
		this.hasPassed = hasPassed;
		this.hasAttempts = hasAttempts;
		this.hasMaxAttempts = hasMaxAttempts;
		this.hasComments = hasComments;
		this.minScore = minScore;
		this.maxScore = maxScore;
		this.cutValue = cutValue;
		this.maxAttempts = maxAttempts;
	}
	
	public Double getMinScoreConfiguration() {
		return minScore;
	}
	
	public Double getMaxScoreConfiguration() {
		return maxScore;
	}
	
	public Double getCutValueConfiguration() {
		return cutValue;
	}
	
	public boolean hasScoreConfigured() {
		return hasScore;
	}
	
	public boolean hasAttemptsConfigured() {
		return hasAttempts;
	}
	
	public boolean hasMaxAttemptsConfigured() {
		return hasMaxAttempts;
	}
	
	public Integer getMaxAttempts() {
		return maxAttempts;
	}

	public boolean hasPassedConfigured() {
		return hasPassed;
	}
	
	public boolean hasCommentConfigured() {
		return hasComments;
	}
	
	public abstract AssessedIdentityListController createIdentityList(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, RepositoryEntry entry, AssessmentToolSecurityCallback assessmentCallback);

}
