package bibteximport;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class BibtexImport extends JPanel implements ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static JFrame frame;

	private static JButton openButton, saveButton, searchButton, addButton;
	private BibtexTable local, remote;
	private static String[] labels;
	private static File openFile;
	private JTextField searchAuthor, searchTitle;

	public BibtexImport(){
		labels = new String[]{"name","type","title","author","year"};

		openButton = new JButton("Open");
		openButton.addActionListener(this);
		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		saveButton.setMnemonic(KeyEvent.VK_S);
		JPanel card1 = new JPanel();
		card1.setLayout(new BoxLayout(card1, BoxLayout.Y_AXIS));
		card1.add(openButton);
		card1.add(saveButton);

		searchAuthor = new JTextField();
		searchAuthor.setColumns(17);
		searchTitle = new JTextField();
		searchTitle.setColumns(17);
		JPanel searches = new JPanel();
		searches.setLayout(new BoxLayout(searches, BoxLayout.Y_AXIS));
		searches.add(new JLabel("Author"));
		searches.add(searchAuthor);
		searches.add(new JLabel("Title"));
		searches.add(searchTitle);

		searchButton = new JButton("Search");
		searchButton.addActionListener(this);
		searchButton.setVerticalAlignment(SwingConstants.BOTTOM);
		JPanel card2 = new JPanel();
		card2.add(searches);
		card2.add(searchButton);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(card1);
		buttonPanel.add(card2);



		local = new BibtexTable(labels);
		JScrollPane localScroll = new JScrollPane(local.table);

		addButton = new JButton("Add >>");
		addButton.addActionListener(this);

		remote = new BibtexTable(labels);
		JScrollPane remoteScroll = new JScrollPane(remote.table);

		JPanel bigPanel = new JPanel();
		bigPanel.setLayout(new BoxLayout(bigPanel, BoxLayout.X_AXIS));
		bigPanel.add(remoteScroll);
		bigPanel.add(addButton);
		bigPanel.add(localScroll);

		local.resetTable();
		remote.resetTable();

		setLayout(new BorderLayout(0, 0));
		add(buttonPanel, BorderLayout.PAGE_START);
		add(bigPanel, BorderLayout.CENTER);
		
		local.table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		local.table.getActionMap().put("delete", new AbstractAction(){
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ae) {
				local.removeSelected();
			}
		});
		
		remote.table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		remote.table.getActionMap().put("enter", new AbstractAction(){
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ae) {
				addButton.doClick();
			}
		});
		
		searchTitle.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		searchTitle.getActionMap().put("enter", new AbstractAction(){
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ae) {
				searchButton.doClick();
			}
		});
		searchAuthor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		searchAuthor.getActionMap().put("enter", new AbstractAction(){
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ae) {
				searchButton.doClick();
			}
		});
	}
	

	public void actionPerformed(ActionEvent action){
		if(action.getSource() == openButton){
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter("Bibtex Files", "bib"));
			if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
				openFile = fc.getSelectedFile();
				try {
					local.openFile(openFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
		}
		else if (action.getSource() == saveButton){
			if(openFile == null){
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("Bibtex Files", "bib"));
				if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
					try {
						local.saveFile(fc.getSelectedFile());
						openFile = fc.getSelectedFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}	
			}
			try {
				local.saveFile(openFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (action.getSource() == searchButton){
			Document doc;
			try {
				String url = "http://www.ams.org/mathscinet/search/publications.html?"
						+ "pg4=AUCN&s4="
						+ URLEncoder.encode(searchAuthor.getText(),"UTF-8")
						+ "&pg5=TI&s5="
						+ URLEncoder.encode(searchTitle.getText(), "UTF-8")
						+ "&fmt=bibtex";
				doc = Jsoup.connect(url).get();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			remote.addCitations(doc.select("pre").html());
		}
		else if(action.getSource() == addButton){
			int[] rows = remote.table.getSelectedRows();
			for(int i : rows)
				local.add(remote.citeAt(i));
			local.resetTable();
		}
	}

	private static void createGUI(){
		frame = new JFrame("Bibtex Import");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		BibtexImport bib = new BibtexImport();
		bib.setOpaque(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setContentPane(bib);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				createGUI();
			}
		});  
	}

}
