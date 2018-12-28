package de.tuberlin.tfdacmacs.centralserver.gpp.db;

import com.couchbase.client.java.Bucket;
import de.tuberlin.tfdacmacs.basics.db.CouchbaseDB;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GlobalPublicParameterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

public class GlobalPublicParameterDTODB extends CouchbaseDB<GlobalPublicParameterDTO> {

    @Autowired
    public GlobalPublicParameterDTODB(Bucket bucket,
            GlobalPublicParameterDTORepository repository,
            CouchbaseTemplate template) {
        super(bucket, repository, template, GlobalPublicParameterDTO.class);
    }
}
