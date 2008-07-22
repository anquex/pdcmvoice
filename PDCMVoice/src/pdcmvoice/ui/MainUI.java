/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainUI.java
 *
 * Created on 22-lug-2008, 7.26.38
 */

package pdcmvoice.ui;

/**
 *
 * @author marco
 */
public class MainUI extends javax.swing.JFrame {

    /** Creates new form MainUI */
    public MainUI() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        OnlineList = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        MainTabbedPanel = new javax.swing.JTabbedPane();
        StatusPanel = new javax.swing.JPanel();
        SettingsMainPanel = new javax.swing.JPanel();
        AudioSettingsPanel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jComboBox4 = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        ConnectionSettingsPanel = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jTextField10 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jSeparator2 = new javax.swing.JSeparator();
        jCheckBox3 = new javax.swing.JCheckBox();
        DCTestPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("PDCMVoice");

        OnlineList.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel6.setText("Online Users");

        jButton4.setText("Call");

        org.jdesktop.layout.GroupLayout OnlineListLayout = new org.jdesktop.layout.GroupLayout(OnlineList);
        OnlineList.setLayout(OnlineListLayout);
        OnlineListLayout.setHorizontalGroup(
            OnlineListLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(OnlineListLayout.createSequentialGroup()
                .addContainerGap()
                .add(OnlineListLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jButton4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                    .add(jLabel6))
                .addContainerGap())
        );
        OnlineListLayout.setVerticalGroup(
            OnlineListLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(OnlineListLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 341, Short.MAX_VALUE)
                .add(jButton4)
                .addContainerGap())
        );

        StatusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        org.jdesktop.layout.GroupLayout StatusPanelLayout = new org.jdesktop.layout.GroupLayout(StatusPanel);
        StatusPanel.setLayout(StatusPanelLayout);
        StatusPanelLayout.setHorizontalGroup(
            StatusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 272, Short.MAX_VALUE)
        );
        StatusPanelLayout.setVerticalGroup(
            StatusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 349, Short.MAX_VALUE)
        );

        MainTabbedPanel.addTab("Status", StatusPanel);

        AudioSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Audio Settings"));

        jLabel9.setText("Local Encoding");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        jComboBox4.setSelectedIndex(3);

        jLabel15.setText("Quality");

        org.jdesktop.layout.GroupLayout AudioSettingsPanelLayout = new org.jdesktop.layout.GroupLayout(AudioSettingsPanel);
        AudioSettingsPanel.setLayout(AudioSettingsPanelLayout);
        AudioSettingsPanelLayout.setHorizontalGroup(
            AudioSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(AudioSettingsPanelLayout.createSequentialGroup()
                .add(jLabel9)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jComboBox2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 19, Short.MAX_VALUE)
                .add(jLabel15)
                .add(18, 18, 18)
                .add(jComboBox4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        AudioSettingsPanelLayout.setVerticalGroup(
            AudioSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(AudioSettingsPanelLayout.createSequentialGroup()
                .add(AudioSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(jComboBox2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jComboBox4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel15))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        ConnectionSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Local Connection Settings"));

        jLabel7.setText("RTP Port");

        jLabel8.setText("RTCP   Port");

        jLabel10.setText("Recovery");

        jLabel11.setText("Master Port");

        org.jdesktop.layout.GroupLayout ConnectionSettingsPanelLayout = new org.jdesktop.layout.GroupLayout(ConnectionSettingsPanel);
        ConnectionSettingsPanel.setLayout(ConnectionSettingsPanelLayout);
        ConnectionSettingsPanelLayout.setHorizontalGroup(
            ConnectionSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ConnectionSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(ConnectionSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel8)
                    .add(jLabel11))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ConnectionSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jTextField6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                    .add(jTextField8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE))
                .add(16, 16, 16)
                .add(ConnectionSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel7)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ConnectionSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jTextField7)
                    .add(jTextField5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        ConnectionSettingsPanelLayout.setVerticalGroup(
            ConnectionSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ConnectionSettingsPanelLayout.createSequentialGroup()
                .add(ConnectionSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(jLabel7)
                    .add(jTextField8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextField5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ConnectionSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(jTextField6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10)
                    .add(jTextField7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton2.setText("Apply");

        jButton3.setText("Restore Defaults");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Trasmission Settings"));

        jCheckBox1.setSelected(true);
        jCheckBox1.setText("Dynamic Adaptation");
        jCheckBox1.setToolTipText("if enabled PDCM tried to adjust dyncamically \\n\nsending and receiving settings according to \ncurrent Voice Session quality");
        jCheckBox1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jCheckBox1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jLabel12.setText("Min Buffered Millis");

        jLabel13.setText("Max Buffered Millis");

        jLabel14.setText("Frames Per Packet");

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2" }));

        jCheckBox2.setSelected(true);
        jCheckBox2.setText("enable RDT");
        jCheckBox2.setToolTipText("if enabled PDCM tried to adjust dyncamically \\n\nsending and receiving settings according to \ncurrent Voice Session quality");
        jCheckBox2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jCheckBox2.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        jCheckBox3.setSelected(true);
        jCheckBox3.setText("Background Recovery");
        jCheckBox3.setToolTipText("if enabled PDCM tried to adjust dyncamically \\n\nsending and receiving settings according to \ncurrent Voice Session quality");
        jCheckBox3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jCheckBox3.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox3ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(50, 50, 50)
                        .add(jCheckBox1))
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, jCheckBox2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel14, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                                .add(jComboBox3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 128, Short.MAX_VALUE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jTextField9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel13, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jTextField10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jCheckBox3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jCheckBox1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextField9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel13)
                    .add(jTextField10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel12))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBox3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel14))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jCheckBox2)
                    .add(jCheckBox3))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout SettingsMainPanelLayout = new org.jdesktop.layout.GroupLayout(SettingsMainPanel);
        SettingsMainPanel.setLayout(SettingsMainPanelLayout);
        SettingsMainPanelLayout.setHorizontalGroup(
            SettingsMainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(SettingsMainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(SettingsMainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(AudioSettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, SettingsMainPanelLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 25, Short.MAX_VALUE)
                        .add(jButton3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton2)
                        .add(65, 65, 65))
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(ConnectionSettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        SettingsMainPanelLayout.setVerticalGroup(
            SettingsMainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(SettingsMainPanelLayout.createSequentialGroup()
                .add(AudioSettingsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ConnectionSettingsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(7, 7, 7)
                .add(SettingsMainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton3)
                    .add(jButton2))
                .addContainerGap())
        );

        MainTabbedPanel.addTab("Settings", SettingsMainPanel);

        DCTestPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Direct Connection Test"));

        jLabel1.setText("Remote Address");

        jLabel2.setText("Remote RTP   Port");

        jLabel3.setText("Remote RTCP Port");

        jButton1.setText("jButton1");

        jLabel4.setText("Remote Encoding");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel5.setText("Notes:");

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setFont(new java.awt.Font("Courier", 0, 12)); // NOI18N
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText("Simply starts transmitting and receiving without caring about settings and host status.\n");
        jTextArea1.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextArea1);

        org.jdesktop.layout.GroupLayout DCTestPanelLayout = new org.jdesktop.layout.GroupLayout(DCTestPanel);
        DCTestPanel.setLayout(DCTestPanelLayout);
        DCTestPanelLayout.setHorizontalGroup(
            DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(DCTestPanelLayout.createSequentialGroup()
                .add(DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, DCTestPanelLayout.createSequentialGroup()
                        .addContainerGap(189, Short.MAX_VALUE)
                        .add(jButton1))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, DCTestPanelLayout.createSequentialGroup()
                        .add(DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(jLabel1)
                            .add(jLabel2)
                            .add(jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 55, Short.MAX_VALUE)
                        .add(DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jComboBox1, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextField3)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextField1)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextField2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE))))
                .addContainerGap())
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
            .add(DCTestPanelLayout.createSequentialGroup()
                .add(jLabel5)
                .addContainerGap())
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
        );
        DCTestPanelLayout.setVerticalGroup(
            DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(DCTestPanelLayout.createSequentialGroup()
                .add(DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jTextField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .add(23, 23, 23)
                .add(jButton1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
        );

        MainTabbedPanel.addTab("DC Test", DCTestPanel);

        fileMenu.setText("File");

        openMenuItem.setText("Open");
        fileMenu.add(openMenuItem);

        saveMenuItem.setText("Save");
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setText("Save As ...");
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Edit");

        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        helpMenu.setText("Help");

        contentsMenuItem.setText("Contents");
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(MainTabbedPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 293, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(OnlineList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(MainTabbedPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                .addContainerGap())
            .add(OnlineList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox3ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AudioSettingsPanel;
    private javax.swing.JPanel ConnectionSettingsPanel;
    private javax.swing.JPanel DCTestPanel;
    private javax.swing.JTabbedPane MainTabbedPanel;
    private javax.swing.JPanel OnlineList;
    private javax.swing.JPanel SettingsMainPanel;
    private javax.swing.JPanel StatusPanel;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    // End of variables declaration//GEN-END:variables

}