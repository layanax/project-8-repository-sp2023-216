import components.queue.Queue;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.statement.Statement;
import components.statement.Statement1;
import components.utilities.Reporter;
import components.utilities.Tokenizer;

/**
 * Layered implementation of secondary methods {@code parse} and
 * {@code parseBlock} for {@code Statement}.
 *
 * @author Layan Abdallah & Oak Hodous
 *
 */
public final class Statement1Parse1 extends Statement1 {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Converts {@code c} into the corresponding {@code Condition}.
     *
     * @param c
     *            the condition to convert
     * @return the {@code Condition} corresponding to {@code c}
     * @requires [c is a condition string]
     * @ensures parseCondition = [Condition corresponding to c]
     */
    private static Condition parseCondition(String c) {
        assert c != null : "Violation of: c is not null";
        assert Tokenizer
                .isCondition(c) : "Violation of: c is a condition string";
        return Condition.valueOf(c.replace('-', '_').toUpperCase());
    }

    /**
     * Parses an IF or IF_ELSE statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires <pre>
     * [<"IF"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [an if string is a proper prefix of #tokens] then
     *  s = [IF or IF_ELSE Statement corresponding to if string at start of #tokens]  and
     *  #tokens = [if string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static void parseIf(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0 && tokens.front().equals("IF") : ""
                + "Violation of: <\"IF\"> is proper prefix of tokens";

        //assign token to start and check
        String start = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isKeyword(start),
                "token is invalid");

        //assign token to condition and check
        String condition = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isCondition(condition),
                "condition is invalid");

        //parse condition and check THEN
        Condition c = parseCondition(condition);
        Reporter.assertElseFatalError(tokens.dequeue().equals("THEN"),
                "token is invalid");

        //create new statement and parse block
        Statement s1 = s.newInstance();
        s1.parseBlock(tokens);
        if (tokens.front().equals("ELSE")) {
            Reporter.assertElseFatalError(tokens.dequeue().equals("ELSE"),
                    "Invalid token");
            //create new statement for ELSE
            Statement s2 = s.newInstance();
            s2.parseBlock(tokens);
            s.assembleIfElse(c, s1, s2);
        } else {
            s.assembleIf(c, s1);
        }
        /*
         * Dequeue end.
         */
        Reporter.assertElseFatalError(tokens.dequeue().equals("END"),
                "Invalid token");
        String endKind = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isKeyword(endKind),
                "Invalid token");
    }

    /**
     * Parses a WHILE statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires <pre>
     * [<"WHILE"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [a while string is a proper prefix of #tokens] then
     *  s = [WHILE Statement corresponding to while string at start of #tokens]  and
     *  #tokens = [while string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static void parseWhile(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0 && tokens.front().equals("WHILE") : ""
                + "Violation of: <\"WHILE\"> is proper prefix of tokens";

        //assign token to start and check
        String start = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isKeyword(start),
                "Invalid token");

        //assign token to condition and check
        String condition = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isCondition(condition),
                "Invalid token");

        //parse condition then check DO
        Condition con = parseCondition(condition);
        Reporter.assertElseFatalError(tokens.dequeue().equals("DO"),
                "Invalid token");

        //create new statement and parse block
        Statement s1 = s.newInstance();
        s1.parseBlock(tokens);
        s.assembleWhile(con, s1);

        String error = tokens.dequeue();

        //check for END token
        Reporter.assertElseFatalError(error.equals("END"),
                "Invalid token test" + error);
        String endKind = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isKeyword(endKind),
                "Invalid token");

    }

    /**
     * Parses a CALL statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires [identifier string is a proper prefix of tokens]
     * @ensures <pre>
     * s =
     *   [CALL Statement corresponding to identifier string at start of #tokens]  and
     *  #tokens = [identifier string at start of #tokens] * tokens
     * </pre>
     */
    private static void parseCall(Queue<String> tokens, Statement s) {

        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0
                && Tokenizer.isIdentifier(tokens.front()) : ""
                        + "Violation of: identifier string is proper prefix of"
                        + "tokens error:" + tokens.front();

        //checks if token is a valid identifier
        String name = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isIdentifier(name),
                "Invalid token");
        s.assembleCall(name);

    }

    /*
     * Constructors -----------------------------------------------------------
     */

    /**
     * No-argument constructor.
     */
    public Statement1Parse1() {
        super();
    }

    /*
     * Public methods ---------------------------------------------------------
     */

    @Override
    public void parse(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        String name = tokens.front();
        Reporter.assertElseFatalError(
                Tokenizer.isIdentifier(name) || Tokenizer.isKeyword(name),
                "Invalid token");

        //parse and execute WHILE or IF or CALL
        if (name.equals("WHILE")) {
            parseWhile(tokens, this);
        } else if (name.equals("IF")) {
            parseIf(tokens, this);
        } else {
            parseCall(tokens, this);
        }

    }

    @Override
    public void parseBlock(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        Statement s = this.newInstance();

        //parse statements until there are no more statements/blocks to parse
        while (Tokenizer.isIdentifier(tokens.front())
                || tokens.front().equals("IF")
                || tokens.front().equals("WHILE")) {
            this.parse(tokens);
            s.addToBlock(s.lengthOfBlock(), this);
        }
        //transfer parsed block to current block
        this.transferFrom(s);

    }

    /*
     * Main test method -------------------------------------------------------
     */

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        /*
         * Get input file name
         */
        out.print("Enter valid BL statement(s) file name: ");
        String fileName = in.nextLine();
        /*
         * Parse input file
         */
        out.println("*** Parsing input file ***");
        Statement s = new Statement1Parse1();
        SimpleReader file = new SimpleReader1L(fileName);
        Queue<String> tokens = Tokenizer.tokens(file);
        file.close();
        s.parse(tokens); // replace with parseBlock to test other method
        /*
         * Pretty print the statement(s)
         */
        out.println("*** Pretty print of parsed statement(s) ***");
        s.prettyPrint(out, 0);

        in.close();
        out.close();
    }

}
