package com.eislab.af.translator.spokes;

import com.eislab.af.translator.data.BaseContext;

public class UaServer_spoke implements BaseSpokeProvider{

	BaseSpoke nextSpoke;
	
	
	public UaServer_spoke(String property, String string) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void in(BaseContext context) {
		// TODO Auto-generated method stub
		System.out.println(context.getContent());		
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

	@Override
	public String getAddress() {
		// TODO Auto-generated method stub
		return null;
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
