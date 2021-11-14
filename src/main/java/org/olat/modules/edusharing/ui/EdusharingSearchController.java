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
package org.olat.modules.edusharing.ui;

import static java.util.Collections.singletonList;

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.helpers.Settings;
import org.olat.core.util.WebappHelper;
import org.olat.modules.edusharing.EdusharingSearchCallbackMapper;
import org.olat.modules.edusharing.model.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdusharingSearchController extends BasicController {
	
	private final MapperKey mapperKey;
	private final EdusharingSearchCallbackMapper mapper;
	
	@Autowired
	private MapperService mapperService;

	public EdusharingSearchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("search");
		
		mainVC.contextPut("url", WebappHelper.getServletContextPath() + "/edusharing/search");
		
		mapper = new EdusharingSearchCallbackMapper();
		mapperKey = mapperService.register(ureq.getUserSession(), mapper);
		
		mainVC.contextPut("reurl", Settings.createServerURI() + mapperKey.getUrl());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if ("esClose".equals(event.getCommand())) {
			fireEvent(ureq, new SearchEvent(mapper.getSearchResult()));
		}
	}

	@Override
	protected void doDispose() {
		mapperService.cleanUp(singletonList(mapperKey));
        super.doDispose();
	}
	
	public static class SearchEvent extends Event {
		
		private static final long serialVersionUID = 6826849263886569816L;
		
		private final SearchResult searchResult;
		
		public SearchEvent(SearchResult searchResult) {
			super("es-seach");
			this.searchResult = searchResult;
		}
		
		public SearchResult getSearchResult() {
			return searchResult;
		}
		
	}

}
