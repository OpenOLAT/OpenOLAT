/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.manager.PositionDAO;
import org.olat.modules.selectus.model.ApplicationAttributeLight;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;
import org.olat.modules.selectus.model.attributes.AttributeConfiguration;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionComparator;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * Initial date: 4 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AbstractReportGenerator {
	
	protected final List<ApplicationAttributeLight> globalAttributes;
	protected final List<PositionAttributeDefinition> globalAttributesDefinitions;
	protected final List<PositionAttributeDefinitionConfiguration> globalAttributesConfigurations;
	
	@Autowired
	protected PositionDAO positionDao;
	@Autowired
	protected TaggingService taggingService;
	@Autowired
	protected RecruitingService recruitingService;
	@Autowired
	protected RecruitingModule recruitingModule;
	@Autowired @Qualifier("salutationGenerator")
	protected SalutationGenerator salutationGenerator;
	
	AbstractReportGenerator() {
		CoreSpringFactory.autowireObject(this);
		
		globalAttributes = positionDao.getGlobalAttributes();
		
		List<PositionAttributeDefinition> definitions = recruitingService.getGlobalAttributeDefinition();
		globalAttributesDefinitions = new ArrayList<>(definitions.size());
		for(PositionAttributeDefinition definition:definitions) {
			if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.date
					|| definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.number
					|| definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.percentage
					|| definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.question
					|| definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.select) {
				globalAttributesDefinitions.add(definition);
			}
		}
		
		if(globalAttributesDefinitions.size() > 1) {
			Collections.sort(globalAttributesDefinitions, new PositionAttributeDefinitionComparator());
		}
		
		globalAttributesConfigurations = new ArrayList<>();
		for(PositionAttributeDefinition attributeDefinition:globalAttributesDefinitions) {
			AttributeConfiguration config = attributeDefinition.getConfiguration(AttributeConfiguration.class);
			globalAttributesConfigurations.add(new PositionAttributeDefinitionConfiguration(attributeDefinition,
					attributeDefinition.getTypeEnum(), config));
		}
	}

}
