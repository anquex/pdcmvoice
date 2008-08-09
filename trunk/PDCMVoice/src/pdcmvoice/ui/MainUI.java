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

import java.net.SocketException;
import pdcmvoice.impl.RTCPStats;
import pdcmvoice.impl.VoiceSession;
import pdcmvoice.settings.AudioSettings;
import pdcmvoice.settings.ConnectionSettings;
import pdcmvoice.settings.TransmissionSettings;
import pdcmvoice.settings.VoiceSessionSettings;
import static pdcmvoice.impl.Constants.*;

import java.text.NumberFormat;

/**
 *
 * @author marco
 */
public class MainUI extends javax.swing.JFrame {

    //GUI Fields
    private ConnectionSettings myConnectionSettings=new ConnectionSettings();
    private TransmissionSettings myTransmissionSettings= new TransmissionSettings();
    private AudioSettings myAudioSettings=new AudioSettings();

    private AudioSettings DCTremoteAudioSettings=new AudioSettings();
    private ConnectionSettings DCTremoteConnectionSettings=new ConnectionSettings();

    private VoiceSessionSettings voiceSessionSettings;

    private VoiceSession voiceSession;

    private String remoteAddress="192.168.0.22";

    private RTCPStats rtcpStats;

    private NumberFormat nf=NumberFormat.getPercentInstance();


    private void renderStatus(){
        String bufferInfo="N/A";
        String ppl="N/A";
        if (voiceSession!=null){
            RPPayload.setText(""+voiceSession.lastReceivedPacketPayload());
            RPframesNumber.setText(""+voiceSession.lastReceivedPacketFrames());
            RPframesSize.setText(""+voiceSession.lastReceivedPacketFramesSize());
            RPRDT.setText(""+voiceSession.lastReceivedPacketRDT());
            
            SPframesNumber.setText(""+voiceSession.lastSentPacketFrames());
            SPPayload.setText(""+voiceSession.lastSentPacketPayload());
            SPframesSize.setText(""+voiceSession.lastSentPacketFramesSize());
            SPRDT.setText(""+voiceSession.lastSentPacketRDT());

            PBbufferedMillis.setText(""+voiceSession.getBufferedMillis());
            bufferInfo=voiceSession.getMinBufferedMillis()+" | ";
            bufferInfo+=voiceSession.getMaxBufferedMillis();

            ppl=nf.format(voiceSession.getPercivedPacketLoss());
        }else{
            RPPayload.setText("N/A");
            RPframesNumber.setText("N/A");
            RPframesSize.setText("N/A");
            RPRDT.setText("N/A");
            
            SPframesNumber.setText("N/A");
            SPPayload.setText("N/A");
            SPframesSize.setText("N/A");
            SPRDT.setText("N/A");

            PBbufferedMillis.setText("N/A");
        }
        PBMinMax.setText(bufferInfo);
        PercivedPL.setText(ppl);
    }

    private void renderLocalConnectionSettings(){
        UILocalMaster.setText(""+myConnectionSettings.getMaster());
        UILocalRTP.setText(""+myConnectionSettings.getRTP());
        UILocalRTCP.setText(""+myConnectionSettings.getRTCP());
        UILocalRecovery.setText(""+myConnectionSettings.getRecovery());

    }

    private void updateLocalConnectionSettings(){
        myConnectionSettings.setMaster(UILocalMaster.getText());
        myConnectionSettings.setRTP(UILocalRTP.getText());
        myConnectionSettings.setRTCP(UILocalRTCP.getText());
        myConnectionSettings.setRecovery(UILocalRecovery.getText());
    }
    private void renderLocalTransmissionSettings(){
        DynamicAdaptation.setSelected(myTransmissionSettings.getDynamic());
        UIminBuf.setText(""+myTransmissionSettings.getMinBufferSize());
        UImaxBuf.setText(""+myTransmissionSettings.getMaxBufferSize());
        UIRDT.setSelected(myTransmissionSettings.getRDT());
        UIBackgroundRecovery.setSelected(myTransmissionSettings.getRecovery());
        UIFramesPerPacket.setSelectedIndex(myTransmissionSettings.getFramesPerPacket()-1);
    }
    private void updateLocalTransmissionSettings(){
        myTransmissionSettings.setDynamic(DynamicAdaptation.isSelected());
        myTransmissionSettings.setFramesPerPacket(UIFramesPerPacket.getSelectedIndex()+1);
        myTransmissionSettings.setMaxBufferSize(UImaxBuf.getText());
        myTransmissionSettings.setMinBufferSize(UIminBuf.getText());
        myTransmissionSettings.setRDT(UIRDT.isSelected());
        myTransmissionSettings.setRecovery(UIBackgroundRecovery.isSelected());
    }

    private void updateAudioSettings(){
         myAudioSettings.setFormat(FORMAT_CODES[UILocalEncoding.getSelectedIndex()]);
         myAudioSettings.setSpeexQuality(UILocalQuality.getSelectedIndex());
    }

    private void renderAudioSettings(){
        UILocalEncoding.setSelectedIndex(myAudioSettings.getFormat()-1);
        UILocalQuality.setSelectedIndex(myAudioSettings.getSpeexQuality());
    }

    private void  updateDCTSettings(){
        remoteAddress=UIRemoteAddress.getText();

        DCTremoteConnectionSettings.setRTP(UIRemoteRTP.getText());
        DCTremoteConnectionSettings.setRTCP(UIRemoteRTCP.getText());
        DCTremoteAudioSettings.setFormat(FORMAT_CODES[UIRemoteEncoding.getSelectedIndex()]);

    }

    private void renderDCTSettings(){
        UIRemoteAddress.setText(remoteAddress);

        UIRemoteRTP.setText(""+ DCTremoteConnectionSettings.getRTP());
        UIRemoteRTCP.setText(""+DCTremoteConnectionSettings.getRTCP());
        UIRemoteEncoding.setSelectedIndex(DCTremoteAudioSettings.getFormat()-1);

    }

    private void renderRTCPStats(){
        if (rtcpStats!=null){
            SRBytesSent.setText(""+rtcpStats.SRoctetCount);
            SRPacketsSent.setText(""+rtcpStats.SRpacketCount);
           
            LRRDelay.setText(""+rtcpStats.RRdelayLastSR);
            RRSessionPL.setText(""+rtcpStats.RRcumulPacketsLost);
            RRJitter.setText(""+rtcpStats.RRinterArrivalJitter);
            String iplp=nf.format((float)rtcpStats.RRlossFraction/256);
            RRIntervalPL.setText(iplp);

        }else{
            SRBytesSent.setText("N/A");
            RRIntervalPL.setText("N/A");
            RRJitter.setText("N/A");
            LRRDelay.setText("N/A");
            SRPacketsSent.setText("N/A");
            RRSessionPL.setText("N/A");
        }

    }


    /** Creates new form MainUI */
    public MainUI() {
        initComponents();
        renderLocalConnectionSettings();
        renderLocalTransmissionSettings();
        renderAudioSettings();
        renderDCTSettings();
        renderStatus();
        UpdateGUI guiUpdater= new UpdateGUI();
        guiUpdater.start();
        nf.setMaximumFractionDigits(1);
        nf.setMaximumIntegerDigits(2);

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
        jPanel4 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        SPframesNumber = new javax.swing.JTextField();
        SPframesSize = new javax.swing.JTextField();
        SPPayload = new javax.swing.JTextField();
        SPRDT = new javax.swing.JTextField();
        jPanel10 = new javax.swing.JPanel();
        RPframesNumber = new javax.swing.JTextField();
        RPframesSize = new javax.swing.JTextField();
        RPPayload = new javax.swing.JTextField();
        RPRDT = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        PBbufferedMillis = new javax.swing.JTextField();
        PBMinMax = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        PercivedPL = new javax.swing.JTextField();
        timeElapsed = new javax.swing.JLabel();
        timeElapsed1 = new javax.swing.JLabel();
        RTCPPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        SRPacketsSent = new javax.swing.JTextField();
        SRBytesSent = new javax.swing.JTextField();
        LRRDelay = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        RRIntervalPL = new javax.swing.JTextField();
        RRJitter = new javax.swing.JTextField();
        RRSessionPL = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        SettingsMainPanel = new javax.swing.JPanel();
        AudioSettingsPanel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        UILocalEncoding = new javax.swing.JComboBox();
        UILocalQuality = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        ConnectionSettingsPanel = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        UILocalRTP = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        UILocalRTCP = new javax.swing.JTextField();
        UILocalRecovery = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        UILocalMaster = new javax.swing.JTextField();
        ApplySettingsButton = new javax.swing.JButton();
        RestoreDefaultsButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        DynamicAdaptation = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        UIminBuf = new javax.swing.JTextField();
        UImaxBuf = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        UIFramesPerPacket = new javax.swing.JComboBox();
        UIRDT = new javax.swing.JCheckBox();
        jSeparator2 = new javax.swing.JSeparator();
        UIBackgroundRecovery = new javax.swing.JCheckBox();
        DCTestPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        UIRemoteRTCP = new javax.swing.JTextField();
        UIRemoteRTP = new javax.swing.JTextField();
        UIRemoteAddress = new javax.swing.JTextField();
        transmitButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        UIRemoteEncoding = new javax.swing.JComboBox();
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
        menuBar1 = new javax.swing.JMenuBar();
        fileMenu1 = new javax.swing.JMenu();
        openMenuItem1 = new javax.swing.JMenuItem();
        exitMenuItem1 = new javax.swing.JMenuItem();
        editMenu1 = new javax.swing.JMenu();
        cutMenuItem1 = new javax.swing.JMenuItem();
        copyMenuItem1 = new javax.swing.JMenuItem();
        pasteMenuItem1 = new javax.swing.JMenuItem();
        deleteMenuItem1 = new javax.swing.JMenuItem();
        helpMenu1 = new javax.swing.JMenu();
        contentsMenuItem1 = new javax.swing.JMenuItem();
        aboutMenuItem1 = new javax.swing.JMenuItem();

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
                    .add(jButton4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                    .add(jLabel6))
                .addContainerGap())
        );
        OnlineListLayout.setVerticalGroup(
            OnlineListLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(OnlineListLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 353, Short.MAX_VALUE)
                .add(jButton4)
                .addContainerGap())
        );

        StatusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Last Packet"));

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel22.setText("Audio Frames");

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("Audio Frame Size");

        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel26.setText("Playload");

        jPanel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        SPframesNumber.setText("jTextField1");

        SPframesSize.setText("jTextField2");

        SPPayload.setText("jTextField3");

        SPRDT.setText("jTextField3");

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel8Layout.createSequentialGroup()
                        .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, SPframesNumber, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 53, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, SPframesSize, 0, 0, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, SPPayload, 0, 0, Short.MAX_VALUE))
                        .addContainerGap())
                    .add(jPanel8Layout.createSequentialGroup()
                        .add(SPRDT, 0, 0, Short.MAX_VALUE)
                        .add(11, 11, 11))))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .add(SPframesNumber, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(SPframesSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(SPPayload, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(SPRDT, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        RPframesNumber.setText("jTextField1");

        RPframesSize.setText("jTextField2");

        RPPayload.setText("jTextField3");

        RPRDT.setText("jTextField3");

        org.jdesktop.layout.GroupLayout jPanel10Layout = new org.jdesktop.layout.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, RPframesNumber, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 53, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, RPframesSize, 0, 0, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, RPPayload, 0, 0, Short.MAX_VALUE)
                    .add(RPRDT, 0, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .add(RPframesNumber, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(RPframesSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(RPPayload, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(RPRDT, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel29.setFont(new java.awt.Font("Bodoni MT", 2, 12));
        jLabel29.setForeground(new java.awt.Color(153, 0, 0));
        jLabel29.setText("Received");

        jLabel30.setFont(new java.awt.Font("Bodoni MT", 2, 12));
        jLabel30.setForeground(new java.awt.Color(153, 0, 0));
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel30.setText("Sent");

        jLabel31.setText("RDT");

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel31)
                            .add(jLabel23)
                            .add(jLabel22, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                            .add(jLabel26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 52, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(6, 6, 6))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                        .add(jLabel29)
                        .add(28, 28, 28)))
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(22, 22, 22)
                        .add(jLabel30))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel29)
                    .add(jLabel30))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(18, 18, 18)
                        .add(jLabel22)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jLabel23)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jLabel26)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel31))
                    .add(jPanel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Playout Buffer"));

        jLabel24.setText("Buffered ms");

        jLabel25.setText("Min | Max");

        PBbufferedMillis.setText("jTextField5");

        PBMinMax.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        PBMinMax.setText("jTextField6");

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jLabel24)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(PBbufferedMillis, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(11, 11, 11)
                .add(jLabel25)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(PBMinMax, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel24)
                    .add(jLabel25)
                    .add(PBbufferedMillis, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(PBMinMax, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(47, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("General"));

        jLabel28.setText("Percived PL");

        PercivedPL.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        PercivedPL.setText("jTextField1");
        PercivedPL.setMinimumSize(new java.awt.Dimension(64, 19));

        timeElapsed.setText("Not Started");

        timeElapsed1.setText("Elapsed:");

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .add(28, 28, 28)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel28)
                    .add(timeElapsed1))
                .add(31, 31, 31)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(timeElapsed)
                        .addContainerGap())
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(PercivedPL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                        .add(91, 91, 91))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(timeElapsed, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(timeElapsed1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(PercivedPL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel28))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout StatusPanelLayout = new org.jdesktop.layout.GroupLayout(StatusPanel);
        StatusPanel.setLayout(StatusPanelLayout);
        StatusPanelLayout.setHorizontalGroup(
            StatusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(StatusPanelLayout.createSequentialGroup()
                .add(StatusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        StatusPanelLayout.setVerticalGroup(
            StatusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(StatusPanelLayout.createSequentialGroup()
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(7, 7, 7)
                .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        MainTabbedPanel.addTab("Status", StatusPanel);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Last Sender Report"));

        jLabel16.setText("Packets Sent");

        jLabel17.setText("Bytes    Sent");

        jLabel21.setText("LRR Delay ");
        jLabel21.setToolTipText("packet loss in the inverval between previous and current report");

        SRPacketsSent.setPreferredSize(new java.awt.Dimension(64, 19));

        SRBytesSent.setMinimumSize(new java.awt.Dimension(23, 18));
        SRBytesSent.setPreferredSize(new java.awt.Dimension(64, 19));

        LRRDelay.setMinimumSize(new java.awt.Dimension(23, 18));
        LRRDelay.setPreferredSize(new java.awt.Dimension(64, 19));

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel21))
                    .add(jLabel16)
                    .add(jLabel17))
                .add(71, 71, 71)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(SRBytesSent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(SRPacketsSent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(LRRDelay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(62, 62, 62))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel16)
                    .add(SRPacketsSent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel17)
                    .add(SRBytesSent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel21)
                    .add(LRRDelay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Last Receiver Report"));

        jLabel18.setText("Interval PL Ratio");
        jLabel18.setToolTipText("packet loss in the inverval between previous and current report");

        RRIntervalPL.setMinimumSize(new java.awt.Dimension(23, 18));
        RRIntervalPL.setPreferredSize(new java.awt.Dimension(64, 19));

        RRJitter.setMinimumSize(new java.awt.Dimension(23, 18));
        RRJitter.setPreferredSize(new java.awt.Dimension(64, 19));
        RRJitter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RRJitterActionPerformed(evt);
            }
        });

        RRSessionPL.setMinimumSize(new java.awt.Dimension(23, 18));
        RRSessionPL.setPreferredSize(new java.awt.Dimension(64, 19));

        jLabel19.setText("Session Lost Packets");
        jLabel19.setToolTipText("packet loss in the inverval between previous and current report");

        jLabel20.setText("Jitter");
        jLabel20.setToolTipText("packet loss in the inverval between previous and current report");

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel18)
                    .add(jLabel20)
                    .add(jLabel19))
                .add(33, 33, 33)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanel3Layout.createSequentialGroup()
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(RRJitter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(RRSessionPL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(RRIntervalPL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(63, 63, 63))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel18)
                    .add(RRIntervalPL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel19)
                    .add(RRSessionPL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel20)
                    .add(RRJitter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Calculated Stats"));

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 288, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 134, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout RTCPPanelLayout = new org.jdesktop.layout.GroupLayout(RTCPPanel);
        RTCPPanel.setLayout(RTCPPanelLayout);
        RTCPPanelLayout.setHorizontalGroup(
            RTCPPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(RTCPPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(RTCPPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        RTCPPanelLayout.setVerticalGroup(
            RTCPPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(RTCPPanelLayout.createSequentialGroup()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        MainTabbedPanel.addTab("RTCP Stats", RTCPPanel);

        AudioSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Audio Settings"));

        jLabel9.setText("Local Encoding");

        UILocalEncoding.setModel(new javax.swing.DefaultComboBoxModel(FORMAT_NAMES));
        UILocalEncoding.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UILocalEncodingActionPerformed(evt);
            }
        });

        UILocalQuality.setModel(new javax.swing.DefaultComboBoxModel(SPEEX_QUALITIES_NAMES));
        UILocalQuality.setSelectedIndex(DEFAULT_SPEEX_QUALITY_INDEX);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Quality");

        org.jdesktop.layout.GroupLayout AudioSettingsPanelLayout = new org.jdesktop.layout.GroupLayout(AudioSettingsPanel);
        AudioSettingsPanel.setLayout(AudioSettingsPanelLayout);
        AudioSettingsPanelLayout.setHorizontalGroup(
            AudioSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(AudioSettingsPanelLayout.createSequentialGroup()
                .add(jLabel9)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(UILocalEncoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 75, Short.MAX_VALUE)
                .add(jLabel15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 42, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(UILocalQuality, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        AudioSettingsPanelLayout.setVerticalGroup(
            AudioSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(AudioSettingsPanelLayout.createSequentialGroup()
                .add(AudioSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(UILocalEncoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(UILocalQuality, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel15))
                .addContainerGap(43, Short.MAX_VALUE))
        );

        ConnectionSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Local Connection Settings"));

        jLabel7.setText("RTP Port");

        jLabel8.setText("RTCP   Port");

        jLabel10.setText("Recovery");

        jLabel11.setText("Master Port");

        UILocalMaster.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UILocalMasterActionPerformed(evt);
            }
        });

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
                    .add(UILocalRTCP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                    .add(UILocalMaster, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE))
                .add(16, 16, 16)
                .add(ConnectionSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel7)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ConnectionSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(UILocalRecovery)
                    .add(UILocalRTP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE))
                .addContainerGap(56, Short.MAX_VALUE))
        );
        ConnectionSettingsPanelLayout.setVerticalGroup(
            ConnectionSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ConnectionSettingsPanelLayout.createSequentialGroup()
                .add(ConnectionSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(jLabel7)
                    .add(UILocalMaster, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(UILocalRTP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ConnectionSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(UILocalRTCP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10)
                    .add(UILocalRecovery, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        ApplySettingsButton.setText("Apply");
        ApplySettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ApplySettingsButtonActionPerformed(evt);
            }
        });

        RestoreDefaultsButton.setText("Restore Defaults");
        RestoreDefaultsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RestoreDefaultsButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Trasmission Settings"));

        DynamicAdaptation.setSelected(true);
        DynamicAdaptation.setText("Dynamic Adaptation");
        DynamicAdaptation.setToolTipText("if enabled PDCM tried to adjust dyncamically \\n\nsending and receiving settings according to \ncurrent Voice Session quality");
        DynamicAdaptation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DynamicAdaptation.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        DynamicAdaptation.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                DynamicAdaptationItemStateChanged(evt);
            }
        });

        jLabel12.setText("Min Buffered ms");

        UIminBuf.setText(""+DEFAULT_MIN_BUFFER_SIZE);
        UIminBuf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UIminBufActionPerformed(evt);
            }
        });

        UImaxBuf.setText(""+DEFAULT_MAX_BUFFER_SIZE);

        jLabel13.setText("Max Buffered ms");

        jLabel14.setText("Frames Per Packet");

        UIFramesPerPacket.setModel(new javax.swing.DefaultComboBoxModel(ALLOWED_FRAMES_PER_PACKET_MENU));
        UIFramesPerPacket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UIFramesPerPacketActionPerformed(evt);
            }
        });

        UIRDT.setSelected(true);
        UIRDT.setText("enable RDT");
        UIRDT.setToolTipText("enable Rendundant Data Transmission");
        UIRDT.setContentAreaFilled(false);
        UIRDT.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        UIRDT.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        UIRDT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UIRDTActionPerformed(evt);
            }
        });

        UIBackgroundRecovery.setSelected(true);
        UIBackgroundRecovery.setText("Background Recovery");
        UIBackgroundRecovery.setToolTipText("if enabled PDCM tried to adjust dyncamically \\n\nsending and receiving settings according to \ncurrent Voice Session quality");
        UIBackgroundRecovery.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        UIBackgroundRecovery.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        UIBackgroundRecovery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UIBackgroundRecoveryActionPerformed(evt);
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
                        .add(DynamicAdaptation))
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, UIRDT, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel14, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(UIBackgroundRecovery, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(UIminBuf, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(UIFramesPerPacket, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(jLabel13, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(2, 2, 2)
                                .add(UImaxBuf, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(DynamicAdaptation)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(jLabel13)
                    .add(UImaxBuf, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(UIminBuf, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(UIFramesPerPacket, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel14))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(UIRDT)
                    .add(UIBackgroundRecovery))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout SettingsMainPanelLayout = new org.jdesktop.layout.GroupLayout(SettingsMainPanel);
        SettingsMainPanel.setLayout(SettingsMainPanelLayout);
        SettingsMainPanelLayout.setHorizontalGroup(
            SettingsMainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(SettingsMainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(SettingsMainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, SettingsMainPanelLayout.createSequentialGroup()
                        .add(RestoreDefaultsButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ApplySettingsButton))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, ConnectionSettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, AudioSettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        SettingsMainPanelLayout.setVerticalGroup(
            SettingsMainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(SettingsMainPanelLayout.createSequentialGroup()
                .add(AudioSettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ConnectionSettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(7, 7, 7)
                .add(SettingsMainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ApplySettingsButton)
                    .add(RestoreDefaultsButton))
                .addContainerGap())
        );

        MainTabbedPanel.addTab("Settings", SettingsMainPanel);

        DCTestPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Direct Connection Test"));
        DCTestPanel.setEnabled(false);

        jLabel1.setText("Remote Address");

        jLabel2.setText("Remote RTP   Port");

        jLabel3.setText("Remote RTCP Port");

        UIRemoteAddress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UIRemoteAddressActionPerformed(evt);
            }
        });

        transmitButton.setText("Start Transmitting");
        transmitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transmitButtonActionPerformed(evt);
            }
        });

        jLabel4.setText("Remote Encoding");

        UIRemoteEncoding.setModel(new javax.swing.DefaultComboBoxModel(FORMAT_NAMES));

        jLabel5.setText("Notes:");

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setFont(new java.awt.Font("Courier", 0, 12));
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
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, transmitButton)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, DCTestPanelLayout.createSequentialGroup()
                        .add(DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(jLabel1)
                            .add(jLabel2)
                            .add(jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 53, Short.MAX_VALUE)
                        .add(DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(UIRemoteEncoding, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, UIRemoteAddress)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, UIRemoteRTCP)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, UIRemoteRTP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE))))
                .addContainerGap())
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
            .add(DCTestPanelLayout.createSequentialGroup()
                .add(jLabel5)
                .addContainerGap())
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
        );
        DCTestPanelLayout.setVerticalGroup(
            DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(DCTestPanelLayout.createSequentialGroup()
                .add(DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(UIRemoteAddress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(UIRemoteRTP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(UIRemoteRTCP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(DCTestPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(UIRemoteEncoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .add(23, 23, 23)
                .add(transmitButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
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

        fileMenu1.setText("File");

        openMenuItem1.setText("Connect");
        fileMenu1.add(openMenuItem1);

        exitMenuItem1.setText("Exit");
        exitMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu1.add(exitMenuItem1);

        menuBar1.add(fileMenu1);

        editMenu1.setText("Preferences");

        cutMenuItem1.setText("Cut");
        editMenu1.add(cutMenuItem1);

        copyMenuItem1.setText("Copy");
        editMenu1.add(copyMenuItem1);

        pasteMenuItem1.setText("Paste");
        editMenu1.add(pasteMenuItem1);

        deleteMenuItem1.setText("Delete");
        editMenu1.add(deleteMenuItem1);

        menuBar1.add(editMenu1);

        helpMenu1.setText("Help");

        contentsMenuItem1.setText("Contents");
        helpMenu1.add(contentsMenuItem1);

        aboutMenuItem1.setText("About");
        helpMenu1.add(aboutMenuItem1);

        menuBar1.add(helpMenu1);

        setJMenuBar(menuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(MainTabbedPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(OnlineList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(MainTabbedPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
            .add(OnlineList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void UIRDTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UIRDTActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_UIRDTActionPerformed

    private void UIBackgroundRecoveryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UIBackgroundRecoveryActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_UIBackgroundRecoveryActionPerformed

    private void UIRemoteAddressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UIRemoteAddressActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_UIRemoteAddressActionPerformed

    private void ApplySettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ApplySettingsButtonActionPerformed

        updateLocalConnectionSettings();
        renderLocalConnectionSettings();

        updateLocalTransmissionSettings();
        renderLocalTransmissionSettings();

        updateAudioSettings();
        renderAudioSettings();

    }//GEN-LAST:event_ApplySettingsButtonActionPerformed

    private void RestoreDefaultsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RestoreDefaultsButtonActionPerformed
        // TODO add your handling code here:
        myConnectionSettings.restoreDefaults();
        renderLocalConnectionSettings();

        myTransmissionSettings.restoreDefaults();
        renderLocalTransmissionSettings();

        myAudioSettings.restoreDefaults();
        renderAudioSettings();
    }//GEN-LAST:event_RestoreDefaultsButtonActionPerformed

    private void UILocalEncodingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UILocalEncodingActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UILocalEncodingActionPerformed

    private void UILocalMasterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UILocalMasterActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UILocalMasterActionPerformed

    private void UIminBufActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UIminBufActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UIminBufActionPerformed

    private void DynamicAdaptationItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_DynamicAdaptationItemStateChanged
        boolean canManualModify=!DynamicAdaptation.isSelected();
        UIminBuf.setEnabled(canManualModify);
        UImaxBuf.setEnabled(canManualModify);
        UIRDT.setEnabled(canManualModify);
        UIBackgroundRecovery.setEnabled(canManualModify);
        UIFramesPerPacket.setEnabled(canManualModify);
    }//GEN-LAST:event_DynamicAdaptationItemStateChanged

    private void UIFramesPerPacketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UIFramesPerPacketActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UIFramesPerPacketActionPerformed

    private void transmitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transmitButtonActionPerformed
        updateDCTSettings();
        renderDCTSettings();
        if(voiceSession==null){
            try{
                voiceSessionSettings=new VoiceSessionSettings(myAudioSettings, 
                                                              myConnectionSettings, 
                                                              myTransmissionSettings, 
                                                              DCTremoteAudioSettings, 
                                                              DCTremoteConnectionSettings,
                                                              remoteAddress
                                                              );
                voiceSession=new VoiceSession(voiceSessionSettings);
                rtcpStats=voiceSession.getRTCPStats();
                transmitButton.setText("Stop Transmitting");
                voiceSession.start();
            }
            catch(SocketException e){
                e.printStackTrace();
            }
           // catch(UnsupportedAudioFileException ingore){}
            catch(Exception e){e.printStackTrace();}
        }
        else{
            voiceSession.stop();
            voiceSession=null;
            rtcpStats=null;
            transmitButton.setText("Start Transmitting");
        }

    }//GEN-LAST:event_transmitButtonActionPerformed

    private void RRJitterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RRJitterActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_RRJitterActionPerformed

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
    private javax.swing.JButton ApplySettingsButton;
    private javax.swing.JPanel AudioSettingsPanel;
    private javax.swing.JPanel ConnectionSettingsPanel;
    private javax.swing.JPanel DCTestPanel;
    private javax.swing.JCheckBox DynamicAdaptation;
    private javax.swing.JTextField LRRDelay;
    private javax.swing.JTabbedPane MainTabbedPanel;
    private javax.swing.JPanel OnlineList;
    private javax.swing.JTextField PBMinMax;
    private javax.swing.JTextField PBbufferedMillis;
    private javax.swing.JTextField PercivedPL;
    private javax.swing.JTextField RPPayload;
    private javax.swing.JTextField RPRDT;
    private javax.swing.JTextField RPframesNumber;
    private javax.swing.JTextField RPframesSize;
    private javax.swing.JTextField RRIntervalPL;
    private javax.swing.JTextField RRJitter;
    private javax.swing.JTextField RRSessionPL;
    private javax.swing.JPanel RTCPPanel;
    private javax.swing.JButton RestoreDefaultsButton;
    private javax.swing.JTextField SPPayload;
    private javax.swing.JTextField SPRDT;
    private javax.swing.JTextField SPframesNumber;
    private javax.swing.JTextField SPframesSize;
    private javax.swing.JTextField SRBytesSent;
    private javax.swing.JTextField SRPacketsSent;
    private javax.swing.JPanel SettingsMainPanel;
    private javax.swing.JPanel StatusPanel;
    private javax.swing.JCheckBox UIBackgroundRecovery;
    private javax.swing.JComboBox UIFramesPerPacket;
    private javax.swing.JComboBox UILocalEncoding;
    private javax.swing.JTextField UILocalMaster;
    private javax.swing.JComboBox UILocalQuality;
    private javax.swing.JTextField UILocalRTCP;
    private javax.swing.JTextField UILocalRTP;
    private javax.swing.JTextField UILocalRecovery;
    private javax.swing.JCheckBox UIRDT;
    private javax.swing.JTextField UIRemoteAddress;
    private javax.swing.JComboBox UIRemoteEncoding;
    private javax.swing.JTextField UIRemoteRTCP;
    private javax.swing.JTextField UIRemoteRTP;
    private javax.swing.JTextField UImaxBuf;
    private javax.swing.JTextField UIminBuf;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem aboutMenuItem1;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem contentsMenuItem1;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem copyMenuItem1;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem cutMenuItem1;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem deleteMenuItem1;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenu editMenu1;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem exitMenuItem1;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu fileMenu1;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenu helpMenu1;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuBar menuBar1;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem openMenuItem1;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem pasteMenuItem1;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JLabel timeElapsed;
    private javax.swing.JLabel timeElapsed1;
    private javax.swing.JButton transmitButton;
    // End of variables declaration//GEN-END:variables

    class UpdateGUI extends Thread{

        public void run(){
            while(true){
                try {
                    // do updates
                    renderRTCPStats();
                    sleep(500);
                    renderStatus();
                    sleep(500);
                    if (voiceSession!=null  )
                        timeElapsed.setText(voiceSession.getElapsed());
                } catch (InterruptedException ignore) {
                    ignore.printStackTrace();
                    break;
                }
            }
        }
    }
}
