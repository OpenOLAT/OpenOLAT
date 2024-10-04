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
package org.olat.modules.catalog.ui;

import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.modules.catalog.CatalogV2Module;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class WebCatalogTemporarilyDisabledController extends BasicController {

	private final EmptyState emptyState;
	
	@Autowired
	private CatalogV2Module catalogModule;

	public WebCatalogTemporarilyDisabledController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("temporarily_disabled");
		putInitialPanel(mainVC);
		
		emptyState = EmptyStateFactory.create("empty", mainVC, this);
		emptyState.setMessageI18nKey("web.catalog.temporarily.disabled.title");
		emptyState.setHintI18nKey("web.catalog.temporarily.disabled.text");
		emptyState.setButtonI18nKey("web.catalog.goto.login");
		emptyState.setIconCss("o_icon_catalog");
		emptyState.setIndicatorIconCss("o_icon_none");
		emptyState.getButton().setUrl(WebappHelper.getServletContextPath() + DispatcherModule.getPathDefault());
		
		if (catalogModule.hasHeaderBgImage()) {
			String mapperUri = registerMapper(ureq, new VFSMediaMapper(catalogModule.getHeaderBgImage()));
			mainVC.contextPut("bgImageUrl", mapperUri);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == emptyState) {
			doGotoDmz(ureq);
		}
	}

	private void doGotoDmz(UserRequest ureq) {
		DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());
	}

}
