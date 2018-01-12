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
package org.olat.modules.qpool.manager;

import java.io.File;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * When a identity is deleted, the BaseSecurityManager removes the identity from
 * all SecurityGroups. As a result of this, the identity is no longer the author
 * of his question items. If the identity was the only author of a question
 * item, the question item has no author anymore. This class deletes the
 * question items before the deletion of the identity, if it is the only author
 * of the question. The question items are only deleted, if the appropriate
 * option is enabled.
 * 
 * Initial date: 11.01.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QuestionPoolUserDataDeletable implements UserDataDeletable {

	private static final OLog log = Tracing.createLoggerFor(QuestionPoolUserDataDeletable.class);

	@Autowired
	private QuestionPoolModule qpoolModule;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QuestionItemDAO questionItemDao;
	
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName, File archivePath) {
		if (!qpoolModule.isDeleteQuestionsWithoutAuthor()) return;
			
		List<QuestionItemShort> itemsWithOneAuthor = questionItemDao.getItemsWithOneAuthor(identity);
		qpoolService.deleteItems(itemsWithOneAuthor);
		
		String logMessage = getLogMessage(identity, itemsWithOneAuthor);
		log.info(logMessage);
	}

	private String getLogMessage(Identity identity, List<QuestionItemShort> items) {
		return new StringBuilder()
				.append("Deleted ")
				.append(items.size())
				.append(" question items form the question pool after the deletion of identity ")
				.append(identity)
				.append(".")
				.toString();
	}

}
