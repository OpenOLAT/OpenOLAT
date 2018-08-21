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
package org.olat.modules.quality.generator.provider.fallback;

import java.util.Date;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorProvider;
import org.olat.modules.quality.generator.ui.AbstractGeneratorEditController;
import org.olat.modules.quality.generator.ui.GeneratorListController;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class FallbackProvider implements QualityGeneratorProvider {

	public static final String TYPE = "fallback";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getDisplayname(Locale locale) {
		Translator translator = Util.createPackageTranslator(GeneratorListController.class, locale);
		return translator.translate("provider.fallback.display.name");
	}

	@Override
	public ProviderConfigController getConfigController(UserRequest ureq, WindowControl wControl, Form mainForm, QualityGeneratorConfigs configs) {
		return new FallbackProviderController(ureq, wControl, mainForm);
	}

	@Override
	public String getEnableInfo(QualityGenerator generator, QualityGeneratorConfigs configs, Date fromDate,
			Date toDate, Locale locale) {
		return "";
	}

	@Override
	public boolean hasWhiteListController() {
		return false;
	}

	@Override
	public AbstractGeneratorEditController getWhiteListController(UserRequest ureq, WindowControl wControl,
			QualitySecurityCallback secCallback, TooledStackedPanel stackPanel, QualityGenerator generator,
			QualityGeneratorConfigs configs) {
		return null;
	}

	@Override
	public void generate(QualityGenerator generator, QualityGeneratorConfigs configs, Date fromDate, Date toDate) {
		//
	}

}
