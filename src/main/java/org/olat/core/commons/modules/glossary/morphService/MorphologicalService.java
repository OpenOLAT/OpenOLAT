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

package org.olat.core.commons.modules.glossary.morphService;

import java.util.List;

/**
 * Description:<br>
 * Connects to a morphological service and lets retreive flexions for a word.
 * 
 * <P>
 * Initial Date:  23.12.2008 <br>
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public interface MorphologicalService {

	public static final String STATUS_KNOWN = "known";
	public static final String STATUS_GUESSED = "guessed";
	public static final String STATUS_ERROR = "error";

	
	/**
	 * returns a list of Flexions found for a given word.
	 * @param partOfSpeech  	possible values, see: assumePartOfSpeech()
	 * @param word	a single word or a wordgroup
	 * @return list of flexions found with a morphological service
	 */
	public List<String> getFlexions(String partOfSpeech, String word);
	
	/**
	 * same as getFlexions(String partOfSpeech, String word) 
	 * but with automatic assumption of partofspeech
	 * @param word
	 * @return
	 */
	public List<String> getFlexions(String word);
	
	/**
	 * returns part-of-speech for a given word or wordgroup
	 * @param glossTerm
	 * @return part of speech, which can be:
	 * - a				for an adjective 								"schön"
	 * - n				for a noun											"Haus"
	 * - an				for adjective and noun					"schönes Haus"
	 */
	public String assumePartOfSpeech(String glossTerm) ;
	
	/**
	 * Get the Status from the reply arrived from the morphological service
	 * status can either be:
	 * - known			Flexions were found in Lexicon
	 * - guessed 		Flexions were generated with heuristics
	 * - error			Couldn't find anything or service error / unavailable
	 * @return
	 */
	public String getReplyStatus();
	

	/**
	 * get service name as description in guis
	 * @return
	 */
	public String getMorphServiceDescriptor() ;
	

	/**
	 * get unique identifier to store with glossary-config
	 * @return
	 */
	public String getMorphServiceIdentifier() ;
	
}
