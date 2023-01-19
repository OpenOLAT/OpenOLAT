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
package org.olat.repository.ui.list;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.CorruptedCourseException;
import org.olat.repository.RepositoryEntry;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RepositoryEntryPublicInfosController extends RepositoryEntryDetailsController {


	public RepositoryEntryPublicInfosController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, entry, true);
	}

	@Override
	protected void doStart(UserRequest ureq) {
		try {
			String businessPath = "[RepositoryEntry:" + getEntry().getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} catch (CorruptedCourseException e) {
			logError("Course corrupted: " + getEntry().getKey() + " (" + getEntry().getOlatResource().getResourceableId() + ")", e);
			showError("cif.error.corrupted");
		}
	}

	@Override
	protected void doBook(UserRequest ureq) {
		// No need
	}

}
