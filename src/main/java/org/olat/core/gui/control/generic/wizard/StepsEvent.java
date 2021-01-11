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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.core.gui.control.generic.wizard;

import org.olat.core.gui.control.Event;

/**
 * Initial Date:  11.01.2008 <br>
 * @author patrickb
 */
public class StepsEvent extends Event {

	private static final long serialVersionUID = -3050157198786051420L;
	
	public static final StepsEvent ACTIVATE_NEXT = new StepsEvent("next", PrevNextFinishConfig.NOOP);
	public static final StepsEvent ACTIVATE_PREVIOUS = new StepsEvent("prev", PrevNextFinishConfig.NOOP);
	public static final StepsEvent INFORM_FINISHED = new StepsEvent("finished", PrevNextFinishConfig.NOOP);
	public static final StepsEvent STEPS_CHANGED = new StepsEvent("steps.changed", PrevNextFinishConfig.NOOP);
	
	private PrevNextFinishConfig pnfConf;

	public StepsEvent(String command, PrevNextFinishConfig pnfConf) {
		super(command);
		this.pnfConf = pnfConf;
	}

	public boolean isBackIsEnabled() {
		return pnfConf.isBackIsEnabled();
	}

	public boolean isNextIsEnabled() {
		return pnfConf.isNextIsEnabled();
	}

	public boolean isFinishIsEnabled() {
		return pnfConf.isFinishIsEnabled();
	}

}
