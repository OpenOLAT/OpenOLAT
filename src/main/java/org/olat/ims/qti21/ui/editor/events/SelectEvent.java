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
package org.olat.ims.qti21.ui.editor.events;

import org.olat.core.gui.control.Event;

import uk.ac.ed.ph.jqtiplus.node.test.ControlObject;

/**
 * 
 * Initial date: 15 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectEvent extends Event {

	private static final long serialVersionUID = 1554430878603467523L;
	public static final String SELECT = "select-control-object";
	
	private final SelectionTarget target;
	private final ControlObject<?> controlObject;
	
	public SelectEvent(ControlObject<?> controlObject, SelectionTarget target) {
		super(SELECT);
		this.target = target;
		this.controlObject = controlObject;
	}
	
	public SelectionTarget getTarget() {
		return target;
	}
	
	public ControlObject<?> getControlObject() {
		return controlObject;
	}
	
	public enum SelectionTarget {
		
		description,
		expert,
		score,
		feedback
		
	}

}
