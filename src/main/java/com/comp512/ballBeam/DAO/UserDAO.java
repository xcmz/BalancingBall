package com.comp512.ballBeam.DAO;

import com.comp512.ballBeam.bean.User;
import org.springframework.data.repository.CrudRepository;

public interface UserDAO extends CrudRepository<User, Long> {
    User findUserByUsername(String username);

    boolean existsUserByUsername(String username);

}
