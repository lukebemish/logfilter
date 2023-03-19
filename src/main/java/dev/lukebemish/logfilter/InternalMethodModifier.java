package dev.lukebemish.logfilter;

import org.gradle.internal.impldep.com.sun.istack.NotNull;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InternalMethodModifier extends ClassVisitor {
    protected InternalMethodModifier(int api, @NotNull ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (name.equals("runMain")) {
            var visitor = cv.visitMethod(access, name, descriptor, signature, exceptions);
            visitor.visitCode();
            visitor.visitVarInsn(Opcodes.ALOAD, 0);
            visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "org/gradle/wrapper/GradleWrapperMain", "mainWrapped", "([Ljava/lang/String;)V", false);
            visitor.visitInsn(Opcodes.RETURN);
            visitor.visitMaxs(1,1);
            visitor.visitEnd();
            return null;
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
}
