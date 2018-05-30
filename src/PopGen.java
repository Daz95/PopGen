import javax.sound.midi.MidiUnavailableException;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class PopGen extends JApplet {
	PopGenPanel panel;

	public void init() {
		try {
			getContentPane().setVisible(false);
			JFrame frame = new JFrame("PopGen");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			PopGenPanel panel = new PopGenPanel();
			panel.setVisible(true);
			frame.setContentPane(panel);
			frame.pack();
            frame.setMinimumSize( frame.getSize() );
            frame.setLocationByPlatform(true);
            frame.setVisible(true);

		} catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	JFrame f = new JFrame("PopGen");
                f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
                PopGenPanel PopGenPanel;
				try {
					PopGenPanel = new PopGenPanel();
					f.setContentPane(PopGenPanel);
	                f.pack();
	                f.setMinimumSize( f.getSize() );
	                f.setLocationByPlatform(true);
	                f.setVisible(true);
				} catch (MidiUnavailableException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
    }

}
