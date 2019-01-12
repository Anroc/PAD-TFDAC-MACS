package de.tuberlin.tfdacmacs.csp.files.db;

import de.tuberlin.tfdacmacs.csp.files.data.FileInformation;
import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileInformationDB extends CouchbaseDB<FileInformation> {

    @Autowired
    public FileInformationDB(FileInformationRepository repository) {
        super(repository, FileInformation.class);
    }

}
