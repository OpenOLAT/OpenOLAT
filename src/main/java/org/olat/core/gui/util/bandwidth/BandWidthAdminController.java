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
package org.olat.core.gui.util.bandwidth;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * @author Felix Jost, http://www.goodsolutions.ch
 *
 */
public class BandWidthAdminController extends BasicController {
	private final CPSPauser cpsPauser;
	private Link l_k4, l_k8, l_k18, l_k64, l_unlimit;
	private VelocityContainer mainVc;

	public BandWidthAdminController(UserRequest ureq, WindowControl wControl, CPSPauser cpsPauser) {
		super(ureq, wControl);
		this.cpsPauser = cpsPauser;
		mainVc = createVelocityContainer("bandwidthmain");
		
		// 56kBit/s analog: brutto ca 8k? , netto 4k
		// 128kBit/s ISDN:  netto ca. 8k
		// 300kBit slowest DSL: ca 300/8/2 = 18.75k
		// 1MBit/s DSL: ca 64k/s
		// unlimited 
		l_k4 = LinkFactory.createLink("link.4k", mainVc, this);
		l_k8 = LinkFactory.createLink("link.8k", mainVc, this);
		l_k18 = LinkFactory.createLink("link.18k", mainVc, this);
		l_k64 = LinkFactory.createLink("link.64k", mainVc, this);
		l_unlimit = LinkFactory.createLink("link.unlimit", mainVc, this);		
		putInitialPanel(mainVc);
		
		setCPS(-1);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == l_k4) {
			setCPS(4*1024);
		} else if (source == l_k8) {
			setCPS(8*1024);
		} else if (source == l_k18) {
			setCPS(18*1024);
		} else if (source == l_k64) {
			setCPS(64*1024);
		} else if (source == l_unlimit) {
			setCPS(-1);
		}

	}

	private void setCPS(int cps) {
		mainVc.contextPut("cur", cps==-1? "unlimited" : cps+" bits/s");
		cpsPauser.setCPS(cps);
	}

}
