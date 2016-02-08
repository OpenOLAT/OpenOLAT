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
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.manager.QEducationalContextDAO;
import org.olat.modules.qpool.manager.QItemTypeDAO;
import org.olat.modules.qpool.manager.QLicenseDAO;
import org.olat.modules.qpool.manager.QPoolFileStorage;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.olat.modules.qpool.manager.TaxonomyLevelDAO;

/**
 * 
 * Initial date: 05.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ImportProcessor {
	
	public QTI21ImportProcessor(Identity owner, Locale defaultLocale, String filename, File file,
			QuestionItemDAO questionItemDao, QItemTypeDAO qItemTypeDao, QEducationalContextDAO qEduContextDao,
			TaxonomyLevelDAO taxonomyLevelDao, QLicenseDAO qLicenseDao, QPoolFileStorage qpoolFileStorage,
			DB dbInstance) {
		// TODO Auto-generated constructor stub
	}

	public List<QuestionItem> process() {
		return null;
	}

}
