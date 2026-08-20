/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.sections.Section;
import org.olat.core.gui.components.sections.Sections;
import org.olat.core.gui.components.sections.SectionsFactory;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumElementInfoTaughtByController;
import org.olat.modules.curriculum.ui.CurriculumElementInfosOutlineController;
import org.olat.modules.lecture.LectureBlock;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.author.MediaContainerFilter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 18 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementInfoPageSectionsController extends BasicController {

	private CurriculumElementInfosOutlineController outlineCtrl;
	private CurriculumElementInfoTaughtByController taughtByCtrl;

	private final boolean hasContent;

	@Autowired
	private CurriculumService curriculumService;

	public CurriculumElementInfoPageSectionsController(UserRequest ureq, WindowControl wControl, CurriculumElement element, List<LectureBlock> lectureBlocks) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(),
				Util.createPackageTranslator(CurriculumElementInfosOutlineController.class, getLocale(), getTranslator())));

		VFSContainer mediaContainer = curriculumService.getMediaContainer(element);
		if (mediaContainer != null && mediaContainer.getName().equals("media")) {
			mediaContainer = mediaContainer.getParentContainer();
			mediaContainer.setDefaultItemFilter(new MediaContainerFilter(mediaContainer));
		}
		String baseUrl = mediaContainer != null ? registerMapper(ureq, new VFSContainerMapper(mediaContainer)) : null;

		List<Section> sections = new ArrayList<>();
		addTextSection(sections, "description", "cif.description", element.getDescription(), baseUrl);

		if (element.isShowOutline()) {
			outlineCtrl = new CurriculumElementInfosOutlineController(ureq, wControl, element, lectureBlocks);
			listenTo(outlineCtrl);
			if (!outlineCtrl.isEmpty()) {
				sections.add(SectionsFactory.createSection("outline", translate("infos.outline"), outlineCtrl.getInitialComponent()));
			}
		}

		if (!element.getTaughtBys().isEmpty()) {
			taughtByCtrl = new CurriculumElementInfoTaughtByController(ureq, wControl, element, lectureBlocks);
			listenTo(taughtByCtrl);
			if (!taughtByCtrl.isEmpty()) {
				sections.add(SectionsFactory.createSection("taughtby", translate("infos.taughtby"), taughtByCtrl.getInitialComponent()));
			}
		}

		addTextSection(sections, "objectives", "cif.objectives", element.getObjectives(), baseUrl);
		addTextSection(sections, "requirements", "cif.requirements", element.getRequirements(), baseUrl);
		addTextSection(sections, "credits", "cif.credits", element.getCredits(), baseUrl);

		hasContent = !sections.isEmpty();
		Sections sectionsCmp = SectionsFactory.createSections("sections", null);
		sectionsCmp.setSections(sections);
		putInitialPanel(sectionsCmp);
	}

	private void addTextSection(List<Section> sections, String id, String titleI18nKey, String text, String baseUrl) {
		String html = getFormattedText(text, baseUrl);
		if (StringHelper.containsNonWhitespace(html)) {
			sections.add(SectionsFactory.createTextSection(id, translate(titleI18nKey), html));
		}
	}

	private String getFormattedText(String text, String baseUrl) {
		if (!StringHelper.containsNonWhitespace(text)) {
			return null;
		}

		String formattedText = StringHelper.xssScan(text);
		if (StringHelper.containsNonWhitespace(baseUrl)) {
			formattedText = FilterFactory.getBaseURLToMediaRelativeURLFilter(baseUrl).filter(formattedText);
		}
		return Formatter.formatLatexFormulas(formattedText);
	}

	public boolean hasContent() {
		return hasContent;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
