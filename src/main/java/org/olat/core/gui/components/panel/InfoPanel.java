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
package org.olat.core.gui.components.panel;

import java.util.Objects;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;

/**
 * 
 * Initial date: 21 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InfoPanel extends AbstractComponent {
	
	private static final InfoPanelRenderer RENDERER = new InfoPanelRenderer();
	
	private String title;
	private String informations;
	private String persistedStatusId;
	private String status;
	
	public InfoPanel(String name) {
		super(name);
	}
	
	public void setPersistedStatusId(UserRequest ureq, String id) {
		persistedStatusId = id;
		reloadStatus(ureq);
	}

	public void reloadStatus(UserRequest ureq) {
		String reloadedStatus = (String)ureq.getUserSession().getGuiPreferences().get(InfoPanel.class, persistedStatusId);
		if (!Objects.equals(reloadedStatus, status)) {
			setDirty(true);
		}
		status = reloadedStatus;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getInformations() {
		return informations;
	}

	public void setInformations(String informations) {
		this.informations = informations;
		setDirty(true);
	}
	
	public boolean isCollapsed() {
		return "collapsed".equals(status);
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		saveStatus(ureq, cmd);
	}
	
	protected void saveStatus(UserRequest ureq, String cmd) {
		if("expanded".equals(cmd) || "collapsed".equals(cmd)) {
			ureq.getUserSession().getGuiPreferences().putAndSave(InfoPanel.class, persistedStatusId, cmd);
			status = cmd;
		}
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
