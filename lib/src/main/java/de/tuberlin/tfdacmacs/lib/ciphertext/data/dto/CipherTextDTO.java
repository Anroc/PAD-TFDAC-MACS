package de.tuberlin.tfdacmacs.lib.ciphertext.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CipherTextDTO {

    @NotBlank
    private String id;

    @NotBlank
    private String c1;
    @NotBlank
    private String c2;
    @NotBlank
    private String c3;

    @NotEmpty
    private Set<String> accessPolicy;
    @Nullable
    private String ownerId;

    @NotBlank
    private String fileId;

    public boolean isTwoFactorSecured() {
        return this.ownerId != null;
    }
}
