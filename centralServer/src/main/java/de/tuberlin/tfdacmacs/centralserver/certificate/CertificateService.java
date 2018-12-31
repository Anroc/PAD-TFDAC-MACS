package de.tuberlin.tfdacmacs.centralserver.certificate;

import de.tuberlin.tfdacmacs.basics.config.KeyStoreConfig;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.certificate.JavaKeyStore;
import de.tuberlin.tfdacmacs.basics.exceptions.ServiceException;
import de.tuberlin.tfdacmacs.centralserver.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.centralserver.certificate.db.CertificateDB;
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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateService {

    private final JavaKeyStore javaKeyStore;
    private final KeyStoreConfig keyStoreConfig;
    private final CertificateSigner certificateSigner;
    private final CertificateDB certificateDB;

    @EventListener(ApplicationReadyEvent.class)
    public void bootstrap() {
        Certificate certificate = new Certificate(Certificate.ROOT_CA, getCertificateAuthorityCertificate());
        certificateDB.upsert(certificate);
    }

    public boolean existCertificate(@NonNull String id) {
        return certificateDB.exist(id);
    }

    public Certificate prepareCertificate(@NonNull String id) {
        Certificate certificate = new Certificate(id);
        certificateDB.insert(certificate);
        return certificate;
    }

    public Optional<Certificate> findCertificate(@NonNull String id) {
        return certificateDB.findEntity(id);
    }

    public Certificate certificateRequest(
            @NonNull Certificate certificate,
            @NonNull PKCS10CertificationRequest certificateRequest,
            @NonNull PublicKey publicKey) {

         X509Certificate cert = processCertificateRequest(certificate, certificateRequest, publicKey);
         certificate.setCertificate(cert);
         certificateDB.update(certificate);
         return certificate;
    }

    private X509Certificate processCertificateRequest(@NonNull Certificate certificate,
            @NonNull PKCS10CertificationRequest certificateRequest, @NonNull PublicKey publicKey) {
        try{
            PrivateKey key = (PrivateKey) javaKeyStore.getKeyEntry(keyStoreConfig.getCaAlias(), keyStoreConfig.getKeyPassword());
            X509Certificate caCertificate = getCertificateAuthorityCertificate();
            CertificationRequest certificationRequest = certificateRequest.toASN1Structure();
            if(! matchesCommonName(certificationRequest, certificate.getId()) ) {
                throw new ServiceException(HttpStatus.FORBIDDEN);
            }
            return certificateSigner.sign(certificationRequest, key, caCertificate, certificate.getId(), publicKey);
        } catch (UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException | NoSuchProviderException | IOException | OperatorCreationException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    public X509Certificate getCertificateAuthorityCertificate() {
        try {
            return (X509Certificate) javaKeyStore.getCertificate(keyStoreConfig.getCaAlias());
        } catch (KeyStoreException e) {
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
