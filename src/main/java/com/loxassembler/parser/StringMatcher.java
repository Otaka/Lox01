package com.loxassembler.parser;

import java.util.regex.Pattern;

/**
 * @author Dmitry
 */
public class StringMatcher {

    private String label;
    private Pattern pattern;

    public StringMatcher(Pattern pattern) {
        this.pattern = pattern;
    }

    public StringMatcher(String pattern) {
        this.pattern = Pattern.compile(pattern);
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
