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
import java.util.Date;

import jakarta.persistence.Transient;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentTestSession extends CreateInfo, ModifiedInfo {
	
	public Long getKey();
	
	public Date getFinishTime();
	
	public void setFinishTime(Date timestamp);
	
	public Date getTerminationTime();
	
	public void setTerminationTime(Date timestamp);
	
	public Boolean getPassed();

	public void setPassed(Boolean passed);

	public BigDecimal getScore();

	public void setScore(BigDecimal score);
	
	public BigDecimal getManualScore();
	
	public void setManualScore(BigDecimal manualScore);
	
	public BigDecimal getMaxScore();
	
	public void setMaxScore(BigDecimal maxScore);
	
	public Integer getNumOfQuestions();

	public void setNumOfQuestions(Integer numOfQuestions);

	public Integer getNumOfAnsweredQuestions();

	public void setNumOfAnsweredQuestions(Integer numOfAnsweredQuestions);
	
	/**
	 * Additional time in a time limited test
	 * @return The additional time in seconds
	 */
	public Integer getExtraTime();
	
	/**
	 * @return The additional time in seconds for compensation for disadvantage
	 */
	public Integer getCompensationExtraTime();
	
	/**
	 * @return The score + manual score
	 */
	@Transient
	public default BigDecimal getFinalScore() {
		BigDecimal finalScore = getScore();
		BigDecimal manualScore = getManualScore();
		if(finalScore == null) {
			finalScore = manualScore;
		} else if(manualScore != null) {
			finalScore = finalScore.add(manualScore);
		}
		return finalScore;
	}
	
	/**
	 * Return the duration in milliseconds
	 * @return
	 */
	public Long getDuration();
	
	/**
	 * SSet the duration in milliseconds
	 * @param duration
	 */
	public void setDuration(Long duration);
	
	public boolean isExploded();
	
	public void setExploded(boolean exploded);
	
	public boolean isCancelled();
	
	public void setCancelled(boolean cancelled);
	
	public String getStorage();
	
	
	public Identity getIdentity();
	
	public String getAnonymousIdentifier();
	
	public boolean isAuthorMode();
	
	public AssessmentEntry getAssessmentEntry();
	
	public RepositoryEntry getTestEntry();
	
	public RepositoryEntry getRepositoryEntry();
	
	public String getSubIdent();

}
