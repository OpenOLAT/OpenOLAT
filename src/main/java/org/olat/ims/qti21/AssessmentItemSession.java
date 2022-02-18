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
package org.olat.ims.qti21;

import java.math.BigDecimal;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 02.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentItemSession extends CreateInfo, ModifiedInfo, AssessmentItemSessionRef {

	
	/**
	 * @return The identifier of the assessmentItemRef of the session
	 */
	public String getAssessmentItemIdentifier();
	
	/**
	 * @return The identifier of the first section in the ancestors of
	 * 		the assessmentItemRef of the session
	 */
	public String getSectionIdentifier();
	
	/**
	 * @return The identifier of the testPart in the ancestors of the
	 * 		assessmentItemRef of the session
	 */
	public String getTestPartIdentifier();
	
	public Boolean getPassed();

	public void setPassed(Boolean passed);
	
	/**
	 * @return The duration in milliseconds
	 */
	public Long getDuration();
	
	/**
	 * @param duration The duration in milliseconds
	 */
	public void setDuration(Long duration);
	
	public BigDecimal getScore();

	public void setScore(BigDecimal score);
	
	public BigDecimal getManualScore();
	
	public void setManualScore(BigDecimal score);
	
	public String getCoachComment();
	
	public void setCoachComment(String comment);
	
	public boolean isToReview();
	
	public void setToReview(boolean toReview);
	
	public AssessmentTestSession getAssessmentTestSession();

}
