package benchmark.invokeinterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TestBuilder implements Opcodes {
	
	private static final int METHODS_PER_CLASS = 2000;
	
	private final Map<String, byte[]> definedClasses;
	
    public TestBuilder(Map<String, byte[]> definedClasses) {
		this.definedClasses = definedClasses;
	}

	public void build(int interfaces, int methodsPerInterface, boolean firstInterface, boolean firstMethod) throws Exception {
        List<String> methodsToImplement = new ArrayList<>();
        
        for (int id = 0; id < interfaces; id++)
            createClass("Interface" + id, makeInterface(id, methodsPerInterface, methodsToImplement));
        
        createClass("Implementation", makeImplementation(interfaces, methodsToImplement, 0));
        
        int interfaceIdToCall = firstInterface ? 0 : (interfaces - 1);
        int methodToCall = firstMethod ? 0 : (methodsPerInterface - 1);
        createClass("Test", makeTest(interfaceIdToCall, methodToCall));
    }

    private void createClass(String name, byte[] data) throws Exception {
        String fqn = "benchmark.invokeinterface.test." + name;
        definedClasses.put(fqn, data);
    }
    
    private byte[] makeTest(int interfaceToCall, int methodToCall) {

		ClassWriter classWriter = new ClassWriter(0);
		FieldVisitor fieldVisitor;
		MethodVisitor methodVisitor;
		
		String sig = "get" + interfaceToCall + "_" + methodToCall;
		String implementedInterface = "Interface" + interfaceToCall;

		classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "benchmark/invokeinterface/test/Test", null,
				"benchmark/invokeinterface/test/AbstractTest", null);

		classWriter.visitSource("Test.java", null);

		{
			fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_FINAL, "subject",
					"Lbenchmark/invokeinterface/test/" + implementedInterface + ";", null, null);
			fieldVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>",
					"(Ljava/lang/Object;)V", null, null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(7, label0);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitMethodInsn(INVOKESPECIAL, "benchmark/invokeinterface/test/AbstractTest", "<init>", "()V",
					false);
			Label label1 = new Label();
			methodVisitor.visitLabel(label1);
			methodVisitor.visitLineNumber(8, label1);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitVarInsn(ALOAD, 1);
			methodVisitor.visitFieldInsn(PUTFIELD, "benchmark/invokeinterface/test/Test", "subject",
					"Lbenchmark/invokeinterface/test/" + implementedInterface + ";");
			Label label2 = new Label();
			methodVisitor.visitLabel(label2);
			methodVisitor.visitLineNumber(9, label2);
			methodVisitor.visitInsn(RETURN);
			Label label3 = new Label();
			methodVisitor.visitLabel(label3);
			methodVisitor.visitLocalVariable("this", "Lbenchmark/invokeinterface/test/Test;", null, label0, label3, 0);
			methodVisitor.visitLocalVariable("subject", "Lbenchmark/invokeinterface/test/" + implementedInterface + ";", null, label0,
					label3, 1);
			methodVisitor.visitMaxs(2, 2);
			methodVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "performTest", "()I", null, null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(13, label0);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "benchmark/invokeinterface/test/Test", "subject",
					"Lbenchmark/invokeinterface/test/" + implementedInterface + ";");
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "benchmark/invokeinterface/test/" + implementedInterface, sig,
					"()I", true);
			methodVisitor.visitInsn(IRETURN);
			Label label1 = new Label();
			methodVisitor.visitLabel(label1);
			methodVisitor.visitLocalVariable("this", "Lbenchmark/invokeinterface/test/Test;", null, label0, label1, 0);
			methodVisitor.visitMaxs(1, 1);
			methodVisitor.visitEnd();
		}
		classWriter.visitEnd();

		return classWriter.toByteArray();
    }

    private byte[] makeInterface(int interfaceId, int methods, List<String> methodsToImplement) throws Exception {

        ClassWriter classWriter = new ClassWriter(0);
        
        int interfaceCount = (int) Math.ceil((double)methods / METHODS_PER_CLASS);
        String[] superInterfaces = new String[interfaceCount];
        for (int i = 0; i < interfaceCount; i++) {
        	int methodsStart = i * METHODS_PER_CLASS;
        	int methodsEnd = Math.min(methods, methodsStart + METHODS_PER_CLASS);
        	createClass("InterfaceD" + interfaceId + "_" + methodsStart, makeInterfaceDelegate(interfaceId, methodsStart, methodsEnd, methodsToImplement));
        	superInterfaces[i] = "benchmark/invokeinterface/test/InterfaceD" + interfaceId + "_" + methodsStart;
        }

        classWriter.visit(V1_8, ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE, "benchmark/invokeinterface/test/Interface" + interfaceId, null, "java/lang/Object", superInterfaces);
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
    
    private byte[] makeInterfaceDelegate(int interfaceId, int methodsStart, int methodsEnd, List<String> methodsToImplement) throws Exception {
    	
    	ClassWriter classWriter = new ClassWriter(0);
    	MethodVisitor methodVisitor;
    	
    	classWriter.visit(V1_8, ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE, "benchmark/invokeinterface/test/InterfaceD" + interfaceId + "_" + methodsStart, null, "java/lang/Object", null);
    	
    	for (int i = methodsStart; i < methodsEnd; i++) {
    		String sig = "get" + interfaceId + "_" + i;
    		methodsToImplement.add(sig);
    		methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, sig, "()I", null, null);
    		methodVisitor.visitEnd();
    	}
    	classWriter.visitEnd();
    	
    	return classWriter.toByteArray();
    }

    private byte[] makeImplementation(int interfaces, List<String> methods, int depth) throws Exception {
        ClassWriter classWriter = new ClassWriter(0);
        MethodVisitor methodVisitor;
        
        // List of methods may be too long -> generate a delegate that we extend
        String supertype = "java/lang/Object";
        if (methods.size() > METHODS_PER_CLASS) {
        	List<String> submethods = methods.subList(METHODS_PER_CLASS, methods.size());
        	supertype = "benchmark/invokeinterface/test/Implementation" + (depth + 1);
        	createClass("Implementation" + (depth + 1), makeImplementation(0, submethods, depth + 1));
        	methods = methods.subList(0, METHODS_PER_CLASS);
        }
        
        String typename = "benchmark/invokeinterface/test/Implementation" + (depth == 0 ? "" : depth);

        String[] implementedInterfaces = new String[interfaces];
        for (int p = 0; p < interfaces; p++)
            implementedInterfaces[p] = "benchmark/invokeinterface/test/Interface" + p;
        classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, typename, null, supertype, implementedInterfaces);


        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(3, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, supertype, "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "L" + typename + ";", null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }

        for (String method : methods) {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, method, "()I", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(7, label0);
            methodVisitor.visitLdcInsn(1400);
            methodVisitor.visitInsn(IRETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "L" + typename + ";", null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
}
