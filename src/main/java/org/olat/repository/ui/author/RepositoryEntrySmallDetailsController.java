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
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.references.ReferenceManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntrySmallDetailsController extends BasicController {
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private RepositoryService repositoryService;
	
	public RepositoryEntrySmallDetailsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		
		VelocityContainer mainVC = createVelocityContainer("small_details");
		mainVC.contextPut("v", entry);

		String cssClass = RepositoyUIFactory.getIconCssClass(entry);
		mainVC.contextPut("cssClass", cssClass);
		mainVC.contextPut("displayName", entry.getDisplayname());
		mainVC.contextPut("description", entry.getDescription());
		
		List<Long> authorKeys = repositoryService.getAuthors(entry);
		List<String> authorNames = new ArrayList<>(authorKeys.size());
		int count = 0;
		for(Long authorKey:authorKeys) {
			String authorName = userManager.getUserDisplayName(authorKey);
    		authorNames.add(authorName);
    		if(++count > 10) {
    			authorNames.add("...");
    			break;
    		}
		}
		mainVC.contextPut("authornames", authorNames);
		
		List<String> referenceDetails = referenceManager.getReferencesToSummary(entry.getOlatResource());
        if (referenceDetails != null) {
        	mainVC.contextPut("referenceDetails", referenceDetails);
        }
		
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
