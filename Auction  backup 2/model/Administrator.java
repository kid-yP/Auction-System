package model;

public class Administrator extends User {
    public Administrator(User user) {
        super(user.getUserId(), user.getUsername(), user.getPassword(), user.getEmail(),
              user.getFirstName(), user.getLastName(), user.getRole(), user.getBalance());
    }
}