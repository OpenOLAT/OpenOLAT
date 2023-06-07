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
package org.olat.modules.openbadges;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.image.Size;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * Initial date: 2023-05-08<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface OpenBadgesManager {

	//
	// Template
	//

	void createTemplate(String name, File templateFile, String targetFileName, String description,
						String category, Collection<String> scopes, Identity savedBy);

	List<BadgeTemplate> getTemplates();

	List<TemplateWithSize> getTemplatesWithSizes();

	VFSLeaf getTemplateVfsLeaf(String templateImage);

	void updateTemplate(BadgeTemplate template);

	void deleteTemplate(BadgeTemplate template);


	//
	// Class
	//

	void createBadgeClass(String uuid, String version, File uploadedFile, String targetFileName,
						  String name, String description, String criteria, String issuer, String tags,
						  Identity savedBy);

	List<BadgeClass> getBadgeClasses();

	List<BadgeClassWithSize> getBadgeClassesWithSizes();

	BadgeClass getBadgeClass(String uuid);

	VFSLeaf getBadgeClassVfsLeaf(String classFile);

	void updateBadgeClass(BadgeClass badgeClass);

	void deleteBadgeClass(BadgeClass badgeClass);

	//
	// Assertion
	//

	void createBadgeAssertion(String uuid, String recipientEmail, BadgeClass badgeClass, Date issuedOn,
							  Identity savedBy);

	List<BadgeAssertion> getBadgeAssertions();

	List<BadgeAssertionWithSize> getBadgeAssertionsWithSizes();

	BadgeAssertion getBadgeAssertion(String uuid);

	void updateBadgeAssertion(BadgeAssertion badgeAssertion);

	void deleteBadgeAssertion(BadgeAssertion badgeAssertion);

	//
	// Types
	//

	enum FileType {
		png,
		svg
	}

	record TemplateWithSize (BadgeTemplate template, Size size) {
		public Size fitIn(int width, int height) {
			double sourceAspectRatio = (double) size.getWidth() / (double) size.getHeight();
			double targetAspectRatio = (double) width / (double) height;
			if (sourceAspectRatio > targetAspectRatio) {
				return new Size(width, (int) Math.round(width / sourceAspectRatio), false);
			} else {
				return new Size((int) Math.round(height * sourceAspectRatio), height, false);
			}
		}
	}

	record BadgeClassWithSize (BadgeClass badgeClass, Size size) {
		public Size fitIn(int width, int height) {
			double sourceAspectRatio = (double) size.getWidth() / (double) size.getHeight();
			double targetAspectRatio = (double) width / (double) height;
			if (sourceAspectRatio > targetAspectRatio) {
				return new Size(width, (int) Math.round(width / sourceAspectRatio), false);
			} else {
				return new Size((int) Math.round(height * sourceAspectRatio), height, false);
			}
		}
	}

	record BadgeAssertionWithSize (BadgeAssertion badgeAssertion, Size size) {
		public Size fitIn(int width, int height) {
			double sourceAspectRatio = (double) size.getWidth() / (double) size.getHeight();
			double targetAspectRatio = (double) width / (double) height;
			if (sourceAspectRatio > targetAspectRatio) {
				return new Size(width, (int) Math.round(width / sourceAspectRatio), false);
			} else {
				return new Size((int) Math.round(height * sourceAspectRatio), height, false);
			}
		}
	}
}
