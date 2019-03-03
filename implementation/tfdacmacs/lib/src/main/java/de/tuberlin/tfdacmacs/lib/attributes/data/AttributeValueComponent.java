package de.tuberlin.tfdacmacs.lib.attributes.data;

import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.rsa.signature.SignatureBody;
import it.unisa.dia.gas.jpbc.Element;

public interface AttributeValueComponent<T> extends SignatureBody {

    T getValue();

    Element getPublicKeyComponent();

    long getVersion();

    @Override
    default String buildSignatureBody() {
        return new StringBuilder(getValue().toString())
                .append(SignatureBody.DEFAULT_VALUE_SEPERATOR)
                .append(ElementConverter.convert(getPublicKeyComponent()))
                .append(SignatureBody.DEFAULT_VALUE_SEPERATOR)
                .append(getVersion())
                .toString();
    }
}
