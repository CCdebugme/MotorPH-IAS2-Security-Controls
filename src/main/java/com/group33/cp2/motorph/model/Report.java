package com.group33.cp2.motorph.model;

// Wraps a generated system report's text content in a typed object.
public class Report {

    private String content;

    public Report(String content) {
        this.content = content;
    }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    @Override
    public String toString() {
        return "Report {content='" + content + "'}";
    }
}
