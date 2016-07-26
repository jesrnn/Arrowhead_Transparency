package com.eislab.af.translator.spokes;

import com.eislab.af.translator.data.BaseContext;

public class UaClient_spoke implements BaseSpokeConsumer {

	BaseSpoke nextSpoke;

	

	public UaClient_spoke(String ProviderAddress) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void in(BaseContext context) {
		// TODO Auto-generated method stub
		context.setContent("Hello world");
		this.nextSpoke.in(context);
	}

	@Override
	public void setNextSpoke(Object nextSpoke) {
		// TODO Auto-generated method stub
		this.nextSpoke = (BaseSpoke) nextSpoke;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	
	public int activity = 0;
	
	@Override
	public int getLastActivity() {
		// TODO Auto-generated method stub
		return activity;
	}

	@Override
	public void clearActivity() {
		// TODO Auto-generated method stub
		activity = 0;
	}

}
