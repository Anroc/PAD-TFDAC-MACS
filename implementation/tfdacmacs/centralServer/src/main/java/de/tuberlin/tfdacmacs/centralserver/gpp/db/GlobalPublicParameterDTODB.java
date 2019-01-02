package de.tuberlin.tfdacmacs.centralserver.gpp.db;

import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GlobalPublicParameterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GlobalPublicParameterDTODB extends CouchbaseDB<GlobalPublicParameterDTO> {

    @Autowired
    public GlobalPublicParameterDTODB(GlobalPublicParameterDTORepository repository) {
        super(repository, GlobalPublicParameterDTO.class);
    }
}
