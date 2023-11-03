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
package org.olat.core.gui.components.form.flexible.elements;

import org.olat.core.gui.components.form.flexible.FormMultipartItem;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;

import java.io.File;

/**
 * Initial date: 2022-09-06<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface AVRecording extends FormMultipartItem {

	boolean isUploadSuccess();

	File getRecordedFile();

	File getPosterFile();

	String getFileName();

	/**
	 * Moves the uploaded file to a directory specified by 'destinationContainer'.
	 * This method either stores the file in the destination container as uploaded (if it is
	 * in the required format), otherwise it stores an empty proxy file and stores the original file
	 * as a hidden master file in the same container.
	 */
	VFSLeaf moveUploadFileTo(VFSContainer destinationContainer, String requestedName);

	/**
	 * Triggers the conversion of the given item to a media format that can be played on all browsers.
	 * Only triggers conversion of the file if the uploaded file has a mime type that needs converting.
	 *
	 * Replaces the file with an empty proxy file and stores the original file as a hidden master file in
	 * the same folder.
	 *
	 * @param leaf A leaf that wraps the copied or moved temporary file of this recording.
	 */
	void triggerConversionIfNeeded(VFSLeaf leaf);
}
