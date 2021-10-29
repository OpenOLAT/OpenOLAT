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
package org.olat.course.run.scoring;

/**
 * 
 * Initial date: 17 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AccountingEvaluatorsBuilder {
	
	private static AccountingEvaluators CONVENTIONAL = builder()
			.withBlockerEvaluator(AccountingEvaluatorsFactory.createUnchangingBlockerEvaluator())
			.withStartDateEvaluator(AccountingEvaluatorsFactory.createNoBlockingStartDateEvaluator())
			.withEndDateEvaluator(AccountingEvaluatorsFactory.createNoBlockingEndDateEvaluator())
			.withObligationEvaluator(AccountingEvaluatorsFactory.createNoneObligationEvaluator())
			.withDurationEvaluator(AccountingEvaluatorsFactory.createNullDurationEvaluator())
			.withScoreEvaluator(AccountingEvaluatorsFactory.createUnchangingScoreEvaluator())
			.withMaxScoreEvaluator(AccountingEvaluatorsFactory.createConfigMaxScoreEvaluator())
			.withPassedEvaluator(AccountingEvaluatorsFactory.createUnchangingPassedEvaluator())
			.withRootPassedEvaluator(AccountingEvaluatorsFactory.createUnchangingRootPassedEvaluator())
			.withCompletionEvaluator(AccountingEvaluatorsFactory.createUnchangingCompletionEvaluator())
			.withStatusEvaluator(AccountingEvaluatorsFactory.createUnchangingStatusEvaluator())
			.withFullyAssessedEvaluator(AccountingEvaluatorsFactory.createUnchangingFullyAssessedEvaluator())
			.withLastModificationsEvaluator(AccountingEvaluatorsFactory.createUnchangingLastModificationsEvaluator())
			.build();
	
	private BlockerEvaluator blockerEvaluator;
	private StartDateEvaluator startDateEvaluator;
	private EndDateEvaluator endDateEvaluator;
	private ObligationEvaluator obligationEvaluator;
	private DurationEvaluator durationEvaluator;
	private ScoreEvaluator scoreEvaluator;
	private MaxScoreEvaluator maxScoreEvaluator;
	private PassedEvaluator passedEvaluator;
	private RootPassedEvaluator rootPassedEvaluator;
	private CompletionEvaluator completionEvaluator;
	private StatusEvaluator statusEvaluator;
	private FullyAssessedEvaluator fullyAssessedEvaluator;
	private LastModificationsEvaluator lastModificationsEvaluator;
	
	private AccountingEvaluatorsBuilder() {
		//
	}
	
	public AccountingEvaluatorsBuilder withBlockerEvaluator(BlockerEvaluator blockerEvaluator) {
		this.blockerEvaluator = blockerEvaluator;
		return this;
	}
	
	public AccountingEvaluatorsBuilder withStartDateEvaluator(StartDateEvaluator startDateEvaluator) {
		this.startDateEvaluator = startDateEvaluator;
		return this;
	}
	
	public AccountingEvaluatorsBuilder withEndDateEvaluator(EndDateEvaluator endDateEvaluator) {
		this.endDateEvaluator = endDateEvaluator;
		return this;
	}
	
	public AccountingEvaluatorsBuilder withObligationEvaluator(ObligationEvaluator obligationEvaluator) {
		this.obligationEvaluator = obligationEvaluator;
		return this;
	}
	
	public AccountingEvaluatorsBuilder withDurationEvaluator(DurationEvaluator durationEvaluator) {
		this.durationEvaluator = durationEvaluator;
		return this;
	}
	
	public AccountingEvaluatorsBuilder withScoreEvaluator(ScoreEvaluator scoreEvaluator) {
		this.scoreEvaluator = scoreEvaluator;
		return this;
	}
	
	public AccountingEvaluatorsBuilder withMaxScoreEvaluator(MaxScoreEvaluator maxScoreEvaluator) {
		this.maxScoreEvaluator = maxScoreEvaluator;
		return this;
	}

	public AccountingEvaluatorsBuilder withNullScoreEvaluator() {
		this.scoreEvaluator = AccountingEvaluatorsFactory.createNullScoreEvaluator();
		return this;
	}
	
	public AccountingEvaluatorsBuilder withPassedEvaluator(PassedEvaluator passedEvaluator) {
		this.passedEvaluator = passedEvaluator;
		return this;
	}
	
	public AccountingEvaluatorsBuilder withRootPassedEvaluator(RootPassedEvaluator rootPassedEvaluator) {
		this.rootPassedEvaluator = rootPassedEvaluator;
		return this;
	}
	
	public AccountingEvaluatorsBuilder withCompletionEvaluator(CompletionEvaluator completionEvaluator) {
		this.completionEvaluator = completionEvaluator;
		return this;
	}
	
	public AccountingEvaluatorsBuilder withStatusEvaluator(StatusEvaluator statusEvaluator) {
		this.statusEvaluator = statusEvaluator;
		return this;
	}
	
	public AccountingEvaluatorsBuilder withFullyAssessedEvaluator(FullyAssessedEvaluator fullyAssessedEvaluator) {
		this.fullyAssessedEvaluator = fullyAssessedEvaluator;
		return this;
	}
	
	public AccountingEvaluatorsBuilder withLastModificationsEvaluator(LastModificationsEvaluator lastModificationsEvaluator) {
		this.lastModificationsEvaluator = lastModificationsEvaluator;
		return this;
	}
	
	public AccountingEvaluators build() {
		AccountingEvaluatorsImpl impl = new AccountingEvaluatorsImpl();
		impl.blockerEvaluator = this.blockerEvaluator != null
				? this.blockerEvaluator
				: AccountingEvaluatorsFactory.createUnchangingBlockerEvaluator();
		impl.startDateEvaluator = this.startDateEvaluator != null 
				? this.startDateEvaluator
				: AccountingEvaluatorsFactory.createNoBlockingStartDateEvaluator();
		impl.endDateEvaluator = this.endDateEvaluator != null 
				? this.endDateEvaluator
				: AccountingEvaluatorsFactory.createNoBlockingEndDateEvaluator();
		impl.obligationEvaluator = this.obligationEvaluator != null 
				? this.obligationEvaluator
				: AccountingEvaluatorsFactory.createNoneObligationEvaluator();
		impl.durationEvaluator = this.durationEvaluator != null 
				? this.durationEvaluator
				: AccountingEvaluatorsFactory.createNullDurationEvaluator();
		impl.scoreEvaluator = this.scoreEvaluator != null
				? this.scoreEvaluator
				: AccountingEvaluatorsFactory.createUnchangingScoreEvaluator();
		impl.maxScoreEvaluator = this.maxScoreEvaluator != null
				? this.maxScoreEvaluator
				: AccountingEvaluatorsFactory.createConfigMaxScoreEvaluator();
		impl.passedEvaluator = this.passedEvaluator != null
				? this.passedEvaluator
				: AccountingEvaluatorsFactory.createUnchangingPassedEvaluator();
		impl.rootPassedEvaluator = this.rootPassedEvaluator != null
				? this.rootPassedEvaluator
				: AccountingEvaluatorsFactory.createUnchangingRootPassedEvaluator();
		impl.completionEvaluator = this.completionEvaluator != null
				? this.completionEvaluator
				: AccountingEvaluatorsFactory.createUnchangingCompletionEvaluator();
		impl.statusEvaluator = this.statusEvaluator != null
				? this.statusEvaluator
				: AccountingEvaluatorsFactory.createUnchangingStatusEvaluator();
		impl.fullyAssessedEvaluator = this.fullyAssessedEvaluator != null
				? this.fullyAssessedEvaluator
				: AccountingEvaluatorsFactory.createUnchangingFullyAssessedEvaluator();
		impl.lastModificationsEvaluator = this.lastModificationsEvaluator != null
				? this.lastModificationsEvaluator
				: AccountingEvaluatorsFactory.createUnchangingLastModificationsEvaluator();
		return impl;
	}
	
	public static AccountingEvaluatorsBuilder builder() {
		return new AccountingEvaluatorsBuilder();
	}
	
	public static AccountingEvaluators defaultConventional() {
		return CONVENTIONAL;
	}
	
	private static class AccountingEvaluatorsImpl implements AccountingEvaluators {
		
		private BlockerEvaluator blockerEvaluator;
		private StartDateEvaluator startDateEvaluator;
		private EndDateEvaluator endDateEvaluator;
		private ObligationEvaluator obligationEvaluator;
		private DurationEvaluator durationEvaluator;
		private ScoreEvaluator scoreEvaluator;
		private MaxScoreEvaluator maxScoreEvaluator;
		private PassedEvaluator passedEvaluator;
		private RootPassedEvaluator rootPassedEvaluator;
		private CompletionEvaluator completionEvaluator;
		private StatusEvaluator statusEvaluator;
		private FullyAssessedEvaluator fullyAssessedEvaluator;
		private LastModificationsEvaluator lastModificationsEvaluator;

		@Override
		public BlockerEvaluator getBlockerEvaluator() {
			return blockerEvaluator;
		}

		@Override
		public StartDateEvaluator getStartDateEvaluator() {
			return startDateEvaluator;
		}

		@Override
		public EndDateEvaluator getEndDateEvaluator() {
			return endDateEvaluator;
		}

		@Override
		public ObligationEvaluator getObligationEvaluator() {
			return obligationEvaluator;
		}

		@Override
		public DurationEvaluator getDurationEvaluator() {
			return durationEvaluator;
		}

		@Override
		public ScoreEvaluator getScoreEvaluator() {
			return scoreEvaluator;
		}

		@Override
		public MaxScoreEvaluator getMaxScoreEvaluator() {
			return maxScoreEvaluator;
		}

		@Override
		public PassedEvaluator getPassedEvaluator() {
			return passedEvaluator;
		}

		@Override
		public RootPassedEvaluator getRootPassedEvaluator() {
			return rootPassedEvaluator;
		}
		
		@Override
		public CompletionEvaluator getCompletionEvaluator() {
			return completionEvaluator;
		}
		
		@Override
		public StatusEvaluator getStatusEvaluator() {
			return statusEvaluator;
		}

		@Override
		public FullyAssessedEvaluator getFullyAssessedEvaluator() {
			return fullyAssessedEvaluator;
		}

		@Override
		public LastModificationsEvaluator getLastModificationsEvaluator() {
			return lastModificationsEvaluator;
		}

	}

}
