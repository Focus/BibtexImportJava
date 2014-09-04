/*
 * Copyright 2013 Bati Sengul
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bibteximport;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class BibtexImport extends JPanel implements ActionListener{

	private String VERSION = "1.3";
	private String WEBSITE = "https://sourceforge.net/projects/bibteximport/";
	private boolean MAC = System.getProperty("os.name").toLowerCase().indexOf("mac") != -1 ? true:false; 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	

	private static JFrame frame;

	private static JButton searchButton, addButton;
	private BibtexTable local, remote;
	private static String[] labels;
	private static File openFile;
	private JTextField searchAuthor, searchTitle;
	private JMenuBar menu;
	private JMenu recentMenu;
	private JMenuItem[] recentMenuItems;
	private JMenuItem newMenu, openMenu, saveMenu, saveAsMenu;
	private boolean edit = false;

	public BibtexImport(){
		labels = BibtexPrefs.getLabels();

		searchAuthor = new JTextField();
		searchAuthor.setColumns(17);
		searchTitle = new JTextField();
		searchTitle.setColumns(17);
		JPanel searches = new JPanel();
		searches.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		searches.add(new JLabel("Author"));
		searches.add(searchAuthor);
		searches.add(new JLabel("Title"));
		searches.add(searchTitle);

		searchButton = new JButton("Search");
		searchButton.addActionListener(this);
		searchButton.setVerticalAlignment(SwingConstants.BOTTOM);

		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		searchPanel.add(searches);
		searchPanel.add(searchButton);

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
		add(searchPanel, BorderLayout.PAGE_START);
		add(bigPanel, BorderLayout.CENTER);

		local.table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		local.table.getActionMap().put("delete", new AbstractAction(){
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				local.removeSelected();
			}
		});
		

		local.model.addTableModelListener(new TableModelListener(){
			public void tableChanged(TableModelEvent tme) {
				if(!edit)
					frame.setTitle("*"+frame.getTitle());
				edit = true;
			}
		});
		

		remote.table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		remote.table.getActionMap().put("enter", new AbstractAction(){
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				addButton.doClick();
			}
		});

		searchTitle.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		searchTitle.getActionMap().put("enter", new AbstractAction(){
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				searchButton.doClick();
			}
		});
		searchAuthor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		searchAuthor.getActionMap().put("enter", new AbstractAction(){
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				searchButton.doClick();
			}
		});

		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if(MAC){
			MacQuit macIsAnnoying = new MacQuit(new Callable<Boolean>(){

				public Boolean call() throws Exception {
					return nagForSave();
				}
			});
			macIsAnnoying.setAboutMenu(new Callable<Void>(){

				public Void call() throws Exception {
					showAboutMenu();
					return null;
				}
			});
		}
		frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent ev){
				if(nagForSave())
					frame.dispose();
			}
		});
		
		URL imgURL = getClass().getResource("icon.png");
		if(imgURL != null){
			ImageIcon icon = new ImageIcon(imgURL);
			frame.setIconImage(icon.getImage());
		}
		createMenuBar();
	}


	public void actionPerformed(ActionEvent action){
		if(action.getSource() == newMenu){
			if(!nagForSave())
				return;
			local.flush();
			edit = false;
			frame.setTitle("Untitled");
			openFile = null;
		}
		else if(action.getSource() == openMenu){
			if(!nagForSave())
				return;
			JFileChooser fc = new JFileChooser(BibtexPrefs.getLastDir());
			fc.setFileFilter(new FileNameExtensionFilter("Bibtex Files", "bib"));
			if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
				openFile(fc.getSelectedFile());
		}
		else if (action.getSource() == saveMenu || action.getSource() == saveAsMenu){
			if(openFile == null || action.getSource() == saveAsMenu){
				JFileChooser fc = new JFileChooser(BibtexPrefs.getLastDir());
				fc.setFileFilter(new FileNameExtensionFilter("Bibtex Files", "bib"));
				if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
					saveFile(fc.getSelectedFile());
				return;
			}
			saveFile(openFile);
		}
		else if (action.getSource() == searchButton){
			Document doc;
			try {
				String url = "http://www.ams.org/mathscinet/search/publications.html?"
						+ "pg4=AUCN&s4="
						+ URLEncoder.encode(searchAuthor.getText(),"UTF-8")
						+ "&pg5=TI&s5="
						+ URLEncoder.encode(searchTitle.getText(), "UTF-8")
						+ "&fmt=bibtex&extend=1";
				doc = Jsoup.connect(url).get();
			} catch (IOException e) {
				e.printStackTrace();
				errorDialog(e.toString());
				return;
			}
			remote.addCitations(Jsoup.parse(doc.select("pre").html()).text());
		}
		else if(action.getSource() == addButton){
			int[] rows = remote.table.getSelectedRows();
			for(int i : rows)
				local.add(remote.citeAt(i));
			local.resetTable();
		}
		else{
			Object obj = action.getSource();
			for(JMenuItem jmi : recentMenuItems){
				if(obj == jmi){
					if(!nagForSave())
						return;
					File file = new File(jmi.getText());
					if(file.exists())
						openFile(file);
					else
						errorDialog("The file " + jmi.getText() + " does not exist!");
					return;
				}
			}
		}
	}
	private void saveFile(File file){
		try {
			local.saveFile(file);
			openFile = file;
			populateRecentMenu();
			frame.setTitle(openFile.getName());
			edit = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void openFile(File file){
		try {
			local.openFile(file);
			openFile = file;
			frame.setTitle(file.getName());
			edit = false;
			BibtexPrefs.addOpenedFile(file);
			populateRecentMenu();
		} catch (IOException e) {
			e.printStackTrace();
			errorDialog(e.toString());
		} catch (IllegalArgumentException e){
			errorDialog("The bibtex file " +file.getPath()+ " appears to be invalid.\n"
					+ "Parsing error occured because you have the following in your file:\n"
					+e.getMessage());
		}
	}
	
	private void populateRecentMenu(){
		recentMenu.removeAll();
		String[] files = BibtexPrefs.getOpenedFiles();
		recentMenuItems = new JMenuItem[files.length];
		for(int i = 0; i < files.length; i++){
			recentMenuItems[i] = new JMenuItem(files[i]);
			recentMenuItems[i].addActionListener(this);
			recentMenu.add(recentMenuItems[i]);
		}
	}

	private void createMenuBar(){
		menu = new JMenuBar();
		int ctrl;
		if(MAC)
			ctrl = ActionEvent.META_MASK;
		else
			ctrl = ActionEvent.CTRL_MASK;

		JMenu fileMenu = new JMenu("File");
		newMenu = new JMenuItem("New");
		newMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ctrl));
		newMenu.addActionListener(this);
		openMenu = new JMenuItem("Open");
		openMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ctrl));
		openMenu.addActionListener(this);
		recentMenu = new JMenu("Open Recent");
		populateRecentMenu();
		saveMenu = new JMenuItem("Save");
		saveMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl));
		saveMenu.addActionListener(this);
		saveAsMenu = new JMenuItem("Save As...");
		saveAsMenu.addActionListener(this);
		fileMenu.add(newMenu);
		fileMenu.add(openMenu);
		fileMenu.add(recentMenu);
		fileMenu.add(saveMenu);
		fileMenu.add(saveAsMenu);

		if(!MAC){
			JMenu helpMenu = new JMenu("Help");
			JMenuItem aboutMenuI = new JMenuItem("About");

			aboutMenuI.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					showAboutMenu();
				}		
			});
			helpMenu.add(aboutMenuI);
			menu.add(helpMenu);
		}

		menu.add(fileMenu);
	}
	
	private void showAboutMenu(){
		JOptionPane.showMessageDialog(frame,
				"Bibtex Import\n"
						+ "Copyright(c) Bati Sengul 2013\n"
						+ "Version: " + VERSION + "\n"
						+ "Wesbite: " + WEBSITE
						,"About",JOptionPane.INFORMATION_MESSAGE);
	}

	private void errorDialog(String error){
		JOptionPane.showMessageDialog(frame,error,"Error",JOptionPane.ERROR_MESSAGE);
	}

	private boolean nagForSave(){
		if(edit){
			String name = frame.getTitle();
			if(name.length() > 1)
				name = name.substring(1, name.length());
			int res = JOptionPane.showConfirmDialog(frame, "Save changes to "+name+"?");
			switch(res){
			case JOptionPane.CANCEL_OPTION:
				return false;
			case JOptionPane.YES_OPTION:
				if(openFile == null){
					JFileChooser fc = new JFileChooser(BibtexPrefs.getLastDir());
					fc.setFileFilter(new FileNameExtensionFilter("Bibtex Files", "bib"));
					if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
						saveFile(fc.getSelectedFile());
						return true;
					}
					else 
						return false;
				}
				try {
					local.saveFile(openFile);
					frame.setTitle(openFile.getName());
					edit = false;
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			case JOptionPane.NO_OPTION:
				return true;
			}
		}
		return true;
	}


	private static void createGUI(){
		frame = new JFrame("Untitled");
		
		BibtexImport bib = new BibtexImport();
		bib.setOpaque(true);
		
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setContentPane(bib);
		frame.setJMenuBar(bib.menu);
		frame.pack();
		frame.setVisible(true);

	}

	public static void main(String[] args) {
		
		System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Bibtex Import");
        System.setProperty("apple.awt.brushMetalLook","true");
		System.setProperty("apple.awt.antialiasing","on");
		System.setProperty("apple.awt.textantialiasing","on");
		
        
        try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		javax.swing.SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				createGUI();
			}
		});  
	}

}
