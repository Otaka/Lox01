package com.loxassembler.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author Dmitry
 */
public class StringParser {

    private Map<String, StringMatcher> matchers = new HashMap<>();
    private List<StringMatcher> skipBeforeEachStepMatcher = new ArrayList<>();
    private String string;
    private int currentOffset;

    public int getCurrentOffset() {
        return currentOffset;
    }

    public void setCurrentOffset(int currentOffset) {
        this.currentOffset = currentOffset;
    }

    public void addMatcher(String label, StringMatcher matcher) {
        matchers.put(label, matcher);
        matcher.setLabel(label);
    }

    public void addSkipMatcher(StringMatcher matcher) {
        skipBeforeEachStepMatcher.add(matcher);
    }

    public void reset(String string) {
        this.string = string;
        currentOffset = 0;
    }

    private void runSkipStage() {
        boolean modified = true;
        while (modified) {
            modified = false;
            for (StringMatcher skipMatcher : skipBeforeEachStepMatcher) {
                Matcher matcher = skipMatcher.getPattern().matcher(string);
                if (matcher.find(currentOffset) && matcher.start() == currentOffset) {
                    int length = matcher.group().length();
                    if (length == 0) {
                        throw new IllegalStateException("Matcher with regex [" + skipMatcher.getPattern().pattern() + "] produced 0 length result. This will lead to infinite loop");
                    }
                    currentOffset += length;
                    modified = true;
                }
            }
        }
    }

    public boolean match(String label, MatchedToken matchedToken) {
        StringMatcher matcher = matchers.get(label);
        if (matcher == null) {
            throw new IllegalArgumentException("There is no defined matcher with label [" + label + "]");
        }
        runSkipStage();
        Matcher m = matcher.getPattern().matcher(string);
        if (m.find(currentOffset)) {
            if (m.start() == currentOffset) {
                String tokenString = m.group(matcher.getPatternGroupId());
                int fullTokenLength = m.group(0).length();
                if (fullTokenLength == 0) {
                    throw new IllegalStateException("Matcher with regex [" + matcher.getPattern().pattern() + "] produced 0 length result. This will lead to infinite loop");
                }
                matchedToken.setText(tokenString);
                matchedToken.setLength(fullTokenLength);
                matchedToken.setPosition(currentOffset);
                matchedToken.setMatcher(matcher);
                currentOffset += fullTokenLength;
                return true;
            }
        }

        return false;
    }

    public boolean isFinished() {
        return currentOffset == string.length() - 1;
    }

    public String getTextFromCurrentPosition(int length) {
        int maxLength = Math.min(length, string.length() - currentOffset);
        if (length != maxLength) {
            return string.substring(currentOffset, maxLength) + "...";
        } else {
            return string.substring(currentOffset, maxLength);
        }
    }
}
