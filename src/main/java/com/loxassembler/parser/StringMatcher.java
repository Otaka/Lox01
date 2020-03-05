package com.loxassembler.parser;

import java.util.regex.Pattern;

/**
 * @author Dmitry
 */
public class StringMatcher {

    private int patternGroupId;
    private String label;
    private Pattern pattern;

    public StringMatcher(Pattern pattern, int patternGroupId) {
        this.patternGroupId = patternGroupId;
        this.pattern = pattern;
    }
    
    public StringMatcher(String pattern, int patternGroupId) {
        this.patternGroupId = patternGroupId;
        this.pattern = Pattern.compile(pattern);
    }

    public StringMatcher(Pattern pattern) {
        this.pattern = pattern;
    }

    public StringMatcher(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public int getPatternGroupId() {
        return patternGroupId;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
