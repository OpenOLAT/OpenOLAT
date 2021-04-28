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
package org.olat.modules.forms.ui;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.olat.core.util.openxml.OpenXMLWorkbookStyles;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.ui.EvaluationFormExcelExport.UserColumns;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 26 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UserPropertiesColumns implements UserColumns {
	
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final Translator userPropertyTranslator;

	public UserPropertiesColumns(List<UserPropertyHandler> userPropertyHandlers, Translator userPropertyTranslator) {
		this.userPropertyHandlers = userPropertyHandlers;
		this.userPropertyTranslator = userPropertyTranslator;
	}

	@Override
	public void addHeaderColumns(Row row, AtomicInteger col, OpenXMLWorkbookStyles styles) {
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			String header = userPropertyTranslator.translate(userPropertyHandler.i18nColumnDescriptorLabelKey());
			row.addCell(col.getAndIncrement(), header, styles.getBottomAlignStyle());
		}
	}

	@Override
	public void addColumns(EvaluationFormSession session, Row row, AtomicInteger col) {
		if (session.getParticipation() != null && session.getParticipation().getExecutor() != null) {
			User user = session.getParticipation().getExecutor().getUser();
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				String value = userPropertyHandler.getUserProperty(user, userPropertyTranslator.getLocale());
				row.addCell(col.getAndIncrement(), value);
			}
		}
	}

}
