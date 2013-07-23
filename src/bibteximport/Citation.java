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

	public void parse (String parseIn) throws IllegalArgumentException{
		String bib = parseIn;
		if(bib.lastIndexOf('}') == -1)
			throw new IllegalArgumentException(parseIn);
		int balance, it;
		balance = 1;
		it = bib.indexOf('{') + 1;
		if(it == -1)
			throw new IllegalArgumentException(parseIn);
		while(balance > 0){
			if(it == bib.length())
				throw new IllegalArgumentException(parseIn);
			if(bib.charAt(it) == '{')
				balance++;
			else if(bib.charAt(it) == '}')
				balance--;
			it++;
		}
		bib = bib.substring(0,it-1);

		int index, index2;
		index = bib.indexOf('{');
		index2 = bib.indexOf(',');
		if(index != -1 && index2 > index && index2 < bib.length()-1 ){
			type = bib.substring(0,index);
			name = bib.substring(index + 1, index2);
		}
		else
			throw new IllegalArgumentException(parseIn);
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

			balance = 1;
			it = 0;
			while(balance > 0){
				if(it == bib.length())
					throw new IllegalArgumentException(parseIn);
				if(bib.charAt(it) == '{')
					balance++;
				else if(bib.charAt(it) == '}')
					balance--;
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
