/**
 * This software is based on OLAT
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
 * Copyright (c) JLS goodsolutions GmbH, Zurich, Switzerland.
 * http://www.goodsolutions.ch <br>
 * All rights reserved.
 * <p>
 */
package org.olat.core.commons.chiefcontrollers;

import org.olat.core.commons.chiefcontrollers.controller.simple.SimpleBaseController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.ChiefControllerCreator;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;

/**
 * Description:<br>
 * <P>
 * Initial Date: 03.01.2007 <br>
 * 
 * @author Felix Jost
 */
public class BaseChiefControllerCreator implements ChiefControllerCreator {
	private ControllerCreator contentControllerCreator;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.olat.core.gui.control.ChiefControllerCreator#createChiefController(org.olat.core.gui.UserRequest)
	 */
	public ChiefController createChiefController(UserRequest ureq) {
		
		BaseChiefController bcc = new BaseChiefController(ureq);
		SimpleBaseController sbasec = new SimpleBaseController(ureq, bcc.getWindowControl());
		
		WindowControl bwControl = sbasec.getWindowControl();
		
		// check if there is a bookmark part in the url
		String bookmark = ureq.getParameter("o_bkm"); // e.g. [demo*5]
		if (bookmark != null) {
			// attach the launcher data
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(bookmark);
			// generate new window control with the business control attached to it
			bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, bwControl);
		}		
		
		Controller c = contentControllerCreator.createController(ureq, bwControl);
		sbasec.setContentController(c);
		
		bcc.setContentController(true, sbasec);
		return bcc;
	}
	/**
	 * [used by spring]
	 * @param contentControllerCreator The contentControllerCreator to set.
	 */
	public void setContentControllerCreator(ControllerCreator contentControllerCreator) {
		this.contentControllerCreator = contentControllerCreator;
	}

}
