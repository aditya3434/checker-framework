import org.checkerframework.checker.tainting.qual.Tainted;
import org.checkerframework.checker.tainting.qual.Untainted;

public class IndirectInformationFlow {

    @SuppressWarnings("return.type.incompatible")
    @Untainted int getSecureNumber() {
        return 1234;
    }

    void main() {

        int socialSecurityNo = getSecureNumber();
        @Tainted int i = 1234;
        // Tainted object comparison with untainted object
        // If -Aflag option is enabled, [condition.flow.unsafe] error would be raised here
        if (i == socialSecurityNo) {
            System.out.println(i);
        }

        // Method invocation with an untainted object
        // If -Aflag option is enabled, [method.invocation.flow.unsafe] error would be raised here
        @Untainted String s = "Secure string";
        if (s.contains("Secure")) {
            System.out.println(s);
        }
    }
}
