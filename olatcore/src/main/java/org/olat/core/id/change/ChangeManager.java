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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch All rights reserved.
 * <p>
 */
package org.olat.core.id.change;

import org.olat.core.gui.control.Event;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.CoordinatorManager;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  21.07.2006 <br>
 *
 * @author Felix Jost
 */
public class ChangeManager {
	
	//public static final int ACTION_READ = 1;
	//public static final int ACTION_APPEND = 2;
	public static final int ACTION_CREATE = 4;
	public static final int ACTION_UPDATE = 8;
	public static final int ACTION_DELETE = 16;
	
	//private static ChangeManager INSTANCE = new ChangeManager();
	
	
	private ChangeManager() {
		// singleton
	}
	
	/*public static ChangeManager getInstance() {
		return INSTANCE;
	}*/
	
	/**
	 * @see ObjectAccessEvent
	 * @param action
	 * @param ores
	 */
	public static void changed(int action, OLATResourceable ores) {
		ObjectAccessEvent cevent = new ObjectAccessEvent(action, ores);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(cevent, ores);
	}

	public static boolean isChangeEvent(Event event) {
		return event instanceof ObjectAccessEvent; // event.getCommand() == CHANGE_COMMAND;
	}
	
}
