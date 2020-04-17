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
package org.olat.ims.qti21.model.statistics;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;

/**
 * 
 * Initial date: 16 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrderStatistics {

	private final SimpleChoice choice;
	
	private final long numOfCorrect;
	private final long numOfIncorrect;
	private final long notAnswered;
	
	public OrderStatistics(SimpleChoice choice, long numOfCorrect, long numOfIncorrect, long notAnswered) {
		this.numOfCorrect = numOfCorrect;
		this.numOfIncorrect = numOfIncorrect;
		this.notAnswered = notAnswered;
		this.choice = choice;
	}
	
	public long getNumOfCorrect() {
		return numOfCorrect;
	}

	public long getNumOfIncorrect() {
		return numOfIncorrect;
	}

	public long getNotAnswered() {
		return notAnswered;
	}

	public SimpleChoice getChoice() {
		return choice;
	}
}
