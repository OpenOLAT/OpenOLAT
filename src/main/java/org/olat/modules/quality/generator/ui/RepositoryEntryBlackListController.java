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
package org.olat.modules.quality.generator.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 27 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryBlackListController extends RepositoryEntryListController {
	
	private static final String CONFIG_KEY = "course.black.list";

	public RepositoryEntryBlackListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			QualityGeneratorConfigs configs) {
		super(ureq, wControl, stackPanel, configs);
	}

	@Override
	protected String getConfigKey() {
		return CONFIG_KEY;
	}

	@Override
	protected String getTablePrefKey() {
		return "quality-re-black-list";
	}
	
	public static List<RepositoryEntryRef> getRepositoryEntryRefs(QualityGeneratorConfigs generatorConfigs) {
		return RepositoryEntryListController.getRepositoryEntryRefs(generatorConfigs, CONFIG_KEY);
	}
	
	public static void setRepositoryEntryRefs(QualityGeneratorConfigs generatorConfigs, List<? extends RepositoryEntryRef> entries) {
		RepositoryEntryListController.setRepositoryEntryRef(generatorConfigs, entries, CONFIG_KEY);
	}
	
	public static void addRepositoryEntryRef(QualityGeneratorConfigs generatorConfigs, RepositoryEntryRef entry) {
		RepositoryEntryListController.addRepositoryEntryRef(generatorConfigs, entry, CONFIG_KEY);
	}
	
	public static void removeRepositoryEntryRef(QualityGeneratorConfigs generatorConfigs, RepositoryEntryRef entry) {
		RepositoryEntryListController.removeRepositoryEntryRefs(generatorConfigs, entry, CONFIG_KEY);
	}

}
