package de.tuberlin.tfdacmacs.client.policy;

import de.tuberlin.tfdacmacs.client.antlr.PolicyBaseListener;
import de.tuberlin.tfdacmacs.client.antlr.PolicyLexer;
import de.tuberlin.tfdacmacs.client.antlr.PolicyParser;
import lombok.NonNull;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Component;

@Component
public class AccessPolicyParser {

    public void parse(@NonNull String policy) {
        PolicyLexer policyLexer = new PolicyLexer(CharStreams.fromString(policy));
        CommonTokenStream commonTokenStream = new CommonTokenStream(policyLexer);
        PolicyParser parser = new PolicyParser(commonTokenStream);
        ParseTreeWalker parseTreeWalker = ParseTreeWalker.DEFAULT;
        parseTreeWalker.walk(new ParserListener(), parser.policy());
    }

    public class ParserListener extends PolicyBaseListener {

        @Override
        public void enterPolicy(PolicyParser.PolicyContext ctx) {
            System.out.println(ctx.start.getType() + " " + ctx.getText());
        }

        @Override public void enterOr_expression(PolicyParser.Or_expressionContext ctx) {
            System.out.println("-- OR --");
            System.out.println(ctx.getChild(0).getText());
            System.out.println(ctx.getChild(2).getText());
        }
    }
}
