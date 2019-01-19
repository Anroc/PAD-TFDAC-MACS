package de.tuberlin.tfdacmacs.crypto.pairing.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DNFCipherText {

    @NotEmpty
    private List<CipherText> cipherTexts;

    @NotNull
    private File file;
}
