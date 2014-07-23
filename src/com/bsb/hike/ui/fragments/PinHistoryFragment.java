package com.bsb.hike.ui.fragments;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ImageView.ScaleType;

import com.actionbarsherlock.app.SherlockListFragment;
import com.bsb.hike.HikeConstants;
import com.bsb.hike.R;
import com.bsb.hike.BitmapModule.BitmapUtils;
import com.bsb.hike.BitmapModule.HikeBitmapFactory;
import com.bsb.hike.adapters.PinHistoryAdapter;
import com.bsb.hike.adapters.PinHistoryAdapter.PinHistoryItemsListener;
import com.bsb.hike.db.HikeConversationsDatabase;
import com.bsb.hike.models.ConvMessage;
import com.bsb.hike.models.Conversation;
import com.bsb.hike.utils.ChatTheme;
import com.bsb.hike.utils.Logger;
import com.bsb.hike.utils.Utils;

public class PinHistoryFragment extends SherlockListFragment implements PinHistoryItemsListener
{
	private PinHistoryAdapter PHadapter;
			
	private List<ConvMessage> textPins;
	
	private String msisdn;
	
	private ChatTheme chatTheme;
		
	private HikeConversationsDatabase mDb;

	private Conversation mConversation;

	private long convId;
	
	public PinHistoryFragment(String userMSISDN, long convId2)
	{
		this.msisdn = userMSISDN;
		this.convId = convId2;
	}
		
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View parent = inflater.inflate(R.layout.sticky_pins, null);
		
		ListView pinsList = (ListView) parent.findViewById(android.R.id.list);
		
		mDb = HikeConversationsDatabase.getInstance();
		
		this.mConversation = mDb.getConversation(msisdn, HikeConstants.MAX_PINS_TO_LOAD_INITIALLY);
		
		this.textPins = mDb.getAllPinMessage(0, HikeConstants.MAX_PINS_TO_LOAD_INITIALLY, msisdn,convId);

		chatTheme = mDb.getChatThemeForMsisdn(msisdn);
		
		pinsList.setEmptyView(parent.findViewById(android.R.id.empty));
		
		return parent;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) 
	{
		super.onViewCreated(view, savedInstanceState);
						
		if (chatTheme != ChatTheme.DEFAULT)
		{
			backgroundImage.setScaleType(chatTheme.isTiled() ? ScaleType.FIT_XY : ScaleType.CENTER_CROP);
			backgroundImage.setImageDrawable(getChatTheme(chatTheme));
		}
		else
		{
			backgroundImage.setImageResource(chatTheme.bgResId());
		}
		
		// reset unread pin count to 0
		try 
		{
			mConversation.getMetaData().setUnreadCount(HikeConstants.MESSAGE_TYPE.TEXT_PIN, 0);
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		
		if (PHadapter != null)
		{
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		PHadapter = new PinHistoryAdapter(getActivity(), textPins, msisdn, convId, mConversation, this);
		
		setListAdapter(PHadapter);		
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
	}

	public Drawable getChatTheme(ChatTheme chatTheme)
	{
		/*
		 * for xhdpi and above we should not scale down the chat theme nodpi asset for hdpi and below to save memory we should scale it down
		 */
		int inSampleSize = 1;
		if(!chatTheme.isTiled() && Utils.densityMultiplier < 2)
		{
			inSampleSize = 2;
		}
		
		Bitmap b = HikeBitmapFactory.decodeSampledBitmapFromResource(getResources(), chatTheme.bgResId(), inSampleSize);

		BitmapDrawable bd = HikeBitmapFactory.getBitmapDrawable(getResources(), b);

		Logger.d(getClass().getSimpleName(), "chat themes bitmap size= " + BitmapUtils.getBitmapSize(b));

		if (bd != null && chatTheme.isTiled())
		{
			bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		}

		return bd;
	}

	@Override
	public void onLastItemRequested() 
	{		
		this.textPins = mDb.getAllPinMessage(PHadapter.getCount(), HikeConstants.MAX_OLDER_PINS_TO_LOAD_EACH_TIME, msisdn, convId);
		
		this.PHadapter.appendPinstoView(textPins);
	}
}
