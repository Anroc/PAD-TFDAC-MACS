package de.tuberlin.tfdacmacs.attributeauthority.ciphertext.client;

import com.google.common.collect.Lists;
import de.tuberlin.tfdacmacs.attributeauthority.certificate.client.CertificateClient;
import de.tuberlin.tfdacmacs.attributeauthority.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.attributeauthority.client.CAClient;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.CipherTextAttributeUpdateKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import de.tuberlin.tfdacmacs.crypto.rsa.AsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.lib.ciphertext.data.dto.AttributeCipherTextUpdateKeyDTO;
import de.tuberlin.tfdacmacs.lib.ciphertext.data.dto.AttributeCipherTextUpdateRequest;
import de.tuberlin.tfdacmacs.lib.exceptions.SignatureInvalidException;
import de.tuberlin.tfdacmacs.lib.gpp.GlobalPublicParameterProvider;
import de.tuberlin.tfdacmacs.lib.user.data.dto.TwoFactorPublicKeyDTO;
import it.unisa.dia.gas.jpbc.Field;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CipherTextClient {

    private final CAClient caClient;
    private final CertificateClient certificateClient;
    private final GlobalPublicParameterProvider globalPublicParameterProvider;
    private final AsymmetricCryptEngine asymmetricCryptEngine;

    public List<CipherText> findCipherTextsByAttribute(@NonNull String attributeValueId) {
        Field gt = globalPublicParameterProvider.getGlobalPublicParameter().gt();
        Field g1 = getG1();

        return caClient.getCipherTexts(Lists.newArrayList(attributeValueId))
                .stream()
                .map(cipherTextDTO -> new CipherText(
                        cipherTextDTO.getId(),
                        ElementConverter.convert(cipherTextDTO.getC1(), gt),
                        ElementConverter.convert(cipherTextDTO.getC2(), g1),
                        ElementConverter.convert(cipherTextDTO.getC3(), g1),
                        new HashSet<>(cipherTextDTO.getAccessPolicy()),
                        cipherTextDTO.getOwnerId(),
                        cipherTextDTO.getFileId()
                )).collect(Collectors.toList());
    }

    private Field getG1() {
        return globalPublicParameterProvider.getGlobalPublicParameter().g1();
    }

    public Map<String, TwoFactorKey.Public> findTwoFactorPublicKeys(@NonNull Set<VersionedID> ownerIds) {
        return ownerIds.stream()
                .map(VersionedID::getId)
                .map(caClient::getUser)
                .collect(Collectors.toMap(
                        userResponse -> userResponse.getId(),
                        userResponse -> {
                            TwoFactorPublicKeyDTO twoFactorPublicKey = userResponse.getTwoFactorPublicKey();

                            Certificate certificate = certificateClient.getCertificate(
                                    twoFactorPublicKey.getSigningDeviceId(), userResponse.getId()
                            );
                            PublicKey publicKey = certificate.getCertificate().getPublicKey();

                            if( ! asymmetricCryptEngine.isSignatureAuthentic(
                                    twoFactorPublicKey.getSignature(),
                                    twoFactorPublicKey.signature()
                                            .pack(userResponse.getId())
                                            .pack(twoFactorPublicKey)
                                            .toString(),
                                    publicKey
                            )) {
                                throw new SignatureInvalidException(String.format("Signature of %s was invalid.", twoFactorPublicKey));
                            }

                            return new TwoFactorKey.Public(
                                    userResponse.getId(),
                                    ElementConverter.convert(twoFactorPublicKey.getTwoFactorAuthenticationPublicKey(), getG1()),
                                    twoFactorPublicKey.getVersion());
                        }));
    }

    public void updateCipherTexts(@NonNull Map<String, CipherTextAttributeUpdateKey> cipherTestUpdates) {
        Optional<CipherTextAttributeUpdateKey> first = cipherTestUpdates.values().stream().findAny();
        if(! first.isPresent()) {
            return;
        }

        long targetVersion = first.get().getVersion();
        String attributeValueId = first.get().getAttributeValueId().getId();

        caClient.updateCipherTexts(
                new AttributeCipherTextUpdateRequest(
                        attributeValueId,
                        targetVersion,
                        cipherTestUpdates.entrySet().stream().collect(Collectors.toMap(
                                entry -> entry.getKey(),
                                entry -> new AttributeCipherTextUpdateKeyDTO(
                                        entry.getValue().getDataOwnerId(),
                                        ElementConverter.convert(entry.getValue().getUpdateKey())
                                )
                        ))
                )
        );
    }
}
