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

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

public class BibtexTable implements TableModelListener {

	private String[] labels;
	private Vector<Citation> citations;
	public DefaultTableModel model;
	public JTable table;
	public int order;

	public BibtexTable(String[] labelsIn){
		this.labels = labelsIn;
		citations = new Vector<Citation>();
		model = new DefaultTableModel(labels,1);
		model.addTableModelListener(this);
		table = new JTable(model);
		table.getTableHeader().addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent ev){
				int col = table.columnAtPoint(ev.getPoint());
				if(col >= 0 && col < labels.length)
					orderByColumn(labels[col]);
			}
		});
		this.order = 1;
	}
	


	public void resetTable(){
		model.setRowCount(0);
		for(Citation cite : citations){
			String[] entry = new String[labels.length];
			for(int i = 0; i < labels.length; i++)
				entry[i] = cite.getString(labels[i]);
			model.addRow(entry);
		}
	}
	@Override
	public void tableChanged(TableModelEvent tme) {
		switch(tme.getType()){
		case TableModelEvent.UPDATE:
			int row = tme.getFirstRow();
			int column = tme.getColumn();
			String entry = (String) model.getValueAt(row, column);
			if(row < citations.size())
				citations.get(row).replaceString(labels[column], entry);
			resetTable();
			break;
		}
			
	}
	
	public void openFile(File file) throws IOException{
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
	
	public void addCitations(String input){
		String[] bibStrings = input.toString().split("@");
		if(bibStrings.length <= 0)
			return;
		citations.clear();
		for(int i = 0; i < bibStrings.length; i++){
			if(!bibStrings[i].isEmpty() && bibStrings[i] != "")
				citations.add(new Citation(bibStrings[i]));
		}
		this.resetTable();
	}
	
	public void saveFile(File file) throws IOException{
		PrintWriter pw = new PrintWriter(file);
		for(Citation cite : citations)
			pw.write(cite.asString());
		pw.close();
	}
	
	public void add(Citation cite){
		citations.add(cite);
	}
	public Citation citeAt(int i){
		if(i < citations.size())
			return citations.get(i);
		else
			return new Citation();
	}
	
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
	
	public void flush(){
		citations.clear();
		this.resetTable();
	}
	
	public void orderByColumn(final String label){
		Comparator<Citation> comp = new Comparator<Citation>(){
			@Override
			public int compare(Citation cite0, Citation cite1) {
				String ent0 = cite0.getString(label);
				String ent1 = cite1.getString(label);
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
