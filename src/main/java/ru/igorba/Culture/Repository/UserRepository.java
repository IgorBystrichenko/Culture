package ru.igorba.Culture.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.igorba.Culture.Models.User;


@Repository
public interface UserRepository extends MongoRepository<User, String> {
    User findUserByUsername(String username);
}
