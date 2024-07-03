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
package org.olat.modules.topicbroker.manager;


import org.olat.basesecurity.IdentityImpl;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.topicbroker.TBAuditLog.TBFileAuditLog;
import org.olat.modules.topicbroker.TBCustomFieldType;
import org.olat.modules.topicbroker.model.TBBrokerImpl;
import org.olat.modules.topicbroker.model.TBCustomFieldDefinitionExport;
import org.olat.modules.topicbroker.model.TBCustomFieldDefinitionImpl;
import org.olat.modules.topicbroker.model.TBCustomFieldDefinitionsExport;
import org.olat.modules.topicbroker.model.TBCustomFieldImpl;
import org.olat.modules.topicbroker.model.TBParticipantImpl;
import org.olat.modules.topicbroker.model.TBProcessInfos;
import org.olat.modules.topicbroker.model.TBSelectionImpl;
import org.olat.modules.topicbroker.model.TBTopicImpl;
import org.olat.repository.RepositoryEntry;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TopicBrokerXStream {
	
	private static final XStream xstream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		Class<?>[] types = new Class[] {
				TBBrokerImpl.class,
				TBCustomFieldDefinitionImpl.class,
				TBCustomFieldType.class,
				TBCustomFieldImpl.class,
				TBParticipantImpl.class,
				TBTopicImpl.class,
				TBSelectionImpl.class,
				TBProcessInfos.class,
				TBFileAuditLog.class,
				TBCustomFieldDefinitionsExport.class,
				TBCustomFieldDefinitionExport.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		
		xstream.alias("TBBroker", TBBrokerImpl.class);
		xstream.omitField(TBBrokerImpl.class, "repositoryEntry");
		xstream.alias("TBCustomFieldDefinition", TBCustomFieldDefinitionImpl.class);
		xstream.alias("TBCustomFieldType", TBCustomFieldType.class);
		xstream.alias("TBCustomField", TBCustomFieldImpl.class);
		xstream.alias("TBTopic", TBTopicImpl.class);
		xstream.alias("TBParticipant", TBParticipantImpl.class);
		xstream.alias("TBSelection", TBSelectionImpl.class);
		
		xstream.alias("TBProcessInfos", TBProcessInfos.class);
		xstream.alias("TBFileAuditLog", TBFileAuditLog.class);
		
		xstream.alias("TBCustomFieldDefinitionsExport", TBCustomFieldDefinitionsExport.class);
		xstream.alias("TBCustomFieldDefinitionExport", TBCustomFieldDefinitionExport.class);
		xstream.addImplicitCollection(TBCustomFieldDefinitionsExport.class, "definitions");
		
		xstream.alias("RepositoryEntry", RepositoryEntry.class);
		xstream.omitField(RepositoryEntry.class, "olatResource");
		xstream.omitField(RepositoryEntry.class, "lifecycle");
		xstream.omitField(RepositoryEntry.class, "statistics");
		xstream.omitField(RepositoryEntry.class, "deletionDate");
		xstream.omitField(RepositoryEntry.class, "deletedBy");
		xstream.omitField(RepositoryEntry.class, "groups");
		xstream.omitField(RepositoryEntry.class, "organisations");
		xstream.omitField(RepositoryEntry.class, "taxonomyLevels");
		xstream.omitField(RepositoryEntry.class, "educationalType");
		xstream.omitField(RepositoryEntry.class, "runtimeType");
		
		xstream.alias("Identity", IdentityImpl.class);
		xstream.omitField(IdentityImpl.class, "version");
		xstream.omitField(IdentityImpl.class, "creationDate");
		xstream.omitField(IdentityImpl.class, "plannedInactivationDate");
		xstream.omitField(IdentityImpl.class, "lastLogin");
		xstream.omitField(IdentityImpl.class, "status");
		xstream.omitField(IdentityImpl.class, "user");
	}
	
	public static String toXml(Object obj) {
		return xstream.toXML(obj);
	}
	
	@SuppressWarnings("unchecked")
	public static <U> U fromXml(String xml, @SuppressWarnings("unused") Class<U> cl) {
		Object obj = xstream.fromXML(xml);
		return (U)obj;
	}
	
}
