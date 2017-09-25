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

import java.util.List;

import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * Interface to provide the response identifier for custom/additional feedbacks
 * with conditions and the list of possible answers.
 * 
 * Initial date: 1 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ResponseIdentifierForFeedback {
	
	public Identifier getResponseIdentifier();
	
	public List<Answer> getAnswers();
	
	public class Answer {
		private final String label;
		private final Identifier identifier;
		
		public Answer(Identifier identifier, String label) {
			this.label = label;
			this.identifier = identifier;
		}
		
		public String getLabel() {
			return label;
		}
		
		public Identifier getIdentifier() {
			return identifier;
		}

	}
}
