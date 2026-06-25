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
package org.olat.course.assessment.ui.mode;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.PList;
import org.olat.course.assessment.ui.mode.SafeExamBrowserRawConfigurationTableModel.KeyValueCols;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * Initial date: 24 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SafeExamBrowserRawConfigurationController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private SafeExamBrowserRawConfigurationTableModel tableModel;
	
	public SafeExamBrowserRawConfigurationController(UserRequest ureq, WindowControl wControl, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "raw_configuration", rootForm);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(KeyValueCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(KeyValueCols.value));
		
		tableModel = new SafeExamBrowserRawConfigurationTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "configurationTable", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setSortEnabled(false);
	}
	
	protected void loadConfiguration(String plist) {
		List<SafeExamBrowserConfigurationKeyValueRow> rows = new ArrayList<>();
		
		if(StringHelper.containsNonWhitespace(plist)) {
			PList list = PList.valueOf(plist);
			Element dictElement = list.getRootDict();
			
			String key = null;
			for(Node node = dictElement.getFirstChild(); node != null; node = node.getNextSibling()) {
				if(node instanceof Element element) {
					if("key".equalsIgnoreCase(element.getNodeName())) {
						key = element.getTextContent();
					} else if(key != null) {
						Object value = toStringValue(element);
						if(value != null) {
							rows.add(new SafeExamBrowserConfigurationKeyValueRow(key, value));
						}
						key = null;
					}
				}
			}
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private static String toStringValue(Element valueElement) {
		String type = valueElement.getNodeName().toLowerCase();
		return switch(type) {
			case "true" -> Boolean.TRUE.toString().toLowerCase();
			case "false" -> Boolean.FALSE.toString().toLowerCase();
			case "integer", "real", "string", "data", "date" -> valueElement.getTextContent();
			case "dict" -> dictToStringValue(valueElement);
			case "array" -> arrayToStringValue(valueElement);
			default -> null;
		};
	}
	
	private static String arrayToStringValue(Element arrayElement) {	
		StringBuilder array = new StringBuilder();
		
		for(Node node = arrayElement.getFirstChild(); node != null; node = node.getNextSibling()) {
			if(node instanceof Element element) {
				String str = toStringValue(element);
				if(StringHelper.containsNonWhitespace(str)) {
					if(!array.isEmpty()) {
						array.append(" , ");
					}
					array.append(str);
				}
			}
		}

		return array.isEmpty() ? "" : "[ " + array.toString() + " ]";
	}
	
	private static String dictToStringValue(Element arrayElement) {	
		StringBuilder dict = new StringBuilder();

		String key = null;
		for(Node node = arrayElement.getFirstChild(); node != null; node = node.getNextSibling()) {
			if(node instanceof Element element) {
				if("key".equalsIgnoreCase(element.getNodeName())) {
					key = element.getTextContent();
				} else if(key != null) {
					Object value = toStringValue(element);
					if(value != null) {
						dict.append("{ ").append(key).append(" : ").append(value).append(" }");
					}
					key = null;
				}
			}
		}

		return dict.isEmpty() ? "" : dict.toString();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
