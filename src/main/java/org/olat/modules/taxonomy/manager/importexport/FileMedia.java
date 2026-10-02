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
package org.olat.modules.taxonomy.manager.importexport;

import java.io.File;

/**
 * A teaser or background image of a level.
 *
 * @param zipPath The path of the image in the ZIP, relative to the workbook, e.g. <code>media/MATH/teaser/math.png</code>
 * @param filename The file name to store
 * @param file The file on disk: the stored image on export, the extracted image on import. Can be null
 *             on import if the ZIP does not contain the image.
 *
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public record FileMedia(String zipPath, String filename, File file) {
	//
}
