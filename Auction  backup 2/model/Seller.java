package model;

public class Seller extends User {
    public Seller(User user) {
        super(user.getUserId(), user.getUsername(), user.getPassword(), user.getEmail(),
              user.getFirstName(), user.getLastName(), user.getRole(), user.getBalance());
    }
}