import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class TicketPanel extends JPanel {
    JTextField txtTickId, txtPrice;
    JComboBox<String> comboEvent, comboFan, comboSeat;
    JTable table;
    DefaultTableModel model;

    public TicketPanel() {
        setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Ticket Details"));

        txtTickId = new JTextField(); txtPrice = new JTextField();
        comboEvent = new JComboBox<>(); comboFan = new JComboBox<>(); comboSeat = new JComboBox<>();

        formPanel.add(new JLabel("Ticket ID (PK):")); formPanel.add(txtTickId);
        formPanel.add(new JLabel("Event (PK/FK):")); formPanel.add(comboEvent);
        formPanel.add(new JLabel("Fan:")); formPanel.add(comboFan);
        formPanel.add(new JLabel("Seat (FK):")); formPanel.add(comboSeat);
        formPanel.add(new JLabel("Price:")); formPanel.add(txtPrice);

        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Insert");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnSearch = new JButton("Search (ID)");
        JButton btnLoad = new JButton("Refresh");

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnDelete);
        btnPanel.add(btnSearch); btnPanel.add(btnLoad);

        model = new DefaultTableModel(new Object[]{"ID", "Event", "Price", "Seat", "Sec", "Stad", "Fan"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel top = new JPanel(new BorderLayout());
        top.add(formPanel, BorderLayout.CENTER);
        top.add(btnPanel, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnLoad.addActionListener(e -> { loadData(); loadCombos(); });

        btnSearch.addActionListener(e -> {
            String search = txtTickId.getText().trim();
            if (search.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Ticket ID"); return; }
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "SELECT * FROM Ticket WHERE Ticket_ID=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(search));
                ResultSet rs = pst.executeQuery();
                model.setRowCount(0);
                while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getInt(7), rs.getInt(2), rs.getInt(5), rs.getInt(3), rs.getInt(4), rs.getInt(8)});
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnAdd.addActionListener(e -> {
            if (txtTickId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ticket ID is required!");
                return;
            }
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "INSERT INTO Ticket (Ticket_ID, Event_Event_ID, Fan_Fan_ID, Seat_Seat_Number, Seat_Seating_Section_Section_ID, Seat_Seating_Section_STADIUM_Stadium_ID, Price) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);

                String seatSel = (String) comboSeat.getSelectedItem();
                int seatNum = Integer.parseInt(seatSel.split(" ")[1]);
                int secId = Integer.parseInt(seatSel.substring(seatSel.indexOf("Sec: ") + 5, seatSel.indexOf(",")));
                int stadId = Integer.parseInt(seatSel.substring(seatSel.indexOf("Stad: ") + 6, seatSel.indexOf(")")));

                pst.setInt(1, Integer.parseInt(txtTickId.getText()));
                pst.setInt(2, DBUtils.getSelectedId(comboEvent));
                pst.setInt(3, DBUtils.getSelectedId(comboFan));
                pst.setInt(4, seatNum);
                pst.setInt(5, secId);
                pst.setInt(6, stadId);
                DBUtils.setOptionalInt(pst, 7, txtPrice.getText());
                pst.executeUpdate();
                loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnUpdate.addActionListener(e -> {
            if (txtTickId.getText().trim().isEmpty()) return;
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "UPDATE Ticket SET Fan_Fan_ID=?, Seat_Seat_Number=?, Seat_Seating_Section_Section_ID=?, Seat_Seating_Section_STADIUM_Stadium_ID=?, Price=? WHERE Ticket_ID=? AND Event_Event_ID=?";
                PreparedStatement pst = conn.prepareStatement(sql);

                String seatSel = (String) comboSeat.getSelectedItem();
                int seatNum = Integer.parseInt(seatSel.split(" ")[1]);
                int secId = Integer.parseInt(seatSel.substring(seatSel.indexOf("Sec: ") + 5, seatSel.indexOf(",")));
                int stadId = Integer.parseInt(seatSel.substring(seatSel.indexOf("Stad: ") + 6, seatSel.indexOf(")")));

                pst.setInt(1, DBUtils.getSelectedId(comboFan));
                pst.setInt(2, seatNum);
                pst.setInt(3, secId);
                pst.setInt(4, stadId);
                DBUtils.setOptionalInt(pst, 5, txtPrice.getText());
                pst.setInt(6, Integer.parseInt(txtTickId.getText()));
                pst.setInt(7, DBUtils.getSelectedId(comboEvent));
                pst.executeUpdate();
                loadData();
                JOptionPane.showMessageDialog(this, "Ticket Updated!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnDelete.addActionListener(e -> {
            try (Connection conn = DBUtils.getConnection()) {
                int row = table.getSelectedRow();
                if (row == -1) { JOptionPane.showMessageDialog(this, "Select a row"); return; }
                int tickId = (int) model.getValueAt(row, 0);
                int evtId = (int) model.getValueAt(row, 1);
                PreparedStatement pst = conn.prepareStatement("DELETE FROM Ticket WHERE Ticket_ID=? AND Event_Event_ID=?");
                pst.setInt(1, tickId); pst.setInt(2, evtId);
                pst.executeUpdate();
                loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtTickId.setText(model.getValueAt(row, 0).toString());
                    txtPrice.setText(model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "");
                }
            }
        });

        loadData(); loadCombos();
    }

    private void loadCombos() {
        comboEvent.removeAllItems(); comboFan.removeAllItems(); comboSeat.removeAllItems();
        try (Connection conn = DBUtils.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT Event_ID, Event_Name FROM Event");
            while (rs.next()) comboEvent.addItem(rs.getInt(1) + " - " + rs.getString(2));

            rs = stmt.executeQuery("SELECT Fan_ID, Fan_Fname FROM Fan");
            while (rs.next()) comboFan.addItem(rs.getInt(1) + " - " + rs.getString(2));

            rs = stmt.executeQuery("SELECT Seat_Number, Seating_Section_Section_ID, Seating_Section_STADIUM_Stadium_ID FROM Seat");
            while (rs.next()) comboSeat.addItem("Seat: " + rs.getInt(1) + " (Sec: " + rs.getInt(2) + ", Stad: " + rs.getInt(3) + ")");

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadData() {
        try (Connection conn = DBUtils.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM Ticket");
            model.setRowCount(0);
            while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getInt(7), rs.getInt(2), rs.getInt(5), rs.getInt(3), rs.getInt(4), rs.getInt(8)});
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
