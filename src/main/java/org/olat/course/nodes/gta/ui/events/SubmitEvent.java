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
package org.olat.course.nodes.gta.ui.events;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 31.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SubmitEvent extends Event {

	private static final long serialVersionUID = -5176813548413973870L;
	
	public static final String UPLOAD = "upload";
	public static final String UPDATE = "update";
	public static final String DELETE = "delete";
	public static final String CREATE = "create";
	public static final String EDIT = "edit";

	private String filename;
	
	public SubmitEvent(String command, String filename) {
		super(command);
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}
	
	public String getLogMessage() {
		String operation;
		switch(getCommand()) {
			case CREATE: operation = "create document"; break;
			case DELETE: operation = "delete document"; break;
			case EDIT: operation = "edit document"; break;
			case UPDATE: operation = "update document"; break;
			case UPLOAD: operation = "upload document"; break;
			default: operation = "unkown operation"; break;
		}
		return operation;
	}
}
