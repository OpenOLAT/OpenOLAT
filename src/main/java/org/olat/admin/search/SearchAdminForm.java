/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

/**
 * Fulltext search input form.
 * @author Christian Guretzki
 * 
 */
public class SearchAdminForm extends FormBasicController {
	
	private TextElement indexInterval;
	private TextElement blackList;
	private MultipleSelectionElement excelFileEnabled;
	private MultipleSelectionElement pptFileEnabled;
	private MultipleSelectionElement pdfFileEnabled;
	private FormSubmit submit;
	
	/**
	 * 
	 * @param name  Name of the form
	 */
	public SearchAdminForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}
		
	public long getIndexInterval() {
		return Long.parseLong(indexInterval.getValue());
	}
	
	public void setIndexInterval(long v) {
		indexInterval.setValue(Long.toString(v));
	}
	
	public List<String> getFileBlackList() {
		String value = blackList.getValue();
		if(StringHelper.containsNonWhitespace(value)) {
			String[] files = value.split(",");
			Set<String> list = new HashSet<>();
			for(String file:files) {
				list.add(file);
			}
			return new ArrayList<>(list);
		}
		return Collections.emptyList();
	}
	
	public void setFileBlackList(List<String> files) {
		if(files == null) return;
		
		StringBuilder sb = new StringBuilder();
		for(String file:files) {
			if(sb.length() > 0) sb.append(',');
			sb.append(file);
		}
		blackList.setValue(sb.toString());
	}
	
	public boolean isPptFileEnabled() {
		return pptFileEnabled.isMultiselect() && pptFileEnabled.isSelected(0);
	}
	
	public void setPptFileEnabled(boolean enabled) {
		pptFileEnabled.select("on", enabled);
	}
	
	public boolean isExcelFileEnabled() {
		return excelFileEnabled.isMultiselect() && excelFileEnabled.isSelected(0);
	}
	
	public void setExcelFileEnabled(boolean enabled) {
		excelFileEnabled.select("on", enabled);
	}
	
	public boolean isPdfFileEnabled() {
		return pdfFileEnabled.isMultiselect() && pdfFileEnabled.isSelected(0);
	}
	
	public void setPdfFileEnabled(boolean enabled) {
		pdfFileEnabled.select("on", enabled);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("search.admin.form.title");
		
		indexInterval = uifactory.addTextElement("indexInterval", "search.admin.label.index.interval", 20, "", formLayout);
		indexInterval.setRegexMatchCheck("\\d+", "error.index.interval.must.be.number");
		//indexInterval.setMinValueCheck(0, "error.index.interval.must.be.number");
		indexInterval.setDisplaySize(4);
		
		blackList = uifactory.addTextAreaElement("search.admin.label.blackList", 3, 80, "", formLayout);
		blackList.setExampleKey("search.admin.label.blackList.example", null);
		
		excelFileEnabled = uifactory.addCheckboxesHorizontal("search.admin.label.enableExcel", formLayout, new String[]{"on"}, new String[]{""});
		pptFileEnabled = uifactory.addCheckboxesHorizontal("search.admin.label.enablePpt", formLayout, new String[]{"on"}, new String[]{""});
		pdfFileEnabled = uifactory.addCheckboxesHorizontal("search.admin.label.enablePdf", formLayout, new String[]{"on"}, new String[]{""});

		submit = new FormSubmit("submit", "submit");
		formLayout.add(submit);
	}
}