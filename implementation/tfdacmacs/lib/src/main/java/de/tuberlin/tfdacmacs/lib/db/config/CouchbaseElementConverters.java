package de.tuberlin.tfdacmacs.lib.db.config;

import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.lib.gpp.GlobalPublicParameterProvider;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.FieldOver;
import it.unisa.dia.gas.plaf.jpbc.field.gt.GTFiniteField;
import it.unisa.dia.gas.plaf.jpbc.field.z.ZrElement;
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
        G1, G2, GT, ZR, UNKNOWN
    }

    @WritingConverter
    @Component
    @Slf4j
    public static class Write implements Converter<Element, String> {

        @Override
        public String convert(Element value) {
            PBCFieldType fieldType = PBCFieldType.UNKNOWN;
            Field field = value.getField();
            if(value instanceof FieldOver) {
                field = ((FieldOver) value).getTargetField();
            }

            if(field instanceof PBCField) {
                if(field instanceof PBCG1Field) {
                    fieldType = PBCFieldType.G1;
                } else if( field instanceof PBCG2Field) {
                    fieldType = PBCFieldType.G2;
                } else if (field instanceof PBCGTField) {
                    fieldType = PBCFieldType.GT;
                } else if (field instanceof PBCZrField) {
                    fieldType = PBCFieldType.ZR;
                }
            } else if (value instanceof ZrElement) {
                fieldType = PBCFieldType.ZR;
            } else if (value instanceof GTFiniteField) {
                fieldType = PBCFieldType.GT;
            }

            if(fieldType == PBCFieldType.UNKNOWN) {
                log.warn("Field could not be assigned: [{}]", value.getField().getClass().getCanonicalName());
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
                    field = gppProvider.getGlobalPublicParameter().gt();
                    break;
                case ZR:
                    field = gppProvider.getGlobalPublicParameter().zr();
                    break;
                case UNKNOWN:
                    log.warn("PBCFieldType was UNKNOWN.");
                case G1:
                default:
                    field = gppProvider.getGlobalPublicParameter().g1();
                    break;
                }
                return ElementConverter.convert(split[0], field);
        }
    }
}
