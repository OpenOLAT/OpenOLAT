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
package org.olat.course.run.leave;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseModule;
import org.olat.course.run.CourseRuntimeController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryService;

/**
 *
 * Initial date: 10.09.2015<br>
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmLeaveController extends ConfirmationController {

	private final RepositoryEntry entry;

	public ConfirmLeaveController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		this(ureq, wControl, entry, Util.createPackageTranslator(CourseRuntimeController.class, ureq.getLocale(),
				Util.createPackageTranslator(RepositoryService.class, ureq.getLocale())));
	}

	private ConfirmLeaveController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, Translator translator) {
		super(ureq, wControl, getMessage(translator, entry), getAcknowledge(translator, entry),
				translator.translate("course.leave.button"), ButtonType.submitPrimary, null, false);
		setTranslator(Util.createPackageTranslator(ConfirmationController.class, getLocale(), translator));
		this.entry = entry;
		initForm(ureq);
	}

	private static String getMessage(Translator translator, RepositoryEntry entry) {
		boolean isCourseStandalone = CourseModule.ORES_TYPE_COURSE.equals(entry.getOlatResource().getResourceableTypeName())
				&& entry.getRuntimeType() == RepositoryEntryRuntimeType.standalone;
		return isCourseStandalone ? translator.translate("course.leave.message") : translator.translate("course.leave.message.content");
	}

	private static String getAcknowledge(Translator translator, RepositoryEntry entry) {
		return translator.translate("course.leave.acknowledge", translator.translate(entry.getOlatResource().getResourceableTypeName()));
	}

	@Override
	protected void initFormElements(FormLayoutContainer confirmCont) {
		uifactory.addStaticTextElement("rows", entry.getOlatResource().getResourceableTypeName(),
				StringHelper.escapeHtml(entry.getDisplayname()), confirmCont);
	}
}
