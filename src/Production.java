
import java.util.List;

public class Production {

    private String lhs;
    private List<String> rhs;

    public Production(String lhs, List<String> rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public String getLhs() {
        return lhs;
    }

    public List<String> getRhs() {
        return rhs;
    }

    public void display() {
        System.out.println(lhs + " -> " + String.join(" ", rhs));
    }
}
