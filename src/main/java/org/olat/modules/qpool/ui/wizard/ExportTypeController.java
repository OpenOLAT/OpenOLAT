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
package org.olat.modules.qpool.ui.wizard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.modules.qpool.ExportFormatOptions;
import org.olat.modules.qpool.ExportFormatOptions.Outcome;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemShort;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportTypeController extends StepFormBasicController {

	private String[] formatKeys;
	private String[] formatValues;
	private Map<String, ExportFormatOptions> formatMap = new HashMap<>();
	
	private SingleSelection formatEl;
	
	@Autowired
	private QPoolService qpoolService;
	
	public ExportTypeController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, List<QuestionItemShort> items) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);

		List<ExportFormatOptions> formatList =  qpoolService.getExportFormatOptions(items, Outcome.download).stream()
				.sorted(new ExportFormatOptionsComparator())
				.collect(Collectors.toList());
		
		List<String> formatKeyList = new ArrayList<>();
		List<String> formatValueList = new ArrayList<>();
		for(ExportFormatOptions format:formatList) {
			String outcome = format.getOutcome().name();
			String key = format.getFormat() + "__" + outcome;
			String formatName = format.getFormat().replace(" ", "_");
			String translation = translate("export.outcome." + outcome + "." + formatName);
			formatKeyList.add(key);
			formatValueList.add(translation);
			formatMap.put(key, format);
		}

		formatKeys = formatKeyList.toArray(new String[formatKeyList.size()]);
		formatValues = formatValueList.toArray(new String[formatValueList.size()]);
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("export.type.desc");
		
		formatEl = uifactory.addDropdownSingleselect("export.type", "export.type", formLayout, formatKeys, formatValues, null);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(formatEl.isOneSelected()) {
			ExportFormatOptions options = formatMap.get(formatEl.getSelectedKey());
			addToRunContext("format", options);
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private static class ExportFormatOptionsComparator implements Comparator<ExportFormatOptions> {
		@Override
		public int compare(ExportFormatOptions o1, ExportFormatOptions o2) {
			if(o1 == null && o2 == null) {
				return 0;
			} else if(o1 == null) {
				return -1;
			} else if(o2 == null) {
				return 1;
			}
			
			String f1 = o1.getFormat();
			String f2 = o2.getFormat();
			if(f1 == null && f2 == null) {
				return 0;
			} else if(f1 == null) {
				return -1;
			} else if(f2 == null) {
				return 1;
			}
			return f1.compareTo(f2);
		}
	}
}