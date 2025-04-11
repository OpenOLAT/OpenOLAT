/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.user;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: Feb 3, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class UserPortraitComponent extends AbstractComponent {
	
	private static final ComponentRenderer RENDERER = new UserPortraitRenderer();
	
	private final String avatarMapperUrl;
	private final Translator compTranslator;
	private PortraitUser portraitUser;
	private PortraitSize size = PortraitSize.medium;
	private boolean displayPresence = true;

	protected UserPortraitComponent(String name, Locale locale, String avatarMapperUrl) {
		super(name);
		this.avatarMapperUrl = avatarMapperUrl;
		this.compTranslator = Util.createPackageTranslator(UserPortraitComponent.class, locale);
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	String getAvatarMapperUrl() {
		return avatarMapperUrl;
	}

	Translator getCompTranslator() {
		return compTranslator;
	}

	public PortraitUser getPortraitUser() {
		return portraitUser;
	}

	public void setPortraitUser(PortraitUser portraitUser) {
		this.portraitUser = portraitUser;
		setDirty(true);
	}

	public PortraitSize getSize() {
		return size;
	}

	public void setSize(PortraitSize size) {
		this.size = size;
		setDirty(true);
	}

	public boolean isDisplayPresence() {
		return displayPresence;
	}

	public void setDisplayPresence(boolean displayPresence) {
		this.displayPresence = displayPresence;
		setDirty(true);
	}

}
