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

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
/**
 * Implements the tables for searching and displaying the local BibTex file.
 * @author Bati Sengul
 */
public class BibtexTable implements TableModelListener {

	private String[] labels;
	public Vector<Citation> citations;
	public DefaultTableModel model;
	public JTable table;
	public int order;
	/**
	 * Creates a new table
	 * @param labelsIn the labels that sit at the top of the table: e.g. {@code ["Key", "Type", "Title", "Author", "Year"]}
	 */

	public BibtexTable(String[] labelsIn){
		this.labels = labelsIn;
		citations = new Vector<Citation>();
		model = new DefaultTableModel(labels,1);
		model.addTableModelListener(this);
		table = new JTable(model);
		table.getTableHeader().addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent ev){
				if(ev.getModifiers() != InputEvent.BUTTON1_MASK)
					return;
				int col = table.columnAtPoint(ev.getPoint());
				if(col >= 0 && col < labels.length)
					orderByColumn(labels[col]);
			}
		});
		DefaultCellEditor dce = (DefaultCellEditor) table.getDefaultEditor(Object.class);
		dce.getComponent().addMouseListener(new RightClick());
		this.order = 1;
	}
	/**
	 * Draws the table from scratch using the citations.
	 */
	public void resetTable(){
		model.setRowCount(0);
		for(Citation cite : citations){
			String[] entry = new String[labels.length];
			for(int i = 0; i < labels.length; i++)
				entry[i] = cite.getString(labels[i]);
			model.addRow(entry);
		}
	}
	/**
	 * Resets the labels on top of the table
	 * @param labelsIn an array of new labels
	 */
	public void resetLabels(String[] labelsIn){
		this.labels = labelsIn;
		model.setColumnIdentifiers(labels);
		model = new DefaultTableModel(labels,1);
		this.resetTable();
	}
	
	/**
	 * Detects if the user has edited something in one of the tables.
	 */
	public void tableChanged(TableModelEvent tme) {
		if(tme.getType() == TableModelEvent.UPDATE){
			int row = tme.getFirstRow();
			int column = tme.getColumn();
			String entry = (String) model.getValueAt(row, column);
			if(row < citations.size())
				citations.get(row).replaceString(labels[column], entry);
			resetTable();
		}
			
	}
	/**
	 * Opens a given .bib file and displays the contents on the table, disregarding any previous content.
	 * @param file the file to be read
	 * @throws IOException if the file cannot be read
	 * @throws IllegalArgumentException if the BibTex syntax within the file is wrong
	 */
	public void openFile(File file) throws IOException, IllegalArgumentException{
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();
		while(line != null){
			sb.append(line);
			line = br.readLine();
		}
		br.close();
		
		this.addCitations(sb.toString());
	}
	/**
	 * Displays a group of BibTex citations, disregarding any previous content.
	 * @param input multiple BibTex citations
	 * @throws IllegalArgumentException if one of the BibTex entries has incorrect syntax
	 */
	public void addCitations(String input) throws IllegalArgumentException{
		String[] bibStrings = input.toString().split("@");
		if(bibStrings.length <= 0)
			return;
		citations.clear();
		for(int i = 1; i < bibStrings.length; i++){
			if(bibStrings[i] != null && bibStrings[i].length() > 0)
				citations.add(new Citation(bibStrings[i]));
		}
		this.resetTable();
	}
	/**
	 * Saves the contents of the table as a .bib file
	 * @param file saving destination
	 * @throws IOException if the file cannot be saved
	 */
	public void saveFile(File file) throws IOException{
		PrintWriter pw = new PrintWriter(file);
		for(Citation cite : citations)
			pw.write(cite.asString());
		pw.close();
	}
	/**
	 * Adds a Citation to the table. The table is not redrawn after this operation!
	 * @param cite a citation to be added to the table
	 */
	public void add(Citation cite){
		citations.add(cite);
	}
	
	/**
	 * Gets the Citation at a given position.
	 * @param i the row of the desired citation
	 * @return Citation sitting at row {@code i}
	 */
	public Citation citeAt(int i){
		if(i < citations.size())
			return citations.get(i);
		else
			return new Citation();
	}
	/**
	 * Removes the currently selected rows on the table.
	 */
	public void removeSelected(){
		int[] ind = this.table.getSelectedRows();
		Arrays.sort(ind);
		int discount = 0;
		for(int i : ind){
			citations.remove(i-discount);
			discount++;
		}
		this.resetTable();
	}
	/**
	 * Start everything fresh.
	 */
	public void flush(){
		citations.clear();
		this.resetTable();
	}
	/**
	 * Order the table by column
	 * @param field a field of the Citation to order by
	 */
	public void orderByColumn(final String field){
		Comparator<Citation> comp = new Comparator<Citation>(){
			public int compare(Citation cite0, Citation cite1) {
				String ent0 = cite0.getString(field);
				String ent1 = cite1.getString(field);
				if(ent1 == null)
					return 1*order;
				if(ent0 == null)
					return -1*order;
				ent0 = ent0.toLowerCase();
				ent1 = ent1.toLowerCase();
				return ent0.compareTo(ent1)*order;
			}
		};
		Collections.sort(citations, comp);
		this.resetTable();
		order = -order;
	}
}