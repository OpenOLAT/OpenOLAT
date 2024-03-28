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
package org.olat.modules.ceditor.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilderFactory;
import org.olat.modules.ceditor.model.QuizQuestion;
import org.olat.modules.ceditor.model.QuizSettings;
import org.olat.modules.ceditor.model.jpa.QuizPart;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;

/**
 * Initial date: 2024-03-26<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class ContentEditorQti {

	private static final Logger logger = Tracing.createLoggerFor(ContentEditorQti.class);

	@Autowired
	private ContentEditorFileStorage contentEditorFileStorage;
	@Autowired
	private QTI21Service qtiService;

	public String generateStoragePath(QuizPart quizPart) {
		String id = quizPart.getKey() != null ? quizPart.getId() : null;
		if (!StringHelper.containsNonWhitespace(id)) {
			logger.error("trying to generate storage path with missing key");
			return null;
		}
		File questionsSubdirectory = contentEditorFileStorage.generateQuestionsSubDirectory();
		File storagePath = new File(questionsSubdirectory, id);
		if (!storagePath.exists()) {
			storagePath.mkdir();
		}
		return contentEditorFileStorage.getRelativePath(storagePath);
	}

	public boolean finishInitialization(QuizPart quizPart, Locale locale) {
		boolean setStoragePath = false;
		QuizSettings quizSettings = quizPart.getSettings();

		if (quizPart.getKey() == null) {
			logger.info("Can't finish initialization for quizPart '{}'  because of missing key", quizSettings.getTitle());
			return false;
		}
		if (!StringHelper.containsNonWhitespace(quizPart.getStoragePath())) {
			String storagePath = generateStoragePath(quizPart);
			if (storagePath != null) {
				quizPart.setStoragePath(storagePath);
				setStoragePath = true;
			} else {
				logger.info("Can't set storage path for quizPart '{}'.", quizPart.getKey());
			}
		}
		if (!initializationNeeded(quizSettings)) {
			return setStoragePath;
		}

		AtomicBoolean performedInitialization = new AtomicBoolean(false);
		List<QuizQuestion> quizQuestions = quizSettings.getQuestions().stream().map(q -> {
			if (q.needsInitialization()) {
				performedInitialization.set(true);
				return createQuestion(quizPart, q, locale);
			} else {
				return q;
			}
		}).collect(Collectors.toList());
		if (performedInitialization.get()) {
			quizSettings.setQuestions(quizQuestions);
			quizPart.setSettings(quizSettings);
		}

		return performedInitialization.get();
	}

	private boolean initializationNeeded(QuizSettings quizSettings) {
		if (quizSettings == null || quizSettings.getQuestions() == null) {
			return false;
		}
		for (QuizQuestion quizQuestion : quizSettings.getQuestions()) {
			if (quizQuestion.needsInitialization()) {
				return true;
			}
		}
		return false;
	}

	public QuizQuestion createQuestion(QuizPart quizPart, QuizQuestion quizQuestion, Locale locale) {
		QTI21QuestionType type = QTI21QuestionType.safeValueOf(quizQuestion.getType());
		return createQuestion(quizPart, type, locale);
	}

	public QuizQuestion createQuestion(QuizPart quizPart, QTI21QuestionType type, Locale locale) {
		String questionId = IdentifierGenerator.newAsString(type.getPrefix());
		return createQuestion(quizPart, type, questionId, locale);
	}

	private QuizQuestion createQuestion(QuizPart quizPart, QTI21QuestionType type, String questionId, Locale locale) {
		File questionsDir = contentEditorFileStorage.getFile(quizPart.getStoragePath());
		File questionDir = new File(questionsDir, questionId);
		questionDir.mkdir();
		File questionFile = new File(questionDir, questionId + ".xml");

		AssessmentItemBuilder itemBuilder = AssessmentItemBuilderFactory.get(type, locale);
		if (itemBuilder == null) {
			logger.warn("Could not create assessment item builder for type '{}'", type);
			return null;
		}

		AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
		qtiService.persistAssessmentObject(questionFile, assessmentItem);

		QuizQuestion quizQuestion = new QuizQuestion();
		quizQuestion.setId(questionId);
		quizQuestion.setType(type.name());
		quizQuestion.setTitle(assessmentItem.getTitle());
		quizQuestion.setXmlFilePath(contentEditorFileStorage.getRelativePath(questionFile));

		return quizQuestion;
	}

	public QuizQuestion cloneQuestion(QuizPart quizPart, QuizQuestion quizQuestion, Translator translator) {
		File questionsDir = contentEditorFileStorage.getFile(quizPart.getStoragePath());
		String clonedQuestionId = IdentifierGenerator.newAsString(quizQuestion.getType());
		File clonedQuestionDir = new File(questionsDir, clonedQuestionId);
		clonedQuestionDir.mkdir();
		File clonedQuestionFile = new File(clonedQuestionDir, clonedQuestionId + ".xml");
		File sourceQuestionFile = contentEditorFileStorage.getFile(quizQuestion.getXmlFilePath());

		try {
			Files.copy(sourceQuestionFile.toPath(), clonedQuestionFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error("Failed to clone question XML '{}': {}", sourceQuestionFile.toPath().toString(), e);
		}

		QuizQuestion clonedQuizQuestion = new QuizQuestion();
		clonedQuizQuestion.setId(clonedQuestionId);
		clonedQuizQuestion.setType(quizQuestion.getType());
		String clonedTitle = quizQuestion.getTitle() + " " + translator.translate("copy.suffix");
		clonedQuizQuestion.setTitle(clonedTitle);
		clonedQuizQuestion.setXmlFilePath(contentEditorFileStorage.getRelativePath(clonedQuestionFile));

		return clonedQuizQuestion;
	}

	public String copyQuestion(QuizQuestion quizQuestion, String targetStoragePath) {
		File sourceQuestionFile = contentEditorFileStorage.getFile(quizQuestion.getXmlFilePath());
		File targetQuestionsDir = contentEditorFileStorage.getFile(targetStoragePath);
		File targetQuestionDir = new File(targetQuestionsDir, quizQuestion.getId());
		targetQuestionDir.mkdir();
		File targetQuestionFile = new File(targetQuestionDir, quizQuestion.getId() + ".xml");
		try {
			Files.copy(sourceQuestionFile.toPath(), targetQuestionFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error("Failed to clone question XML '{}': {}", sourceQuestionFile.toPath().toString(), e);
		}
		return contentEditorFileStorage.getRelativePath(targetQuestionFile);
	}

	public void deleteQuestion(QuizPart quizPart, QuizQuestion quizQuestion) {
		File questionFile = contentEditorFileStorage.getFile(quizQuestion.getXmlFilePath());
		try {
			Files.deleteIfExists(questionFile.toPath());
		} catch (IOException e) {
			logger.error("Failed to delete question XML '{}': {}", questionFile.toPath().toString(), e);
		}

		File questionsDir = contentEditorFileStorage.getFile(quizPart.getStoragePath());
		File questionDir = new File(questionsDir, quizQuestion.getId());
		try {
			Files.deleteIfExists(questionDir.toPath());
		} catch (IOException e) {
			logger.error("Failed to delete question directory '{}': {}", questionDir.toPath().toString(), e);
		}
	}

	public void deleteQuestionsDirectory(QuizPart quizPart) {
		File questionsDir = contentEditorFileStorage.getFile(quizPart.getStoragePath());
		try {
			Files.deleteIfExists(questionsDir.toPath());
		} catch (IOException e) {
			logger.warn("Failed to delete the questions directory '{}': e", questionsDir.toPath().toString(), e);
		}
	}

	public record QuizQuestionStorageInfo(File questionDirectory, VFSContainer questionContainer,
										  File questionFile) {}

	public QuizQuestionStorageInfo getStorageInfo(QuizPart quizPart, QuizQuestion quizQuestion) {
		File questionsDirectory = contentEditorFileStorage.getFile(quizPart.getStoragePath());
		File questionDirectory = new File(questionsDirectory, quizQuestion.getId());
		VFSContainer questionContainer = new LocalFolderImpl(questionDirectory);
		File questionFile = new File(questionDirectory, quizQuestion.getId() + ".xml");

		return new QuizQuestionStorageInfo(questionDirectory, questionContainer, questionFile);
	}
}
