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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorProvider;
import org.olat.modules.quality.generator.provider.fallback.FallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityGeneratorProviderFactory {
	
	private static final String FALLBACK_PROVIDER_TYPE = FallbackProvider.TYPE;
	
	@Autowired
	private List<QualityGeneratorProvider> loadedProviders;
	private Map<String, QualityGeneratorProvider> providers = new HashMap<>();
	private QualityGeneratorProvider fallbackProvider;
	
	@PostConstruct
	void initProviders() {
		for (QualityGeneratorProvider loadedProvider: loadedProviders) {
			if (FALLBACK_PROVIDER_TYPE.equals(loadedProvider.getType())) {
				fallbackProvider = loadedProvider;
			} else {
				providers.put(loadedProvider.getType(), loadedProvider);
			}
		}
	}
	
	public QualityGeneratorProvider getProvider(String type) {
		QualityGeneratorProvider provider = providers.get(type);
		if (provider == null) {
			provider = fallbackProvider;
		}
		return provider;
	}
	
	public Collection<QualityGeneratorProvider> getProviders() {
		return providers.values();
	}
	
	boolean isAvailable(QualityGenerator generator) {
		QualityGeneratorProvider provider = getProvider(generator.getType());
		if (FALLBACK_PROVIDER_TYPE.equals(provider.getType())) {
			return false;
		}
		return true;
	}

}
