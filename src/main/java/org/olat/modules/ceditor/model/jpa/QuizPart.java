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
package org.olat.modules.ceditor.model.jpa;

import java.io.Serial;
import java.util.List;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.modules.ceditor.manager.ContentEditorQti;
import org.olat.modules.ceditor.model.QuizElement;
import org.olat.modules.ceditor.model.QuizQuestion;
import org.olat.modules.ceditor.model.QuizSettings;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.model.MediaImpl;
import org.olat.modules.cemedia.model.MediaVersionImpl;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

/**
 * Initial date: 2024-03-11<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name="cequizpart")
public class QuizPart extends AbstractPart implements QuizElement {

	@ManyToOne(targetEntity=MediaImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_media_id", nullable=false, insertable=true, updatable=true)
	private Media backgroundImageMedia;

	@ManyToOne(targetEntity=MediaVersionImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_media_version_id", nullable=true, insertable=true, updatable=true)
	private MediaVersion backgroundImageMediaVersion;

	@ManyToOne(targetEntity= IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_identity_id", nullable=true, insertable=true, updatable=true)
	private Identity backgroundImageIdentity;

	@Serial
	private static final long serialVersionUID = 3265460228842353944L;

	public Media getBackgroundImageMedia() {
		return backgroundImageMedia;
	}

	public void setBackgroundImageMedia(Media backgroundImageMedia) {
		this.backgroundImageMedia = backgroundImageMedia;
	}

	public MediaVersion getBackgroundImageMediaVersion() {
		return backgroundImageMediaVersion;
	}

	public void setBackgroundImageMediaVersion(MediaVersion backgroundImageMediaVersion) {
		this.backgroundImageMediaVersion = backgroundImageMediaVersion;
	}

	public Identity getBackgroundImageIdentity() {
		return backgroundImageIdentity;
	}

	public void setBackgroundImageIdentity(Identity backgroundImageIdentity) {
		this.backgroundImageIdentity = backgroundImageIdentity;
	}

	@Override
	@Transient
	public String getType() {
		return "quiz";
	}

	@Override
	public QuizPart copy() {
		QuizPart part = new QuizPart();
		copy(part);
		part.setBackgroundImageMedia(getBackgroundImageMedia());
		part.setBackgroundImageMediaVersion(getBackgroundImageMediaVersion());
		return part;
	}

	@Override
	public boolean afterCopy() {
		ContentEditorQti contentEditorQti = CoreSpringFactory.getImpl(ContentEditorQti.class);
		String targetStoragePath = contentEditorQti.generateStoragePath(this);
		setStoragePath(targetStoragePath);

		QuizSettings quizSettings = getSettings();
		List<QuizQuestion> questions = quizSettings.getQuestions();
		if (questions != null) {
			for (QuizQuestion question : questions) {
				String relativeTargetFilePath = contentEditorQti.copyQuestion(question, targetStoragePath);
				question.setXmlFilePath(relativeTargetFilePath);
			}
		}
		setSettings(quizSettings);

		return true;
	}
}
