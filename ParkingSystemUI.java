import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** A small Swing front end for the ParkingSystem data-structure project. */
public class ParkingSystemUI extends JFrame {
    private static final Color NAVY = new Color(19, 45, 72);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color BACKGROUND = new Color(244, 247, 251);
    private static final Color TEXT = new Color(31, 41, 55);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel totalCount = new JLabel();
    private final JLabel twoWAvailable = new JLabel();
    private final JLabel fourWAvailable = new JLabel();
    private final JLabel totalRevenue = new JLabel();
    private final DefaultTableModel activeModel = tableModel("Vehicle No.", "Owner", "Type", "Slot", "Entry Time", "Duration");
    private final DefaultTableModel historyModel = tableModel("Vehicle No.", "Owner", "Type", "Slot", "Exit Time", "Fee");
    private final JTextField vehicleNo = new JTextField();
    private final JTextField ownerName = new JTextField();
    private final JComboBox<String> vehicleType = new JComboBox<>(new String[]{"2W - Two Wheeler", "4W - Four Wheeler"});
    private final JTextField searchField = new JTextField();
    private final JLabel searchResult = new JLabel("Enter a vehicle number to look it up.");

    public static void launch() {
        SwingUtilities.invokeLater(() -> new ParkingSystemUI().setVisible(true));
    }

    public ParkingSystemUI() {
        super("ParkEase | Parking Management");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 680));
        setSize(1200, 740);
        setLocationRelativeTo(null);
        buildInterface();
        refreshData();
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createTopBar(), BorderLayout.NORTH);

        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(24, 28, 28, 28));
        content.add(createDashboard(), "Dashboard");
        content.add(createParkVehiclePage(), "Park Vehicle");
        content.add(createVehiclesPage(), "Active Vehicles");
        content.add(createSearchPage(), "Search Vehicle");
        content.add(createHistoryPage(), "History");
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(225, 0));
        sidebar.setBackground(NAVY);
        sidebar.setBorder(new EmptyBorder(28, 18, 24, 18));

        JLabel brand = new JLabel("PARKEASE");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("SansSerif", Font.BOLD, 23));
        JLabel subtitle = new JLabel("PARKING MANAGEMENT");
        subtitle.setForeground(new Color(191, 219, 254));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(3));
        sidebar.add(subtitle);
        sidebar.add(Box.createVerticalStrut(38));

        addNavButton(sidebar, "Dashboard", "Dashboard");
        addNavButton(sidebar, "Park a Vehicle", "Park Vehicle");
        addNavButton(sidebar, "Active Vehicles", "Active Vehicles");
        addNavButton(sidebar, "Search Vehicle", "Search Vehicle");
        addNavButton(sidebar, "Parking History", "History");
        sidebar.add(Box.createVerticalGlue());
        JLabel rate = new JLabel("Rates: 2W Rs. 1/min | 4W Rs. 2/min");
        rate.setAlignmentX(Component.LEFT_ALIGNMENT);
        rate.setForeground(new Color(191, 219, 254));
        rate.setFont(new Font("SansSerif", Font.PLAIN, 10));
        sidebar.add(rate);
        return sidebar;
    }

    private void addNavButton(JPanel parent, String label, String card) {
        JButton button = new JButton(label);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(Color.WHITE);
        button.setBackground(NAVY);
        button.setBorder(new EmptyBorder(10, 12, 10, 10));
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.addActionListener(e -> { cards.show(content, card); refreshData(); });
        parent.add(button);
        parent.add(Box.createVerticalStrut(6));
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)), new EmptyBorder(17, 28, 17, 28)));
        JLabel title = new JLabel("Parking Allotment System");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        JLabel status = new JLabel("System online");
        status.setForeground(new Color(22, 163, 74));
        status.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bar.add(title, BorderLayout.WEST);
        bar.add(status, BorderLayout.EAST);
        return bar;
    }

    private JPanel createDashboard() {
        JPanel page = pagePanel("Dashboard", "A quick view of your parking facility.");
        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(statCard("ACTIVE VEHICLES", totalCount, NAVY));
        stats.add(statCard("2W SLOTS AVAILABLE", twoWAvailable, new Color(8, 145, 178)));
        stats.add(statCard("4W SLOTS AVAILABLE", fourWAvailable, new Color(124, 58, 237)));
        stats.add(statCard("TOTAL REVENUE", totalRevenue, new Color(22, 163, 74)));
        page.add(stats);
        page.add(Box.createVerticalStrut(25));

        JPanel overview = whitePanel(new BorderLayout(0, 16));
        overview.setBorder(new EmptyBorder(20, 22, 20, 22));
        JLabel heading = new JLabel("Current parking overview");
        heading.setFont(new Font("SansSerif", Font.BOLD, 16));
        overview.add(heading, BorderLayout.NORTH);
        JTable table = makeTable(activeModel);
        overview.add(new JScrollPane(table), BorderLayout.CENTER);
        page.add(overview);
        return page;
    }

    private JPanel createParkVehiclePage() {
        JPanel page = pagePanel("Park a Vehicle", "Register a vehicle and allocate the next available slot.");
        JPanel form = whitePanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(28, 30, 28, 30));
        form.setMaximumSize(new Dimension(620, 350));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 0, 7, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        addFormField(form, c, 0, "Vehicle number", vehicleNo);
        addFormField(form, c, 2, "Owner name", ownerName);
        addFormField(form, c, 4, "Vehicle type", vehicleType);
        JButton park = primaryButton("Allocate Parking Slot");
        c.gridy = 6; c.insets = new Insets(19, 0, 0, 0);
        form.add(park, c);
        park.addActionListener(e -> parkVehicle());
        page.add(form);
        return page;
    }

    private void addFormField(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
        JLabel fieldLabel = new JLabel(label.toUpperCase());
        fieldLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        fieldLabel.setForeground(new Color(75, 85, 99));
        c.gridy = row; form.add(fieldLabel, c);
        c.gridy = row + 1;
        field.setPreferredSize(new Dimension(0, 36));
        form.add(field, c);
    }

    private JPanel createVehiclesPage() {
        JPanel page = pagePanel("Active Vehicles", "Vehicles currently parked in the facility.");
        JPanel tablePanel = whitePanel(new BorderLayout(0, 14));
        tablePanel.setBorder(new EmptyBorder(20, 22, 22, 22));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton refresh = secondaryButton("Refresh");
        JButton checkout = primaryButton("Check Out Selected");
        refresh.addActionListener(e -> refreshData());
        checkout.addActionListener(e -> checkoutSelected());
        actions.add(refresh); actions.add(checkout);
        tablePanel.add(actions, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(makeTable(activeModel)), BorderLayout.CENTER);
        page.add(tablePanel);
        return page;
    }

    private JPanel createSearchPage() {
        JPanel page = pagePanel("Search Vehicle", "Find current parking details by vehicle number.");
        JPanel panel = whitePanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(28, 30, 28, 30));
        panel.setMaximumSize(new Dimension(700, 260));
        GridBagConstraints c = new GridBagConstraints(); c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        JLabel prompt = new JLabel("VEHICLE NUMBER"); prompt.setFont(new Font("SansSerif", Font.BOLD, 11)); prompt.setForeground(new Color(75, 85, 99));
        c.gridy = 0; panel.add(prompt, c);
        c.gridy = 1; c.insets = new Insets(8, 0, 0, 0); searchField.setPreferredSize(new Dimension(0, 36)); panel.add(searchField, c);
        JButton search = primaryButton("Search Vehicle"); c.gridy = 2; c.insets = new Insets(15, 0, 16, 0); panel.add(search, c);
        search.addActionListener(e -> searchVehicle());
        searchResult.setFont(new Font("SansSerif", Font.PLAIN, 14)); searchResult.setForeground(TEXT);
        c.gridy = 3; c.insets = new Insets(0, 0, 0, 0); panel.add(searchResult, c);
        page.add(panel);
        return page;
    }

    private JPanel createHistoryPage() {
        JPanel page = pagePanel("Parking History", "Completed visits and collected parking fees.");
        JPanel panel = whitePanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 22, 22, 22));
        panel.add(new JScrollPane(makeTable(historyModel)), BorderLayout.CENTER);
        page.add(panel);
        return page;
    }

    private JPanel pagePanel(String title, String subtitle) {
        JPanel page = new JPanel(); page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS)); page.setOpaque(false);
        JLabel heading = new JLabel(title); heading.setForeground(TEXT); heading.setFont(new Font("SansSerif", Font.BOLD, 25));
        JLabel detail = new JLabel(subtitle); detail.setForeground(new Color(100, 116, 139)); detail.setFont(new Font("SansSerif", Font.PLAIN, 14));
        page.add(heading); page.add(Box.createVerticalStrut(5)); page.add(detail); page.add(Box.createVerticalStrut(24));
        return page;
    }

    private JPanel statCard(String label, JLabel value, Color accent) {
        JPanel card = whitePanel(new BorderLayout()); card.setBorder(new CompoundBorder(new MatteBorder(4, 0, 0, 0, accent), new EmptyBorder(17, 17, 17, 17)));
        JLabel caption = new JLabel(label); caption.setForeground(new Color(100, 116, 139)); caption.setFont(new Font("SansSerif", Font.BOLD, 10));
        value.setForeground(TEXT); value.setFont(new Font("SansSerif", Font.BOLD, 25));
        card.add(caption, BorderLayout.NORTH); card.add(value, BorderLayout.CENTER); return card;
    }

    private JPanel whitePanel(LayoutManager layout) { JPanel panel = new JPanel(layout); panel.setBackground(Color.WHITE); return panel; }
    private DefaultTableModel tableModel(String... names) { return new DefaultTableModel(names, 0) { public boolean isCellEditable(int r, int c) { return false; } }; }
    private JTable makeTable(DefaultTableModel model) { JTable t = new JTable(model); t.setRowHeight(30); t.setFont(new Font("SansSerif", Font.PLAIN, 13)); t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12)); t.getTableHeader().setBackground(new Color(241, 245, 249)); t.setSelectionBackground(new Color(219, 234, 254)); return t; }
    private JButton primaryButton(String text) { JButton b = new JButton(text); b.setForeground(Color.WHITE); b.setBackground(BLUE); b.setFocusPainted(false); b.setBorder(new EmptyBorder(10, 17, 10, 17)); return b; }
    private JButton secondaryButton(String text) { JButton b = new JButton(text); b.setForeground(TEXT); b.setBackground(Color.WHITE); b.setFocusPainted(false); b.setBorder(new CompoundBorder(new LineBorder(new Color(203, 213, 225)), new EmptyBorder(9, 16, 9, 16))); return b; }

    private void parkVehicle() {
        String no = vehicleNo.getText().trim().toUpperCase(); String owner = ownerName.getText().trim();
        if (no.isEmpty() || owner.isEmpty()) { showError("Please enter both a vehicle number and an owner name."); return; }
        if (ParkingSystem.parkedVehicles.containsKey(no)) { showError("This vehicle is already parked."); return; }
        String type = vehicleType.getSelectedIndex() == 0 ? "2W" : "4W";
        MyQueue slots = type.equals("2W") ? ParkingSystem.free2WSlots : ParkingSystem.free4WSlots;
        if (slots.isEmpty()) { showError("No " + type + " slots are available."); return; }
        int slot = slots.poll(); ParkingSystem.parkedVehicles.put(no, new Vehicle(no, owner, type, slot));
        vehicleNo.setText(""); ownerName.setText(""); refreshData();
        JOptionPane.showMessageDialog(this, "Vehicle " + no + " has been allotted " + type + " slot " + slot + ".", "Parking confirmed", JOptionPane.INFORMATION_MESSAGE);
        cards.show(content, "Active Vehicles");
    }

    private void checkoutSelected() {
        // All active tables share the same model, so the selected row is read from the visible vehicle table.
        String no = null;
        for (Component component : content.getComponents()) {
            no = findSelectedVehicle(component);
            if (no != null) break;
        }
        if (no == null) { showError("Select a vehicle row to check it out."); return; }
        checkout(no);
    }

    private String findSelectedVehicle(Component component) {
        if (component instanceof JTable table && table.getModel() == activeModel && table.getSelectedRow() >= 0)
            return String.valueOf(activeModel.getValueAt(table.getSelectedRow(), 0));
        if (component instanceof Container container) for (Component child : container.getComponents()) { String found = findSelectedVehicle(child); if (found != null) return found; }
        return null;
    }

    private void checkout(String no) {
        Vehicle v = ParkingSystem.parkedVehicles.remove(no);
        if (v == null) { showError("Vehicle was not found."); return; }
        v.setExitTime(); long minutes = Math.max(1, v.getDurationMinutes()); v.fee = minutes * (v.type.equals("2W") ? 1 : 2);
        if (v.type.equals("2W")) ParkingSystem.free2WSlots.add(v.slotNo); else ParkingSystem.free4WSlots.add(v.slotNo);
        ParkingSystem.totalRevenue += v.fee;
        String month = java.time.YearMonth.now().toString(); Double current = ParkingSystem.monthlyRevenue.get(month);
        ParkingSystem.monthlyRevenue.put(month, (current == null ? 0 : current) + v.fee); ParkingSystem.history.add(v);
        refreshData(); JOptionPane.showMessageDialog(this, "Check-out complete. Parking fee: Rs. " + String.format("%.2f", v.fee), "Payment recorded", JOptionPane.INFORMATION_MESSAGE);
    }

    private void searchVehicle() {
        String no = searchField.getText().trim().toUpperCase(); Vehicle v = ParkingSystem.parkedVehicles.get(no);
        if (v == null) { searchResult.setForeground(new Color(185, 28, 28)); searchResult.setText("No active parking record found for " + no + "."); return; }
        long mins = Duration.between(v.entryTime, LocalDateTime.now()).toMinutes(); searchResult.setForeground(new Color(22, 101, 52));
        searchResult.setText("Found: " + v.ownerName + " | " + v.type + " | Slot " + v.slotNo + " | Parked for " + mins + " min");
    }

    private void refreshData() {
        totalCount.setText(String.valueOf(ParkingSystem.parkedVehicles.size())); twoWAvailable.setText(String.valueOf(ParkingSystem.free2WSlots.size()));
        fourWAvailable.setText(String.valueOf(ParkingSystem.free4WSlots.size())); totalRevenue.setText("Rs. " + String.format("%.2f", ParkingSystem.totalRevenue));
        activeModel.setRowCount(0); for (Vehicle v : ParkingSystem.parkedVehicles.values()) activeModel.addRow(new Object[]{v.vehicleNo, v.ownerName, v.type, v.slotNo, TIME_FORMAT.format(v.entryTime), elapsed(v.entryTime)});
        historyModel.setRowCount(0); for (Vehicle v : ParkingSystem.history) historyModel.addRow(new Object[]{v.vehicleNo, v.ownerName, v.type, v.slotNo, TIME_FORMAT.format(v.exitTime), "Rs. " + String.format("%.2f", v.fee)});
    }

    private String elapsed(LocalDateTime entry) { long mins = Duration.between(entry, LocalDateTime.now()).toMinutes(); return mins + " min"; }
    private void showError(String message) { JOptionPane.showMessageDialog(this, message, "Action needed", JOptionPane.WARNING_MESSAGE); }
}
