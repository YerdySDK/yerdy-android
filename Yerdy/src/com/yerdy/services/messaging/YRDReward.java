package com.yerdy.services.messaging;

import java.util.ArrayList;
import java.util.List;

/**
 * Pull message reward 
 * @author Chris
 */
public class YRDReward {

	List<YRDRewardItem> _rewards = null;
	
	//Expected format
	//"key:value;key:value;
	public YRDReward(String rewardString) {
		_rewards = new ArrayList<YRDRewardItem>();
		
		if(rewardString != null && rewardString.length() > 0) {
			String[] rewardArr = rewardString.split(";");
			for(String str : rewardArr) {
				String[] rewardItem = str.split(",");
				if(rewardItem.length == 2) {
					String key = rewardItem[0].trim();
					String value = rewardItem[1].trim();
					if(key.length() > 0 && value.length() > 0) {
						_rewards.add(new YRDRewardItem(key, value));
					}
				}
			}
		}
		
	}
	
	public List<YRDRewardItem> getRewards() {
		return _rewards;
	}
}
