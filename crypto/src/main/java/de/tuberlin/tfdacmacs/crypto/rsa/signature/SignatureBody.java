package de.tuberlin.tfdacmacs.crypto.rsa.signature;

public interface SignatureBody {

    char DEFAULT_VALUE_SEPERATOR = ';';

    String buildSignatureBody();
}
