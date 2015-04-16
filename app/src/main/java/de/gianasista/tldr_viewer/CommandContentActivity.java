package de.gianasista.tldr_viewer;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import de.gianasista.tldr_viewer.backend.TldrApiClient;
import de.gianasista.tldr_viewer.util.CommandContentDelegate;
import de.gianasista.tldr_viewer.util.TldrContentProvider;

/**
 * @author vera
 *
 */
public class CommandContentActivity extends ActionBarActivity implements CommandContentDelegate, Handler.Callback 
{
	private TextView textView;
	private TldrContentProvider contentProvider;
	
	private Timer loadingTimer;
	
	private static final String[] loadingText = { "Loading", "Loading .", "Loading ..", "Loading ..."};
	private int loadingPos = 0;
	
	private Handler loadingUpdateHandler;

    private String commandName;
    private String[] platforms;
    private int platformIndex;

    private static final int ID_PLATFORM_COMMON = 0;
    private static final int ID_PLATFORM_LINUX = 1;
    private static final int ID_PLATFORM_OSX = 2;
    private static final int ID_PLATFORM_SUNOS = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_command_content);
		textView = (TextView)findViewById(R.id.detail_content);

		commandName = getIntent().getStringExtra(CommandListActivity.COMMAND_NAME);
        platforms = getIntent().getStringArrayExtra(CommandListActivity.PLATFORMS);
        platformIndex = 0;

		loadingUpdateHandler = new Handler(this);
		loadingTimer = new Timer();
		TimerTask loadingTask = new TimerTask() {
			
			@Override
			public void run() 
			{
				loadingUpdateHandler.sendEmptyMessage(0);
			}
			
		};
		
		loadingTimer.schedule(loadingTask, 0, 500);

        updatePageContent();
		textView.setMovementMethod(new ScrollingMovementMethod());
	}

    private void updatePageContent() {
        getSupportActionBar().setTitle(commandName+" ("+platforms[platformIndex]+")");
        TldrApiClient.getPageContent(commandName, platforms[platformIndex], this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(platforms.length > 1) {
            for(String platform : platforms) {
                if("common".equals(platform))
                    menu.add(0, ID_PLATFORM_COMMON, 0, "common");
                else if("linux".equals(platform))
                    menu.add(0, ID_PLATFORM_LINUX, 0, "linux");
                else if("osx".equals(platform))
                    menu.add(0, ID_PLATFORM_OSX, 0, "osx");
                else if("sunos".equals(platform))
                    menu.add(0, ID_PLATFORM_SUNOS, 0, "sunos");
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case ID_PLATFORM_COMMON: platformIndex = getPlatformIndex("common"); break;
            case ID_PLATFORM_LINUX: platformIndex = getPlatformIndex("linux"); break;
            case ID_PLATFORM_OSX: platformIndex = getPlatformIndex("osx"); break;
            case ID_PLATFORM_SUNOS: platformIndex = getPlatformIndex("sunos"); break;
            default: platformIndex = 0; break;
        }

        updatePageContent();
        return super.onOptionsItemSelected(item);
    }

    private int getPlatformIndex(String platform) {
        for(int i=0; i<platforms.length; i++)
            if(platform.equals(platforms[i]))
                return i;

        return 0;
    }

	@Override
	public void receiveCommandContent(String content) 
	{
		if(loadingTimer != null)
			loadingTimer.cancel();
		
		textView.setText(Html.fromHtml(content));
	}

	@Override
	public boolean handleMessage(Message msg) {
		textView.setText(loadingText[loadingPos]);
		loadingPos = (loadingPos+1) % 4;
		return true;
	}

}