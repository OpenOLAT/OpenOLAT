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

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OLATResourceableEvent;

/**
 * Description:<br>
 * an event which must be fired whenever a business object / olatresourceable
 * changed.<br>
 * <P>
 * Initial Date: 21.07.2006 <br>
 * 
 * @author Felix Jost
 */
public class ObjectAccessEvent extends OLATResourceableEvent {
	private static final String CHANGE_COMMAND = new String("o__change_com328898234932");
	private final int action;
	
	/**
	 * 
	 * @param action how it changed or was read (use constants of changemanager class (c,r,u,d,a)
	 * @param ores the olat resourceable that changed
	 */
	ObjectAccessEvent(int action, OLATResourceable ores) {
		super(CHANGE_COMMAND, ores);
		this.action = action;
	}

	/**
	 * @return Returns the action. (see constants of ChangeManager class)
	 */
	public int getAction() {
		return action;
	}
	
	public String toString() {
		String oresS = getDerivedOres();
		return "action "+getAction()+", ores "+oresS;
	}
	
}
