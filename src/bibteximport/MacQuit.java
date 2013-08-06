package bibteximport;

import java.net.URL;
import java.util.concurrent.Callable;

import javax.swing.ImageIcon;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;



public class MacQuit extends Application{
	@SuppressWarnings("deprecation")
	public MacQuit(final Callable<Boolean> func){
		URL imgURL = getClass().getResource("icon.png");
		if(imgURL != null){
			ImageIcon icon = new ImageIcon(imgURL);
			this.setDockIconImage(icon.getImage());
		}
		this.setQuitHandler(new QuitHandler(){
			public void handleQuitRequestWith(QuitEvent qe, QuitResponse qr) {
				try {
					if(func.call())
						qr.performQuit();
					else
						qr.cancelQuit();
				} catch (Exception e) {
					e.printStackTrace();
					qr.performQuit();
				}
			}
		});
	}
	
	public void setAboutMenu(final Callable<Void> func){
		this.setAboutHandler(new AboutHandler(){

			public void handleAbout(AboutEvent ae) {
				try {
					func.call();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
	}

}
