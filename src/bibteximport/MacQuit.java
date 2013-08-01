package bibteximport;

import java.util.concurrent.Callable;

import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;


@SuppressWarnings("restriction")
public class MacQuit extends Application{
	@SuppressWarnings("deprecation")
	public MacQuit(final Callable<Boolean> func){
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

}
