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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumElementInfosController;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: Sep 8, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ImplementationCurriculumElementInfosController extends CurriculumElementInfosController {

	public ImplementationCurriculumElementInfosController(UserRequest ureq, WindowControl wControl,
			CurriculumElement element, RepositoryEntry entry, Identity bookedIdentity, boolean preview) {
		super(ureq, wControl, element, entry, bookedIdentity, preview);
	}

	@Override
	protected void doStart(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

}
