package com.loxassembler.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void skip() {
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
        matchedToken.getMatchedGroups().clear();
        matchedToken.setLength(0);
        matchedToken.setPosition(-1);
        matchedToken.setText(null);
        matchedToken.setMatcher(null);
        StringMatcher matcher = matchers.get(label);
        if (matcher == null) {
            throw new IllegalArgumentException("There is no defined matcher with label [" + label + "]");
        }

        skip();
        Matcher m = matcher.getPattern().matcher(string);
        if (m.find(currentOffset)) {
            if (m.start() == currentOffset) {
                int fullTokenLength = m.group(0).length();
                if (fullTokenLength == 0) {
                    throw new IllegalStateException("Matcher with regex [" + matcher.getPattern().pattern() + "] produced 0 length result. This will lead to infinite loop");
                }

                matchedToken.setText(m.group(0));
                matchedToken.setLength(fullTokenLength);
                matchedToken.setPosition(currentOffset);
                matchedToken.setMatcher(matcher);

                List<String> groupNames = getNamedGroupsFromPattern(matcher.getPattern());
                for (String groupName : groupNames) {
                    String groupValue=m.group(groupName);
                    if(groupValue!=null){
                        StringWithPosition str = new StringWithPosition(groupValue, m.start(groupName), m.end(groupName));
                        matchedToken.getMatchedGroups().put(groupName, str);
                    }
                }

                currentOffset += fullTokenLength;
                return true;
            }
        }

        return false;
    }

    public boolean isFinished() {
        return currentOffset == string.length();
    }

    public String getTextFromCurrentPosition(int length) {
        int maxLength = Math.min(length, string.length() - currentOffset);
        if (length != maxLength) {
            return string.substring(currentOffset, maxLength) + "...";
        } else {
            return string.substring(currentOffset, maxLength);
        }
    }

    public static List<String> getNamedGroupsFromPattern(Pattern regex) {
        try {
            Method namedGroupsMethod = Pattern.class.getDeclaredMethod("namedGroups");
            namedGroupsMethod.setAccessible(true);

            Map<String, Integer> namedGroups;
            namedGroups = (Map<String, Integer>) namedGroupsMethod.invoke(regex);

            if (namedGroups == null) {
                throw new InternalError();
            }

            return new ArrayList<>(namedGroups.keySet());
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
}
