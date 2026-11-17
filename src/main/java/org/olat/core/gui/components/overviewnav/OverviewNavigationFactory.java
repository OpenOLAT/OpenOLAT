/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.overviewnav;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.translator.Translator;

/**
 *
 * Initial date: 16 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OverviewNavigationFactory {
	
	public static OverviewNavigation createOverviewNavigation(String name, String guiPrefsKey, UserRequest ureq, VelocityContainer vc) {
		OverviewNavigation navigation = new OverviewNavigation(name, guiPrefsKey, ureq);
		if (vc != null) {
			vc.put(navigation.getComponentName(), navigation);
		}
		return navigation;
	}
	
	public static Link createLink(String name, String cmd, Translator translator, ComponentEventListener listener,
			String iconCss, String elementCss, boolean visible) {
		Link link = LinkFactory.createLink(name, cmd, translator, null, listener, Link.LINK_CUSTOM_CSS);
		link.setIconLeftCSS("o_icon o_icon-fw " + iconCss);
		link.setElementCssClass(elementCss);
		link.setVisible(visible);
		return link;
	}
	
	public static OverviewNavigationItem createItem(Link link, String description) {
		OverviewNavigationItem item = new OverviewNavigationItem();
		item.setLink(link);
		item.setDescription(description);
		return item;
	}
	
	public static OverviewNavigationGroup createGroup(String heading, OverviewNavigationItem... items) {
		OverviewNavigationGroup group = new OverviewNavigationGroup();
		group.setHeading(heading);
		group.setItems(List.of(items));
		return group;
	}
	
}
