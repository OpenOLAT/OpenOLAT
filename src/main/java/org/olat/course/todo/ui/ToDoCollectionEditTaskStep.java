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
package org.olat.course.todo.ui;

import java.util.List;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.todo.model.ToDoTaskCollectionEditContext;
import org.olat.modules.todo.ToDoTask;

/**
 * 
 * Initial date: 5 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoCollectionEditTaskStep extends BasicStep {

	private final ToDoTask toDoTaskCollection;
	private final List<TagInfo> tagInfos;

	public ToDoCollectionEditTaskStep(UserRequest ureq, ToDoTask toDoTaskCollection, List<TagInfo> tagInfos) {
		super(ureq);
		this.toDoTaskCollection = toDoTaskCollection;
		this.tagInfos = tagInfos;
		setI18nTitleAndDescr("course.todo.collection.todo.step", null);
		setNextStep(new ToDoCollectionEditOverviewStep(ureq));
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.NEXT;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl,
			StepsRunContext stepsRunContext, Form form) {
		ToDoTaskCollectionEditContext context = new ToDoTaskCollectionEditContext();
		context.setToDoTaskCollection(toDoTaskCollection);
		context.setCollectionTagDisplayNames(tagInfos.stream().filter(TagInfo::isSelected).map(TagInfo::getDisplayName).toList());
		stepsRunContext.put(ToDoTaskCollectionEditContext.KEY, context);
		return new ToDoCollectionEditTaskController(ureq, windowControl, form, stepsRunContext, tagInfos);
	}

}
