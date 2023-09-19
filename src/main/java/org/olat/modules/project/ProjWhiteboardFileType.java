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
package org.olat.modules.project;

import org.olat.core.commons.services.doceditor.ContentProvider;
import org.olat.core.commons.services.doceditor.ContentProviderFactory;

/**
 * 
 * Initial date: 1 Sep 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public enum ProjWhiteboardFileType {
	
	board("whiteboard.dwb", ContentProviderFactory.emptyDrawiowb()),
	preview("whiteboard.svg", ContentProviderFactory.emptyDrawioSvg());
	
	private final String filename;
	private final ContentProvider contentProvider;

	private ProjWhiteboardFileType(String filename, ContentProvider contentProvider) {
		this.filename = filename;
		this.contentProvider = contentProvider;
	}

	public String getFilename() {
		return filename;
	}

	public ContentProvider getContentProvider() {
		return contentProvider;
	}
	
}
