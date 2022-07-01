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
package org.olat.modules.qpool;

import java.math.BigDecimal;
import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 21.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface QuestionItemShort extends OLATResourceable, CreateInfo, ModifiedInfo {
	
	public Long getKey();
	
	//general
	public String getIdentifier();
	
	public String getMasterIdentifier();

	public String getTitle();
	
	public String getLanguage();
	
	public String getKeywords();
	
	//classification
	public TaxonomyLevel getTaxonomyLevel();
	
	public String getTaxonomicPath();
	
	public String getTopic();
	
	//educational
	public String getEducationalContextLevel();

	public String getEducationalLearningTime();
	
	//question
	public String getItemType();
	
	public BigDecimal getDifficulty();
	
	public BigDecimal getStdevDifficulty();

	public BigDecimal getDifferentiation();

	public int getNumOfAnswerAlternatives();

	public int getUsage();

	//lifecycle
	public QuestionStatus getQuestionStatus();
	
	public Date getQuestionStatusLastModified();
	
	//technics
	public String getFormat();
	
	//management
	public Integer getCorrectionTime();
	
}
