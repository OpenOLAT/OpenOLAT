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
package org.olat.ims.qti21.model;

import java.util.Date;

import org.olat.ims.qti21.AssessmentTestMarks;

/**
 * 
 * Initial date: 19.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InMemoryAssessmentTestMarks implements AssessmentTestMarks {

	private String marks;
	private String hiddenRubrics;
	
	public InMemoryAssessmentTestMarks() {
		//
	}
	
	@Override
	public Date getCreationDate() {
		return null;
	}
	
	@Override
	public Date getLastModified() {
		return null;
	}

	@Override
	public void setLastModified(Date date) {
		//
	}

	@Override
	public String getMarks() {
		return marks;
	}

	@Override
	public void setMarks(String marks) {
		this.marks = marks;
	}

	@Override
	public String getHiddenRubrics() {
		return hiddenRubrics;
	}

	@Override
	public void setHiddenRubrics(String rubrics) {
		this.hiddenRubrics = rubrics;
	}
}
