import org.checkerframework.checker.tainting.qual.Tainted;
import org.checkerframework.checker.tainting.qual.Untainted;

public class IndirectInformationFlow {

    @Untainted int getSecureNumber() {
        // :: error: (return.type.incompatible)
        return 1234;
    }

    @Tainted String getTaintedString() {
        return "Tainted String";
    }

    void main() {

        int socialSecurityNo = getSecureNumber();
        @Tainted int i = 1234;

        // :: error: (condition.flow.unsafe)
        if (i == socialSecurityNo) {
            System.out.println(i);
        }

        @Untainted String s = "Secure string";
        String t = getTaintedString();

        // :: error: (method.invocation.flow.unsafe)
        if (s.contains(t)) {
            System.out.println(s);
        }
    }
}
