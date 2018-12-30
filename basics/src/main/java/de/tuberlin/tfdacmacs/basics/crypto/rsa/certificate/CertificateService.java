package de.tuberlin.tfdacmacs.basics.crypto.rsa.certificate;

import de.tuberlin.tfdacmacs.basics.config.KeyStoreConfig;
import de.tuberlin.tfdacmacs.basics.exceptions.ServiceException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateService {

    private final JavaKeyStore javaKeyStore;
    private final KeyStoreConfig keyStoreConfig;
    private final CertificateSigner certificateSigner;

    public X509Certificate certificateRequest(
            @NonNull String id,
            @NonNull PKCS10CertificationRequest certificateRequest,
            @NonNull PublicKey publicKey) {
        try {
            PrivateKey key = (PrivateKey) javaKeyStore.getKeyEntry(keyStoreConfig.getCaAlias(), keyStoreConfig.getKeyPassword());
            X509Certificate caCertificate = (X509Certificate) javaKeyStore.getCertificate(keyStoreConfig.getCaAlias());
            CertificationRequest certificationRequest = certificateRequest.toASN1Structure();
            if(! matchesCommonName(certificationRequest, id) ) {
                throw new ServiceException(HttpStatus.FORBIDDEN);
            }
            return certificateSigner.sign(certificationRequest, key, caCertificate, id, publicKey);
        } catch (UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException | NoSuchProviderException | IOException | OperatorCreationException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean matchesCommonName(@NonNull CertificationRequest certificationRequest, @NonNull String id) {
        RDN[] rdns = certificationRequest.getCertificationRequestInfo().getSubject().getRDNs();
        for(RDN rdn : rdns) {
            for(AttributeTypeAndValue attributeTypeAndValue : rdn.getTypesAndValues()) {
                if (attributeTypeAndValue.getType().getId().equals(BCStyle.CN.getId())) {
                    return (((DERUTF8String) attributeTypeAndValue.getValue()).getString().equals(id));
                }
            }
        }
        return false;
    }
}
