package de.tuberlin.tfdacmacs.crypto.benchmark.pairing;

import de.tuberlin.tfdacmacs.crypto.benchmark.CipherTextLength;
import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.DNFCipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import lombok.Data;

@Data
public class ABECipherText implements CipherTextLength {

    private final DNFCipherText cipherText;

    @Override
    public long getSize() {
        return cipherText.getCipherTexts().stream().mapToLong(this::getSize).sum();
    }

    @Override
    public long getNumberOfFileKeys() {
        return cipherText.getCipherTexts().size();
    }

    private long getSize(CipherText cipherText) {
        long size = cipherText.getAccessPolicy()
                .stream()
                .mapToLong(this::getSize)
                .sum();

        if(cipherText.isTwoFactorSecured()) {
            size += getSize(cipherText.getOwnerId());
        }
        size += cipherText.getC1().getLengthInBytes();
        size += cipherText.getC2().getLengthInBytes();
        size += cipherText.getC3().getLengthInBytes();
        return size;
    }

    private long getSize(VersionedID versionedID) {
        return versionedID.getId().getBytes().length + Long.BYTES;
    }
}
