package ru.igorba.Culture.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.igorba.Culture.Models.ERole;
import ru.igorba.Culture.Models.Role;
import ru.igorba.Culture.Models.User;
import ru.igorba.Culture.Repository.RoleRepository;
import ru.igorba.Culture.Repository.UserRepository;

import java.util.*;

@Service
public class MongoAuthUserDetailService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

        User user = userRepository.findUserByUsername(userName);
        if(user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        user.getRoles()
                .forEach(role -> {
                    grantedAuthorities.add(new SimpleGrantedAuthority(role.getName().toString()));
                });

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), grantedAuthorities);
    }
    public User findUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    public boolean saveUser(User user) {
        User userFromDB = userRepository.findUserByUsername(user.getUsername());

        if (userFromDB != null) {
            return false;
        }

        Role role = new Role();
        role.setName(ERole.ROLE_USER);
        saveRole(role);

        role = roleRepository.findRoleByName(ERole.ROLE_USER);
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        user.setRoles(roles);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return true;
    }

    public void saveRole(Role role) {
        Role roleFromDB = roleRepository.findRoleByName(role.getName());

        if (roleFromDB != null) {
            return;
        }

        roleRepository.save(role);
    }
}