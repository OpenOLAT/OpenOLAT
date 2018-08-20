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
package org.olat.modules.quality.generator.manager;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.StringHelper;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfig;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityGeneratorConfigsImpl implements QualityGeneratorConfigs {

	private final QualityGenerator generator;
	private final List<QualityGeneratorConfig> configs;

	@Autowired
	private QualityGeneratorService generatorService;
	
	public QualityGeneratorConfigsImpl(QualityGenerator generator) {
		CoreSpringFactory.autowireObject(this);
		this.generator = generator;
		this.configs = generatorService.loadGeneratorConfigs(generator);
	}

	@Override
	public String getValue(String identifier) {
		for (QualityGeneratorConfig config: configs) {
			if (identifier.equals(config.getIdentifier())) {
				return config.getValue();
			}
		}
		return null;
	}
	
	@Override
	public void setValue(String identifier, String value) {
		if (isDeletable(value)) {
			deleteConfig(identifier);
		} else if (isUpdateable(identifier)) {
			updateConfig(identifier, value);
		} else {
			createConfig(identifier, value);
		}
	}
	
	private boolean isDeletable(String value) {
		return !StringHelper.containsNonWhitespace(value);
	}

	private void deleteConfig(String identifier) {
		int configIndex = getConfigIndex(identifier);
		if (configIndex > -1) {
			QualityGeneratorConfig config = configs.get(configIndex);
			generatorService.deleteConfig(config);
			configs.remove(configIndex);
		}
	}

	private boolean isUpdateable(String identifier) {
		return getConfigIndex(identifier) > -1;
	}

	private void updateConfig(String identifier, String value) {
		int configIndex = getConfigIndex(identifier);
		if (configIndex > -1) {
			QualityGeneratorConfig config = configs.get(configIndex);
			if (!config.getValue().equals(value)) {
				config.setValue(value);
				config = generatorService.updateGeneratorConfig(config);
				configs.set(configIndex, config);
			}
		}
	}

	private void createConfig(String identifier, String value) {
		QualityGeneratorConfig config = generatorService.createGeneratorConfig(generator, identifier, value);
		configs.add(config);
	}
	
	private int getConfigIndex(String identifier) {
		for (int index = 0; index < configs.size(); index++) {
			QualityGeneratorConfig config = configs.get(index);
			if (identifier.equals(config.getIdentifier())) {
				return index;
			}
		}
		return -1;
	}
	
}
