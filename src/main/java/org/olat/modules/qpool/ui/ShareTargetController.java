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
package org.olat.modules.qpool.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.qpool.QPoolSecurityCallback;

/**
 * 
 * Initial date: 15.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ShareTargetController extends BasicController {
	
	public static final String SHARE_POOL_CMD = "qpool.share.pool";
	public static final String SHARE_GROUP_CMD = "qpool.share.group";

	private Link shareGroup;
	private Link sharePool;
	
	public ShareTargetController(UserRequest ureq, WindowControl wControl, QPoolSecurityCallback securityCallback) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("share_target");
		if (securityCallback.canUsePools()) {
			sharePool = LinkFactory.createLink("share.pool", mainVC, this);
			sharePool.setIconLeftCSS("o_icon o_icon-fw o_icon_pool_pool");
		}
		if (securityCallback.canUseGroups()) {
			shareGroup = LinkFactory.createLink("share.group", mainVC, this);
			shareGroup.setIconLeftCSS("o_icon o_icon-fw o_icon_pool_share");
		}
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(shareGroup == source) {
			fireEvent(ureq, new Event(SHARE_GROUP_CMD));
		} else if(sharePool == source) {
			fireEvent(ureq, new Event(SHARE_POOL_CMD));
		}
	}
}
