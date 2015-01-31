package com.bsb.hike.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bsb.hike.HikeMessengerApp;
import com.bsb.hike.HikePubSub;
import com.bsb.hike.HikePubSub.Listener;
import com.bsb.hike.NUXConstants;
import com.bsb.hike.R;
import com.bsb.hike.models.NUXTaskDetails;
import com.bsb.hike.models.NuxInviteFriends;
import com.bsb.hike.ui.utils.RecyclingImageView;
import com.bsb.hike.utils.HikeAppStateBaseFragmentActivity;
import com.bsb.hike.utils.Logger;
import com.bsb.hike.utils.NUXManager;
import com.bsb.hike.utils.Utils;

public class NUXInviteActivity extends HikeAppStateBaseFragmentActivity implements OnClickListener,Listener
{

	private Button butInviteFriends, butSkip;

	private TextView tvRewardMain, tvRewardSubText;

	private ImageView imgvInviteFrd;

	private String HikePubListener=HikePubSub.NUX_DESTROY_ACTIVITY;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		if (Utils.requireAuth(this))
		{
			return;
		}
		Logger.d("footer","onCreate");
		getSupportActionBar().hide();

		HikeMessengerApp app = (HikeMessengerApp) getApplication();

		app.connectToService();

		setContentView(R.layout.nux_invite_friends);

		bindViews();

		bindListeners();

		processViewElemets();
		
		Logger.d("footer","onCreateFinished");
		
		HikeMessengerApp.getPubSub().addListener(HikePubListener, this);
		
		/**
		 * Cancelling all notifications ...
		 */
		HikeMessengerApp.getPubSub().publish(HikePubSub.CANCEL_ALL_NOTIFICATIONS, null);

	}

	@Override
	protected void onNewIntent(Intent intent)
	{
	Logger.d("footer","onNewIntent");
		super.onNewIntent(intent);
	}
	
	private void bindViews()
	{
		butInviteFriends = (Button) findViewById(R.id.but_inviteFrnds);

		butSkip = (Button) findViewById(R.id.but_skip);

		tvRewardMain = (TextView) findViewById(R.id.tv_reward_main);

		tvRewardSubText = (TextView) findViewById(R.id.tv_reward_subText);

		imgvInviteFrd = (RecyclingImageView) findViewById(R.id.imgv_invitefrd);
	}

	private void bindListeners()
	{
		butInviteFriends.setOnClickListener(this);

		butSkip.setOnClickListener(this);

	}

	private void processViewElemets()
	{
		NUXManager mmNuxManager = NUXManager.getInstance();
		NuxInviteFriends mmInviteFriends = mmNuxManager.getNuxInviteFriendsPojo();
	NUXTaskDetails mmTaskDetails=mmNuxManager.getNuxTaskDetailsPojo();
		if (mmInviteFriends != null)
		{
			if (!TextUtils.isEmpty(mmInviteFriends.getButText()))
			{
				butInviteFriends.setText(mmInviteFriends.getButText());
			}

			if (!TextUtils.isEmpty(mmInviteFriends.getRewardTitle()))
			{
				String rewardTitle = String.format(mmInviteFriends.getRewardTitle(), mmTaskDetails.getMin(), mmTaskDetails.getIncentiveAmount());
				tvRewardMain.setText(rewardTitle);
			}

			if (!TextUtils.isEmpty(mmInviteFriends.getRewardSubText()))
			{
				String rewardSubText = String.format(mmInviteFriends.getRewardSubText(), mmTaskDetails.getMin(), mmTaskDetails.getIncentiveAmount());
				tvRewardSubText.setText(rewardSubText);

			}
			if (mmInviteFriends.getImageBitmap() != null)
			{
				imgvInviteFrd.setImageBitmap(mmInviteFriends.getImageBitmap());
			}
			if (!mmInviteFriends.showSkipButton())
			{
				butSkip.setVisibility(View.INVISIBLE);
			}
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.but_skip:
		//	IntentManager.openHomeActivity(this);
			NUXManager.getInstance().setCurrentState(NUXConstants.NUX_SKIPPED);
			startActivity(Utils.getHomeActivityIntent(this));
			finish();
			break;

		case R.id.but_inviteFrnds:

			if (!(NUXManager.getInstance().getCurrentState() == NUXConstants.NUX_KILLED))
			{
				NUXManager.getInstance().startNuxSelector(this);
			}
			else
			{
				startActivity(Utils.getHomeActivityIntent(this));
				finish();
			}
			break;
		}

	}

	@Override
	public void onEventReceived(String type, Object object)
	{
		super.onEventReceived(type, object);
		if (type == HikePubListener)
		{
			this.finish();
		}

	}
}
