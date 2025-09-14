package com.dipa.notefournote.users;

public interface UserService {

    User registerUser(String username, String password);

    UserLogged loginUser(String username, String password);

    UserLogged refreshToken(String refreshToken);

}