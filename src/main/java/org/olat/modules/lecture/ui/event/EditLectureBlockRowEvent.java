/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.lecture.ui.event;

import org.olat.core.gui.control.Event;
import org.olat.modules.lecture.model.LectureBlockRow;

/**
 * 
 * Initial date: 26 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditLectureBlockRowEvent extends Event {
	
	private static final long serialVersionUID = 5204729788527755948L;
	public static final String EDIT_LECTURE_BLOCK = "edit-lecture-block";
	
	private final LectureBlockRow row;
	
	public EditLectureBlockRowEvent(LectureBlockRow row) {
		super(EDIT_LECTURE_BLOCK);
		this.row = row;
	}
	
	public LectureBlockRow getRow() {
		return row;
	}
}
