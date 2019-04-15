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
package org.olat.modules.portfolio.manager;

import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.forms.EvaluationFormReadyToDelete;
import org.olat.modules.portfolio.ui.BinderRuntimeController;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssignmentFormReadyToDelete implements EvaluationFormReadyToDelete {
	
	@Autowired
	private AssignmentDAO assignmentDao;

	@Override
	public boolean readyToDelete(RepositoryEntry entry, Locale locale, ErrorList errors) {
		if(assignmentDao.isFormEntryInUse(entry)) {
			Translator translator = Util.createPackageTranslator(BinderRuntimeController.class, locale);
			errors.setError(translator.translate("details.delete.error.assignments", new String[] { entry.getDisplayname() }));
			return false;
		}
		return true;
	}
}
