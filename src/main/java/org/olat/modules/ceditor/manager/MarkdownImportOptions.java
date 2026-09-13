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
package org.olat.modules.ceditor.manager;

/**
 * The options an author chooses in the import dialog and that the conversion
 * itself needs. They travel as one object, so a new option does not add another
 * parameter to
 * {@link MarkdownImportService#convertAndPersist(String, org.olat.modules.ceditor.Page, org.olat.core.id.Identity, org.olat.core.id.OLATResourceable, String, java.io.File, java.util.Locale, String, int, String, org.olat.modules.ceditor.ui.PageElementTarget, MarkdownImportOptions)}.
 *
 * Initial date: Sep 13, 2026<br>
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 * @param generateImageMetadata true: the imported images get title, description,
 *                              alternative text and tags from the AI
 */
public record MarkdownImportOptions(boolean generateImageMetadata) {

	/** No automatic AI usage. Fails closed unless the caller asks for it. */
	public static final MarkdownImportOptions NONE = new MarkdownImportOptions(false);

}
