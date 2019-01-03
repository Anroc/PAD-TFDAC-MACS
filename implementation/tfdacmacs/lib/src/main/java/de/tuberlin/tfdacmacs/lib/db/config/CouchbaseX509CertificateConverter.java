package de.tuberlin.tfdacmacs.lib.db.config;

import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;

public class CouchbaseX509CertificateConverter {

    @WritingConverter
    @Component
    public static class Write implements Converter<X509Certificate, String> {

        @Override
        public String convert(X509Certificate value) {
            return KeyConverter.from(value).toBase64();
        }
    }

    @ReadingConverter
    @Component
    @RequiredArgsConstructor
    @Slf4j
    public static class Read implements Converter<String, X509Certificate> {

        @Override
        public X509Certificate convert(String value) {
            return KeyConverter.from(value).toX509Certificate();
        }
    }
}
