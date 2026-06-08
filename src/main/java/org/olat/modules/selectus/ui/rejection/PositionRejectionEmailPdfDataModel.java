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
package org.olat.modules.selectus.ui.rejection;

import java.util.List;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.MailLogInfos;
import org.olat.modules.selectus.model.RejectionEmailLog;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.DefaultExportTableDataModel;

/**
 * 
 * Initial date: 22.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionRejectionEmailPdfDataModel extends DefaultExportTableDataModel<MailLogInfos> {
	
	private final Translator translator;


	public PositionRejectionEmailPdfDataModel(List<MailLogInfos> rows, Translator translator) {
		super(rows);
		this.translator = translator;
	}
	
	public Translator getTranslator() {
		return translator;
	}

	@Override
	public int getColumnCount() {
		return Fields.values().length;
	}

	@Override
	public String getFieldNameAt(int col) {
		Fields field = Fields.values()[col];
		return field.name();
	}

	@Override
	public String getHeader(int col) {
		Fields field = Fields.values()[col];
		return translator.translate(field.key());
	}

	@Override
	public Object getValueAt(int row, int col) {
		MailLogInfos logInfos = getObject(row);
		RejectionEmailLog log = logInfos.getMailLog();
		ApplicationLight app = logInfos.getApplication();
		Fields field = Fields.values()[col];
		switch(field) {
			case id: return app.getId() == null ? "" : app.getId().toString();
			case title: {
				if(StringHelper.containsNonWhitespace(app.getPerson().getTitle())) {
					String title = translator.translate(app.getPerson().getTitle());
					if(title != null && title.length() < 15) {
						return title;
					}
				}
				return app.getPerson().getTitle();
			}
			case firstName: return app.getPerson().getFirstName();
			case lastName: return app.getPerson().getLastName();
			case mail: return app.getPerson().getMail();
			case logCreationDate: return DateCellRenderer.format(log.getCreationDate());
			case template: return log.getMailTemplate();
			default: return app;
		}
	}
	
	public enum Fields {
		id("edit.application.id"),
		title("edit.application.title"),
		firstName("edit.application.firstName"),
		lastName("edit.application.lastName"),
		mail("edit.application.mail"),
		logCreationDate("edit.log.creationDate"),
		template("edit.log.template");
		
		private final String key;
		
		private Fields(String key) {
			this.key = key;
		}
		
		public String key() {
			return key;
		}
	}
}