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
package org.olat.modules.ceditor.ui.event;

import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Event;
import org.olat.modules.ceditor.ui.model.EditorFragment;

/**
 * 
 * Initial date: 12 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditionEvent extends Event {

	private static final long serialVersionUID = -4401756279492847419L;
	
	private final Link link;
	private final EditorFragment fragment;
	
	public EditionEvent(String cmd, EditorFragment fragment) {
		super(cmd);
		link = null;
		this.fragment = fragment;
	}
	
	public EditionEvent(String cmd, EditorFragment fragment, Link link) {
		super(cmd);
		this.link = link;
		this.fragment = fragment;
	}
	
	public Link getLink() {
		return link;
	}
	
	public EditorFragment getFragment() {
		return fragment;
	}

}
