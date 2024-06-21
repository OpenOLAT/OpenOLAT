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
package org.olat.course.nodes.gta;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.modules.forms.EvaluationFormParticipation;

/**
 * 
 * Initial date: 12 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface TaskReviewAssignment extends CreateInfo, ModifiedInfo {
	
	public Long getKey();
	
	public boolean isAssigned();
	
	public void setAssigned(boolean assigned);
	
	public TaskReviewAssignmentStatus getStatus();
	
	public void setStatus(TaskReviewAssignmentStatus status);
	
	public Task getTask();
	
	public Identity getAssignee();
	
	public EvaluationFormParticipation getParticipation();
	
	public Float getRating();
	
	public void setRating(Float rating);
	

}
