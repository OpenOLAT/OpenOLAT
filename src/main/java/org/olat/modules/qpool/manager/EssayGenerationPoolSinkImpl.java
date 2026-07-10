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
package org.olat.modules.qpool.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.ui.AiAdminController;
import org.olat.core.commons.services.ai.essay.AiSourceCompanion;
import org.olat.core.commons.services.ai.essay.AiSourceCompanionFileStore;
import org.olat.core.commons.services.ai.essay.EssayAiGrading;
import org.olat.core.commons.services.ai.essay.EssayAiGradingFileStore;
import org.olat.core.commons.services.ai.essay.EssayFormativeFeedbackService;
import org.olat.core.commons.services.ai.essay.EssayGenerationPoolSink;
import org.olat.core.commons.services.ai.essay.EssayItemDraft;
import org.olat.core.commons.services.ai.model.MCQuestionData;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.pool.AiQtiItemFactory;
import org.olat.ims.qti21.pool.QTI21QPoolServiceProvider;
import org.olat.ims.qti21.questionimport.AssessmentItemAndMetadata;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;

/**
 * Question-pool implementation of {@link EssayGenerationPoolSink}.
 * <p>
 * On job completion, persists each accepted essay {@link EssayItemDraft}
 * and each {@link MCQuestionData} as fresh question-pool items owned by
 * the requester. The QTI items themselves are built by the shared
 * {@link AiQtiItemFactory} so the shape (scoring, shuffle, modal feedback)
 * matches the ceditor QuizPart sink exactly.
 * <p>
 * For essay items, after the QTI XML has been written, the matching
 * {@link EssayAiGrading} POJO is serialised as {@code ai-grading.json}
 * into the pool item's storage directory so the runtime AI grading
 * pipeline picks up the rubric, key points, reference excerpt and
 * model answer at submission time.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class EssayGenerationPoolSinkImpl implements EssayGenerationPoolSink {

	private static final Logger log = Tracing.createLoggerFor(EssayGenerationPoolSinkImpl.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private AiQtiItemFactory aiQtiItemFactory;
	@Autowired
	private AiModule aiModule;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QuestionPoolModule qpoolModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private QuestionPoolLicenseHandler licenseHandler;
	@Autowired
	private EssayAiGradingFileStore essayAiGradingFileStore;
	@Autowired
	private AiSourceCompanionFileStore aiSourceCompanionFileStore;

	@Override
	public List<Long> persistGeneratedItems(Identity owner,
			List<EssayItemDraft> essayDrafts,
			Map<EssayItemDraft, EssayAiGrading> draftToGrading,
			List<MCQuestionData> mcQuestions,
			Locale locale,
			Long taxonomyLevelKey) {
		List<Long> created = new ArrayList<>();
		if (owner == null) {
			log.warn("EssayGenerationPoolSink: missing owner — skipping persistence");
			return created;
		}
		Locale loc = locale == null ? Locale.ENGLISH : locale;
		Translator translator = Util.createPackageTranslator(AiAdminController.class, loc);
		String solutionTitle = translator.translate("ai.solution.title");
		String wrongTitle = translator.translate("mc.feedback.wrong.title");

		TaxonomyLevel taxonomyLevel = resolveTaxonomyLevel(taxonomyLevelKey);

		// MC items first, then essay — the pool list shows newest first so the
		// essays appear at the top, matching the order users entered the request.
		if (mcQuestions != null) {
			for (MCQuestionData mc : mcQuestions) {
				QuestionItem item = persistMcItem(owner, mc, loc, solutionTitle, wrongTitle, taxonomyLevel);
				if (item != null) {
					created.add(item.getKey());
				}
			}
		}
		if (essayDrafts != null) {
			for (EssayItemDraft draft : essayDrafts) {
				EssayAiGrading grading = draftToGrading == null ? null : draftToGrading.get(draft);
				QuestionItem item = persistEssayItem(owner, draft, grading, loc, solutionTitle, taxonomyLevel);
				if (item != null) {
					created.add(item.getKey());
				}
			}
		}

		// Make sure all background-thread changes hit the DB before the caller
		// (drawer / dialog poll) sees them.
		dbInstance.commit();
		return created;
	}

	private TaxonomyLevel resolveTaxonomyLevel(Long taxonomyLevelKey) {
		if (taxonomyLevelKey == null) return null;
		try {
			return taxonomyService.getTaxonomyLevel(() -> taxonomyLevelKey);
		} catch (Exception e) {
			log.warn("EssayGenerationPoolSink: could not resolve taxonomy level {}: {}",
					taxonomyLevelKey, e.getMessage());
			return null;
		}
	}

	private QuestionItem persistMcItem(Identity owner, MCQuestionData data, Locale locale,
			String solutionTitle, String wrongTitle, TaxonomyLevel taxonomyLevel) {
		try {
			String spiId = aiModule == null ? null : aiModule.getMCGeneratorSpiId();
			String model = aiModule == null ? null : aiModule.getMCGeneratorModel();
			AiQtiItemFactory.McItem built = aiQtiItemFactory.buildMcItem(data, locale, solutionTitle,
					wrongTitle, spiId, model);
			if (built == null) {
				return null;
			}
			AssessmentItemAndMetadata metaItem = new AssessmentItemAndMetadata(built.builder());
			applyMcMetadata(metaItem, data, taxonomyLevel);
			QuestionItem item = importAndStamp(owner, metaItem, locale, taxonomyLevel);
			// Write the AI provenance companion next to the imported QTI XML so
			// the pool round-trip preserves "AI-generated" semantics without
			// hijacking the QTI toolName attribute.
			if (item != null && StringHelper.containsNonWhitespace(spiId)) {
				saveSourceCompanionNextToItem(item, spiId, model, true);
			}
			return item;
		} catch (Exception e) {
			log.warn("Failed to persist AI MC question into pool: {}", e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Resolve the on-disk directory of the freshly imported pool item and
	 * write the AI source companion next to the QTI XML.
	 */
	private void saveSourceCompanionNextToItem(QuestionItem item, String spiId, String model, boolean unsupervised) {
		try {
			QPoolFileStorage storage = CoreSpringFactory.getImpl(QPoolFileStorage.class);
			File itemDir = storage.getDirectory(((QuestionItemImpl) item).getDirectory());
			if (itemDir == null || !itemDir.isDirectory()) {
				log.warn("EssayGenerationPoolSink: item dir for question {} not found — skipping ai-source.json",
						item.getKey());
				return;
			}
			aiSourceCompanionFileStore.save(itemDir, new AiSourceCompanion(spiId, model, null, unsupervised));
		} catch (Exception e) {
			log.warn("Could not persist ai-source.json for pool item {}: {}",
					item == null ? null : item.getKey(), e.getMessage(), e);
		}
	}

	private QuestionItem persistEssayItem(Identity owner, EssayItemDraft draft, EssayAiGrading grading,
			Locale locale, String solutionTitle, TaxonomyLevel taxonomyLevel) {
		if (draft == null) return null;
		try {
			AiQtiItemFactory.EssayItem built = aiQtiItemFactory.buildEssayItem(
					draft, locale, solutionTitle, grading);
			AssessmentItemAndMetadata metaItem = new AssessmentItemAndMetadata(built.builder());
			applyEssayMetadata(metaItem, draft, grading, taxonomyLevel);

			// importExcelItem persists the QTI XML into the pool's storage dir
			// and registers the QuestionItem. We then need to re-locate that
			// directory so the ai-grading.json can be written next to it.
			QuestionItem item = importAndStamp(owner, metaItem, locale, taxonomyLevel);
			if (item != null && grading != null) {
				saveGradingNextToItem(item, grading, built.item());
			}
			return item;
		} catch (Exception e) {
			log.warn("Failed to persist AI essay draft into pool: {}", e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Common pool-import flow: hand the {@link AssessmentItemAndMetadata}
	 * to {@link QTI21QPoolServiceProvider#importExcelItem}, write the audit
	 * log entry, set review-status when the pool review process is off, and
	 * stamp the default license. Mirrors the legacy {@code NewAiItemController}
	 * behaviour so the AI generator does not behave differently from existing
	 * import paths.
	 */
	private QuestionItem importAndStamp(Identity owner, AssessmentItemAndMetadata metaItem, Locale locale,
			TaxonomyLevel taxonomyLevel) {
		QTI21QPoolServiceProvider spi = CoreSpringFactory.getImpl(QTI21QPoolServiceProvider.class);
		QuestionItem importedItem = spi.importExcelItem(owner, metaItem, locale);
		if (importedItem == null) return null;

		QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(owner,
				Action.CREATE_QUESTION_ITEM_BY_IMPORT);
		builder.withAfter(importedItem);
		qpoolService.persist(builder.create());

		if (importedItem instanceof QuestionItemImpl itemImpl) {
			if (taxonomyLevel != null) {
				itemImpl.setTaxonomyLevel(taxonomyLevel);
			}
			if (!qpoolModule.isReviewProcessEnabled()) {
				itemImpl.setQuestionStatus(QuestionStatus.review);
			}
			qpoolService.updateItem(itemImpl);
		}

		if (licenseModule.isEnabled(licenseHandler)) {
			licenseService.delete(importedItem);
			licenseService.createDefaultLicense(importedItem, licenseHandler, owner);
		}
		return importedItem;
	}

	/**
	 * Resolve the on-disk directory of the freshly imported pool item and
	 * write the AI grading metadata next to the QTI XML. Recomputes the
	 * content hash and back-fills the {@code assessmentItemIdentifier} so
	 * the integrity check at grading time succeeds.
	 */
	private void saveGradingNextToItem(QuestionItem item, EssayAiGrading grading, AssessmentItem qtiItem) {
		try {
			QPoolFileStorage storage = CoreSpringFactory.getImpl(QPoolFileStorage.class);
			File itemDir = storage.getDirectory(((QuestionItemImpl) item).getDirectory());
			if (itemDir == null || !itemDir.isDirectory()) {
				log.warn("EssayGenerationPoolSink: item dir for question {} not found — skipping grading file",
						item.getKey());
				return;
			}
			String identifier = qtiItem != null && qtiItem.getIdentifier() != null
					? qtiItem.getIdentifier() : "essay-" + item.getKey();
			grading.setAssessmentItemIdentifier(identifier);
			grading.setContentHash(EssayFormativeFeedbackService.computeContentHash(grading));
			essayAiGradingFileStore.save(itemDir, grading);
		} catch (Exception e) {
			log.warn("Could not persist ai-grading.json for pool item {}: {}",
					item == null ? null : item.getKey(), e.getMessage(), e);
		}
	}

	private void applyMcMetadata(AssessmentItemAndMetadata metaItem, MCQuestionData data,
			TaxonomyLevel taxonomyLevel) {
		if (data == null) return;
		String topic = data.getTopic();
		if (StringHelper.containsNonWhitespace(topic)) {
			metaItem.setTopic(StringHelper.xssScan(topic));
		}
		String keywords = data.getKeywords();
		if (StringHelper.containsNonWhitespace(keywords)) {
			metaItem.setKeywords(StringHelper.xssScan(keywords));
		}
		if (taxonomyLevel != null) {
			metaItem.setTaxonomyPath(taxonomyLevel.getMaterializedPathIdentifiers());
		}
		if (aiModule != null && StringHelper.containsNonWhitespace(aiModule.getMCGeneratorSpiId())) {
			metaItem.setAiProvider(AiQtiItemFactory.TOOL_PREFIX + aiModule.getMCGeneratorSpiId());
			metaItem.setAiModel(aiModule.getMCGeneratorModel());
			metaItem.setUnsupervisedAiGenerated(true);
		}
		metaItem.setEditor("OpenOLAT");
		metaItem.setEditorVersion(Settings.getVersion());
	}

	private void applyEssayMetadata(AssessmentItemAndMetadata metaItem, EssayItemDraft draft,
			EssayAiGrading grading, TaxonomyLevel taxonomyLevel) {
		if (draft != null && StringHelper.containsNonWhitespace(draft.learningObjective())) {
			metaItem.setTopic(StringHelper.xssScan(draft.learningObjective()));
		}
		if (taxonomyLevel != null) {
			metaItem.setTaxonomyPath(taxonomyLevel.getMaterializedPathIdentifiers());
		}
		if (grading != null && StringHelper.containsNonWhitespace(grading.getGeneratorSpi())) {
			metaItem.setAiProvider(AiQtiItemFactory.TOOL_PREFIX + grading.getGeneratorSpi());
			metaItem.setAiModel(grading.getGeneratorModel());
			metaItem.setUnsupervisedAiGenerated(true);
		}
		metaItem.setEditor("OpenOLAT");
		metaItem.setEditorVersion(Settings.getVersion());
	}
}
