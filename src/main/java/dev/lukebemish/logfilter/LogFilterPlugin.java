package dev.lukebemish.logfilter;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.api.tasks.wrapper.Wrapper;
import org.jetbrains.annotations.NotNull;

public class LogFilterPlugin implements Plugin<Project> {
    @SuppressWarnings("Convert2Lambda")
    @Override
    public void apply(@NotNull Project project) {
        LogFilterExtension extension = project.getExtensions().create("logfilter", LogFilterExtension.class);
        project.getTasks().withType(Wrapper.class, task -> {
        });

        var into = project.getBuildDir().toPath().resolve("patchedWrapper").toFile();

        project.getTasks().register("patchWrapperClass", WrapperCopyTask.class, task -> {
            var wrapperTask = (Wrapper) project.getTasks().getByName("wrapper");
            task.dependsOn(wrapperTask);
            task.getRemove().set(extension.getRemove());
            task.from(project.zipTree(wrapperTask.getJarFile()));
            task.into(into);
            task.doLast(new Action<>() {
                @Override
                public void execute(@NotNull Task task1) {
                    try {
                        JarModifier.patchJar(into);
                        JarModifier.writeFilter(task, into);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            task.doFirst(new Action<>() {
                @Override
                public void execute(@NotNull Task task1) {
                    try {
                        FileUtils.deleteDirectory(into);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });

        project.getTasks().register("archiveWrapper", Zip.class, task -> {
            var wrapperTask = (Wrapper) project.getTasks().getByName("wrapper");
            task.dependsOn(wrapperTask);
            task.dependsOn("patchWrapperClass");
            task.from(into);
            var jarFile = wrapperTask.getJarFile();
            task.getDestinationDirectory().set(jarFile.getParentFile());
            task.getArchiveFileName().set(jarFile.getName());
        });

        project.getTasks().named("wrapper", task -> {
            task.finalizedBy("archiveWrapper");
        });
    }
}
