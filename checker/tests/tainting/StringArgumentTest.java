import org.checkerframework.checker.tainting.qual.Tainted;
import org.checkerframework.checker.tainting.qual.Untainted;

public class StringArgumentTest {

    @Untainted("SQL") String verify(@Tainted("SQL") String s) {
        // :: error: (return.type.incompatible)
        return s;
    }

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
