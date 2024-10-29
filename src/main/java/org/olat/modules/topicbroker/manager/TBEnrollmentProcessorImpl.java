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

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBEnrollmentProcess;
import org.olat.modules.topicbroker.TBEnrollmentProcessor;
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

	private final int runs;
	private final TBBroker broker;
	private final List<TBTopic> topics;
	private final List<TBSelection> selections;
	private DefaultEnrollmentProcess bestProcess;

	public TBEnrollmentProcessorImpl(int runs, TBBroker broker, List<TBTopic> topics, List<TBSelection> selections) {
		this.runs = runs;
		this.broker = broker;
		this.topics = topics;
		this.selections = selections;
		runProcesses();
	}

	private void runProcesses() {
		for (int i = 0; i < runs; i++) {
			DefaultEnrollmentProcess currentProcess = new DefaultEnrollmentProcess(broker, topics, selections);
			if (log.isDebugEnabled()) {
				long numEnromments = currentProcess.getPreviewSelections().stream().filter(TBSelection::isEnrolled).count();
				log.debug("Process {}: costs={}, numEnrollments={}", i, currentProcess.getCosts(), numEnromments);
			}
			
			if (bestProcess == null || bestProcess.getCosts() < currentProcess.getCosts()) {
				bestProcess = currentProcess;
				log.debug("Process {} is the best so far.", i);
			}
		}	
	}

	@Override
	public TBEnrollmentProcess getBest() {
		return bestProcess;
	}

}
