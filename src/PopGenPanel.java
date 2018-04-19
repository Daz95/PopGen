import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/** The main UI of Beeper. */
class PopGenPanel extends JPanel{

    JSlider rating;
    

    PopGenPanel() throws MidiUnavailableException, InterruptedException {    	
        super(new BorderLayout());
        // Use current OS look and feel.
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setPreferredSize( new Dimension(300,300) );

        JPanel mainPanel = new JPanel();
        JPanel ratingPanel = new JPanel( new BorderLayout() );
        JPanel rateButtonPanel = new JPanel(new BorderLayout(3,3));
        
        BoxLayout layout = new BoxLayout(mainPanel,BoxLayout.Y_AXIS);
        mainPanel.setLayout(layout);


        rating = new JSlider(JSlider.HORIZONTAL,0,10,5);
        rating.setPaintTicks(true);
        rating.setMajorTickSpacing(1);
        rating.setMinorTickSpacing(1);
        rating.setToolTipText("Rating");

        ratingPanel.setBorder(new TitledBorder("Rating"));

        ratingPanel.add(rating);
        mainPanel.add(ratingPanel);

        
        rateButtonPanel.setBorder( new EmptyBorder(4,4,4,4) );
        
        JButton rateButton  = new JButton("Rate");
        rateButton.setToolTipText("Rate the song!");
        
        Dimension preferredSize = rateButton.getPreferredSize();
        rateButton.setPreferredSize( new Dimension(
            (int)preferredSize.getWidth(),
            (int)preferredSize.getHeight()*3) );
        
        Song song = new Song();
        
        rateButton.addMouseListener( new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent me) {
                	try {
    					song.evolveAndPlay((int)rating.getValue());
    				} catch (InterruptedException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				};
                }
            } );
        rateButtonPanel.add(rateButton);
        add(rateButtonPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.NORTH);
    } 
}