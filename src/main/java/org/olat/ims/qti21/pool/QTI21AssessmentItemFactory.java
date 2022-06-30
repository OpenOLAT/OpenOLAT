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
package org.olat.ims.qti21.pool;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.ui.editor.AssessmentItemEditorController;
import org.olat.modules.qpool.QItemFactory;
import org.olat.modules.qpool.QuestionItem;

/**
 * 
 * Initial date: 05.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentItemFactory implements QItemFactory {
	
	private final QTI21QuestionType type;
	
	public QTI21AssessmentItemFactory(QTI21QuestionType type) {
		this.type = type;
	}
	
	@Override
	public String getType() {
		return "qti21_".concat(type.name());
	}

	@Override
	public String getLabel(Locale locale) {
		Translator trans = Util.createPackageTranslator(AssessmentItemEditorController.class, locale);
		switch(type) {
			case sc: return "QTI 2.1 " + trans.translate("new.sc");
			case mc: return "QTI 2.1 " + trans.translate("new.mc");
			case kprim: return "QTI 2.1 " + trans.translate("new.kprim");
			case match: return "QTI 2.1 " + trans.translate("new.match");
			case matchdraganddrop: return "QTI 2.1 " + trans.translate("new.matchdraganddrop");
			case matchtruefalse: return "QTI 2.1 " + trans.translate("new.matchtruefalse");
			case fib: return "QTI 2.1 " + trans.translate("new.fib");
			case numerical: return "QTI 2.1 " + trans.translate("new.fib.numerical");
			case essay: return "QTI 2.1 " + trans.translate("new.essay");
			case upload: return "QTI 2.1 " + trans.translate("new.upload");
			case drawing: return "QTI 2.1 " + trans.translate("new.drawing");
			case hotspot: return "QTI 2.1 " + trans.translate("new.hotspot");
			case hottext: return "QTI 2.1 " + trans.translate("new.hottext");
			case inlinechoice: return "QTI 2.1 " + trans.translate("new.inlinechoice");
			case order: return "QTI 2.1 " + trans.translate("new.order");
			default: return type.name();
		}
	}

	@Override
	public QuestionItem createItem(Identity owner, String title, Locale locale) {
		QTI21QPoolServiceProvider spi = CoreSpringFactory.getImpl(QTI21QPoolServiceProvider.class);
		return spi.createItem(owner, type, title, locale);
	}
}