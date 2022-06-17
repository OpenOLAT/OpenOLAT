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
package org.olat.repository.ui.list;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * 
 * Initial date: 5 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryDetailsDescriptionController extends BasicController {
	
	private final String baseUrl;
	private boolean hasDescription = false;

	public RepositoryEntryDetailsDescriptionController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		
		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(entry);
		VFSContainer mediaContainer = handler.getMediaContainer(entry);
		baseUrl = mediaContainer != null
			? registerMapper(ureq, new VFSContainerMapper(mediaContainer.getParentContainer()))
			: null;
		
		VelocityContainer mainVC = createVelocityContainer("details_description");
		
		String description = getFormattedText(entry.getDescription());
		if (StringHelper.containsNonWhitespace(description)) {
			mainVC.contextPut("description", description);
			hasDescription = true;
		}
		
		String requirements = getFormattedText(entry.getRequirements());
		if (StringHelper.containsNonWhitespace(requirements)) {
			mainVC.contextPut("requirements", requirements);
			hasDescription = true;
		}
		
		String objectives = getFormattedText(entry.getObjectives());
		if (StringHelper.containsNonWhitespace(objectives)) {
			mainVC.contextPut("objectives", objectives);
			hasDescription = true;
		}
		
		String credits = getFormattedText(entry.getCredits());
		if (StringHelper.containsNonWhitespace(credits)) {
			mainVC.contextPut("credits", credits);
			hasDescription = true;
		}
		
		putInitialPanel(mainVC);
	}

	public boolean hasDescription() {
		return hasDescription;
	}
	
	private String getFormattedText(final String text) {
		if (!StringHelper.containsNonWhitespace(text)) return null;
		
		String formattedTtext = StringHelper.xssScan(text);
		if (baseUrl != null) {
			formattedTtext = FilterFactory.getBaseURLToMediaRelativeURLFilter(baseUrl).filter(formattedTtext);
		}
		return Formatter.formatLatexFormulas(formattedTtext);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
