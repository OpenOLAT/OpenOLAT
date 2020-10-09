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
