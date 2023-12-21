/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.quality.generator.model;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.modules.quality.generator.QualityGeneratorOverride;
import org.olat.modules.quality.generator.QualityGeneratorOverrides;
import org.olat.modules.quality.generator.QualityGeneratorRef;

/**
 * 
 * Initial date: 11 Dec 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityGeneratorOverridesImpl implements QualityGeneratorOverrides {

	private final List<QualityGeneratorOverride> overrides;
	private Map<String, QualityGeneratorOverride> identToOverride;

	public QualityGeneratorOverridesImpl(List<QualityGeneratorOverride> overrides) {
		this.overrides = overrides;
	}

	@Override
	public QualityGeneratorOverride getOverride(String identitfier) {
		if (identToOverride == null) {
			identToOverride = overrides.stream()
					.collect(Collectors.toMap(QualityGeneratorOverride::getIdentifier, Function.identity()));
		}
		return identToOverride.getOrDefault(identitfier, null);
	}

	@Override
	public List<QualityGeneratorOverride> getOverrides(QualityGeneratorRef generator, Date fromDate, Date toDate) {
		return overrides.stream()
			.filter(override -> override.getGenerator() != null)
			.filter(override -> override.getGenerator().getKey().equals(generator.getKey()))
			.filter(override -> override.getStart() != null)
			.filter(override -> override.getStart().after(fromDate))
			.filter(override -> override.getStart().before(toDate))
			.collect(Collectors.toList());
	}

}
