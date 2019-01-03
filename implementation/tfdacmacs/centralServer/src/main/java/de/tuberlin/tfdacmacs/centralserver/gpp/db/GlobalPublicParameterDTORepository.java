package de.tuberlin.tfdacmacs.centralserver.gpp.db;

import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GlobalPublicParameterDTO;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalPublicParameterDTORepository extends CouchbaseRepository<GlobalPublicParameterDTO, String> {

}
