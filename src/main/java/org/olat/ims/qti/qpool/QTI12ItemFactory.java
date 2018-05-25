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
package org.olat.ims.qti.qpool;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.ims.qti.editor.QTIEditorMainController;
import org.olat.modules.qpool.QItemFactory;
import org.olat.modules.qpool.QuestionItem;

/**
 * 
 * 
 * 
 * Initial date: 26.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12ItemFactory implements QItemFactory {
	
	private final Type type;
	
	public QTI12ItemFactory(Type type) {
		this.type = type;
	}
	
	@Override
	public String getType() {
		return "qti12_".concat(type.name());
	}

	@Override
	public String getLabel(Locale locale) {
		Translator trans = Util.createPackageTranslator(QTIEditorMainController.class, locale);
		switch(type) {
			case sc: return "QTI 1.2 " + trans.translate("item.type.sc");
			case mc: return "QTI 1.2 " + trans.translate("item.type.mc");
			case kprim: return "QTI 1.2 " + trans.translate("item.type.kprim");
			case fib: return "QTI 1.2 " + trans.translate("item.type.fib");
			case essay: return "QTI 1.2 " + trans.translate("item.type.essay");
			default: return type.name();
		}
	}

	@Override
	public QuestionItem createItem(Identity owner, String title, Locale locale) {
		QTIQPoolServiceProvider spi = CoreSpringFactory.getImpl(QTIQPoolServiceProvider.class);
		return spi.createItem(owner, type, title, locale);
	}
	
	public enum Type {
		sc,
		mc,
		kprim,
		fib,
		essay
	}
}
