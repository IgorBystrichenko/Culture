package ru.igorba.Culture.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.igorba.Culture.Models.ERole;
import ru.igorba.Culture.Models.Role;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {
    Role findRoleByName(ERole name);
}
