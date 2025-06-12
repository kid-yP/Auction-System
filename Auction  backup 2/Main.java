import javax.swing.SwingUtilities;
import gui.UserDashboard;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new UserDashboard(null).setVisible(true);
        });
    }
}