/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.export.ui;

import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 22 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportStatusRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public ExportStatusRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof ExportRow exportRow) {
			Task task = exportRow.getRunningTask();
			if(task != null) {
				TaskStatus status = task.getStatus();
				if(status == TaskStatus.newTask || status == TaskStatus.inWork) {
					target.append(translator.translate("table.status.ongoing"));
				} else if(status == TaskStatus.done) {
					renderExpiration(target, exportRow);
				} else if(status == TaskStatus.failed) {
					target.append(translator.translate("table.status.failed"));
				} else {
					target.append("-");
				}
			} else {
				renderExpiration(target, exportRow);
			}
		}
	}
	
	private void renderExpiration(StringOutput target, ExportRow exportRow) {
		if(exportRow.getExpirationDate() != null) {
			int days = exportRow.getExpirationInDays();
			if(days == 1) {
				target.append(translator.translate("row.expiration.day", Integer.toString(days)));
			} else {
				target.append(translator.translate("row.expiration.days", Integer.toString(days)));
			}
		}
	}
}
