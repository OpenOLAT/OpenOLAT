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
package org.olat.ims.qti21.model.xml;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.interactions.DrawingAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.EssayAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder.EntryType;
import org.olat.ims.qti21.model.xml.interactions.HotspotAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.HottextAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.KPrimAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MatchAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.OrderAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.UploadAssessmentItemBuilder;
import org.olat.ims.qti21.ui.editor.AssessmentTestComposerController;

/**
 * 
 * Initial date: 6 déc. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemBuilderFactory {
	
	public static AssessmentItemBuilder get(QTI21QuestionType type, Locale locale) {
		QTI21Service qtiService = CoreSpringFactory.getImpl(QTI21Service.class);
		Translator translator = Util.createPackageTranslator(AssessmentTestComposerController.class, locale);
		return switch(type) {
			case sc -> new SingleChoiceAssessmentItemBuilder(translator.translate("new.sc"), translator.translate("new.answer"), qtiService.qtiSerializer());
			case mc -> new MultipleChoiceAssessmentItemBuilder(translator.translate("new.mc"), translator.translate("new.answer"), qtiService.qtiSerializer());
			case kprim -> new KPrimAssessmentItemBuilder(translator.translate("new.kprim"), translator.translate("new.answer"), qtiService.qtiSerializer());
			case match -> new MatchAssessmentItemBuilder(translator.translate("new.match"), QTI21Constants.CSS_MATCH_MATRIX, qtiService.qtiSerializer());
			case matchdraganddrop -> new MatchAssessmentItemBuilder(translator.translate("new.matchdraganddrop"), QTI21Constants.CSS_MATCH_DRAG_AND_DROP, qtiService.qtiSerializer());
			case matchtruefalse -> new MatchAssessmentItemBuilder(translator.translate("new.matchtruefalse"), QTI21Constants.CSS_MATCH_TRUE_FALSE,
					translator.translate("match.unanswered"), translator.translate("match.true"), translator.translate("match.false"), qtiService.qtiSerializer());
			case fib -> new GapAssessmentItemBuilder(translator.translate("new.fib"), EntryType.text, qtiService.qtiSerializer());
			case numerical -> new GapAssessmentItemBuilder(translator.translate("new.fib.numerical"), EntryType.numerical, qtiService.qtiSerializer());
			case gapmixed -> new GapAssessmentItemBuilder(translator.translate("new.gapmixed"), EntryType.mixed, qtiService.qtiSerializer());
			case essay -> new EssayAssessmentItemBuilder(translator.translate("new.essay"), qtiService.qtiSerializer());
			case upload -> new UploadAssessmentItemBuilder(translator.translate("new.upload"), qtiService.qtiSerializer());
			case drawing -> new DrawingAssessmentItemBuilder(translator.translate("new.drawing"), qtiService.qtiSerializer());
			case hotspot -> new HotspotAssessmentItemBuilder(translator.translate("new.hotspot"), qtiService.qtiSerializer());
			case hottext -> new HottextAssessmentItemBuilder(translator.translate("new.hottext"), translator.translate("new.hottext.start"), translator.translate("new.hottext.text"), qtiService.qtiSerializer());
			case order -> new OrderAssessmentItemBuilder(translator.translate("new.order"), translator.translate("new.answer"), qtiService.qtiSerializer());
			case inlinechoice -> new GapAssessmentItemBuilder(translator.translate("new.inlinechoice"), EntryType.inlineChoice, qtiService.qtiSerializer()); 
			default -> null;
		};
	}
}
