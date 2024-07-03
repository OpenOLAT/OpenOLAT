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
package org.olat.modules.topicbroker.manager;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBBrokerRef;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionSearchParams;
import org.olat.modules.topicbroker.TopicBrokerExportService;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.model.TBCustomFieldDefinitionExport;
import org.olat.modules.topicbroker.model.TBCustomFieldDefinitionsExport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 1 Jul 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class TopicBrokerExportServiceImpl implements TopicBrokerExportService {
	
	@Autowired
	private TopicBrokerService topicBrokerService;
	
	@Override
	public void createCustomFieldDefinitions(Identity doer, TBBroker broker, String customFieldDefinitionsXml) {
		if (!StringHelper.containsNonWhitespace(customFieldDefinitionsXml)) {
			return;
		}
		
		TBCustomFieldDefinitionsExport definitionsExport = TopicBrokerXStream.fromXml(customFieldDefinitionsXml, TBCustomFieldDefinitionsExport.class);
		if (definitionsExport == null) {
			return;
		}
		
		definitionsExport.getDefinitions().forEach(export -> {
			TBCustomFieldDefinition definition = topicBrokerService.createCustomFieldDefinition(doer, broker);
			topicBrokerService.updateCustomFieldDefinition(doer, definition, export.getIdentifier(), export.getName(),
					export.getType(), export.isDisplayInTable());
		});
	}

	@Override
	public String getCustomFieldDefinitionExportXml(TBBrokerRef broker) {
		TBCustomFieldDefinitionSearchParams searchParams = new TBCustomFieldDefinitionSearchParams();
		searchParams.setBroker(broker);
		List<TBCustomFieldDefinition> customFieldDefinitions = topicBrokerService.getCustomFieldDefinitions(searchParams);
		if (customFieldDefinitions.isEmpty()) {
			return null;
		}
		
		List<TBCustomFieldDefinitionExport> export = customFieldDefinitions.stream()
			.map(TBCustomFieldDefinitionExport::of)
			.toList();
		TBCustomFieldDefinitionsExport definitionsExport = new TBCustomFieldDefinitionsExport();
		definitionsExport.setDefinitions(export);
		
		return TopicBrokerXStream.toXml(definitionsExport);
	}

}
