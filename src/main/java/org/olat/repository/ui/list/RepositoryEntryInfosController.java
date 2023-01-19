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
package org.olat.repository.ui.list;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.CorruptedCourseException;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 5 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryInfosController extends RepositoryEntryDetailsController {

	private final boolean inRuntime;
	
	public RepositoryEntryInfosController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean inRuntime) {
		super(ureq, wControl, entry, false);
		this.inRuntime = inRuntime;
	}
	
	@Override
	protected void doStart(UserRequest ureq) {
		if (inRuntime) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			try {
				String businessPath = "[RepositoryEntry:" + getEntry().getKey() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			} catch (CorruptedCourseException e) {
				logError("Course corrupted: " + getEntry().getKey() + " (" + getEntry().getOlatResource().getResourceableId() + ")", e);
				showError("cif.error.corrupted");
			}
		}
	}

	@Override
	protected void doBook(UserRequest ureq) {
		doStart(ureq);
	}

}
