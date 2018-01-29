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
package org.olat.ims.qti21.ui;

import java.util.Date;

/**
 * 
 * Initial date: 1 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21OverrideOptions {
	
	private final Long assessmentTestMaxTimeLimit;
	private final Date startTestDate;
	private final Date endTestDate;
	
	public QTI21OverrideOptions(Long assessmentTestMaxTimeLimit, Date startTestDate, Date endTestDate) {
		this.assessmentTestMaxTimeLimit = assessmentTestMaxTimeLimit;
		this.startTestDate = startTestDate;
		this.endTestDate = endTestDate;
	}

	public Long getAssessmentTestMaxTimeLimit() {
		return assessmentTestMaxTimeLimit;
	}

	public Date getStartTestDate() {
		return startTestDate;
	}

	public Date getEndTestDate() {
		return endTestDate;
	}
	
	public static QTI21OverrideOptions nothingOverriden() {
		return new QTI21OverrideOptions(null, null, null);
	}
}
