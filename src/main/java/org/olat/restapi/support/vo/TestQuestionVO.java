package org.olat.restapi.support.vo;


import java.util.List;

public class TestQuestionVO {
    private String content;
    private String id;
    private String type;
    private List<TestAnswerVO> answers;
    List<String> correctAnswers;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TestAnswerVO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<TestAnswerVO> answers) {
        this.answers = answers;
    }

    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(List<String> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}