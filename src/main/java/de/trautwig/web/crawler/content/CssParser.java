package de.trautwig.web.crawler.content;

import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.stream.Stream;

public class CssParser {

    private Stream.Builder<String> result = Stream.builder();

    public static CssParser fromStylesheet(Reader reader) throws IOException {
        InputSource source = new InputSource(reader);
        CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
        return new CssParser(parser.parseStyleSheet(source, null, null));
    }

    public static CssParser fromStylesheet(String stylesheet) {
        try {
            InputSource source = new InputSource(new StringReader(stylesheet));
            CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
            return new CssParser(parser.parseStyleSheet(source, null, null));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CssParser fromInline(String inline) {
        try {
            InputSource source = new InputSource(new StringReader(inline));
            CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
            return new CssParser(parser.parseStyleDeclaration(source));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CssParser(CSSStyleSheet styleSheet) {
        visit(styleSheet.getCssRules());
    }

    public CssParser(CSSStyleDeclaration inlineStyle) {
        visit(inlineStyle);
    }

    public Stream<String> getResult() {
        return result.build();
    }

    private void visit(CSSRuleList rules) {
        for (int idx = 0; idx < rules.getLength(); idx++) {
            visit(rules.item(idx));
        }
    }

    private void visit(CSSRule rule) {
        if (rule instanceof CSSImportRule) {
            visit((CSSImportRule) rule);
        } else if (rule instanceof CSSStyleRule) {
            visit((CSSStyleRule) rule);
        }
    }

    private void visit(CSSImportRule rule) {
        result.add(rule.getHref());
    }

    private void visit(CSSStyleRule rule) {
        visit(rule.getStyle());
    }

    private void visit(CSSStyleDeclaration style) {
        for (int idx = 0; idx < style.getLength(); idx++) {
            CSSValue value = style.getPropertyCSSValue(style.item(idx));
            visit(value);
        }
    }

    private void visit(CSSValue value) {
        if (CSSValue.CSS_VALUE_LIST == value.getCssValueType()) {
            visit((CSSValueList) value);
        } else if (CSSValue.CSS_PRIMITIVE_VALUE == value.getCssValueType()) {
            visit((CSSPrimitiveValue) value);
        }
    }

    private void visit(CSSValueList list) {
        for (int idx = 0; idx < list.getLength(); idx++) {
            visit(list.item(idx));
        }
    }

    private  void visit(CSSPrimitiveValue value) {
        if (CSSPrimitiveValue.CSS_URI == value.getPrimitiveType()) {
            result.add(value.getStringValue());
        }
    }

}
