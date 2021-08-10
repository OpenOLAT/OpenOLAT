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
package org.olat.repository.ui.author.copy.wizard.dates;

import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.control.Event;

/**
 * Initial date: 26.05.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class MoveDatesEvent extends Event {

	private static final long serialVersionUID = 4345938782630176773L;

	private boolean moveDates;
	private boolean rememberChoice;
	private boolean moveAllAfterCurrentDate;
	
	private DateChooser dateChooser;
	
	public MoveDatesEvent(DateChooser dateChooser, boolean moveDates, boolean rememberChoice, boolean moveAllAfterCurrentDate) {
		super("done");
		
		this.dateChooser = dateChooser;
		this.moveDates = moveDates;
		this.rememberChoice = rememberChoice;
		this.moveAllAfterCurrentDate = moveAllAfterCurrentDate;
	}
	
	public DateChooser getDateChooser() {
		return dateChooser;
	}
	
	public boolean isMoveDates() {
		return moveDates;
	}
	
	public boolean isRememberChoice() {
		return rememberChoice;
	}
	
	public boolean isMoveAllAfterCurrentDate() {
		return moveAllAfterCurrentDate;
	}

}
