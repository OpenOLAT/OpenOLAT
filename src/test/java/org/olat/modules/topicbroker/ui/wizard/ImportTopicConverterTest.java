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

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.olat.core.util.DateUtils;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.model.TBImportTopic;
import org.olat.modules.topicbroker.model.TBTransientBroker;
import org.olat.test.OlatTestCase;

/**
 * 
 * Initial date: Jun 18, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ImportTopicConverterTest extends OlatTestCase {

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
	
	private ImportTopicConverter createConverter(String input) {
		return new ImportTopicConverter(Locale.ENGLISH, new TBTransientBroker(), List.of(), new TestingGroupRestrictionCandidates(), input, null);
	}


}
