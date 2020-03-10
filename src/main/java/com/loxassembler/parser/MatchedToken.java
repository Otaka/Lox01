package com.loxassembler.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry
 */
public class MatchedToken {

    private StringMatcher matcher;
    private String text;
    private Map<String, StringWithPosition> matchedGroups = new HashMap<>();
    private int position;
    private int length;

    public MatchedToken(StringMatcher matcher, String text, int position, int length) {
        this.matcher = matcher;
        this.text = text;
        this.position = position;
        this.length = length;
    }

    public MatchedToken() {
    }

    public Map<String, StringWithPosition> getMatchedGroups() {
        return matchedGroups;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setMatcher(StringMatcher matcher) {
        this.matcher = matcher;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLength() {
        return length;
    }

    public StringMatcher getMatcher() {
        return matcher;
    }

    public int getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }

}
