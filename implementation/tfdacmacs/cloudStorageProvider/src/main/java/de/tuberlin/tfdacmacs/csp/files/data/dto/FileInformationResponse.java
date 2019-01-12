package de.tuberlin.tfdacmacs.csp.files.data.dto;

import de.tuberlin.tfdacmacs.csp.files.data.FileInformation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInformationResponse {

    @NotBlank
    private String id;

    public static FileInformationResponse from(@NonNull FileInformation fileInformation) {
        return new FileInformationResponse(
                fileInformation.getId()
        );
    }
}
