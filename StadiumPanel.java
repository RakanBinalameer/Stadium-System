import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StadiumPanel extends JPanel {
    JTextField txtId, txtName, txtLoc, txtCap, txtYear;
    JTable table;
    DefaultTableModel model;

    public StadiumPanel() {
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Stadium Details"));

        txtId = new JTextField(); txtName = new JTextField();
        txtLoc = new JTextField(); txtCap = new JTextField();
        txtYear = new JTextField();

        formPanel.add(new JLabel("Stadium ID (PK):")); formPanel.add(txtId);
        formPanel.add(new JLabel("Name:"));             formPanel.add(txtName);
        formPanel.add(new JLabel("Location:"));         formPanel.add(txtLoc);
        formPanel.add(new JLabel("Capacity:"));         formPanel.add(txtCap);
        formPanel.add(new JLabel("Year Opened:"));      formPanel.add(txtYear);

        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Insert");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnSearch = new JButton("Search (Name)");
        JButton btnLoad = new JButton("Load All");

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnDelete);
        btnPanel.add(btnSearch); btnPanel.add(btnLoad);

        model = new DefaultTableModel();
        model.setColumnIdentifiers(new Object[]{"ID", "Name", "Location", "Capacity", "Year"});
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(formPanel, BorderLayout.CENTER);
        topContainer.add(btnPanel, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnLoad.addActionListener(e -> loadData());

        btnSearch.addActionListener(e -> {
            String search = txtName.getText().trim();
            if (search.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter name to search"); return; }
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "SELECT * FROM STADIUM WHERE Name LIKE ?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, "%" + search + "%");
                ResultSet rs = pst.executeQuery();
                model.setRowCount(0);
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getInt("Stadium_ID"), rs.getString("Name"), rs.getString("Location"), rs.getString("Total_Capaicity"), rs.getObject("Year_Opend")});
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnAdd.addActionListener(e -> {
            if (txtId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Stadium ID is required!");
                return;
            }
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "INSERT INTO STADIUM (Stadium_ID, Name, Location, Total_Capaicity, Year_Opend) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(txtId.getText()));
                DBUtils.setOptionalString(pst, 2, txtName.getText());
                DBUtils.setOptionalString(pst, 3, txtLoc.getText());
                DBUtils.setOptionalString(pst, 4, txtCap.getText());
                DBUtils.setOptionalInt(pst, 5, txtYear.getText());
                pst.executeUpdate();
                loadData();
                JOptionPane.showMessageDialog(this, "Stadium Added!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnUpdate.addActionListener(e -> {
            if (txtId.getText().trim().isEmpty()) return;
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "UPDATE STADIUM SET Name=?, Location=?, Total_Capaicity=?, Year_Opend=? WHERE Stadium_ID=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                DBUtils.setOptionalString(pst, 1, txtName.getText());
                DBUtils.setOptionalString(pst, 2, txtLoc.getText());
                DBUtils.setOptionalString(pst, 3, txtCap.getText());
                DBUtils.setOptionalInt(pst, 4, txtYear.getText());
                pst.setInt(5, Integer.parseInt(txtId.getText()));
                pst.executeUpdate();
                loadData();
                JOptionPane.showMessageDialog(this, "Stadium Updated!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnDelete.addActionListener(e -> {
            if (txtId.getText().trim().isEmpty()) return;
            try (Connection conn = DBUtils.getConnection()) {
                String sql = "DELETE FROM STADIUM WHERE Stadium_ID=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(txtId.getText()));
                pst.executeUpdate();
                loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtId.setText(model.getValueAt(row, 0).toString());
                    txtName.setText(model.getValueAt(row, 1) != null ? model.getValueAt(row, 1).toString() : "");
                    txtLoc.setText(model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "");
                    txtCap.setText(model.getValueAt(row, 3) != null ? model.getValueAt(row, 3).toString() : "");
                    txtYear.setText(model.getValueAt(row, 4) != null ? model.getValueAt(row, 4).toString() : "");
                }
            }
        });
        loadData();
    }

    private void loadData() {
        try (Connection conn = DBUtils.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM STADIUM");
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("Stadium_ID"), rs.getString("Name"), rs.getString("Location"), rs.getString("Total_Capaicity"), rs.getObject("Year_Opend")});
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
