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
package org.olat.modules.ceditor.handler;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.QuizElement;
import org.olat.modules.ceditor.model.QuizQuestion;
import org.olat.modules.ceditor.model.QuizSettings;
import org.olat.modules.ceditor.model.jpa.QuizPart;
import org.olat.modules.ceditor.ui.PageEditorV2Controller;
import org.olat.modules.ceditor.ui.QuizEditorController;
import org.olat.modules.ceditor.ui.QuizInspectorController;
import org.olat.modules.ceditor.ui.QuizRunController;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2024-03-11<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class QuizElementHandler implements PageElementHandler, PageElementStore<QuizElement>,
		SimpleAddPageElementHandler, ComponentEventListener {

	private final RepositoryEntry entry;
	private final String subIdent;

	public QuizElementHandler(RepositoryEntry entry, String subIdent) {
		this.entry = entry;
		this.subIdent = subIdent;
	}

	@Override
	public String getType() {
		return "quiz";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_question";
	}

	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.knowledge;
	}

	@Override
	public int getSortOrder() {
		return 10;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, RenderingHints options) {
		if (element instanceof QuizPart quizPart) {
			return new QuizRunController(ureq, wControl, quizPart, options.isEditable(), entry, subIdent);
		}
		return null;
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if (element instanceof QuizPart quizPart) {
			return new QuizEditorController(ureq, wControl, quizPart, this);
		}
		return null;
	}

	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if (element instanceof QuizPart quizPart) {
			return new QuizInspectorController(ureq, wControl, quizPart, this);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		Translator translator = Util.createPackageTranslator(PageEditorV2Controller.class, locale);
		QuizPart quizPart = new QuizPart();
		QuizSettings quizSettings = quizPart.getSettings();
		QuizQuestion quizQuestion = new QuizQuestion();
		quizQuestion.setId(QuizQuestion.NEEDS_INITIALIZATION);
		quizQuestion.setType(QTI21QuestionType.sc.getPrefix());
		quizQuestion.setTitle(translator.translate("quiz.question.sc"));
		quizSettings.getQuestions().add(quizQuestion);
		quizSettings.setTitle(translator.translate("quiz.title.default"));
		quizSettings.setDescription(translator.translate("quiz.description.default"));
		quizPart.setSettings(quizSettings);
		return quizPart;
	}

	@Override
	public QuizElement savePageElement(QuizElement element) {
		PageService pageService = CoreSpringFactory.getImpl(PageService.class);
		if (pageService != null) {
			return pageService.updatePart((QuizPart) element);
		}
		return null;
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
	}
}
