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
package org.olat.gui.demo.guidemo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryEntryInfoCardController;
import org.olat.resource.OLATResourceManager;

/**
 * 
 * Initial date: Nov 10, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GuiDemoRepositoryEntryController extends BasicController {

	public GuiDemoRepositoryEntryController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("guidemo-repo");
		putInitialPanel(mainVC);
		
		GuiDemoRepositoryEntry entry = new GuiDemoRepositoryEntry(123456l);
		entry.setOlatResource(OLATResourceManager.getInstance().createOLATResourceInstance("CourseModule"));
		entry.setDisplayname(translate("repo.displayname"));
		entry.setExternalRef("EXT-001");
		entry.setLocation(translate("repo.location"));
		
		RepositoryEntryInfoCardDemoController info1Ctrl = new RepositoryEntryInfoCardDemoController(ureq, wControl, entry);
		listenTo(info1Ctrl);
		mainVC.put("repo-1", info1Ctrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public static class GuiDemoRepositoryEntry extends RepositoryEntry {

		private static final long serialVersionUID = -7169487525123889632L;
		
		private final Long key;
		
		public GuiDemoRepositoryEntry(Long key) {
			this.key = key;
		}
		
		@Override
		public Long getKey() {
			return key;
		}
		
	}
	
	public static class RepositoryEntryInfoCardDemoController extends RepositoryEntryInfoCardController {
		
		public RepositoryEntryInfoCardDemoController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
			super(ureq, wControl, entry);
		}

		@Override
		protected String getTeaserUrl() {
			StringOutput sb = new StringOutput();
			Renderer.renderStaticURI(sb, "images/openolat/openolat-test-sehr-gut_large.png");
			return sb.toString();
		}
		
	}

}
