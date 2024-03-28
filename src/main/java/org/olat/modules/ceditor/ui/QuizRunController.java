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
package org.olat.modules.ceditor.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.model.QuizQuestion;
import org.olat.modules.ceditor.model.QuizSettings;
import org.olat.modules.ceditor.model.jpa.QuizPart;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

/**
 * Initial date: 2024-03-11<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class QuizRunController extends BasicController implements PageRunElement {

	private final VelocityContainer mainVC;
	private QuizPart quizPart;
	private final boolean editable;
	private Link startButton;

	public QuizRunController(UserRequest ureq, WindowControl wControl, QuizPart quizPart, boolean editable) {
		super(ureq, wControl);
		this.quizPart = quizPart;
		this.editable = editable;
		mainVC = createVelocityContainer("quiz_run");
		mainVC.setElementCssClass("o_quiz_run_element_css_class");
		setBlockLayoutClass(quizPart.getSettings());
		putInitialPanel(mainVC);
		initUI();
		updateUI(ureq);
	}

	private void setBlockLayoutClass(QuizSettings quizSettings) {
		mainVC.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(quizSettings, false));
	}

	private void initUI() {
	}

	private void updateUI(UserRequest ureq) {
		if (quizPart.getBackgroundImageMedia() != null && quizPart.getBackgroundImageMediaVersion() != null) {
			mainVC.put("image", ComponentsFactory.getImageComponent(ureq, quizPart.getBackgroundImageMediaVersion()));
		}

		mainVC.contextPut("title", quizPart.getSettings().getTitle());
		mainVC.contextPut("description", substituteVariables(quizPart.getSettings().getDescription()));
		startButton = LinkFactory.createButton("quiz.start", mainVC, this);
		startButton.setIconLeftCSS("o_icon o_icon-fw o_icon_play");
		startButton.setPrimary(true);
		mainVC.put("quiz.start", startButton);
	}

	private String substituteVariables(String text) {
		if (!StringHelper.containsNonWhitespace(text)) {
			return text;
		}
		return text.replace("$numberOfQuestions", "" + getNumberOfQuestions());
	}

	int getNumberOfQuestions() {
		List<QuizQuestion> quizQuestions = quizPart.getSettings().getQuestions();
		if (quizQuestions == null) {
			return 0;
		}
		return quizQuestions.size();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof ChangePartEvent changePartEvent) {
			if (changePartEvent.getElement() instanceof QuizPart updatedQuizPart) {
				quizPart = updatedQuizPart;
				setBlockLayoutClass(quizPart.getSettings());
				updateUI(ureq);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (startButton == source) {
			System.err.println("start");
		}
	}

	@Override
	public Component getComponent() {
		return getInitialComponent();
	}

	@Override
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		return false;
	}
}
