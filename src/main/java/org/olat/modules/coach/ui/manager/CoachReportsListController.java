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
package org.olat.modules.coach.ui.manager;

import java.util.List;

import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.model.SearchExportMetadataParameters;
import org.olat.core.commons.services.export.ui.ExportsListController;
import org.olat.core.commons.services.export.ui.ExportsListSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;

/**
 * Initial date: 2025-06-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CoachReportsListController extends ExportsListController {
	
	private final ArchiveType type;

	public CoachReportsListController(UserRequest ureq, WindowControl wControl, ArchiveType type, ExportsListSettings options) {
		super(ureq, wControl, null, null, true, options, 
				Util.createPackageTranslator(CoachReportsListController.class, ureq.getLocale()));
		
		this.type = type;
		loadModel();
	}

	@Override
	public SearchExportMetadataParameters getSearchParams() {
		SearchExportMetadataParameters params = new SearchExportMetadataParameters(null, null, List.of(type));
		return params;
	}
}
