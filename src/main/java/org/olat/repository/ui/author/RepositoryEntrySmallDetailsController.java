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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandlerFactory;
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
public class RepositoryEntrySmallDetailsController extends FormBasicController {
	
	private final RepositoryEntry entry;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	protected RepositoryHandlerFactory repositoryHandlerFactory;
	
	public RepositoryEntrySmallDetailsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "small_details");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			layoutCont.contextPut("v", entry);

			String cssClass = RepositoyUIFactory.getIconCssClass(entry);
			layoutCont.contextPut("cssClass", cssClass);
			layoutCont.contextPut("displayName", entry.getDisplayname());
			layoutCont.contextPut("description", entry.getDescription());
			
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
			layoutCont.contextPut("authornames", authorNames);
			
			List<String> referenceDetails = referenceManager.getReferencesToSummary(entry.getOlatResource());
	        if (referenceDetails != null) {
	        	layoutCont.contextPut("referenceDetails", referenceDetails);
	        }	
		}
		
		FormBasicController subDetailsCtrl = repositoryHandlerFactory.getRepositoryHandler(entry)
	        	.createAuthorSmallDetailsController(entry, ureq, getWindowControl(), mainForm);
        if(subDetailsCtrl != null) {
        	listenTo(subDetailsCtrl);
        	formLayout.add("subDetails", subDetailsCtrl.getInitialFormItem());
        }
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
