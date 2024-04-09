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
package org.olat.modules.ceditor;


import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.CodeLanguage;
import org.olat.modules.ceditor.model.CodeSettings;
import org.olat.modules.ceditor.model.ContainerColumn;
import org.olat.modules.ceditor.model.ContainerLayout;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.model.GeneralStyleSettings;
import org.olat.modules.ceditor.model.ImageHorizontalAlignment;
import org.olat.modules.ceditor.model.ImageSettings;
import org.olat.modules.ceditor.model.ImageSize;
import org.olat.modules.ceditor.model.ImageTitlePosition;
import org.olat.modules.ceditor.model.MathSettings;
import org.olat.modules.ceditor.model.MediaSettings;
import org.olat.modules.ceditor.model.QuizQuestion;
import org.olat.modules.ceditor.model.QuizSettings;
import org.olat.modules.ceditor.model.TableColumn;
import org.olat.modules.ceditor.model.TableContent;
import org.olat.modules.ceditor.model.TableRow;
import org.olat.modules.ceditor.model.TableSettings;
import org.olat.modules.ceditor.model.TextSettings;
import org.olat.modules.ceditor.model.TitleSettings;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * The XStream has its security features enabled.
 * 
 * Initial date: 5 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContentEditorXStream {
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				ImageSettings.class, ImageHorizontalAlignment.class, ImageTitlePosition.class, ImageSize.class,
				TextSettings.class, TitleSettings.class, ContainerSettings.class, ContainerLayout.class, ContainerColumn.class,
				TableContent.class, TableRow.class, TableColumn.class, TableSettings.class,
				CodeSettings.class, CodeLanguage.class,
				GeneralStyleSettings.class,
				BlockLayoutSettings.class, MediaSettings.class, MathSettings.class,
				AlertBoxSettings.class,
				QuizSettings.class, QuizQuestion.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));

		xstream.alias("imagesettings", ImageSettings.class);
		xstream.alias("imagehorizontalalignment", ImageHorizontalAlignment.class);
		xstream.alias("imagetitleposition", ImageTitlePosition.class);
		xstream.alias("imagesize", ImageSize.class);

		xstream.alias("textsettings", TextSettings.class);
		xstream.alias("titlesettings", TitleSettings.class);
		
		xstream.alias("containersettings", ContainerSettings.class);
		xstream.alias("containercolumn", ContainerColumn.class);
		
		xstream.alias("tablecontent", TableContent.class);
		xstream.alias("tablerow", TableRow.class);
		xstream.alias("tablecolumn", TableColumn.class);
		xstream.alias("tablesettings", TableSettings.class);

		xstream.alias("codesettings", CodeSettings.class);
		xstream.alias("codelanguage", CodeLanguage.class);

		xstream.alias("generalstylesettings", GeneralStyleSettings.class);

		xstream.alias("blocklayoutsettings", BlockLayoutSettings.class);
		xstream.alias("mediasettings", MediaSettings.class);
		xstream.alias("mathsettings", MathSettings.class);

		xstream.alias("alertboxsettings", AlertBoxSettings.class);

		xstream.alias("quizsettings", QuizSettings.class);
		xstream.alias("quizquestion", QuizQuestion.class);
	}
	
	public static String toXml(Object obj) {
		return xstream.toXML(obj);
	}
	
	@SuppressWarnings("unchecked")
	public static <U> U fromXml(String xml, @SuppressWarnings("unused") Class<U> cl) {
		Object obj = xstream.fromXML(xml);
		return (U)obj;
	}
}
