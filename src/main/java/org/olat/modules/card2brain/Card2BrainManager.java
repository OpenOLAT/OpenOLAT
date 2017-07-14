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
package org.olat.modules.card2brain;

import org.olat.modules.card2brain.manager.Card2BrainVerificationResult;

/**
 * 
 * Initial date: 20.04.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface Card2BrainManager {
	
	/**
	 * Check if a set of flashcards exists on card2brain.
	 * @param alias the alias of the set of flashcards.
	 * @return true if the set of flashcards exists.
	 */
	public boolean checkSetOfFlashcards(String alias);
	
	/**
	 * Verify if the key and the secret of the enterprise login are valid.
	 *
	 * @param url the url of the verification service
	 * @param key the key
	 * @param secret the secret
	 * @return Card2BrainVerificationResult the result of the verification
	 */
	public Card2BrainVerificationResult checkEnterpriseLogin(String url, String key, String secret);

	/**
	 * Parse the alias of the set of flashcards. Remove the unnecessary part
	 * if someone inserts the whole weblink from the card2brain website e.g.
	 * https://card2brain.ch/box/20170420_02_chemie_und_werkstoffe.
	 * 
	 * @param alias
	 *            the original alias value
	 * @return the parsed String
	 */
	public String parseAlias(String alias);
	
}
