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
package org.olat.modules.quality.generator;

import java.util.Date;
import java.util.List;

import org.olat.modules.quality.generator.model.QualityGeneratorOverridesImpl;

/**
 * 
 * Initial date: 11 Dec 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface QualityGeneratorOverrides {
	
	public static final QualityGeneratorOverrides NO_OVERRIDES = new QualityGeneratorOverridesImpl(List.of());
	
	public QualityGeneratorOverride getOverride(String identitfier);

	public List<QualityGeneratorOverride> getOverrides(QualityGeneratorRef generator, Date fromDate, Date toDate);

}
