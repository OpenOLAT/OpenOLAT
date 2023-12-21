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

import java.util.List;

/**
 * 
 * Initial date: 11 Dec 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GeneratorOverrideSearchParams {
	
	private List<Long> generatorKeys;
	private Boolean dataCollectionCreated = Boolean.FALSE;
	
	public List<Long> getGeneratorKeys() {
		return generatorKeys;
	}
	
	public void setGeneratorKeys(List<Long> generatorKeys) {
		this.generatorKeys = generatorKeys;
	}
	
	public void setGenerators(List<? extends QualityGeneratorRef> generators) {
		this.generatorKeys = generators != null? generators.stream().map(QualityGeneratorRef::getKey).toList(): null;
	}
	
	public void setGenerator(QualityGeneratorRef generator) {
		this.generatorKeys = generator != null? List.of(generator.getKey()): null;
	}
	
	public Boolean getDataCollectionCreated() {
		return dataCollectionCreated;
	}
	
	public void setDataCollectionCreated(Boolean dataCollectionCreated) {
		this.dataCollectionCreated = dataCollectionCreated;
	}

}
