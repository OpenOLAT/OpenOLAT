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
package org.olat.modules.project.ui.component;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjProject;

/**
 * 
 * Initial date: 10 May 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjAvatarComponent extends AbstractComponent {
	
	public enum Size { large, medium }
	private static final ComponentRenderer RENDERER = new ProjAvatarRenderer();
	
	private final String imageUrl;
	private final String avatarCssClass;
	private final String abbrev;
	private final Size size;

	public ProjAvatarComponent(String name, ProjProject project, String imageUrl, Size size) {
		super(name);
		this.imageUrl = imageUrl;
		this.avatarCssClass = project.getAvatarCssClass();
		this.abbrev = StringHelper.containsNonWhitespace(project.getTitle())
				? Formatter.truncateOnly(project.getTitle(), 2).toUpperCase()
				: null;
		this.size = size;
		setDomReplacementWrapperRequired(false);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getAvatarCssClass() {
		return avatarCssClass;
	}

	public String getAbbrev() {
		return abbrev;
	}

	public Size getSize() {
		return size;
	}

}
