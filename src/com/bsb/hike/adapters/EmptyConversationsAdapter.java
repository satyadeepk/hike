package com.bsb.hike.adapters;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.bsb.hike.HikeConstants;
import com.bsb.hike.HikeMessengerApp;
import com.bsb.hike.HikePubSub;
import com.bsb.hike.R;
import com.bsb.hike.models.ContactInfo;
import com.bsb.hike.models.EmptyConversationFtueCardItem;
import com.bsb.hike.models.EmptyConversationContactItem;
import com.bsb.hike.models.EmptyConversationItem;
import com.bsb.hike.smartImageLoader.IconLoader;
import com.bsb.hike.ui.ComposeChatActivity;
import com.bsb.hike.ui.HomeActivity;
import com.bsb.hike.ui.PeopleActivity;
import com.bsb.hike.utils.Utils;

public class EmptyConversationsAdapter extends ArrayAdapter<EmptyConversationItem>
{

	private Context context;

	private int mIconImageSize;

	private IconLoader iconLoader;

	private LayoutInflater inflater;
	
	private enum ViewType
	{
		CONTACTS_CARD, FTUE_CARD, SEPERATOR
	}

	private class ViewHolder
	{
		View parent;

		ViewGroup contactsContainer;

		TextView name;

		TextView mainInfo;

		TextView seeAll;

		ImageView cardImg;
	}

	public EmptyConversationsAdapter(Context context, int textViewResourceId, List<EmptyConversationItem> objects)
	{
		super(context, textViewResourceId, objects);

		this.context = context;
		mIconImageSize = context.getResources().getDimensionPixelSize(R.dimen.icon_picture_size);
		iconLoader = new IconLoader(context, mIconImageSize);
		iconLoader.setDefaultAvatarIfNoCustomIcon(true);
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public int getViewTypeCount()
	{
		return ViewType.values().length;
	};


	@Override
	public int getItemViewType(int position)
	{
		EmptyConversationItem item = getItem(position);
		if (item instanceof EmptyConversationContactItem)
		{
			return ViewType.CONTACTS_CARD.ordinal();
		}
		else if (item instanceof EmptyConversationFtueCardItem)
		{
			return ViewType.FTUE_CARD.ordinal();
		}
		return ViewType.SEPERATOR.ordinal();
	}

	@Override
	public View getView(int position, View v, ViewGroup parent)
	{
		ViewType viewType = ViewType.values()[getItemViewType(position)];
		ViewHolder viewHolder;
		if (v == null)
		{
			viewHolder = new ViewHolder();
			if (viewType == ViewType.CONTACTS_CARD)
			{
				EmptyConversationContactItem item = (EmptyConversationContactItem) getItem(position);

				v = inflater.inflate(R.layout.ftue_updates_item, parent, false);

				viewHolder.name = (TextView) v.findViewById(R.id.name);
				viewHolder.mainInfo = (TextView) v.findViewById(R.id.main_info);

				viewHolder.contactsContainer = (ViewGroup) v.findViewById(R.id.contacts_container);
				viewHolder.parent = v.findViewById(R.id.main_content);

				viewHolder.seeAll = (TextView) v.findViewById(R.id.see_all);

				viewHolder.contactsContainer.removeAllViews();
				int limit = HikeConstants.FTUE_CONTACT_CARD_LIMIT;
				for (int i = 0; i < item.getContactList().size(); i++)
				{
					View parentView = inflater.inflate(R.layout.ftue_recommended_list_item, parent, false);
					viewHolder.contactsContainer.addView(parentView);
					if (--limit == 0)
					{
						break;
					}
				}
			}
			else if (viewType == ViewType.FTUE_CARD)
			{
				v = inflater.inflate(R.layout.empty_conv_ftue_item, parent, false);
				viewHolder.name = (TextView) v.findViewById(R.id.card_txt_header);
				viewHolder.mainInfo = (TextView) v.findViewById(R.id.card_txt_msg);
				viewHolder.seeAll = (TextView) v.findViewById(R.id.card_action_txt);
				viewHolder.cardImg = (ImageView) v.findViewById(R.id.card_img);
				viewHolder.parent = v.findViewById(R.id.all_content);
			}
			else if (viewType == ViewType.SEPERATOR)
			{
				v = inflater.inflate(R.layout.empty_conv_ftue_sep, parent, false);
			}
			v.setTag(viewHolder);
		}
		else
		{
			viewHolder = (ViewHolder) v.getTag();
		}

		if (viewType == ViewType.CONTACTS_CARD)
		{
			EmptyConversationContactItem item = (EmptyConversationContactItem) getItem(position);
			viewHolder.name.setText(item.getHeader());

			int limit = HikeConstants.FTUE_CONTACT_CARD_LIMIT;
			View parentView = null;
			for (int i = 0; i < item.getContactList().size(); i++)
			{
				ContactInfo contactInfo = item.getContactList().get(i);
				parentView = viewHolder.contactsContainer.getChildAt(i);

				ImageView avatar = (ImageView) parentView.findViewById(R.id.avatar);
				TextView name = (TextView) parentView.findViewById(R.id.contact);
				TextView status = (TextView) parentView.findViewById(R.id.info);

				iconLoader.loadImage(contactInfo.getMsisdn(), true, avatar, true);

				name.setText(contactInfo.getName());
				status.setText(contactInfo.getMsisdn());

				parentView.setTag(contactInfo);
				parentView.setOnClickListener(contactCardClickListener);

				if (--limit == 0)
				{
					break;
				}
			}

			switch (item.getType())
			{
			case EmptyConversationItem.HIKE_CONTACTS:
				viewHolder.mainInfo.setVisibility(View.GONE);
				if (HomeActivity.ftueContactsData.getTotalHikeContactsCount() > HikeConstants.FTUE_CONTACT_CARD_LIMIT)
				{
					setUpSeeAllButton(viewHolder.seeAll);
				}
				else
				{
					viewHolder.seeAll.setVisibility(View.GONE);
				}
				break;
			case EmptyConversationItem.SMS_CONTACTS:
				viewHolder.mainInfo.setVisibility(View.VISIBLE);
				viewHolder.mainInfo.setText(R.string.ftue_sms_contact_card_subtext);
				if (HomeActivity.ftueContactsData.getTotalSmsContactsCount() > HomeActivity.ftueContactsData.getSmsContacts().size())
				{
					setUpSeeAllButton(viewHolder.seeAll);
				}
				else
				{
					viewHolder.seeAll.setVisibility(View.GONE);
				}
				break;

			}
		}
		else if (viewType == ViewType.FTUE_CARD)
		{
			EmptyConversationFtueCardItem item = (EmptyConversationFtueCardItem) getItem(position);
			viewHolder.name.setText(item.getHeaderTxtResId());
			viewHolder.mainInfo.setText(item.getSubTxtResId());
			viewHolder.seeAll.setText(item.getClickableTxtResId());
			viewHolder.seeAll.setTextColor(item.getClickableTxtColor());
			viewHolder.cardImg.setBackgroundColor(item.getImgBgColor());
			viewHolder.cardImg.setImageResource(item.getImgResId());
			viewHolder.cardImg.setScaleType(ScaleType.CENTER);
			viewHolder.parent.setTag(item);
			viewHolder.parent.setOnClickListener(ftueCardClickListener);
		}
		return v;
	}

	private void setUpSeeAllButton(TextView seeAllView)
	{
		seeAllView.setVisibility(View.VISIBLE);
		seeAllView.setText(R.string.see_all_upper_caps);
		seeAllView.setOnClickListener(seeAllBtnClickListener);
	}

	@Override
	public boolean isEnabled(int position)
	{
		EmptyConversationItem item = (EmptyConversationItem) getItem(position);
		switch (item.getType())
		{
		case EmptyConversationItem.HIKE_CONTACTS:
		case EmptyConversationItem.SMS_CONTACTS:
			return false;

		default:
			break;
		}
		return super.isEnabled(position);
	}

	private OnClickListener contactCardClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			ContactInfo contactInfo = (ContactInfo) v.getTag();

			Utils.startChatThread(context, contactInfo);

			Utils.sendUILogEvent(HikeConstants.LogEvent.FTUE_CARD_START_CHAT_CLICKED, contactInfo.getMsisdn());

		}
	};
	
	private OnClickListener ftueCardClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			EmptyConversationFtueCardItem item = (EmptyConversationFtueCardItem) v.getTag();
			if (item.getType() == EmptyConversationItem.LAST_SEEN)
			{
				Intent intent = new Intent(context, PeopleActivity.class);
				context.startActivity(intent);
				Utils.sendUILogEvent(HikeConstants.LogEvent.FTUE_CARD_LAST_SEEN_CLICKED);
			}
			else if (item.getType() == EmptyConversationItem.HIDDEN_MODE)
			{
				HikeMessengerApp.getPubSub().publish(HikePubSub.STEALTH_UNREAD_TIP_CLICKED, null);
				Utils.sendUILogEvent(HikeConstants.LogEvent.FTUE_CARD_HIDDEN_MODE_CLICKED);
			}
		}
	};

	private OnClickListener seeAllBtnClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(context, ComposeChatActivity.class);
			context.startActivity(intent);
			Utils.sendUILogEvent(HikeConstants.LogEvent.FTUE_CARD_SEEL_ALL_CLICKED);
		}
	};

}