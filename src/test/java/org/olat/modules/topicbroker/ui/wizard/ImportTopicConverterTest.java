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
package org.olat.modules.topicbroker.ui.wizard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicExecutionType;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.model.TBImportTopic;
import org.olat.modules.topicbroker.model.TBTransientBroker;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jun 18, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ImportTopicConverterTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TopicBrokerService topicBrokerService;

	@Test
	public void shouldConvertInput() {
		String input = "666	Lab A	Discover the secrets of life under the microscope!	6	66	6/18/25	3/19/29					";
		
		ImportTopicConverter converter = createConverter(input);
		List<TBImportTopic> topics = converter.getTopics();
		
		assertThat(topics).hasSize(1);
		TBImportTopic importTopic = topics.get(0);
		TBTopic topic = importTopic.getTopic();
		assertThat(importTopic.getIdentifier()).isEqualTo("666");
		assertThat(topic.getIdentifier()).isEqualTo("666");
		assertThat(importTopic.getTitle()).isEqualTo("Lab A");
		assertThat(topic.getTitle()).isEqualTo("Lab A");
		assertThat(importTopic.getDescription()).isEqualTo("Discover the secrets of life under the microscope!");
		assertThat(topic.getDescription()).isEqualTo("Discover the secrets of life under the microscope!");
		assertThat(importTopic.getMinParticipants()).isEqualTo("6");
		assertThat(topic.getMinParticipants()).isEqualTo(6);
		assertThat(importTopic.getMaxParticipants()).isEqualTo("66");
		assertThat(topic.getMaxParticipants()).isEqualTo(66);
		assertThat(importTopic.getBeginDate()).isEqualTo("6/18/25");
		assertThat(topic.getBeginDate()).isEqualTo(DateUtils.toDate(LocalDate.of(2025, 6, 18)));
		assertThat(importTopic.getEndDate()).isEqualTo("3/19/29");
		assertThat(topic.getEndDate()).isEqualTo(DateUtils.toDate(LocalDate.of(2029, 3, 19)));
		assertThat(importTopic.getGroupRestrictions()).isNull();
		assertThat(topic.getGroupRestrictionKeys()).isNull();
		assertThat(importTopic.getMessage()).isNull();
	}
	
	@Test
	public void shouldValidateIdentifier_mandatory() {
		String input = "		B	C	1	2	6/18/25		3/19/29				";
		
		ImportTopicConverter converter = createConverter(input);
		List<TBImportTopic> topics = converter.getTopics();
		
		assertThat(topics.get(0).getIdentifier()).isBlank();
		assertThat(topics.get(0).getTopic().getIdentifier()).isNull();
		assertThat(topics.get(0).getMessage()).isNotNull();
	}
	
	@Test
	public void shouldValidateIdentifier_too_long() {
		String identifier = Stream.generate(() -> "A").limit(TBTopic.IDENTIFIER_MAX_LENGTH + 1).collect(Collectors.joining());
		String input = identifier + "	B	C	1	2							";
		
		ImportTopicConverter converter = createConverter(input);
		List<TBImportTopic> topics = converter.getTopics();
		
		assertThat(topics.get(0).getIdentifier()).isEqualTo(identifier);
		assertThat(topics.get(0).getTopic().getIdentifier()).isNull();
		assertThat(topics.get(0).getMessage()).isNotNull();
	}
	
	@Test
	public void shouldValidateIdentifier_notValid() {
		String identifier = "Identifer???";
		String input = identifier + "	B	C	1	2							";
		
		ImportTopicConverter converter = createConverter(input);
		List<TBImportTopic> topics = converter.getTopics();
		
		assertThat(topics.get(0).getIdentifier()).isEqualTo(identifier);
		assertThat(topics.get(0).getTopic().getIdentifier()).isNull();
		assertThat(topics.get(0).getMessage()).isNotNull();
	}
	
	@Test
	public void shouldValidateTitle_mandatory() {
		String input = "A		C	1	2	6/18/25		3/19/29				";
		
		ImportTopicConverter converter = createConverter(input);
		List<TBImportTopic> topics = converter.getTopics();
		
		assertThat(topics.get(0).getTitle()).isBlank();
		assertThat(topics.get(0).getTopic().getTitle()).isNull();
		assertThat(topics.get(0).getMessage()).isNotNull();
	}
	
	@Test
	public void shouldValidateMinParticipants_mandatory() {
		String input = "A	B	C		2	6/18/25		3/19/29				";
		
		ImportTopicConverter converter = createConverter(input);
		List<TBImportTopic> topics = converter.getTopics();
		
		assertThat(topics.get(0).getMinParticipants()).isBlank();
		assertThat(topics.get(0).getTopic().getMinParticipants()).isNull();
		assertThat(topics.get(0).getMessage()).isNotNull();
	}
	
	@Test
	public void shouldValidateMinParticipants_integer() {
		String input = "A	B	C	1a	2	6/18/25		3/19/29				";
		
		ImportTopicConverter converter = createConverter(input);
		List<TBImportTopic> topics = converter.getTopics();
		
		assertThat(topics.get(0).getMinParticipants()).isEqualTo("1a");
		assertThat(topics.get(0).getTopic().getMinParticipants()).isNull();
		assertThat(topics.get(0).getMessage()).isNotNull();
	}
	
	@Test
	public void shouldValidateMinParticipants_positive() {
		String input = "A	B	C	-1	2	6/18/25		3/19/29				";
		
		ImportTopicConverter converter = createConverter(input);
		List<TBImportTopic> topics = converter.getTopics();
		
		assertThat(topics.get(0).getMinParticipants()).isEqualTo("-1");
		assertThat(topics.get(0).getTopic().getMinParticipants()).isNull();
		assertThat(topics.get(0).getMessage()).isNotNull();
	}
	
	@Test
	public void shouldValidateMaxParticipants_mandatory() {
		String axput = "A	B	C	1		6/18/25		3/19/29				";
		
		ImportTopicConverter converter = createConverter(axput);
		List<TBImportTopic> topics = converter.getTopics();
		
		assertThat(topics.get(0).getMaxParticipants()).isBlank();
		assertThat(topics.get(0).getTopic().getMaxParticipants()).isNull();
		assertThat(topics.get(0).getMessage()).isNotNull();
	}
	
	@Test
	public void shouldValidateMaxParticipants_axteger() {
		String axput = "A	B	C	1	2a	6/18/25		3/19/29				";
		
		ImportTopicConverter converter = createConverter(axput);
		List<TBImportTopic> topics = converter.getTopics();
		
		assertThat(topics.get(0).getMaxParticipants()).isEqualTo("2a");
		assertThat(topics.get(0).getTopic().getMaxParticipants()).isNull();
		assertThat(topics.get(0).getMessage()).isNotNull();
	}
	
	@Test
	public void shouldValidateMaxParticipants_positive() {
		String axput = "A	B	C	1	-2	6/18/25		3/19/29				";
		
		ImportTopicConverter converter = createConverter(axput);
		List<TBImportTopic> topics = converter.getTopics();
		
		assertThat(topics.get(0).getMaxParticipants()).isEqualTo("-2");
		assertThat(topics.get(0).getTopic().getMaxParticipants()).isNull();
		assertThat(topics.get(0).getMessage()).isNotNull();
	}
	
	@Test
	public void shouldValidateTitle_too_long() {
		String title = Stream.generate(() -> "A").limit(TBTopic.TITLE_MAX_LENGTH + 1).collect(Collectors.joining());
		String input = "A	" + title + "	C	1	2	6/18/a25						";
		
		ImportTopicConverter converter = createConverter(input);
		List<TBImportTopic> topics = converter.getTopics();
		
		assertThat(topics.get(0).getTitle()).isEqualTo(title);
		assertThat(topics.get(0).getTopic().getTitle()).isNull();
		assertThat(topics.get(0).getMessage()).isNotNull();
	}
	
	@Test
	public void shouldValidateBeginDate_wrong_format() {
		String input = "A	B	C	1	2	6/18/a25						";
		
		ImportTopicConverter converter = createConverter(input);
		List<TBImportTopic> topics = converter.getTopics();
		
		assertThat(topics.get(0).getBeginDate()).isEqualTo("6/18/a25");
		assertThat(topics.get(0).getTopic().getBeginDate()).isNull();
		assertThat(topics.get(0).getMessage()).isNotNull();
	}
	
	@Test
	public void shouldValidateEndDate_wrong_format() {
		String input = "A	B	C	1	2		6/18/a25					";
		
		ImportTopicConverter converter = createConverter(input);
		List<TBImportTopic> topics = converter.getTopics();
		
		assertThat(topics.get(0).getEndDate()).isEqualTo("6/18/a25");
		assertThat(topics.get(0).getTopic().getEndDate()).isNull();
		assertThat(topics.get(0).getMessage()).isNotNull();
	}
	
	@Test
	public void shouldInformIgnoredPeriod_datesTopic() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		TBBroker broker = topicBrokerService.createBroker(identity, repositoryEntry, random());
		TBTopic datesTopic = topicBrokerService.createTopic(identity, broker);
		topicBrokerService.updateTopic(identity, datesTopic, "DATES", random(), null, TBTopicExecutionType.dates, null, null, 0, 6, null);
		TBTopic periodTopic = topicBrokerService.createTopic(identity, broker);
		topicBrokerService.updateTopic(identity, periodTopic, "PERIOD", random(), null, TBTopicExecutionType.period, null, null, 0, 6, null);
		dbInstance.commitAndCloseSession();
		
		String input = "DATES	B	C	1	2	tbd	3/19/29					\n"
				+ "PERIOD	B	C	1	2	6/18/25	3/19/29					\n"
				+ "NEW	B	C	1	2							";
		ImportTopicConverter converter = new ImportTopicConverter(Locale.ENGLISH, broker, List.of(), new TestingGroupRestrictionCandidates(), input, null);
		List<TBImportTopic> topics = converter.getTopics();
		
		assertThat(topics).hasSize(3);
		assertThat(topics.get(0).getMessage()).isNull();
		assertThat(topics.get(0).getInfo()).isNotNull();
		assertThat(topics.get(0).getTopic().getBeginDate()).isNull();
		assertThat(topics.get(0).getTopic().getEndDate()).isNull();
		assertThat(topics.get(1).getInfo()).isNull();
		assertThat(topics.get(2).getInfo()).isNull();
	}
	
	private ImportTopicConverter createConverter(String input) {
		TBTransientBroker broker = new TBTransientBroker();
		broker.setKey(-1l);
		return new ImportTopicConverter(Locale.ENGLISH, broker, List.of(), new TestingGroupRestrictionCandidates(), input, null);
	}


}
