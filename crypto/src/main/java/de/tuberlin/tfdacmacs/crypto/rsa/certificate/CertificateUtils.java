package de.tuberlin.tfdacmacs.crypto.rsa.certificate;

import com.google.common.collect.Lists;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.*;
import java.util.Collection;
import java.util.List;

@Component
public class CertificateUtils {

    public String extractCommonName(X509Certificate x509Certificate) {
        try {
            X500Name x500name = new JcaX509CertificateHolder(x509Certificate).getSubject();
            RDN cn = x500name.getRDNs(BCStyle.CN)[0];
            return IETFUtils.valueToString(cn.getFirst().getValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void validateCertificate(X509Certificate x509Certificate, X509Certificate... x509Certificates) {
        try {
            InputStream trustStoreInput = new FileInputStream(ResourceUtils.getFile("classpath:ca-truststore.jks"));
            char[] password = "foobar".toCharArray();
            List<X509Certificate> chain = Lists.asList(x509Certificate, x509Certificates);
            Collection<X509CRL> crls = Lists.newArrayList();

            /* Construct a valid path. */
            KeyStore anchors = KeyStore.getInstance(KeyStore.getDefaultType());
            anchors.load(trustStoreInput, password);
            X509CertSelector target = new X509CertSelector();
            target.setCertificate(chain.get(0));
            PKIXBuilderParameters params = new PKIXBuilderParameters(anchors, target);
            CertStoreParameters intermediates = new CollectionCertStoreParameters(chain);
            params.addCertStore(CertStore.getInstance("Collection", intermediates));
            CertStoreParameters revoked = new CollectionCertStoreParameters(crls);
            params.addCertStore(CertStore.getInstance("Collection", revoked));
            CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
            /*
             * If build() returns successfully, the certificate is valid. More details
             * about the valid path can be obtained through the PKIXBuilderResult.
             * If no valid path can be found, a CertPathBuilderException is thrown.
             */
            builder.build(params);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}