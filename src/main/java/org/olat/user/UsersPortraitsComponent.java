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
package org.olat.user;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.util.CodeHelper;

/**
 * 
 * Initial date: 7 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UsersPortraitsComponent extends AbstractComponent {
	
	private static final ComponentRenderer RENDERER = new UsersPortraitsRenderer();
	
	private final Locale locale;
	private String ariaLabel;
	private PortraitSize size = PortraitSize.medium;
	private PortraitLayout layout = PortraitLayout.overlappingPortraits;
	private int maxUsersVisible = 10;
	private List<UserPortraitComponent> userComps;

	UsersPortraitsComponent(UserRequest ureq, String name) {
		super(name);
		locale = ureq.getLocale();
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	public String getAriaLabel() {
		return ariaLabel;
	}

	public void setAriaLabel(String ariaLabel) {
		this.ariaLabel = ariaLabel;
		setDirty(true);
	}

	public PortraitSize getSize() {
		return size;
	}

	public void setSize(PortraitSize size) {
		this.size = size;
		if (userComps != null) {
			userComps.forEach(comp -> comp.setSize(size));
		}
		setDirty(true);
	}

	public PortraitLayout getPortraitLayout() {
		return layout;
	}

	public void setLPortraitLayout(PortraitLayout layout) {
		this.layout = layout;
	}

	public int getMaxUsersVisible() {
		return maxUsersVisible;
	}

	public void setMaxUsersVisible(int maxUsersVisible) {
		this.maxUsersVisible = maxUsersVisible;
		setDirty(true);
	}

	List<UserPortraitComponent> getUserComps() {
		return userComps;
	}

	public void setUsers(List<PortraitUser> users) {
		this.userComps = users.stream()
				.map(this::createPortraitUserComponent)
				.toList();
		setDirty(true);
	}

	private UserPortraitComponent createPortraitUserComponent(PortraitUser portraitUser) {
		UserPortraitComponent userPortraitComp = UserPortraitFactory
				.createUserPortrait("o_" + CodeHelper.getRAMUniqueID(), null, locale);
		userPortraitComp.setPortraitUser(portraitUser);
		userPortraitComp.setDisplayPresence(false);
		userPortraitComp.setSize(size);
		return userPortraitComp;
	}

	public enum PortraitLayout { overlappingPortraits, verticalPortraitsDisplayName }

}
