package model;

public class Buyer extends User {
    public Buyer(User user) {
        super(user.getUserId(), user.getUsername(), user.getPassword(), user.getEmail(),
              user.getFirstName(), user.getLastName(), user.getRole(), user.getBalance());
    }
}