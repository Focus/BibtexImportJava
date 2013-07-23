package bibteximport;

import java.io.File;
import java.util.prefs.Preferences;

public class BibtexPrefs {
	public static int noOfStoredFiles = 4;
	private static Preferences prefs = Preferences.userRoot().node("/bibteximport");
	
	public static void addOpenedFile(File file){
		addOpenedFile(file.getPath());
	}
	
	public static void addOpenedFile(String file){
		String[] files = getOpenedFiles();
		String store = "";
		for(int i = 0; i < Math.min(files.length,noOfStoredFiles); i++){
			if(files[i].compareTo(file) != 0)
				store = store + File.pathSeparator + files[i];
		}
		store = file + store;
		prefs.put("openedFiles", store);
	}
	
	public static String[] getOpenedFiles(){
		String files = prefs.get("openedFiles", null);
		if(files == null)
			return new String[0];
		else
			return files.split(File.pathSeparator);
	}
	
	public static String getFileName(String file){
		int i = file.lastIndexOf(File.separator);
		if(i >= 0 && i < file.length() -1)
			return file.substring(i+1);
		return file;
	}
}
