package benchmark.invokeinterface;

import java.util.HashMap;
import java.util.Map;

public class BenchmarkClassLoader extends ClassLoader {
	
	private final Map<String, byte[]> definedClasses = new HashMap<>();
	
	public BenchmarkClassLoader(int interfaceCount, int methodsPerInterface, boolean firstInterface, boolean firstMethod) throws Exception {
		super(BenchmarkClassLoader.class.getClassLoader());
		new TestBuilder(definedClasses).build(interfaceCount, methodsPerInterface, firstInterface, firstMethod);
	}
	
	@Override
	public Class findClass(String name) throws ClassNotFoundException {
		if (definedClasses.containsKey(name)) {
			byte[] data = definedClasses.get(name);
			return defineClass(name, data, 0, data.length);
		}
		return super.findClass(name);
	}
}
