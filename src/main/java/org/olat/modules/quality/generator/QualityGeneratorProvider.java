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
package org.olat.modules.quality.generator;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
import org.olat.modules.quality.ui.security.GeneratorSecurityCallback;

/**
 * 
 * Initial date: 06.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */	
public interface QualityGeneratorProvider {
	
	public String getType();
	
	public String getDisplayname(Locale locale);
	
	public QualityDataCollectionTopicType getGeneratedTopicType(QualityGeneratorConfigs configs);
	
	public ProviderConfigController getConfigController(UserRequest ureq, WindowControl wControl, Form mainForm,
			QualityGeneratorConfigs configs);
	
	public String getEnableInfo(QualityGenerator generator, QualityGeneratorConfigs configs, QualityGeneratorOverrides overrides,
			Date fromDate, Date toDate, Locale locale);

	public boolean hasWhiteListController();

	public Controller getWhiteListController(UserRequest ureq, WindowControl wControl,
			GeneratorSecurityCallback secCallback, TooledStackedPanel stackPanel, QualityGenerator generator,
			QualityGeneratorConfigs configs);
	
	public boolean hasBlackListController();

	public Controller getBlackListController(UserRequest ureq, WindowControl wControl,
			GeneratorSecurityCallback secCallback, TooledStackedPanel stackPanel, QualityGenerator generator,
			QualityGeneratorConfigs configs);

	public List<QualityDataCollection> generate(QualityGenerator generator, QualityGeneratorConfigs configs,
			QualityGeneratorOverrides overrides, Date fromDate, Date toDate);

	public List<QualityPreview> getPreviews(QualityGenerator generator, QualityGeneratorConfigs configs,
			QualityGeneratorOverrides overrides, GeneratorPreviewSearchParams previewSearchParams);

	public String getAddToBlacklistConfirmationMessage(Locale locale, QualityPreview preview);

	public void addToBlacklist(QualityGeneratorConfigs configs, QualityPreview preview);
	
	public String getRemoveFromBlacklistConfirmationMessage(Locale locale, QualityPreview preview);

	public void removeFromBlacklist(QualityGeneratorConfigs configs, QualityPreview preview);

}
