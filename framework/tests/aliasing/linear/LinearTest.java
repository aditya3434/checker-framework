import org.checkerframework.common.aliasing.qual.Linear;

public class LinearTest {

    void Test() {
        @Linear String s = getLinear();

        String b = s.toLowerCase();
        // Due to the method invocation, s is used up and is now unusable
        // Since s is unusable now, it can't be used for assignment, method invocation or as an
        // argument

        // :: error: (use.unsafe)
        boolean c = s.isEmpty();

        // :: error: (use.unsafe)
        String d = s.toUpperCase();
    }

    @SuppressWarnings("return.type.incompatible")
    static @Linear String getLinear() {
        return "hi";
    }
}