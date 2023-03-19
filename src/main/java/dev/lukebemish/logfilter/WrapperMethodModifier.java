package dev.lukebemish.logfilter;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class WrapperMethodModifier extends ClassVisitor {

    public WrapperMethodModifier(int api, @NotNull ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (name.equals("main")) {
            var visitor = cv.visitMethod(access, name, descriptor, signature, exceptions);
            visitor.visitCode();
            visitor.visitVarInsn(Opcodes.ALOAD, 0);
            visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "dev/lukebemish/logfilter/wrapper/OutputWrapper", "main", "([Ljava/lang/String;)V", false);
            visitor.visitInsn(Opcodes.RETURN);
            visitor.visitMaxs(1,1);
            visitor.visitEnd();
            return cv.visitMethod(access, "mainWrapped", descriptor, signature, exceptions);
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
}
