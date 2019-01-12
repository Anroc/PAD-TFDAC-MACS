package de.tuberlin.tfdacmacs.csp.files.data;

import de.tuberlin.tfdacmacs.lib.db.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInformation extends Entity {

    public FileInformation(@NonNull String id, @NotBlank String path,
            @NotBlank String originalName) {
        super(id);
        this.path = path;
        this.originalName = originalName;
    }

    @NotBlank
    private String path;

    @NotBlank
    private String originalName;

}
