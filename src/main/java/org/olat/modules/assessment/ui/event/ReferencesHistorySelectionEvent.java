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
package org.olat.modules.assessment.ui.event;

import org.olat.core.gui.control.Event;
import org.olat.course.nodes.QTICourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 4 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ReferencesHistorySelectionEvent extends Event {
	
	private static final long serialVersionUID = -5408996394499592090L;

	private static final String CMD = "references-history-selection";
	
	private RepositoryEntry entry;
	private QTICourseNode courseNode;
	
	public ReferencesHistorySelectionEvent(RepositoryEntry entry, QTICourseNode courseNode) {
		super(CMD);
		this.entry = entry;
		this.courseNode = courseNode;
	}
	
	public RepositoryEntry getEntry() {
		return entry;
	}
	
	public QTICourseNode getCourseNode() {
		return courseNode;
	}
}
