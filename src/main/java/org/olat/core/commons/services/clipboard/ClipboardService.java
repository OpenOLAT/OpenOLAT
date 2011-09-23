/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 
package org.olat.core.commons.services.clipboard;

import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.Event;

/**
 * @author Felix Jost, http://www.goodsolutions.ch
 *
 * this is a service instance for handling a clipboard (a la gui copy and paste).
 * it is normally attached to one user, but this is up to the caller of the service.
 */
public interface ClipboardService {
	
	public ControllerCreator createCopyToUIService(ClipboardEntryCreator cbec);

	public ControllerCreator createPasteFromUIService(Class[] acceptedFlavorInterfaces);
	
	public ClipboardEntry getClipboardEntryFrom(Event clipboardEvent);
	
	/**
	 * 
	 * @return (once only allowed) the UI representing the clipboard content (should be presented at some place like e.g. top right of the screen or similar, a place where it is always accessible)
	 */
	public ControllerCreator onceGetClipboardUI();
	
}
