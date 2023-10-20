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
package org.olat.modules.quality.ui;

import java.util.Date;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.controllers.activity.ActivityLogController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 Oct 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityToDoMetadataController extends FormBasicController {
	
	private ActivityLogController activityLogCtrl;
	
	private final ToDoTaskRef toDoTask;
	private final IdentityRef creator;
	private final Date creationDate;
	private final IdentityRef modifier;
	private final Date modifiedDate;
	private final Formatter formatter;
	
	@Autowired
	private UserManager userManager;

	public QualityToDoMetadataController(UserRequest ureq, WindowControl wControl, Form mainForm, ToDoTaskRef toDoTask,
			IdentityRef creator, Date creationDate, IdentityRef modifier, Date modifiedDate) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, mainForm);
		setTranslator(Util.createPackageTranslator(ToDoUIFactory.class, getLocale(), getTranslator()));
		this.toDoTask = toDoTask;
		this.creator = creator;
		this.creationDate = creationDate;
		this.modifier = modifier;
		this.modifiedDate = modifiedDate;
		formatter = Formatter.getInstance(getLocale());
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormStyle("o_two_col_metadata");
		
		String createdDateBy = translate("date.by",
				formatter.formatDate(creationDate),
				userManager.getUserDisplayName(creator.getKey()));
		uifactory.addStaticTextElement("task.created", createdDateBy, formLayout);
		
		String modifiedDateBy = translate("date.by",
				formatter.formatDate(modifiedDate),
				userManager.getUserDisplayName(modifier));
		uifactory.addStaticTextElement("task.last.modified", modifiedDateBy, formLayout);
		
		activityLogCtrl = new QualityToDoActivityLogController(ureq, getWindowControl(), mainForm, toDoTask);
		listenTo(activityLogCtrl);
		activityLogCtrl.getInitialFormItem().setElementCssClass("o_two_span");
		formLayout.add(activityLogCtrl.getInitialFormItem());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
