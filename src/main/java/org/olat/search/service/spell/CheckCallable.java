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
package org.olat.search.service.spell;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.spell.SpellChecker;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 24.10.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class CheckCallable implements Callable<Set<String>> {
	
	private static final Logger log = Tracing.createLoggerFor(CheckCallable.class);
	
	private final String query;
	private final SearchSpellChecker spellCheckerService;
	
	public CheckCallable(String query, SearchSpellChecker spellCheckerService) {
		this.query = query;
		this.spellCheckerService = spellCheckerService;
	}

	@Override
	public Set<String> call() throws Exception {
		try {
			SpellChecker spellChecker  = spellCheckerService.getSpellChecker();
  		if (spellChecker != null) {
  			String[] words = spellChecker.suggestSimilar(query,5);
  			// Remove duplicates
  			Set<String> filteredList = new TreeSet<>();
  			for (String word : words) {
  				filteredList.add(word);
				}
			  return filteredList;
  		}
		} catch (IOException e) {
			log.warn("Can not spell check",e);
		}
		return new HashSet<>();
	}
}
