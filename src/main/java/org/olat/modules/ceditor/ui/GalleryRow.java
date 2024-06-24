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
package org.olat.modules.ceditor.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaToPagePart;
import org.olat.modules.cemedia.MediaVersion;

/**
 * Initial date: 2024-04-29<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GalleryRow {
	private final String id;
	private final String title;
	private final String description;
	private final String version;
	private final MediaToPagePart relation;
	private final MediaVersion mediaVersion;
	private FormLink toolLink;

	public GalleryRow(Translator translator, MediaToPagePart relation, Media media, MediaVersion mediaVersion) {
		id = mediaVersion.getVersionUuid();
		this.relation = relation;
		title = StringHelper.truncateText(media.getTitle());
		description = StringHelper.truncateText(media.getDescription());
		version = PageEditorUIFactory.getVersionName(translator, mediaVersion);
		this.mediaVersion = mediaVersion;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getVersion() {
		return version;
	}

	public MediaToPagePart getRelation() {
		return relation;
	}

	public MediaVersion getMediaVersion() {
		return mediaVersion;
	}

	public FormLink getToolLink() {
		return toolLink;
	}

	public void setToolLink(FormLink toolLink) {
		this.toolLink = toolLink;
	}
}
