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
package org.olat.course.assessment;

import java.util.Date;

/**
 * 
 * Initial date: 24 juin 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface UserEfficiencyStatementShort {
	
	public Long getKey();

	public Date  getLastModified();
	
	public Date getLastUserModified();
	
	public Date getLastCoachModified();
	
	public Date getCreationDate();
	
	public Long getCourseRepoKey();
	
	public String getTitle();

	public String getShortTitle();
	
	public Float getScore();
	
	public String getGrade();
	
	public String getGradeSystemIdent();
	
	public String getPerformanceClassIdent();
	
	public Boolean getPassed();
	
	public Integer getTotalNodes();

	public Integer getAttemptedNodes();
	
	public Integer getPassedNodes();
}
