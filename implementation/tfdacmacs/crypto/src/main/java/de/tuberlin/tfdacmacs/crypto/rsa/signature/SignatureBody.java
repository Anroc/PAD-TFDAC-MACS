package de.tuberlin.tfdacmacs.crypto.rsa.signature;

import java.util.function.Function;

public interface SignatureBody {

    char DEFAULT_VALUE_SEPERATOR = ';';

    String buildSignatureBody();

    default SignatureBodyBuilder signature() {
        return new SignatureBodyBuilder();
    }

    class SignatureBodyBuilder {

        private final StringBuilder stringBuilder = new StringBuilder();

        public SignatureBodyBuilder pack(SignatureBody signatureBody) {
            return pack(signatureBody.buildSignatureBody());
        }

        public SignatureBodyBuilder pack(Object value) {
            if(stringBuilder.length() != 0) {
                stringBuilder.append(DEFAULT_VALUE_SEPERATOR);
            }
            stringBuilder.append(value.toString());
            return this;
        }

        public String finalize(Function<String, String> signingFunction) {
            return signingFunction.apply(stringBuilder.toString());
        }

        public String toString() {
            return stringBuilder.toString();
        }
    }
}
