package de.tuberlin.tfdacmacs.centralserver.user.factory;

import de.tuberlin.tfdacmacs.basics.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.centralserver.user.dto.CertificateRequest;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyPair;

@Component
public class CertificateRequestTestFactory {

    public CertificateRequest create(String email, KeyPair keyPair) throws OperatorCreationException, IOException {
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA512withRSA");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        AsymmetricKeyParameter clientKeyParameter = PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());

        SubjectPublicKeyInfo keyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

        ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId)
                .build(clientKeyParameter);
        PKCS10CertificationRequest request = new PKCS10CertificationRequestBuilder(
                new X500Name(String.format("CN=%s", email)),
                keyInfo
        ).build(sigGen);

        return new CertificateRequest(
                KeyConverter.from(keyPair.getPublic()).toBase64(),
                KeyConverter.from(request.getEncoded()).toBase64()
        );
    }
}
