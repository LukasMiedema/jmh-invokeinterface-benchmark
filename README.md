# invokeinterface benchmark
## JMH benchmark testing VM `invokeinterface` throughput

The benchmark uses its own class loader to generate a set of interfaces and an implementation of those interfaces.

**Structure**

It generates
- one `Test` class, containing the `invokeinterface` instruction
- one `Implementation` class, implementing a number of interfaces (configurable)
- a number of `InterfaceN` classes, each specifying a number of methods (configurable)

**Building**

- Java >= 1.8 (tested with 1.8 and 11)
- Tested with Maven 3.6.3
- `mvn clean package`

**Invocation**

`java -cp target/benchmarks.jar org.openjdk.jmh.Main -rf csv -rff my-results.csv -jvm java`

**Options**

- `firstMethod: boolean` when true, call the first method in the generated interface, otherwise call the last method in the generated interface
- `firstInterface: boolean` when true, call a method in the first interface in the list of interfaces assigned to the implementation, otherwise call a method in the last interface
- `interfaceCount: int` number of interfaces implemented by the `Implementation`
- `methodsPerInterface: int` number of methods to generate, per interface. Note that `Implementation` will have `interfaceCount` x `methodsPerInterface` methods in total, for which high numbers may cause problems with some VMs.

Options can be specified with `-p ...`, e.g. `-p methodsPerInterface=1,500,1000,1500,2000,2500,3000,3500,4000,4500,5000`