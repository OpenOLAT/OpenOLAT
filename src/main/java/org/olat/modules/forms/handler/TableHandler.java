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
package org.olat.modules.forms.handler;

import java.util.Locale;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.CloneElementHandler;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.TableElement;
import org.olat.modules.ceditor.ui.PageRunComponent;
import org.olat.modules.ceditor.ui.PageRunControllerElement;
import org.olat.modules.ceditor.ui.TableEditorController;
import org.olat.modules.ceditor.ui.TableRunController;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Table;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.model.EvaluationFormComponentElement;
import org.olat.modules.forms.ui.model.EvaluationFormControllerReportElement;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.forms.ui.model.EvaluationFormReportElement;
import org.olat.modules.forms.ui.model.ExecutionIdentity;

/**
 * 
 * Initial date: 18 Feb 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TableHandler implements EvaluationFormElementHandler, PageElementStore<TableElement>,
		SimpleAddPageElementHandler, CloneElementHandler, EvaluationFormReportHandler {

	@Override
	public String getType() {
		return "formtable";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_table";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.content;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element,
			PageElementRenderingHints options) {
		if(element instanceof TableElement) {
			Controller ctrl = new TableRunController(ureq, wControl, (TableElement)element);
			return new PageRunControllerElement(ctrl);
		}
		return new PageRunComponent(new Panel("empty"));
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof TableElement) {
			return new TableEditorController(ureq, wControl, (TableElement)element, this);
		}
		return null;
	}

	@Override
	public EvaluationFormReportElement getReportElement(UserRequest ureq, WindowControl windowControl,
			PageElement element, SessionFilter filter, ReportHelper reportHelper) {
		if (element instanceof TableElement) {
			Controller ctrl = new TableRunController(ureq, windowControl, (TableElement)element);
			return new EvaluationFormControllerReportElement(ctrl);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		Table table = new Table();
		table.setId(UUID.randomUUID().toString());
		return table;
	}

	@Override
	public PageElement clonePageElement(PageElement element) {
		if (element instanceof Table) {
			Table table = (Table)element;
			Table clone = new Table();
			clone.setId(UUID.randomUUID().toString());
			clone.setContent(table.getContent());
			clone.setLayoutOptions(table.getLayoutOptions());
			return clone;
		}
		return null;
	}

	@Override
	public TableElement savePageElement(TableElement element) {
		return element;
	}

	@Override
	public EvaluationFormExecutionElement getExecutionElement(UserRequest ureq, WindowControl wControl, Form rootForm,
			PageElement element, ExecutionIdentity executionIdentity) {
		return new EvaluationFormComponentElement(getContent(ureq, wControl, element, null));
	}
	
}
