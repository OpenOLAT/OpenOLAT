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
package org.olat.modules.project.ui.event;

import org.olat.core.gui.control.Event;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.project.ProjNoteRef;

/**
 * 
 * Initial date: 13 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OpenNoteEvent extends Event implements StateEntry {
	
	private static final long serialVersionUID = -8208642119130539941L;

	private final ProjNoteRef note;
	private final boolean edit;
	
	public OpenNoteEvent(ProjNoteRef note, boolean edit) {
		super("open-note");
		this.note = note;
		this.edit = edit;
	}

	public ProjNoteRef getNote() {
		return note;
	}

	public boolean isEdit() {
		return edit;
	}
	
	@Override
	public OpenNoteEvent clone() {
		return new OpenNoteEvent(note, edit);
	}

}
