package ru.igorba.Culture.Models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Document(collection = "Users")
public class User {

    @Id
    private String id;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String username;
    private String password;

    @DBRef
    private Set<Role> roles = new HashSet<>();
    private Set<Long> favorite = new HashSet<>();
    public void addFavorite(Long id) {
        favorite.add(id);
    }
    public void deleteFavorite(Long id) {
        favorite.remove(id);
    }
}