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
package org.olat.core.commons.services.doceditor;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.ui.DocEditorStandaloneController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;

/**
 * 
 * Initial date: 17 Sep 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocEditorContextEntryControllerCreator extends DefaultContextEntryControllerCreator {
	
	private static final Logger log = Tracing.createLoggerFor(DocEditorContextEntryControllerCreator.class);
	
	private final DocEditorService docEditorService;

	public DocEditorContextEntryControllerCreator(DocEditorService docEditorService) {
		this.docEditorService = docEditorService;
	}

	@Override
	public ContextEntryControllerCreator clone() {
		return new DocEditorContextEntryControllerCreator(docEditorService);
	}

	@Override
	public boolean isResumable() {
		return false;
	}

	@Override
	public Controller createController(List<ContextEntry> ces, UserRequest ureq, WindowControl wControl) {
		ContextEntry contextEntry = ces.get(0);
		Access access = getAccess(contextEntry, ureq);
		DocEditorConfigs configs = getConfigs(ureq, access);
		
		log.debug("Create document editor for access. Key {}", access.getKey());
		return new DocEditorStandaloneController(ureq, wControl, access, configs);
	}

	@Override
	public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
		return getAccess(ce, ureq) != null;
	}

	private Access getAccess(ContextEntry contextEntry, UserRequest ureq) {
		Identity identity = ureq.getIdentity();
		OLATResourceable resource = contextEntry.getOLATResourceable();
		Long accessKey = resource.getResourceableId();

		Access access = docEditorService.getAccess(() -> accessKey);
		if (access == null) {
			log.debug("Access not found. Key {}", accessKey);
			return null;
		}
		
		if (!docEditorService.isEditorEnabled(access)) {
			log.debug("Document editor {} not enabled. Key {}", access.getEditorType(), accessKey);
			return null;
		}
		
		if (!access.getIdentity().equals(identity)) {
			log.debug("Access forbidden to {}. Key {}", identity, accessKey);
			return null;
		}
			
		DocEditorConfigs configs = getConfigs(ureq, access);
		if (configs == null) {
			log.debug("No configs in session. Key {}", identity, accessKey);
			return null;
		}
		
		return access;
	}

	private DocEditorConfigs getConfigs(UserRequest ureq, Access access) {
		UserSession usess = ureq.getUserSession();
		String configKey = docEditorService.getConfigKey(access);
		Object entry = usess.getEntry(configKey);
		if (entry instanceof DocEditorConfigs) {
			return (DocEditorConfigs)entry;
		}
		return null;
	}
	
}
