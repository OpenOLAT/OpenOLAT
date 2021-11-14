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
*/

package org.olat.course.statistic;

import java.util.List;

import org.olat.core.extensions.action.ActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.layout.GenericMainController;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.course.ICourse;

/**
 * Initial Date:  03.12.2009 <br>
 * @author bja
 */
public class StatisticMainController extends GenericMainController implements Activateable2 {

	private ICourse course;

	public StatisticMainController(UserRequest ureq, WindowControl windowControl, ICourse course) {
		super(ureq, windowControl);
		this.course = course;
		init(ureq);
		getMenuTree().setRootVisible(false);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		super.activate(ureq, entries, state);
	}

	@Override
	protected Controller createController(ActionExtension ae, UserRequest ureq) {
		if(ae instanceof StatisticActionExtension){
			StatisticActionExtension sae = (StatisticActionExtension)ae;
			return sae.createController(ureq, getWindowControl(), course);
		}else{
			return super.createController(ae, ureq);
		}
	}
	
	@Override
	protected Controller handleOwnMenuTreeEvent(Object uobject, UserRequest ureq) {
		//no own tree events to handle
		return null;
	}
}