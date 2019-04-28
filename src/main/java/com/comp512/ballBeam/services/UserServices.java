package com.comp512.ballBeam.services;

import com.comp512.ballBeam.DAO.UserDAO;
import com.comp512.ballBeam.bean.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("userService")
public class UserServices {
    private final UserDAO userDAO;

    @Autowired
    public UserServices(UserDAO userDAO) {
        this.userDAO = userDAO;
    }


    public boolean exist(String  username) {
        return userDAO.existsUserByUsername(username);
    }

    public void registerUser(User user) {
        userDAO.save(user);
    }

    public User getUserByUsername(String username){
        return userDAO.findUserByUsername(username);
    }

    public void updateUserPoints(String username, float points) {
        User user = userDAO.findUserByUsername(username);
        user.setHighestPoint(points);
        userDAO.save(user);
    }
}
