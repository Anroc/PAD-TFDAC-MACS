package de.tuberlin.tfdacmacs.crypto.pairing.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AndCipherText {

    @NotNull
    private CipherText cipherText;

    @NotNull
    private File file;
}
