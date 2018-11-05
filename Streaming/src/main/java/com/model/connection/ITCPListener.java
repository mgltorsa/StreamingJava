package com.model.connection;

public interface ITCPListener {

   public void onInputMessageData(String data, ITCPListener callback);
   
}
