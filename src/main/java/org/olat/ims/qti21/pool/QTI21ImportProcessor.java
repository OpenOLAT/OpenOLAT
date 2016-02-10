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
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti.questionimport.ItemAndMetadata;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.manager.QEducationalContextDAO;
import org.olat.modules.qpool.manager.QItemTypeDAO;
import org.olat.modules.qpool.manager.QLicenseDAO;
import org.olat.modules.qpool.manager.QPoolFileStorage;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.olat.modules.qpool.manager.TaxonomyLevelDAO;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QuestionItemImpl;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;

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
	private final QuestionItemDAO questionItemDao;
	private final QPoolFileStorage qpoolFileStorage;
	
	public QTI21ImportProcessor(Identity owner, Locale defaultLocale, String filename, File file,
			QuestionItemDAO questionItemDao, QItemTypeDAO qItemTypeDao, QEducationalContextDAO qEduContextDao,
			TaxonomyLevelDAO taxonomyLevelDao, QLicenseDAO qLicenseDao, QPoolFileStorage qpoolFileStorage,
			DB dbInstance) {
		this.owner = owner;
		this.defaultLocale = defaultLocale;
		this.qItemTypeDao = qItemTypeDao;
		this.questionItemDao = questionItemDao;
		this.qpoolFileStorage = qpoolFileStorage;
	}

	public List<QuestionItem> process() {
		return null;
	}
	

	protected QuestionItemImpl processItem(AssessmentItem assessmentItem, String comment, String originalItemFilename,
			String editor, String editorVersion, ItemAndMetadata metadata) {
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
		//processItemMetadata(poolItem, itemEl);
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
	

}
