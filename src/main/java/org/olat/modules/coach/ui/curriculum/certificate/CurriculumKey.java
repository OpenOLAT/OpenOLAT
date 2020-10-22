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
package org.olat.modules.coach.ui.curriculum.certificate;

public class CurriculumKey {
    private final Long curriculum;
    private final Long curriculumElement;
    private final boolean isWithoutCurriculum;

    public CurriculumKey(Long curriculum, Long curriculumElement) {
        this(curriculum, curriculumElement, false);
    }

    public CurriculumKey(Long curriculum, Long curriculumElement, boolean isWithoutCurriculum) {
        this.curriculum = curriculum;
        this.curriculumElement = curriculumElement;
        this.isWithoutCurriculum = isWithoutCurriculum;
    }

    public Long getCurriculum() {
        return curriculum;
    }

    public Long getCurriculumElement() {
        return curriculumElement;
    }

    public boolean isWithoutCurriculum() {
        return this.isWithoutCurriculum;
    }

    @Override
    public String toString() {
        return "CurriculumKey[" +
                "curriculum=" + curriculum +
                ", curriculumElement=" + curriculumElement +
                ']';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof CurriculumKey) {
            CurriculumKey compareKey = (CurriculumKey) obj;
            boolean equals = true;

            // Compare curriculum
            if ((curriculum == null && compareKey.getCurriculum() != null)
                    || (curriculum != null && compareKey.getCurriculum() == null)) {
                equals = false;
            } else if ((curriculum != null && compareKey.getCurriculum() != null)
                    && (!curriculum.equals(compareKey.getCurriculum()))) {
                equals = false;
            }

            // Compare curriculumElement
            if ((curriculumElement == null && compareKey.getCurriculumElement() != null)
                    || (curriculumElement != null && compareKey.getCurriculumElement() == null)) {
                equals = false;
            } else if ((curriculumElement != null && compareKey.getCurriculumElement() != null)
                    && (!curriculumElement.equals(compareKey.getCurriculumElement()))) {
                equals = false;
            }

            return equals;
        } else {
            return  false;
        }
    }

    @Override
    public int hashCode() {
        return (curriculum == null ? 890112 : curriculum.hashCode()) + (curriculumElement == null ? 312739 : curriculumElement.hashCode());
    }
}
