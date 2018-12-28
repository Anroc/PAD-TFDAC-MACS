package de.tuberlin.tfdacmacs.basics.db.config;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.basics.gpp.GlobalPublicParameterProvider;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.plaf.jpbc.pbc.PBCField;
import it.unisa.dia.gas.plaf.jpbc.pbc.field.PBCG1Field;
import it.unisa.dia.gas.plaf.jpbc.pbc.field.PBCG2Field;
import it.unisa.dia.gas.plaf.jpbc.pbc.field.PBCGTField;
import it.unisa.dia.gas.plaf.jpbc.pbc.field.PBCZrField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

public class CouchbaseElementConverters {

    public enum PBCFieldType {
        G1, G2, GT, ZR, UNKOWN
    }

    @WritingConverter
    @Component
    public static class Write implements Converter<Element, String> {

        @Override
        public String convert(Element value) {
            PBCFieldType fieldType = PBCFieldType.UNKOWN;
            if(value instanceof PBCField) {
                if(value instanceof PBCG1Field) {
                    fieldType = PBCFieldType.G1;
                } else if( value instanceof PBCG2Field) {
                    fieldType = PBCFieldType.G2;
                } else if (value instanceof PBCGTField) {
                    fieldType = PBCFieldType.GT;
                } else if (value instanceof PBCZrField) {
                    fieldType = PBCFieldType.ZR;
                }
            }

            return new StringBuilder(ElementConverter.convert(value))
                    .append(':')
                    .append(fieldType.toString())
                    .toString();
        }
    }

    @ReadingConverter
    @Component
    @RequiredArgsConstructor
    @Slf4j
    public static class Read implements Converter<String, Element> {

        private final GlobalPublicParameterProvider gppProvider;

        @Override
        public Element convert(String value) {
            Field field;
            String[] split = value.split(":");
            switch (PBCFieldType.valueOf(split[1])) {
                case G2:
                    field = gppProvider.getGlobalPublicParameter().getPairing().getG2();
                    break;
                case GT:
                    field = gppProvider.getGlobalPublicParameter().getPairing().getGT();
                    break;
                case ZR:
                    field = gppProvider.getGlobalPublicParameter().getPairing().getZr();
                    break;
                case UNKOWN:
                    log.warn("PBCFieldType was UNKNOWN.");
                case G1:
                default:
                    field = gppProvider.getGlobalPublicParameter().getPairing().getG1();
                    break;
                }
                return ElementConverter.convert(split[0], field);
        }
    }
}
