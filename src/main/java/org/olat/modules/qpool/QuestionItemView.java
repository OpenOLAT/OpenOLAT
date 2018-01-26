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


/**
 * 
 * Initial date: 21.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface QuestionItemView extends QuestionItemShort {

	public boolean isAuthor();

	public boolean isReviewer();
	
	public boolean isTeacher();
	
	public boolean isManager();
	
	public boolean isRater();

	public boolean isEditableInPool();

	public boolean isEditableInShare();
	
	public boolean isEditable();
	
	public boolean isReviewableFormat();
	
	public boolean isMarked();
	
	public Double getRating();
	
	public int getNumberOfRatings();
	
	//general
	public String getCoverage();

	public String getAdditionalInformations();
	
	//life cycle
	public String getItemVersion();
	
	public enum OrderBy {
		marks,
		key,
		identifier,
		title,
		topic,
		creationDate,
		lastModified,
		keywords,
		coverage,
		additionalInformations,
		taxonomyLevel,
		taxonomyPath,
		difficulty,
		stdevDifficulty,
		differentiation,
		numOfAnswerAlternatives,
		usage,
		itemType,
		format,
		rating,
		numberOfRatings,
		itemVersion,
		status,
		statusLastModified
	}

}