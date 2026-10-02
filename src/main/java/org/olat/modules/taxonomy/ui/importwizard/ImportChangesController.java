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
package org.olat.modules.taxonomy.ui.importwizard;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.taxonomy.manager.importexport.FileChange;
import org.olat.modules.taxonomy.manager.importexport.FileRow;

/**
 * Content of the callout of a changed row: every changed value with its
 * current value and the value of the file.
 * 
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class ImportChangesController extends BasicController {

	private static final int MAX_LENGTH = 200;

	public ImportChangesController(UserRequest ureq, WindowControl wControl, FileRow row) {
		super(ureq, wControl, TaxonomyImportWizard.getTranslator(ureq));

		VelocityContainer mainVC = createVelocityContainer("import_changes");
		List<ChangeView> changes = row.getChanges().stream().map(this::toView).toList();
		mainVC.contextPut("changes", changes);
		putInitialPanel(mainVC);
	}

	private ChangeView toView(FileChange change) {
		String label = translate(change.column());
		if(change.language() != null) {
			label += " (" + change.language() + ")";
		}
		String before = format(change.before());
		String after = change.contentOnly()
				? translate("importexport.change.content", StringHelper.escapeHtml(change.after()))
				: format(change.after());
		return new ChangeView(StringHelper.escapeHtml(label), before, after);
	}

	/**
	 * @return The value escaped, descriptions as text without markup, shortened
	 */
	private String format(String value) {
		if(!StringHelper.containsNonWhitespace(value)) {
			return "<em>" + translate("importexport.change.empty") + "</em>";
		}
		String text = FilterFactory.getHtmlTagAndDescapingFilter().filter(value);
		if(!StringHelper.containsNonWhitespace(text)) {
			text = value;
		}
		text = text.trim();
		if(text.length() > MAX_LENGTH) {
			text = text.substring(0, MAX_LENGTH) + "…";
		}
		return StringHelper.escapeHtml(text);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	public record ChangeView(String label, String before, String after) {
		//
	}
}
