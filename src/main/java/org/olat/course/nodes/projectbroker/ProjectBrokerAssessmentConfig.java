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
package org.olat.course.nodes.projectbroker;

import org.olat.course.assessment.handler.ModuleAssessmentConfig;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 19 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjectBrokerAssessmentConfig extends ModuleAssessmentConfig {

	public ProjectBrokerAssessmentConfig(ModuleConfiguration config) {
		super(config);
	}

	@Override
	public boolean isAssessable() {
		return true;
	}
	
	@Override
	public boolean hasScore() {
		return false;
	}

	@Override
	public boolean hasPassed() {
		return false;
	}

	@Override
	public boolean hasAttempts() {
		return false;
	}

	@Override
	public boolean hasComment() {
		return false;
	}

	@Override
	public boolean hasIndividualAsssessmentDocuments() {
		return false;
	}
	
	@Override
	public boolean hasStatus() {
		return false; // Project broker Course node has no status-field
	}
	
	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean hasEditableDetails() {
		return config.getBooleanSafe(ProjectBrokerCourseNode.CONF_DROPBOX_ENABLED);
	}

}
