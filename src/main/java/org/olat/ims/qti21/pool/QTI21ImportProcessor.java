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
package org.olat.ims.qti21.pool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti.qpool.QTIMetadataConverter;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemMetadata;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.TaxonomyLevel;
import org.olat.modules.qpool.manager.QEducationalContextDAO;
import org.olat.modules.qpool.manager.QItemTypeDAO;
import org.olat.modules.qpool.manager.QLicenseDAO;
import org.olat.modules.qpool.manager.QPoolFileStorage;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.olat.modules.qpool.manager.TaxonomyLevelDAO;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QLicense;
import org.olat.modules.qpool.model.QuestionItemImpl;

import uk.ac.ed.ph.jqtiplus.node.content.xhtml.image.Img;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.object.Object;

/**
 * 
 * Initial date: 05.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ImportProcessor {
	
	private final Identity owner;
	private final Locale defaultLocale;
	
	private final QItemTypeDAO qItemTypeDao;
	private final QLicenseDAO qLicenseDao;
	private final QuestionItemDAO questionItemDao;
	private final QPoolFileStorage qpoolFileStorage;
	private final TaxonomyLevelDAO taxonomyLevelDao;
	private final QEducationalContextDAO qEduContextDao;
	
	public QTI21ImportProcessor(Identity owner, Locale defaultLocale,
			QuestionItemDAO questionItemDao, QItemTypeDAO qItemTypeDao, QEducationalContextDAO qEduContextDao,
			TaxonomyLevelDAO taxonomyLevelDao, QLicenseDAO qLicenseDao, QPoolFileStorage qpoolFileStorage) {
		this.owner = owner;
		this.defaultLocale = defaultLocale;
		this.qLicenseDao = qLicenseDao;
		this.qItemTypeDao = qItemTypeDao;
		this.qEduContextDao = qEduContextDao;
		this.questionItemDao = questionItemDao;
		this.qpoolFileStorage = qpoolFileStorage;
		this.taxonomyLevelDao = taxonomyLevelDao;
	}

	public List<QuestionItem> process(File file) {
		
		//export zip file
		
		//metadata copy in question
		
		
		
		return null;
	}
	
	protected List<String> getMaterials(AssessmentItem item) {
		List<String> materials = new ArrayList<>();
		QueryUtils.search(Img.class, item).forEach((img) -> {
			if(img.getSrc() != null) {
				materials.add(img.getSrc().toString());
			}
		});

		QueryUtils.search(Object.class, item).forEach((object) -> {
			if(StringHelper.containsNonWhitespace(object.getData())) {
				materials.add(object.getData());
			}
		});
		return materials;
	}

	protected QuestionItemImpl processItem(AssessmentItem assessmentItem, String comment, String originalItemFilename,
			String editor, String editorVersion, AssessmentItemMetadata metadata) {
		//filename
		String filename;
		String ident = assessmentItem.getIdentifier();
		if(originalItemFilename != null) {
			filename = originalItemFilename;
		} else if(StringHelper.containsNonWhitespace(ident)) {
			filename = StringHelper.transformDisplayNameToFileSystemName(ident) + ".xml";
		} else {
			filename = "item.xml";
		}
		String dir = qpoolFileStorage.generateDir();
		
		//title
		String title = assessmentItem.getTitle();
		if(!StringHelper.containsNonWhitespace(title)) {
			title = assessmentItem.getLabel();
		}
		if(!StringHelper.containsNonWhitespace(title)) {
			title = ident;
		}

		QuestionItemImpl poolItem = questionItemDao.create(title, QTI21Constants.QTI_21_FORMAT, dir, filename);
		//description
		poolItem.setDescription(comment);
		//language from default
		poolItem.setLanguage(defaultLocale.getLanguage());
		//question type first
		if(StringHelper.containsNonWhitespace(editor)) {
			poolItem.setEditor(editor);
			poolItem.setEditorVersion(editorVersion);
		}
		//if question type not found, can be overridden by the metadatas
		processItemMetadata(poolItem, metadata);
		if(poolItem.getType() == null) {
			QItemType defType = qItemTypeDao.loadByType(QuestionType.UNKOWN.name());
			poolItem.setType(defType);
		}
		/*if(docInfos != null) {
			processSidecarMetadata(poolItem, docInfos);
		}*/
		if(metadata != null) {
			//processItemMetadata(poolItem, metadata);
		}
		questionItemDao.persist(owner, poolItem);
		return poolItem;
	}
	
	protected void processItemMetadata(QuestionItemImpl poolItem, AssessmentItemMetadata metadata) {
		//non heuristic set of question type
		String typeStr = null;	
		QTI21QuestionType questionType = metadata.getQuestionType();
		if(questionType != null && questionType.getPoolQuestionType() != null) {
			typeStr = questionType.getPoolQuestionType().name();
		}
		if(typeStr != null) {
			QItemType type = qItemTypeDao.loadByType(typeStr);
			if(type != null) {
				poolItem.setType(type);
			}
		}
				
		String coverage = metadata.getCoverage();
		if(StringHelper.containsNonWhitespace(coverage)) {
			poolItem.setCoverage(coverage);
		}
		
		String language = metadata.getLanguage();
		if(StringHelper.containsNonWhitespace(language)) {
			poolItem.setLanguage(language);
		}
		
		String keywords = metadata.getKeywords();
		if(StringHelper.containsNonWhitespace(keywords)) {
			poolItem.setKeywords(keywords);
		}
		
		String taxonomyPath = metadata.getTaxonomyPath();
		if(StringHelper.containsNonWhitespace(taxonomyPath)) {
			QTIMetadataConverter converter = new QTIMetadataConverter(qItemTypeDao, qLicenseDao, taxonomyLevelDao, qEduContextDao);
			TaxonomyLevel taxonomyLevel = converter.toTaxonomy(taxonomyPath);
			poolItem.setTaxonomyLevel(taxonomyLevel);
		}
		
		String level = metadata.getLevel();
		if(StringHelper.containsNonWhitespace(level)) {
			QTIMetadataConverter converter = new QTIMetadataConverter(qItemTypeDao, qLicenseDao, taxonomyLevelDao, qEduContextDao);
			QEducationalContext educationalContext = converter.toEducationalContext(level);
			poolItem.setEducationalContext(educationalContext);
		}
				
		String time = metadata.getTypicalLearningTime();
		if(StringHelper.containsNonWhitespace(time)) {
			poolItem.setEducationalLearningTime(time);
		}
		
		String editor = metadata.getEditor();
		if(StringHelper.containsNonWhitespace(editor)) {
			poolItem.setEditor(editor);
		}
		
		String editorVersion = metadata.getEditorVersion();
		if(StringHelper.containsNonWhitespace(editorVersion)) {
			poolItem.setEditorVersion(editorVersion);
		}
		
		int numOfAnswerAlternatives = metadata.getNumOfAnswerAlternatives();
		if(numOfAnswerAlternatives > 0) {
			poolItem.setNumOfAnswerAlternatives(numOfAnswerAlternatives);
		}
		
		poolItem.setDifficulty(metadata.getDifficulty());
		poolItem.setDifferentiation(metadata.getDifferentiation());
		poolItem.setStdevDifficulty(metadata.getStdevDifficulty());
		
		String license = metadata.getLicense();
		if(StringHelper.containsNonWhitespace(license)) {
			QTIMetadataConverter converter = new QTIMetadataConverter(qItemTypeDao, qLicenseDao, taxonomyLevelDao, qEduContextDao);
			QLicense qLicense = converter.toLicense(license);
			poolItem.setLicense(qLicense);
		}
		
	}
}
