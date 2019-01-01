package de.tuberlin.tfdacmacs.centralserver.certificate;

import de.tuberlin.tfdacmacs.basics.config.KeyStoreConfig;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.util.HashGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.certificate.JavaKeyStore;
import de.tuberlin.tfdacmacs.basics.exceptions.ServiceException;
import de.tuberlin.tfdacmacs.centralserver.authority.AttributeAuthorityService;
import de.tuberlin.tfdacmacs.centralserver.authority.data.AttributeAuthority;
import de.tuberlin.tfdacmacs.centralserver.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.centralserver.certificate.db.CertificateDB;
import de.tuberlin.tfdacmacs.centralserver.user.UserService;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.DERUTF8String;
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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateService {

    private final JavaKeyStore javaKeyStore;
    private final KeyStoreConfig keyStoreConfig;
    private final CertificateSigner certificateSigner;
    private final CertificateDB certificateDB;
    private final UserService userService;
    private final AttributeAuthorityService attributeAuthorityService;
    private final HashGenerator hashGenerator;

    @EventListener(ApplicationReadyEvent.class)
    public void bootstrap() {
        X509Certificate certificateAuthorityCertificate = getCertificateAuthorityCertificate();
        Certificate certificate = new Certificate(Certificate.ROOT_CA, certificateAuthorityCertificate, certificateAuthorityCertificate.getSubjectDN().getName());
        certificateDB.upsert(certificate);
    }

    public Optional<Certificate> findCertificate(@NonNull String id) {
        return certificateDB.findEntity(id);
    }

    public Certificate certificateRequestUser(@NonNull PKCS10CertificationRequest certificateRequest) {
        User associatedUser = getAssociatedUser(certificateRequest);

        Certificate certificate = certificateRequest(certificateRequest);

        updateUser(certificate, associatedUser);
        return certificate;
    }

    public Certificate certificateRequestAuthority(@NonNull PKCS10CertificationRequest certificateRequest) {
        String id = assureAuthorityDoesNotExist(certificateRequest);

        Certificate certificate = certificateRequest(certificateRequest);

        insertAuthority(certificate, id);
        return certificate;
    }

    private Certificate certificateRequest(@NonNull PKCS10CertificationRequest certificateRequest) {
        Certificate certificate = createNewCertificate(certificateRequest);

        X509Certificate cert = processCertificateRequest(certificate, certificateRequest);

        insertCertificate(certificate, cert);
        return certificate;
    }

    private void updateUser(Certificate certificate, User associatedUser) {
        associatedUser.addNewDevice(certificate);
        userService.updateUser(associatedUser);
    }

    private void insertCertificate(Certificate certificate, X509Certificate cert) {
        certificate.setCertificate(cert);
        certificateDB.insert(certificate);
    }

    private User getAssociatedUser(@NonNull PKCS10CertificationRequest certificateRequest) {
        String commonName = extractCommonName(certificateRequest);
        return userService.findUser(commonName)
                .orElseThrow(
                        () -> new ServiceException("Could not find user with CN=[%s].", HttpStatus.UNPROCESSABLE_ENTITY, commonName)
                );
    }

    private void insertAuthority(Certificate certificate, String id) {
        attributeAuthorityService.insert(new AttributeAuthority(id, certificate.getId()));
    }

    private String assureAuthorityDoesNotExist(PKCS10CertificationRequest certificateRequest) {
        String commonName = extractCommonName(certificateRequest);
        if(attributeAuthorityService.exist(commonName)) {
            throw new ServiceException("Authority with id [%s] does already exist", HttpStatus.CONFLICT, commonName);
        }
        return commonName;
    }

    private Certificate createNewCertificate(@NonNull PKCS10CertificationRequest certificationRequest) {
        PublicKey publicKey = extractPublicKey(certificationRequest);
        String commonName = extractCommonName(certificationRequest);

        String certificateId  = hashGenerator.sha256HashWithSimpleEncoding(publicKey.getEncoded());
        Certificate certificate = new Certificate(certificateId, commonName);
        if(certificateDB.exist(certificateId)){
            throw new ServiceException("Certificate for public key [%s] already exist.", HttpStatus.UNPROCESSABLE_ENTITY, certificateId);
        }
        return certificate;
    }

    private PublicKey extractPublicKey(@NonNull PKCS10CertificationRequest certificateRequest) {
        try {
            return KeyFactory
                    .getInstance("RSA").generatePublic(new X509EncodedKeySpec(
                            certificateRequest.getSubjectPublicKeyInfo().toASN1Primitive().getEncoded()));
        } catch(InvalidKeySpecException | NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private X509Certificate processCertificateRequest(@NonNull Certificate certificate, @NonNull PKCS10CertificationRequest certificateRequest) {
        try{
            PrivateKey key = (PrivateKey) javaKeyStore.getKeyEntry(keyStoreConfig.getCaAlias(), keyStoreConfig.getKeyPassword());
            X509Certificate caCertificate = getCertificateAuthorityCertificate();
            return certificateSigner.sign(certificateRequest, key, caCertificate, certificate.getId(), extractPublicKey(certificateRequest));
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

    private boolean matchesCommonName(@NonNull PKCS10CertificationRequest certificationRequest, @NonNull String id) {
        return extractCommonName(certificationRequest).equals(id);
    }

    private String extractCommonName(PKCS10CertificationRequest certificationRequest) {
        RDN[] rdns = certificationRequest.toASN1Structure().getCertificationRequestInfo().getSubject().getRDNs();
        for(RDN rdn : rdns) {
            for(AttributeTypeAndValue attributeTypeAndValue : rdn.getTypesAndValues()) {
                if (attributeTypeAndValue.getType().getId().equals(BCStyle.CN.getId())) {
                    return ((DERUTF8String) attributeTypeAndValue.getValue()).getString();
                }
            }
        }
        throw new IllegalArgumentException("Could not find common name in CSR.");
    }
}
