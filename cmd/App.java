package cmd;
//BT3 COM Param Editor - GUI
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.WindowConstants;

public class App 
{
	public static double seconds=0;
	private static File src;
	public static JLabel fileLabel;
	private static final Image ICON = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("img/icon.png"));
	public static final String HTML_TEXT = "<html><div style='font-family: Tahoma, Geneva, sans-serif; font-weight: bold; font-size: 14px;'>";
	private static final String HTML_TEXT_TITLE = HTML_TEXT.replace("14px;", "24px; color: #894ceb;");
	private static final String HTML_TEXT_SUBTITLE = HTML_TEXT.replace("14px;", "18px; color: #0969da;");
	private static final String HTML_TEXT_TIP = HTML_TEXT.replace("font-weight: bold;", "");
	private static final String WINDOW_TITLE = "BT3 COM Data Editor";
	
	public static void setApp()
	{
		String[] labelText = {"Choose Folder:","What to Detect?","Transformation Type","MAX Health Threshold (%):","Apply Changes"};
		String[] radioBtnText = {"Costume Files","Costume Folders","Selected Form Type","All Characters"};
		String[] transformBonusText = {"No Transformation","No Bonus","Detransformation","Health Bonus","25% MAX Health Bonus","Full Ki Refill"};
		String[] toolTipText = 
		{"Only detects PAK files that end with<br>Xp, for X that ranges from 1 to 4.",
		"Only detects folders with unpacked contents that are named<br>after the costume PAKs and contain 026_com_param.cmr.",
		"Skips characters that have no transformation type,<br>or one that is different from the selected type.",
		"Not recommended if the user wants the COM<br>to recognize transformation bonuses differently."
		};
		//initialize components
		JFrame frame = new JFrame(WINDOW_TITLE);
		JPanel panel = new JPanel();
		JLabel title = new JLabel(HTML_TEXT_TITLE+"COM Data Editor");
		JLabel dirLabelAsBtn = new JLabel(" ");
		JLabel[] labels = new JLabel[labelText.length];
		JTextField healthField = new JTextField();
		JComboBox<String> dropdown = new JComboBox<String>(transformBonusText);
		JCheckBox check = new JCheckBox(HTML_TEXT+"Wii Mode");
		JButton btn = new JButton(HTML_TEXT+"Proceed");
		JRadioButton[] radioBtns = new JRadioButton[radioBtnText.length];
		ButtonGroup radioBtnGrp1 = new ButtonGroup();
		ButtonGroup radioBtnGrp2 = new ButtonGroup();
		GridBagConstraints gbc = new GridBagConstraints();
		Box box1 = Box.createHorizontalBox();
		Box box2 = Box.createHorizontalBox();
		Image glass = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("img/glass.png"));
		glass = glass.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
		ImageIcon glassIcon = new ImageIcon(glass);
		//set component properties
		dirLabelAsBtn.setIcon(glassIcon);
		Dimension healthFieldSize = new Dimension(50,25);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		healthField.setFont(new Font("Tahoma",Font.PLAIN,12));
		healthField.setHorizontalAlignment(JTextField.CENTER);
		healthField.setMinimumSize(healthFieldSize);
		healthField.setMaximumSize(healthFieldSize);
		healthField.setPreferredSize(healthFieldSize);
		panel.setLayout(new GridBagLayout());	
		check.setHorizontalAlignment(SwingConstants.CENTER);
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setToolTipText(HTML_TEXT_TIP+"Made by ViveTheModder, for the DBZ League.");
		for (int i=0; i<labelText.length; i++)
		{
			labels[i] = new JLabel(HTML_TEXT+labelText[i]);
			if (i==1 || i==4) labels[i].setText(labels[i].getText().replace(HTML_TEXT, HTML_TEXT_SUBTITLE));
			labels[i].setHorizontalAlignment(SwingConstants.CENTER);
		}
		for (int i=0; i<radioBtns.length; i++)
		{
			radioBtns[i] = new JRadioButton(HTML_TEXT+radioBtnText[i]);
			radioBtns[i].setHorizontalAlignment(SwingConstants.CENTER);
			radioBtns[i].setToolTipText(HTML_TEXT_TIP+toolTipText[i]);
			if (i>=2) radioBtnGrp2.add(radioBtns[i]);
			else radioBtnGrp1.add(radioBtns[i]);
			if (i%2==0) radioBtns[i].setSelected(true);
		}
		btn.setToolTipText(HTML_TEXT_TIP+"Even if not needed, this button will make the COM<br>recognize all its transformations rather than just one.");
		check.setToolTipText(HTML_TEXT_TIP+"This option is meant for character costume files that contain<br>Big Endian integers, aka PAK files from the Wii version.");
		labels[2].setToolTipText(HTML_TEXT_TIP+"Detransformation is the same as No Bonus in theory,<br>but the game still treats these two as separate options.");
		labels[3].setToolTipText
		(HTML_TEXT_TIP+"The COM will recognize its transformations if its health is less than<br>the threshold. If left empty, the threshold will not be changed.");
		//only type numbers in textfield (up to 3)
		healthField.addKeyListener(new KeyAdapter()
		{
			public void keyTyped(KeyEvent e)
			{
				char ch = e.getKeyChar();
				String text = healthField.getText();
				if (text.length()>2)
				{
					if (!(ch==KeyEvent.VK_DELETE || ch==KeyEvent.VK_BACK_SPACE))
						e.consume();
				}
				if (!(ch>='0' && ch<='9')) e.consume();
			}
		});
		//make label behave like button via mouse listener
		dirLabelAsBtn.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int result = chooser.showOpenDialog(chooser);
				if (result==0) 
				{
					labels[0].setText(HTML_TEXT.replace("'>", "color: #3cb371;'>")+"Choose Folder:");
					src = chooser.getSelectedFile();
				}
			}
		});
		btn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if (src==null) JOptionPane.showMessageDialog(null, HTML_TEXT_TIP+"No folder has been selected!", WINDOW_TITLE, 0);
				else 
				{
					frame.setVisible(false); frame.dispose();
					if (check.isSelected()) Main.isForWii=true;
					if (radioBtns[1].isSelected()) Main.isCmrFile=true;
					else Main.isCmrFile=false;
					if (radioBtns[3].isSelected()) Main.isForAll=true;
					else Main.isForAll=false;
					
					String healthText = healthField.getText();
					if (healthText.equals("")) Main.health=-1; //default value
					else Main.health = Integer.parseInt(healthText);
					Main.transformType = dropdown.getSelectedIndex();
					if (Main.health>100) Main.health=100; //handle overflow
					setProgress();
				}
			}
		});
		//add components
		box1.add(labels[0]);
		box1.add(new JLabel("  "));
		box1.add(dirLabelAsBtn);
		box2.add(labels[3]);
		box2.add(new JLabel("  "));
		box2.add(healthField);
		panel.add(title,gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(box1,gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(labels[1],gbc);
		panel.add(radioBtns[0],gbc);
		panel.add(radioBtns[1],gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(labels[2],gbc);
		panel.add(dropdown,gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(box2,gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(labels[4],gbc);
		panel.add(radioBtns[2],gbc);
		panel.add(radioBtns[3],gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(btn,gbc);
		panel.add(check,gbc);
		frame.add(panel);
		//set frame properties
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(ICON);
		frame.setSize(525,525);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	public static void setProgress()
	{
	    //initialize components
	    JDialog loading = new JDialog();
		JPanel panel = new JPanel();
		JLabel label = new JLabel(HTML_TEXT+"Working on:");
		fileLabel = new JLabel(" ");
		JLabel elapsed = new JLabel(HTML_TEXT+"Time elapsed:");
		JLabel timeLabel = new JLabel();
		GridBagConstraints gbc = new GridBagConstraints();
		Timer timer = new Timer(100, e -> 
		{
			seconds+=0.1;
			timeLabel.setText(HTML_TEXT+(int)(seconds/3600)+"h"+(int)(seconds/60)%60+"m"+String.format("%.1f",seconds%60)+"s");
		});
		timer.start();
		//set component properties
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		label.setHorizontalAlignment(SwingConstants.CENTER);
		fileLabel.setHorizontalAlignment(SwingConstants.CENTER);
		elapsed.setHorizontalAlignment(SwingConstants.CENTER);
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel.setLayout(new GridBagLayout());
		//add components
		panel.add(label,gbc);
		panel.add(fileLabel,gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(elapsed,gbc);
		panel.add(timeLabel,gbc);
		loading.add(panel);
		//set frame properties
		loading.setTitle(WINDOW_TITLE);
		loading.setSize(1024,256);
		loading.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		loading.setIconImage(ICON);
		loading.setLocationRelativeTo(null);
		loading.setVisible(true);
		
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{
			@Override
			protected Void doInBackground() throws Exception 
			{
				long start = System.currentTimeMillis();
				Main.traverse(src);
				long finish = System.currentTimeMillis();
				double time = (finish-start)/(double)1000;
				loading.setVisible(false); loading.dispose();
				JOptionPane.showMessageDialog(null, HTML_TEXT+"COM Parameters overwritten successfully in "+time+" seconds!", 
				WINDOW_TITLE, JOptionPane.INFORMATION_MESSAGE);
				System.exit(0);
				return null;
			}
		};
		worker.execute();
	}
}
