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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Initial date: 2022-10-27<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AVConvertingMenuController extends BasicController {
	public static final Event PLAY_MASTER_EVENT = new Event("playmaster");
	private final VelocityContainer mainVC;
	private final Link playMasterLink;
	private final Object userObject;

	public AVConvertingMenuController(UserRequest ureq, WindowControl wControl, Object userObject) {
		super(ureq, wControl);
		this.userObject = userObject;
		mainVC = createVelocityContainer("av_converting_menu");
		playMasterLink = LinkFactory.createLink("av.converting.menu.playMaster", "playMaster", getTranslator(), mainVC, this, Link.LINK);
		playMasterLink.setNewWindow(true, true);
		playMasterLink.setIconLeftCSS("o_icon o_icon-fw o_icon_video");
		mainVC.put("playMaster", playMasterLink);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == playMasterLink) {
			fireEvent(ureq, PLAY_MASTER_EVENT);
		}
	}

	public Object getUserObject() {
		return userObject;
	}
}
