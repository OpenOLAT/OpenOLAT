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
package org.olat.ims.qti21.model;

import org.olat.modules.qpool.QuestionType;

/**
 * 
 * Initial date: 16.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum QTI21QuestionType {
	sc(true, "sc", QuestionType.SC),
	mc(true, "mc", QuestionType.MC),
	kprim(true, "kprim", QuestionType.KPRIM),
	fib(false, "fib", QuestionType.FIB),
	essay(true, "essay", QuestionType.ESSAY),
	unkown(false, null, null);
	
	private final String prefix;
	private final boolean editor;
	private final QuestionType poolQuestionType;
	
	private QTI21QuestionType(boolean editor, String prefix, QuestionType poolQuestionType) {
		this.editor = editor;
		this.prefix = prefix;
		this.poolQuestionType = poolQuestionType;
	}
	
	public boolean hasEditor() {
		return editor;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public QuestionType getPoolQuestionType() {
		return poolQuestionType;
	}
}
