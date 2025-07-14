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

import java.math.BigInteger;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBEnrollmentProcess;
import org.olat.modules.topicbroker.TBEnrollmentProcessor;
import org.olat.modules.topicbroker.TBEnrollmentStrategy;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBTopic;

/**
 * 
 * Initial date: 29 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBEnrollmentProcessorImpl implements TBEnrollmentProcessor {

	private static final Logger log = Tracing.createLoggerFor(TBEnrollmentProcessorImpl.class);
	
	private final int maxDurationMillis;
	private final TBBroker broker;
	private final List<TBTopic> topics;
	private final List<TBSelection> selections;
	private final TBEnrollmentStrategy strategy;
	private final List<TBEnrollmentStrategy> debugStrategies;
	private long runs = 0;
	private long durationMillis = 0;
	private double bestStrategyValue = 0;
	private DefaultEnrollmentProcess bestProcess;

	public TBEnrollmentProcessorImpl(int maxDurationMillis, TBBroker broker, List<TBTopic> topics, List<TBSelection> selections,
			TBEnrollmentStrategy strategy, List<TBEnrollmentStrategy> debugStrategies) {
		this.maxDurationMillis = maxDurationMillis;
		this.broker = broker;
		this.topics = topics;
		this.selections = selections;
		this.strategy = strategy;
		this.debugStrategies = debugStrategies;
		runProcesses();
	}

	private void runProcesses() {
		BigInteger guessNumRuns = guessNumRuns();
		long maxRuns = toLong(guessNumRuns);
		runs = 1;
		long start = System.currentTimeMillis();
		durationMillis = 0;
		
		log.debug("Processor started.");
		while (runs <= maxRuns && durationMillis < maxDurationMillis) {
			runProcess(runs);
			runs++;
			long finish = System.currentTimeMillis();
			durationMillis = finish - start;
		}
		log.debug("Processor finished. Guessed runs={}, Runs={}, Duration millis={}, Millis per run={}",
				guessNumRuns, runs-1, durationMillis, (double)durationMillis / (runs-1));
		
		log.debug("Best process enrollments:");
		logSelections(bestProcess.getPreviewSelections());
	}

	private void runProcess(long currentRun) {
		DefaultEnrollmentProcess currentProcess = new DefaultEnrollmentProcess(broker, topics, selections);
		List<TBSelection> previewSelections = currentProcess.getPreviewSelections();
		//logSelections(previewSelections);
		
		double currentStrategyValue = strategy.getValue(previewSelections);
		if (log.isDebugEnabled()) {
			long numEnrollments = previewSelections.stream().filter(TBSelection::isEnrolled).count();
			log.debug("Process {}: strategy={}, value={}, numEnrollments={}",
					currentRun, getStrategyName(strategy), currentStrategyValue, numEnrollments);
			
			if (debugStrategies != null && !debugStrategies.isEmpty()) {
				for (TBEnrollmentStrategy debugStrategy : debugStrategies) {
					double debugStrategyValue = debugStrategy.getValue(previewSelections);
					log.debug("Process {}: strategy={}, value={}",
							currentRun, getStrategyName(debugStrategy), debugStrategyValue);
				}
			}
			
		}
		
		if (bestProcess == null || bestStrategyValue < currentStrategyValue) {
			bestStrategyValue = currentStrategyValue;
			bestProcess = currentProcess;
			log.debug("Process {} is the best so far.", currentRun);
		}
	}

	private String getStrategyName(TBEnrollmentStrategy logStrategy) {
		if (logStrategy != null && logStrategy.getConfig() != null && logStrategy.getConfig().getType() != null) {
			return logStrategy.getConfig().getType().name();
		}
		return "";
	}

	private void logSelections(List<TBSelection> selections) {
		if (log.isDebugEnabled()) {
			selections.stream()
				.filter(TBSelection::isEnrolled)
				.sorted((s1, s2) -> Long.compare(s1.getTopic().getKey(), s2.getTopic().getKey()))
				.forEach(selection -> log.debug("Topic: {}, Participant: {}, Selection: {}",
						selection.getTopic().getKey(),
						selection.getParticipant().getKey(),
						selection.getKey()
					));
		}
	}

	@Override
	public TBEnrollmentProcess getBest() {
		return bestProcess;
	}

	@Override
	public double getBestStrategyValue() {
		return bestStrategyValue;
	}

	@Override
	public long getRuns() {
		return runs;
	}

	@Override
	public long getDurationMillis() {
		return durationMillis;
	}

	public void setDurationMillis(long durationMillis) {
		this.durationMillis = durationMillis;
	}

	/*
	 * Very rudimentary estimate of the number of possibilities.
	 */
	private BigInteger guessNumRuns() {
		return factorial(topics.size()).multiply(factorial(broker.getMaxSelections()));
	}

	private long toLong(BigInteger guessedRuns) {
		if (guessedRuns.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0 &&
			guessedRuns.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
			return guessedRuns.longValue();
		}
		return Long.MAX_VALUE;
	}
	
	private BigInteger factorial(int x) {
			BigInteger result = BigInteger.ONE;
			for (int i = 2; i <= x; i++) {
				result = result.multiply(BigInteger.valueOf(i));
			}
			return result;
		}

}
