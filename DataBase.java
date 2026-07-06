import javax.swing.*;

public class DataBase extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) { e.printStackTrace(); }
            new DataBase().setVisible(true);
        });
    }

    public DataBase() {
        try {
            DBUtils.loadDriver();
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Critical Error: MySQL Driver JAR not found!\nAdd mysql-connector-j to your Build Path.");
            System.exit(1);
        }

        setTitle("Stadium Database System (Phase 2 Demo)");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Stadiums", new StadiumPanel());
        tabbedPane.addTab("Events", new EventPanel());
        tabbedPane.addTab("Fans", new FanPanel());
        tabbedPane.addTab("Sponsors", new SponsorPanel());
        tabbedPane.addTab("Event Sponsorship", new SponsorshipPanel());
        tabbedPane.addTab("Sections", new SeatingSectionPanel());
        tabbedPane.addTab("Seats", new SeatPanel());
        tabbedPane.addTab("Tickets", new TicketPanel());

        add(tabbedPane);
    }
}
