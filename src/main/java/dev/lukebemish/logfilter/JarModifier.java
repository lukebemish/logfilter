package dev.lukebemish.logfilter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import dev.lukebemish.logfilter.wrapper.OutputWrapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

abstract public class JarModifier {
    public static final String classInJar = "org/gradle/wrapper/GradleWrapperMain.class";
    public static final String existing = OutputWrapper.class.getName().replace('.', '/') + ".class";



    static byte[] findClass(File directory, String clazz) throws IOException {
        try (var is = new BufferedInputStream(new FileInputStream(directory.toPath().resolve(clazz).toFile()))) {
            return is.readAllBytes();
        }
    }

    static void putClass(byte[] clazz, File directory, String path) throws IOException {
        File output = directory.toPath().resolve(path).toFile();
        output.getParentFile().mkdirs();
        try (var writer = new BufferedOutputStream(new FileOutputStream(output, false))) {
            writer.write(clazz);
        }
    }

    static void patchJar(File output) throws IOException {
        byte[] clazz = findClass(output, classInJar);
        clazz = transform(clazz);
        putClass(clazz, output, classInJar);
        copy(output);
    }

    static void patchInternal(File output) throws IOException {
        byte[] clazz = findClass(output, existing);
        clazz = transformInternal(clazz);
        putClass(clazz, output, existing);
    }

    public static byte[] transform(byte[] bytes) {
        var reader = new ClassReader(bytes);
        var writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);

        reader.accept(new WrapperMethodModifier(Opcodes.ASM9, writer), 0);
        return writer.toByteArray();
    }

    public static byte[] transformInternal(byte[] bytes) {
        var reader = new ClassReader(bytes);
        var writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);

        reader.accept(new InternalMethodModifier(Opcodes.ASM9, writer), 0);
        return writer.toByteArray();
    }

    static void copy(File outputDir) throws IOException {
        copyClass(outputDir, OutputWrapper.class, new HashSet<>());
        patchInternal(outputDir);
    }

    private static void copyClass(File outputDir, Class<?> clazz, Set<Class<?>> stack) throws IOException {
        stack.add(clazz);
        var name = clazz.getName().replace('.', '/') + ".class";
        try (var is = new BufferedInputStream(Objects.requireNonNull(clazz.getClassLoader().getResourceAsStream(name)))) {
            Objects.requireNonNull(is);
            var bytes = is.readAllBytes();
            putClass(bytes, outputDir, name);
        }
        for (Class<?> sub : clazz.getNestMembers()) {
            if (stack.contains(sub)) {
                continue;
            }
            copyClass(outputDir, sub, stack);
        }
    }

    static void writeFilter(WrapperCopyTask task, File directory) throws IOException {
        var remove = new File(directory, "META-INF/dev.lukebemish.logfilter/remove");
        remove.getParentFile().mkdirs();
        try (var writer = new BufferedWriter(new FileWriter(remove, false))) {
            for (String filter : task.getRemove().get()) {
                writer.write(filter);
                writer.newLine();
            }
        }
    }
}
