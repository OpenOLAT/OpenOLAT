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
package org.olat.course.nodes.cl.ui.wizard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.nodes.cl.ui.CheckListEditController;

/**
 * 
 * Initial date: 13.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MetadatasStepController extends StepFormBasicController {
	
	private int numOfChecklist;
	private final GeneratorData data;
	private List<DueDateWrapper> wrappers;
	
	public MetadatasStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "metadatas_config");
		setTranslator(Util.createPackageTranslator(CheckListEditController.class, getLocale(), getTranslator()));
		data = (GeneratorData)getFromRunContext("data");
		numOfChecklist = data.getNumOfNodes();
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("metadatas.details");
		setFormDescription("metadatas.details.description");

		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			wrappers = new ArrayList<>();
			for(int i=0;i<numOfChecklist;i++) {
				wrappers.add(forgeRow(i, layoutCont));
			}
			layoutCont.contextPut("duedates", wrappers);
		}
	}
	
	private DueDateWrapper forgeRow(int i, FormLayoutContainer tableCont) {
		String title = data.getNodePrefix() + " " + (i+1);
		TextElement titleEl = uifactory.addTextElement("title_" + i, null, 32, title, tableCont);
		titleEl.setDisplaySize(21);
		DateChooser dueDateEl = uifactory.addDateChooser("duedate_" + i, "config.due.date", null, tableCont);
		dueDateEl.setDateChooserTimeEnabled(true);
		DueDateWrapper wrapper = new DueDateWrapper(titleEl, dueDateEl);
		return wrapper;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		for(DueDateWrapper wrapper:wrappers) {
			TextElement titleEl = wrapper.getTitleEl();
			titleEl.clearError();
			if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
				titleEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk &= super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<CheckListNode> nodes = new ArrayList<>(wrappers.size());
		for(DueDateWrapper wrapper:wrappers) {
			nodes.add(wrapper.getNodeInfos());
		}
		data.setNodes(nodes);
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	public static final class DueDateWrapper {
		private final TextElement titleEl;
		private final DateChooser dueDateEl;
		private final CheckListNode checkListNode;
		
		public DueDateWrapper(TextElement titleEl, DateChooser dueDateEl) {
			this.titleEl = titleEl;
			this.dueDateEl = dueDateEl;
			checkListNode = new CheckListNode();
		}
		
		public CheckListNode getNodeInfos() {
			checkListNode.setTitle(getTitle());
			checkListNode.setDueDate(getDueDate());
			return checkListNode;
		}

		public Date getDueDate() {
			return dueDateEl.getDate();
		}
			
		public String getDueDateName() {
			return dueDateEl.getComponent().getComponentName();
		}

		public String getTitle() {
			return titleEl.getValue();
		}
		
		public TextElement getTitleEl() {
			return titleEl;
		}
		
		public String getTitleName() {
			return titleEl.getName();
		}
		
	}
}
