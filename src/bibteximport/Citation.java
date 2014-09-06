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
/**
 * A class for storing a single BibTex citation
 * @author Bati Sengul
 */
public class Citation {
	/**
	 * The key of the citation: e.g if the citation is {@code @article{test, title={Something}}} then the key is {@code test}.
	 */
	public String key;
	/**
	 * The type of the citation: e.g if the citation is {@code @article{test, title={Something}}} then the type is {@code article}.
	 */
	public String type;
	/**
	 * The rest of the BibTex items go here. For example if the citation is {@code @article{test, title={Something}, author={Someone}}} 
	 * then {@code properties} will contain two {@code String[2]} items. One of which will be {@code ["title","Something"]} and the other 
	 * {@code ["author, "Someone"]}.
	 */
	public Vector<String[]> properties;

	public Citation(){
		key = new String();
		type = new String();
		properties = new Vector<String[]>();
	}

	/**
	 * Constructor using a BibTex citation.
	 * @param bib a BibTex citation, e.g.  {@code @article{test, title={Something}, author={Someone}}}
	 */
	public Citation(String bib){
		this.properties = new Vector<String[]>();
		this.parse(bib);
	}
	/**
	 * Gets the string corresponding to the field: e.g. if the citation is  {@code @article{test, title={Something}, author={Someone}}}, 
	 * then {@code getString("author")} will return {@code "Someone"}.
	 * @param field the name of the field
	 * @return Value of the field corresponding to the name
	 * @see Citation#replaceString
	 */

	public String getString(String field){
		if(field.compareToIgnoreCase("key") == 0)
			return this.key;
		else if (field.compareToIgnoreCase("type") == 0)
			return this.type;
		for(String[] prop : properties){
			if(field.compareToIgnoreCase(prop[0]) == 0)
				return prop[1];
		}
		return null;
	}
	/**
	 * Replaces the string corresponding to the field: e.g. if the citation is  {@code @article{test, title={Something}, author={Someone}}}, 
	 * then {@code getString("author", "Someone else")} will change the citation to {@code @article{test, title={Something}, author={Someone else}}}.
	 * @param field the name of the field
	 * @param value the new value
	 * @see Citation#getString
	 */	
	public void replaceString(String field, String value){
		if(field.compareToIgnoreCase("name") == 0){
			this.key = value;
			return;
		}
		else if (field.compareToIgnoreCase("type") == 0){
			this.type = value;
			return;
		}
		for(String[] prop : properties){
			if(field.compareToIgnoreCase(prop[0]) == 0)
				prop[1] = value;
		}
	}
	/**
	 * Formats the Citation as a string.
	 * @return The citation as a string
	 */
	
	public String asString(){
		String ret ="@" + this.type + "{" + this.key + ",\n";
		for(String[] prop : properties)
			ret += "    " + prop[0] + " = {" + prop[1] + "},\n";
		ret = ret.substring(0, ret.length() - 2);
		ret += "\n}\n\n";
		return ret;
	}
	/**
	 * Removes end line characters and extra spaces.
	 * @param line a string to be formated
	 * @return 	   The formated string
	 */
	private String formatString(String line){
		String ret = line.replaceAll("\\r\\n|\\n|\\r", " ");
		while(ret.indexOf("  ") != -1)
			ret = ret.replaceAll("  ", " ");
		return ret;
	}
	/**
	 * Parses a BibTex entry from a string. The entry does not need to start with an @.
	 * @param parseIn a string which is the BibTex entry, e.g.  {@code article{test, title={Something}, author={Someone}}}
	 * @throws IllegalArgumentException if the input is not a valid BibTex enrty
	 */

	public void parse (String parseIn) throws IllegalArgumentException{
		String bib = formatString(parseIn);
		if(bib.lastIndexOf('}') == -1)
			throw new IllegalArgumentException(parseIn);
		int balance, it;
		balance = 1;
		it = bib.indexOf('{') + 1;
		if(it == -1)
			throw new IllegalArgumentException(parseIn);
		/* Find the content of the BibTex entry: article{ We want this stuff }
		 * We also have to check that the number of { and the number of } are the same. 
		 */
		while(balance > 0){
			if(it == bib.length())
				throw new IllegalArgumentException(parseIn);
			if(bib.charAt(it) == '{' && bib.charAt(it-1) != '\\')
				balance++;
			else if(bib.charAt(it) == '}' && bib.charAt(it-1) != '\\')
				balance--;
			it++;
		}
		bib = bib.substring(0,it-1);
		int index, index2;
		index = bib.indexOf('{');
		index2 = bib.indexOf(',');
		if(index != -1 && index2 > index && index2 < bib.length()-1 ){
			type = bib.substring(0,index);
			key = bib.substring(index + 1, index2);
		}
		else
			throw new IllegalArgumentException(parseIn);
		
		bib = bib.substring(index2 + 1);
		/*
		 * If originally parseIn is article{key, title = {Hello}, author = {me}}   
		 * bib is  title = {Hello}, author = {me}
		 */
		char delim;
		while(bib.indexOf('{') != -1){
			String[] prop = new String[2];
			index = bib.indexOf('=');
			if(index == -1)
				break;
			prop[0] = bib.substring(0,index).trim();
			
			if(bib.indexOf('"') != -1 && (bib.indexOf('{') == -1 || bib.indexOf('{') > bib.indexOf('"')) ){
				index = bib.indexOf('"');
				delim = '"';
			}
			else if (bib.indexOf('{') != -1){
				index = bib.indexOf('{');
				delim= '{';
			}
			else
				break;
			
			bib = bib.substring(index + 1);
			balance = 1;
			it = 0;
			if(delim == '"'){
				while(true){
					if(it == bib.length())
						throw new IllegalArgumentException(parseIn);
					if(bib.charAt(it) == '"' && (it == 0 || bib.charAt(it-1) != '\\') )
						break;
					it++;
				}
			}
			else{
				while(balance > 0){
					if(it == bib.length())
						throw new IllegalArgumentException(parseIn);
					if(bib.charAt(it) == '{' && (it == 0 || bib.charAt(it-1) != '\\') )
						balance++;
					else if(bib.charAt(it) == '}' && (it == 0 || bib.charAt(it-1) != '\\') )
						balance--;
					it++;
				}
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
