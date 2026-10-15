import javax.swing.*;

public class DataBase extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
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
        setSize(1200, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        HomePanel home = new HomePanel(tabbedPane::setSelectedIndex);
        tabbedPane.addTab("Home", home);
        tabbedPane.addTab("Stadiums", new StadiumPanel());
        tabbedPane.addTab("Events", new EventPanel());
        tabbedPane.addTab("Fans", new FanPanel());
        tabbedPane.addTab("Sponsors", new SponsorPanel());
        tabbedPane.addTab("Event Sponsorship", new SponsorshipPanel());
        tabbedPane.addTab("Sections", new SeatingSectionPanel());
        tabbedPane.addTab("Seats", new SeatPanel());
        tabbedPane.addTab("Tickets", new TicketPanel());

        java.awt.Component banner = Theme.header("Stadium Management System", "Stadiums  \u00B7  Events  \u00B7  Fans  \u00B7  Sponsors  \u00B7  Tickets");
        add(banner, java.awt.BorderLayout.NORTH);
        add(tabbedPane, java.awt.BorderLayout.CENTER);
        Theme.style(tabbedPane);
        tabbedPane.addChangeListener(e -> {
            boolean onHome = tabbedPane.getSelectedComponent() == home;
            banner.setVisible(!onHome);                  // the Home page has its own illustrated banner
            if (onHome) home.refresh();
        });
        banner.setVisible(false);                        // the app starts on Home
    }
}
