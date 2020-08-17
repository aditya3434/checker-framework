import org.checkerframework.checker.tainting.qual.Tainted;
import org.checkerframework.checker.tainting.qual.Untainted;

public class StringArgumentTest {

    @SuppressWarnings("return.type.incompatible")
    @Untainted("SQL") String verify(@Tainted("SQL") String s) {
        return s;
    }

    @SuppressWarnings("return.type.incompatible")
    @Tainted({"SQL", "OS"}) String getStatement() {
        return "Statement";
    }

    void executeSQL(@Untainted("SQL") String s) {}

    void main() {
        @Tainted({"SQL", "OS"}) String s = getStatement();
        // :: error: (argument.type.incompatible)
        executeSQL(s);
        String sql = verify(s);
        executeSQL(sql);
    }
}
