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
package org.olat.modules.quality.manager;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.ui.QualityToDoEditController;
import org.olat.modules.quality.ui.QualityUIFactory;
import org.olat.modules.todo.ToDoContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EvaluationFormSessionToDoTaskProvider extends QualityToDoTaskProvider {

	public static final String TYPE = "quality.evaluation.form.session";
	
	@Autowired
	private QualityDataCollectionDAO dataCollectionDao;
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getDisplayName(Locale locale) {
		return Util.createPackageTranslator(QualityUIFactory.class, locale).translate("todo.type.session");
	}
	
	@Override
	public QualityToDoEditController createCreateController(UserRequest ureq, WindowControl wControl, Identity doer,
			Long originId, String originSubPath) {
		QualityDataCollection dataCollection = dataCollectionDao.loadDataCollectionByKey(() -> originId);
		ToDoContext dataCollectionContext = ToDoContext.of(DataCollectionToDoTaskProvider.TYPE, originId, null, dataCollection.getTitle());
		ToDoContext currentContext = ToDoContext.of(TYPE, originId, originSubPath, dataCollection.getTitle());
		Collection<ToDoContext> availableContexts = List.of(dataCollectionContext, currentContext);
		return new QualityToDoEditController(ureq, wControl, originId, originSubPath, availableContexts, currentContext);
	}
	
}
