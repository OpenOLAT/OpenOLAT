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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.CategoryToElement;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.ui.model.PageRow;

/**
 * 
 * Initial date: 22.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DeletedPageListController extends AbstractPageListController {

	public DeletedPageListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback) {
		super(ureq, wControl, stackPanel, secCallback, BinderConfiguration.createDeletedPagesConfig(), "deleted_pages", false);

		initForm(ureq);
		loadModel(null);
	}

	@Override
	protected void loadModel(String searchString) {
		Map<Long,Long> numberOfCommentsMap = portfolioService.getNumberOfCommentsOnOwnedPage(getIdentity());
		
		List<CategoryToElement> categorizedElements = portfolioService.getCategorizedOwnedPages(getIdentity());
		Map<OLATResourceable,List<Category>> categorizedElementMap = new HashMap<>();
		for(CategoryToElement categorizedElement:categorizedElements) {
			List<Category> categories = categorizedElementMap.get(categorizedElement.getCategorizedResource());
			if(categories == null) {
				categories = new ArrayList<>();
				categorizedElementMap.put(categorizedElement.getCategorizedResource(), categories);
			}
			categories.add(categorizedElement.getCategory());
		}

		List<Page> pages = portfolioService.searchDeletedPages(getIdentity(), searchString);
		List<PageRow> rows = new ArrayList<>(pages.size());
		for (Page page : pages) {
			rows.add(forgeRow(page, null, null, false, categorizedElementMap, numberOfCommentsMap));
		}

		model.setObjects(rows);
		tableEl.reset();
		tableEl.reloadData();
	}
}
