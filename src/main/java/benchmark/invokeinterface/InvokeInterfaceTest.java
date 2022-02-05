package benchmark.invokeinterface;

import benchmark.invokeinterface.test.AbstractTest;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class InvokeInterfaceTest {

    public AbstractTest testImpl;
    
    @Param({"1"})
    public int interfaceCount = 1;
    
    @Param({"1"})
    public int methodsPerInterface;
    
    @Param({"false"})
    public boolean firstMethod;

    @Setup(Level.Trial)
    public void setup() throws Exception {
    	BenchmarkClassLoader bcl = new BenchmarkClassLoader(interfaceCount, methodsPerInterface, false, firstMethod);
    	
    	Object implementation = bcl.loadClass("benchmark.invokeinterface.test.Implementation")
        		.getDeclaredConstructor()
        		.newInstance();
    	
        this.testImpl = (AbstractTest) bcl.loadClass("benchmark.invokeinterface.test.Test")
        		.getDeclaredConstructor(Object.class)
        		.newInstance(implementation);
    }

    @Fork(1)
    @Warmup(timeUnit = TimeUnit.SECONDS, time = 1, iterations = 3)
    @Measurement(timeUnit = TimeUnit.SECONDS, time = 1, iterations = 5)
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void test(Blackhole bh) {
        bh.consume(this.testImpl.performTest());
    }
}
