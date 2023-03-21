package dev.lukebemish.logfilter;

import java.util.regex.Pattern;

import org.gradle.api.provider.ListProperty;

abstract public class LogFilterExtension {
    public abstract ListProperty<String> getRemove();

    public void remove(String pattern) {
        Pattern.compile(pattern);
        getRemove().add(pattern);
    }

    public void removeLine(String pattern) {
        Pattern.compile(pattern);
        getRemove().add("^"+pattern+"$(\\n|\\r)*");
    }
}
