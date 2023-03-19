package dev.lukebemish.logfilter;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Input;

abstract public class WrapperCopyTask extends Copy {
    @Input
    abstract public ListProperty<String> getRemove();
}
