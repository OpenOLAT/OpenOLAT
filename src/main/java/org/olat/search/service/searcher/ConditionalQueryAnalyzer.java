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
package org.olat.search.service.searcher;

import java.io.Reader;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.olat.search.model.AbstractOlatDocument;

/**
 * This analyzer is based on the StandardAnalyzer with the english
 * stop words, but it doesn't normalize the fields resourceurl and
 * resourceurlmd5 to lower case. This is essential because the business
 * path is case sensitive and the standard analyzers of Lucene lower
 * case the search queries.
 * 
 * 
 * Initial date: 17 janv. 2019<br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConditionalQueryAnalyzer extends StopwordAnalyzerBase {

	private static final int maxTokenLength = 255;
	  public ConditionalQueryAnalyzer() {
	    super(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
	  }

	@Override
	protected TokenStreamComponents createComponents(final String fieldName) {
		final StandardTokenizer src = new StandardTokenizer();
		src.setMaxTokenLength(maxTokenLength);
		TokenStream tok = new LowerCaseFilter(src);
		tok = new StopFilter(tok, stopwords);
		return new TokenStreamComponents(src, tok) {
			@Override
			protected void setReader(final Reader reader) {
				// So that if maxTokenLength was changed, the change takes
				// effect next time tokenStream is called:
				src.setMaxTokenLength(maxTokenLength);
				super.setReader(reader);
			}
		};
	}

	@Override
	protected TokenStream normalize(String fieldName, TokenStream in) {
		if(AbstractOlatDocument.RESOURCEURL_FIELD_NAME.equals(fieldName) || AbstractOlatDocument.RESOURCEURL_MD5_FIELD_NAME.equals(fieldName)) {
			return in;
		}
		return new LowerCaseFilter(in);
	}
}
