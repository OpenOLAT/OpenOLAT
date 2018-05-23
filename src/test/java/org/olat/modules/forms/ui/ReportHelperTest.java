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
package org.olat.modules.forms.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationIdentifier;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.ui.ReportHelper.Legend;
import org.olat.modules.portfolio.PageBody;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 06.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReportHelperTest {

	private static final String ANONYMOUS_NAME = "name";
	private static final String ANONYMOUS_COLOR = "color";
	
	private ReportHelper sut;
	
	@Before
	public void setUp() {
		sut = ReportHelper.builder(Locale.CANADA)
				.withAnonymousName(ANONYMOUS_NAME)
				.withAnonymousColor(ANONYMOUS_COLOR)
				.withColors()
				.withLegendNameGenrator(new IdentityNameGenarator())
				.build();
	}

	@Test
	public void shouldGetSameLegendForSameSession() {
		String executorName = "executor";
		IdentityImpl executor = new IdentityImpl();
		executor.setKey(Long.valueOf(1));
		executor.setName(executorName);
		EvaluationFormParticipation participation = new TestableParticipation(Long.valueOf(1), executor);
		EvaluationFormSession session =  new TestableSession(Long.valueOf(1), participation);
		
		Legend legend1 = sut.getLegend(session);
		Legend legend2 = sut.getLegend(session);
		
		assertThat(legend1.getName()).isEqualTo(executorName);
		assertThat(legend2.getName()).isEqualTo(executorName);
		assertThat(legend2.getColor()).isEqualTo(legend1.getColor());
	}
	
	@Test
	public void shouldGetSameLegendForSameParticipation() {
		String executorName = "executor";
		IdentityImpl executor = new IdentityImpl();
		executor.setKey(Long.valueOf(1));
		executor.setName(executorName);
		EvaluationFormParticipation participation = new TestableParticipation(Long.valueOf(1), executor);
		EvaluationFormSession session1 =  new TestableSession(Long.valueOf(1), participation);
		EvaluationFormSession session2 = new TestableSession(Long.valueOf(2), participation);
		
		Legend legend1 = sut.getLegend(session1);
		Legend legend2 = sut.getLegend(session2);
		
		assertThat(legend1.getName()).isEqualTo(executorName);
		assertThat(legend2.getName()).isEqualTo(executorName);
		assertThat(legend2.getColor()).isEqualTo(legend1.getColor());
	}

	@Test
	public void shouldGetSameLegendForSameIdentity() {
		String executorName = "executor";
		IdentityImpl executor = new IdentityImpl();
		executor.setKey(Long.valueOf(1));
		executor.setName(executorName);
		EvaluationFormParticipation participation1 = new TestableParticipation(Long.valueOf(1), executor);
		EvaluationFormSession session1 = new TestableSession(Long.valueOf(1), participation1);
		EvaluationFormParticipation participation2 = new TestableParticipation(Long.valueOf(2), executor);
		EvaluationFormSession session2 =  new TestableSession(Long.valueOf(1), participation2);
		
		Legend legend1 = sut.getLegend(session1);
		Legend legend2 = sut.getLegend(session2);
		
		assertThat(legend1.getName()).isEqualTo(executorName);
		assertThat(legend2.getName()).isEqualTo(executorName);
		assertThat(legend2.getColor()).isEqualTo(legend1.getColor());
	}
	
	@Test
	public void shouldGetOtherValuesForDifferentIdentities() {
		String executorName1 = "executor";
		IdentityImpl executor1 = new IdentityImpl();
		executor1.setKey(Long.valueOf(1));
		executor1.setName(executorName1);
		EvaluationFormParticipation participation1 = new TestableParticipation(Long.valueOf(1), executor1);
		EvaluationFormSession session1 =  new TestableSession(Long.valueOf(1), participation1);
		String executorName2 = "executor2";
		IdentityImpl executor2 = new IdentityImpl();
		executor2.setKey(Long.valueOf(2));
		executor2.setName(executorName2);
		EvaluationFormParticipation participation2 = new TestableParticipation(Long.valueOf(2), executor2);
		EvaluationFormSession session2 = new TestableSession(Long.valueOf(2), participation2);
		
		Legend legend1 = sut.getLegend(session1);
		Legend legend2 = sut.getLegend(session2);
		
		assertThat(legend1.getName()).isEqualTo(executorName1);
		assertThat(legend2.getName()).isEqualTo(executorName2);
		assertThat(legend2.getColor()).isNotEqualTo(legend1.getColor());
	}

	@Test
	public void shouldGetAnonymousLegendForAnonymousSession() {
		EvaluationFormSession anonymousSession = new TestableSession(Long.valueOf(1), null);
		
		Legend legend = sut.getLegend(anonymousSession);
		
		assertThat(legend.getName()).isEqualTo(ANONYMOUS_NAME);
		assertThat(legend.getColor()).isEqualTo(ANONYMOUS_COLOR);
	}

	@Test
	public void shouldGetAnoymousLegendForParticipationWithoutIdentity() {
		EvaluationFormParticipation anonymousParticipation = new TestableParticipation(Long.valueOf(1), null);
		EvaluationFormSession session = new TestableSession(Long.valueOf(1), anonymousParticipation);
		
		Legend legend = sut.getLegend(session);
		
		assertThat(legend.getName()).isEqualTo(ANONYMOUS_NAME);
		assertThat(legend.getColor()).isEqualTo(ANONYMOUS_COLOR);
	}
	
	private static final class TestableSession implements EvaluationFormSession {
		
		private final Long key;
		private final EvaluationFormParticipation particpation;

		public TestableSession(Long key, EvaluationFormParticipation particpation) {
			super();
			this.key = key;
			this.particpation = particpation;
		}

		@Override
		public Long getKey() {
			return key;
		}

		@Override
		public Date getCreationDate() {
			return null;
		}

		@Override
		public Date getLastModified() {
			return null;
		}

		@Override
		public void setLastModified(Date date) {
			//
		}

		@Override
		public Date getSubmissionDate() {
			return null;
		}

		@Override
		public Date getFirstSubmissionDate() {
			return null;
		}

		@Override
		public EvaluationFormSessionStatus getEvaluationFormSessionStatus() {
			return null;
		}

		@Override
		public EvaluationFormParticipation getParticipation() {
			return particpation;
		}

		@Override
		public EvaluationFormSurvey getSurvey() {
			return null;
		}

		@Override
		public Identity getIdentity() {
			return null;
		}

		@Override
		public PageBody getPageBody() {
			return null;
		}

		@Override
		public RepositoryEntry getFormEntry() {
			return null;
		}
	}
	
	private static final class TestableParticipation implements EvaluationFormParticipation {
		
		private final Long key;
		private final Identity executor;

		public TestableParticipation(Long key, Identity executor) {
			this.key = key;
			this.executor = executor;
		}

		@Override
		public Date getCreationDate() {
			return null;
		}

		@Override
		public Date getLastModified() {
			return null;
		}

		@Override
		public void setLastModified(Date date) {
			//
		}

		@Override
		public Long getKey() {
			return key;
		}

		@Override
		public EvaluationFormSurvey getSurvey() {
			return null;
		}

		@Override
		public EvaluationFormParticipationIdentifier getIdentifier() {
			return null;
		}

		@Override
		public EvaluationFormParticipationStatus getStatus() {
			return null;
		}

		@Override
		public boolean isAnonymous() {
			return false;
		}

		@Override
		public void setAnonymous(boolean anonymous) {
			//
		}

		@Override
		public Identity getExecutor() {
			return executor;
		}
	}
	
	private final static class IdentityNameGenarator implements LegendNameGenerator {

		@Override
		public String getName(EvaluationFormSession session, Identity identity) {
			return identity.getName();
		}
		
	}

}
