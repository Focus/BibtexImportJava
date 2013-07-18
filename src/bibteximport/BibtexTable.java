package bibteximport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
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

	public BibtexTable(String[] labels){
		this.labels = labels;
		citations = new Vector<Citation>();
		model = new DefaultTableModel(labels,1);
		model.addTableModelListener(this);
		table = new JTable(model);
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
		
		case TableModelEvent.DELETE:
			//for(int i = tme.getFirstRow(); i < tme.getLastRow(); i++)
				//citations.remove(i);
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
}
