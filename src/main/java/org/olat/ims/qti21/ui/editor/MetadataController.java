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
package org.olat.ims.qti21.ui.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.modules.qpool.manager.MetadataConverterHelper;
import org.olat.modules.qpool.model.LOMDuration;
import org.olat.modules.qpool.ui.QuestionsController;

/**
 * 
 * Initial date: 8 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MetadataController extends FormBasicController {
	
	private final ManifestMetadataBuilder metadataBuilder;
	
	public MetadataController(UserRequest ureq, WindowControl wControl, ManifestMetadataBuilder metadataBuilder) {
		super(ureq, wControl, Util.createPackageTranslator(QuestionsController.class, ureq.getLocale()));
		this.metadataBuilder = metadataBuilder;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String learningTime = metadataBuilder.getEducationalLearningTime();
		LOMDuration duration = MetadataConverterHelper.convertDuration(learningTime);
		String formattedLearningTime = formatLearningTime(duration);
		uifactory.addStaticTextElement("educational.learningTime", formattedLearningTime, formLayout);
		
		Integer correctionTime = metadataBuilder.getOpenOLATMetadataCorrectionTime();
		String correctionTimeStr = null;
		if(correctionTime != null) {
			correctionTimeStr = translate("question.correctionTime.inMinutes", new String[] { correctionTime.toString() });
		}
		uifactory.addStaticTextElement("question.correctionTime", correctionTimeStr, formLayout);
	}
	
	public boolean hasMetadata() {
		return StringHelper.containsNonWhitespace(metadataBuilder.getEducationalLearningTime())
				|| metadataBuilder.getOpenOLATMetadataCorrectionTime() != null;
	}
	
	private String formatLearningTime(LOMDuration duration) {
		StringBuilder sb = new StringBuilder(32);
		boolean started = appendFormatLearningTime(duration.getDay(), "educational.learningTime.day", sb, false);
		started |= appendFormatLearningTime(duration.getHour(), "educational.learningTime.hour", sb, started);
		appendFormatLearningTime(duration.getMinute(), "educational.learningTime.minute", sb, started);
		appendFormatLearningTime(duration.getSeconds(), "educational.learningTime.second", sb, false);
		return sb.toString();
	}
	
	private boolean appendFormatLearningTime(int value, String unit, StringBuilder sb, boolean append) {
		if(append || value > 0) {
			if(sb.length() > 0) sb.append(" ");
			sb.append(value)
			  .append(translate(unit));
		}
		return value > 0;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	

}
