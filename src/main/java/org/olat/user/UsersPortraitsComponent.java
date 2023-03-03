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

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.Disposable;

/**
 * 
 * Initial date: 7 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UsersPortraitsComponent extends AbstractComponent implements Disposable {
	
	private static final ComponentRenderer RENDERER = new UsersPortraitsRenderer();
	
	private UserAvatarMapper avatarMapper;
	private final MapperKey mapperKey;
	private final boolean sharedMapper;
	private String ariaLabel;
	private PortraitSize size = PortraitSize.medium;
	private int maxUsersVisible = 10;
	private List<PortraitUser> users;

	UsersPortraitsComponent(UserRequest ureq, String name, MapperKey mapperKey) {
		super(name);
		
		this.mapperKey = mapperKey;
		this.sharedMapper = mapperKey != null;
		if (mapperKey == null) {
			avatarMapper = new UserAvatarMapper(true);
			mapperKey = CoreSpringFactory.getImpl(MapperService.class).register(ureq.getUserSession(), avatarMapper);
		}
	}
	
	@Override
	public void dispose() {
		if (!sharedMapper) {
			CoreSpringFactory.getImpl(MapperService.class).cleanUp(List.of(mapperKey));
		}
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	MapperKey getMapperKey() {
		return mapperKey;
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
		if (!sharedMapper) {
			boolean useLarge = PortraitSize.medium == size || PortraitSize.large == size;
			avatarMapper.setUseLarge(useLarge);
		}
		setDirty(true);
	}

	public int getMaxUsersVisible() {
		return maxUsersVisible;
	}

	public void setMaxUsersVisible(int maxUsersVisible) {
		this.maxUsersVisible = maxUsersVisible;
		setDirty(true);
	}

	public List<PortraitUser> getUsers() {
		return users;
	}

	public void setUsers(List<PortraitUser> users) {
		this.users = users;
		setDirty(true);
	}
	
	public enum PortraitSize { xsmall, small, medium, large }

	public interface PortraitUser {
		
		public Long getIdentityKey();
		
		public boolean isPortraitAvailable();
		
		public String getPortraitCssClass();
		
		public String getDisplayName();
		
	}

}
