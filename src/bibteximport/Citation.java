package bibteximport;


import java.util.Vector;

public class Citation {
	public String name, type;
	public Vector<String[]> properties;

	public Citation(){
		name = new String();
		type = new String();
		properties = new Vector<String[]>();
	}

	public Citation(String bib){
		this.properties = new Vector<String[]>();
		this.parse(bib);
	}

	public String getString(String name){
		if(name.compareToIgnoreCase("name") == 0)
			return this.name;
		else if (name.compareToIgnoreCase("type") == 0)
			return this.type;
		for(String[] prop : properties){
			if(name.compareToIgnoreCase(prop[0]) == 0)
				return prop[1];
		}
		return null;
	}
	
	public void replaceString(String name, String value){
		if(name.compareToIgnoreCase("name") == 0){
			this.name = value;
			return;
		}
		else if (name.compareToIgnoreCase("type") == 0){
			this.type = value;
			return;
		}
		for(String[] prop : properties){
			if(name.compareToIgnoreCase(prop[0]) == 0)
				prop[1] = value;
		}
	}
	
	public String asString(){
		String ret ="@" + this.type + "{" + this.name + ",\n";
		for(String[] prop : properties)
			ret += "    " + prop[0] + " = {" + prop[1] + "},\n";
		ret = ret.substring(0, ret.length() - 2);
		ret += "\n}\n\n";
		return ret;
	}
	/**
	 * Parses one bibtex entry.
	 * @param bib
	 */

	public void parse(String bib){
		if(bib.lastIndexOf('}') == -1)
			return;
		bib = bib.substring(0,bib.lastIndexOf('}')-1);

		int index, index2;
		index = bib.indexOf('{');
		index2 = bib.indexOf(',');
		if(index != -1 && index2 > index && index2 < bib.length()-1 ){
			type = bib.substring(0,index);
			name = bib.substring(index + 1, index2);
		}
		else
			return;
		bib = bib.substring(index2 + 1);

		while(bib.indexOf('{') != -1){
			String[] prop = new String[2];
			index = bib.indexOf('=');
			if(index == -1)
				break;
			prop[0] = bib.substring(0,index).trim();

			index = bib.indexOf('{');
			if(index == -1)
				break;
			bib = bib.substring(index + 1);

			int balance = 1;
			int it = 0;
			while(balance > 0){
				if(bib.charAt(it) == '{')
					balance++;
				else if(bib.charAt(it) == '}')
					balance--;
				if(it == bib.length()-1)
					return;
				it++;
			}
			prop[1] = bib.substring(0,it - 1);
			properties.add(prop);
			bib = bib.substring(it);
			index = bib.indexOf(',');
			if(index == -1)
				break;
			bib = bib.substring(index + 1);
		}
	}
}
