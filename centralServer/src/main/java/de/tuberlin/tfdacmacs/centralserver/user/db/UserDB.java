package de.tuberlin.tfdacmacs.centralserver.user.db;

import com.couchbase.client.java.Bucket;
import de.tuberlin.tfdacmacs.basics.db.CouchbaseDB;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserDB extends CouchbaseDB<User> {

    @Autowired
    public UserDB(Bucket bucket,
            UserRepository repository,
            CouchbaseTemplate template) {
        super(bucket, repository, template, User.class);
    }
}
