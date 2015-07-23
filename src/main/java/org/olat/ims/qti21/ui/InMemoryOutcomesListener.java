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

import org.olat.ims.qti21.OutcomesListener;

/**
 * Hold the score and pass values in memory
 * 
 * 
 * Initial date: 20.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InMemoryOutcomesListener implements OutcomesListener {
	
	private Float score;
	private Boolean pass;

	public Float getScore() {
		return score;
	}
	
	public Boolean getPass() {
		return pass;
	}

	@Override
	public void updateOutcomes(Float updatedScore, Boolean updatedPassed) {
		score = updatedScore;
		pass = updatedPassed;
	}

	@Override
	public void submit(Float submittedScore, Boolean submittedPass) {
		score = submittedScore;
		pass = submittedPass;
	}
}
