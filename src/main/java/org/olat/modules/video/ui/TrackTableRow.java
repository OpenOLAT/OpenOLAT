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
package org.olat.modules.video.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.util.vfs.VFSLeaf;

/**
 *	model of a tablerow in the table of different videotracks in the videoconfiguration
 *
 * Initial date: 07.04.2015<br>
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class TrackTableRow {

	private final String language;
	private final VFSLeaf track;
	private final FormLink deleteLink;

	public TrackTableRow(String language, VFSLeaf track, FormLink deleteLink) {
		this.language = language;
		this.track = track;
		this.deleteLink = deleteLink;
	}

	public VFSLeaf getTrack() {
		return track;
	}

	public String getLanguage() {
		return language;
	}

	public FormLink getDeleteLink() {
		return deleteLink;
	}
}