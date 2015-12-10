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
package org.olat.ims.qti21.model.xml;

import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;

/**
 * 
 * Initial date: 10.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ScoreBuilder {
	
	private Double score;
	private OutcomeDeclaration outcomeDeclaration;
	
	public ScoreBuilder(Double score, OutcomeDeclaration outcomeDeclaration) {
		this.score = score;
		this.outcomeDeclaration = outcomeDeclaration;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public OutcomeDeclaration getOutcomeDeclaration() {
		return outcomeDeclaration;
	}

	public void setOutcomeDeclaration(OutcomeDeclaration outcomeDeclaration) {
		this.outcomeDeclaration = outcomeDeclaration;
	}
	
	

}
