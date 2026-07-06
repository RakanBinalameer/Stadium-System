import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SponsorshipPanel extends JPanel {
    JComboBox<String> comboSponsor;
    JComboBox<String> comboEvent;
    JTable table;
    DefaultTableModel model;

    public SponsorshipPanel() {
        setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Assign Sponsor to Event"));

        comboSponsor = new JComboBox<>();
        comboEvent = new JComboBox<>();

        formPanel.add(new JLabel("Sponsor (PK/FK):")); formPanel.add(comboSponsor);
        formPanel.add(new JLabel("Event (PK/FK):"));   formPanel.add(comboEvent);

        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Link Sponsor to Event");
        JButton btnDelete = new JButton("Remove Link (From Dropdowns)");
        JButton btnSearch = new JButton("Search (By Event)");
        JButton btnLoad = new JButton("Refresh");

        btnPanel.add(btnAdd); btnPanel.add(btnDelete);
        btnPanel.add(btnSearch); btnPanel.add(btnLoad);

        model = new DefaultTableModel(new Object[]{"Sponsor ID", "Event ID"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel top = new JPanel(new BorderLayout());
        top.add(formPanel, BorderLayout.CENTER);
        top.add(btnPanel, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnLoad.addActionListener(e -> { loadData(); loadCombos(); });

        btnSearch.addActionListener(e -> {
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "SELECT * FROM Sponser_has_Event WHERE Event_Event_ID=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, DBUtils.getSelectedId(comboEvent));
                ResultSet rs = pst.executeQuery();
                model.setRowCount(0);
                while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getInt(2)});
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnAdd.addActionListener(e -> {
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "INSERT INTO Sponser_has_Event (Sponser_Sponser_ID, Event_Event_ID) VALUES (?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, DBUtils.getSelectedId(comboSponsor));
                pst.setInt(2, DBUtils.getSelectedId(comboEvent));
                pst.executeUpdate();
                loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnDelete.addActionListener(e -> {
            try (Connection conn = DBUtils.getConnection()) {
                int spId = DBUtils.getSelectedId(comboSponsor);
                int evId = DBUtils.getSelectedId(comboEvent);

                if (spId == 0 || evId == 0) {
                    JOptionPane.showMessageDialog(this, "Please select both a Sponsor and an Event to remove link.");
                    return;
                }

                String sql = "DELETE FROM Sponser_has_Event WHERE Sponser_Sponser_ID=? AND Event_Event_ID=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, spId);
                pst.setInt(2, evId);
                int rows = pst.executeUpdate();

                if (rows > 0) {
                    loadData();
                    JOptionPane.showMessageDialog(this, "Link Removed!");
                } else {
                    JOptionPane.showMessageDialog(this, "No link found between selected Sponsor and Event.");
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        loadData(); loadCombos();
    }

    private void loadCombos() {
        comboSponsor.removeAllItems();
        comboEvent.removeAllItems();
        try (Connection conn = DBUtils.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT Sponser_ID, Name FROM Sponser");
            while (rs.next()) comboSponsor.addItem(rs.getInt(1) + " - " + rs.getString(2));

            rs = stmt.executeQuery("SELECT Event_ID, Event_Name FROM Event");
            while (rs.next()) comboEvent.addItem(rs.getInt(1) + " - " + rs.getString(2));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadData() {
        try (Connection conn = DBUtils.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM Sponser_has_Event");
            model.setRowCount(0);
            while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getInt(2)});
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
