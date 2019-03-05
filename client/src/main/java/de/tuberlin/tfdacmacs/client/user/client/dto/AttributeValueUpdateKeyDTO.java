package de.tuberlin.tfdacmacs.client.user.client.dto;

import de.tuberlin.tfdacmacs.crypto.rsa.signature.SignatureBody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeValueUpdateKeyDTO implements SignatureBody {

    @NotBlank
    private String updateKey;

    @NotBlank
    private String attributeValueId;

    @Min(0)
    private long targetVersion;
    @Min(1)
    private long updateVersion;

    @NotBlank
    private String signature;

    @Override
    public String buildSignatureBody() {
        return updateKey + SignatureBody.DEFAULT_VALUE_SEPERATOR +
                attributeValueId + SignatureBody.DEFAULT_VALUE_SEPERATOR +
                targetVersion + SignatureBody.DEFAULT_VALUE_SEPERATOR +
                updateVersion;
    }
}
