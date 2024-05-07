/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.commons.services.folder.ui.event;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 7 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FolderAddEvent extends Event {

	private static final long serialVersionUID = -996255422685295660L;
	
	private final List<String> filenames = new ArrayList<>(1);

	public FolderAddEvent() {
		super("folder-add");
	}
	
	public FolderAddEvent(String filename) {
		this();
		filenames.add(filename);
	}
	
	public void addFilename(String filename) {
		this.filenames.add(filename);
	}

	public List<String> getFilenames() {
		return filenames;
	}

}
