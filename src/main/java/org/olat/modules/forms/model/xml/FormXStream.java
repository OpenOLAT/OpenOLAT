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
package org.olat.modules.forms.model.xml;

import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.ceditor.model.ImageSettings;
import org.olat.modules.forms.model.xml.SessionInformations.InformationType;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FormXStream {
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();

	static {
		Class<?>[] types = new Class[] { Choice.class, Choices.class, ChoiceSelectedCondition.class, Container.class,
				Disclaimer.class, FileStoredData.class, FileUpload.class, Form.class, HTMLParagraph.class,
				HTMLRaw.class, Image.class, ImageSettings.class, InformationType.class, MultipleChoice.class,
				Rubric.class, Rule.class, ScaleType.class, SessionInformations.class, SingleChoice.class, Slider.class,
				Spacer.class, StepLabel.class, Table.class, TextInput.class, Title.class, VisibilityAction.class };
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.alias("choice", Choice.class);
		xstream.alias("choices", Choices.class);
		xstream.alias("choiceSelectedCondition", ChoiceSelectedCondition.class);
		xstream.alias("disclaimer", Disclaimer.class);
		xstream.alias("fileStoredData", FileStoredData.class);
		xstream.alias("fileupload", FileUpload.class);
		xstream.alias("form", Form.class);
		xstream.alias("image", Image.class);
		xstream.alias("imageSettgins", ImageSettings.class);
		xstream.alias("informationType", InformationType.class);
		xstream.alias("multiplechoice", MultipleChoice.class);
		xstream.alias("rubric", Rubric.class);
		xstream.alias("rule", Rule.class);
		xstream.alias("sessioninformations", SessionInformations.class);
		xstream.alias("singlechoice", SingleChoice.class);
		xstream.alias("slider", Slider.class);
		xstream.alias("spacer", Spacer.class);
		xstream.alias("table", Table.class);
		xstream.alias("title", Title.class);
		xstream.alias("visibilityAction", VisibilityAction.class);
		xstream.ignoreUnknownElements();
	}
	
	public static XStream getXStream() {
		return xstream;
	}

}
