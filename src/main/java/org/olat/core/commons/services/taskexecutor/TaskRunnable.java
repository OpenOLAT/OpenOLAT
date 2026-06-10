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
package org.olat.core.commons.services.taskexecutor;

/**
 * 
 * Initial date: 16 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface TaskRunnable extends Runnable {
	
	public default Queue getExecutorsQueue() {
		return Queue.standard;
	}
	
	public enum Queue {
		sequential,
		lowPriority,
		external,
		/**
		 * AI calls a user is actively waiting for (e.g. essay AI correction
		 * at learner submit). Pool size configurable in the AI module.
		 */
		aiInteractive,
		/**
		 * Long-running AI batch work (e.g. question generation from page
		 * content). Pool size configurable in the AI module.
		 */
		aiBatch,
		standard;
	}
}
