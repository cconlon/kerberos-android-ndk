package com.mit.kerberos;

/**
 * KerberosAppActivity.java
 *
 * Copyright 1990, 2008 by the Massachusetts Institute of Technology.
 * All Rights Reserved.
 *
 * Export of this software from the United States of America may
 *   require a specific license from the United States Government.
 *   It is the responsibility of any person or organization contemplating
 *   export to obtain such a license before exporting.
 *
 * WITHIN THAT CONSTRAINT, permission to use, copy, modify, and
 * distribute this software and its documentation for any purpose and
 * without fee is hereby granted, provided that the above copyright
 * notice appear in all copies and that both that copyright notice and
 * this permission notice appear in supporting documentation, and that
 * the name of M.I.T. not be used in advertising or publicity pertaining
 * to distribution of the software without specific, written prior
 * permission.  Furthermore if you modify this software you must label
 * your software as modified software and not distribute it in such a
 * fashion that it might be confused with the original M.I.T. software.
 * M.I.T. makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express
 * or implied warranty.
 *
 * Original source developed by yaSSL (http://www.yassl.com)
 *
 * Description: Android application to perform the basic functionality of 
 *              Kerberos' Kinit, Klist, and Kdestroy. In other words,
 *              (1) Get a ticket with Kinit
 *              (2) List the ticket with Klist
 *              (3) Destroy the ticket with Kdestroy           
 */

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

public class KerberosAppActivity extends Activity
{
	static{
    	System.loadLibrary("kerberosapp");
    }
	
	public native int nativeKinit(String argv, int argc);
	public native int nativeKlist(String argv, int argc);
	public native int nativeKvno(String argv, int argc);
	public native int nativeKdestroy(String argv, int argc);
	
	/**
	 * Button listener for kinit ("Get Ticket") button
	 */
	private OnClickListener mButtonListener = new OnClickListener() {
		public void onClick(View v) {
			
			TextView tv = (TextView) findViewById(R.id.textView);
			EditText principal = (EditText) findViewById(R.id.editText1);
			int uid = android.os.Process.myUid();
			String prinValue = principal.getText().toString();
			
			/* Clear TextView */
			tv.setText("");
			
	        String argString = "-V -c /data/local/kerberos/ccache/krb5cc_" + uid + " -k -t /data/local/kerberos/krb5.keytab " + prinValue;
	                
	        int t = nativeKinit(argString, countWords(argString));
	        Log.i("---JAVA JNI---", "Return value from native lib: " + t);
	        	        
	        if(t == 0) {
	        	tv.append("Got Ticket!\n");
	        }
	        else if(t == 1)
	        	tv.append("Failed to get Ticket!\n");   
		}
	};
	
	/**
	 * Button listener for klist ("List Ticket") button
	 */
	private OnClickListener klistButtonListener = new OnClickListener() {
		public void onClick(View v) {
			
			TextView tv = (TextView) findViewById(R.id.textView);
			int uid = android.os.Process.myUid();
			
			/* Clear TextView */
			tv.setText("");
			
	        String argString = "-c /data/local/kerberos/ccache/krb5cc_" + uid;
	                
	        int t = nativeKlist(argString, countWords(argString));
	        Log.i("---JAVA JNI---", "Return value from native lib: " + t);
	        	        
	        if(t == 1)
	        	tv.append("Failed to find Ticket!\n");
		}
	};
	
	/**
	 * Button listener for kvno ("Get Service Ticket") button
	 */
	private OnClickListener kvnoButtonListener = new OnClickListener() {
		public void onClick(View v) {
			
			TextView tv = (TextView) findViewById(R.id.textView);
			EditText principal = (EditText) findViewById(R.id.editText1);
			int uid = android.os.Process.myUid();
			String prinValue = principal.getText().toString();
			
			/* Clear TextView */
			tv.setText("");
			
			String argString = "-c /data/local/kerberos/ccache/krb5cc_" + uid + " -k /data/local/kerberos/krb5.keytab " + prinValue;
	                
	        int t = nativeKvno(argString, countWords(argString));
	        Log.i("---JAVA JNI---", "Return value from native lib: " + t);
	        	        
	        if(t == 0)
	        	tv.append("Finished!\n");
		}
	};
	
	/**
	 * Button listener for kdestroy ("Destroy Ticket") button
	 */
	private OnClickListener kdestroyButtonListener = new OnClickListener() {
		public void onClick(View v) {
			
			TextView tv = (TextView) findViewById(R.id.textView);
			int uid = android.os.Process.myUid();
			
			/* Clear TextView */
			tv.setText("");
			
	        String argString = "-c /data/local/kerberos/ccache/krb5cc_" + uid;
	                
	        int t = nativeKdestroy(argString, countWords(argString));
	        Log.i("---JAVA JNI---", "Return value from native lib: " + t);
	        
	        if(t == 0)
	        	tv.append("Finished!\n");
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Capture our buttons from layout
        Button button = (Button) findViewById(R.id.button1);
        Button btnKlist = (Button) findViewById(R.id.btnList);
        Button btnKdestroy = (Button) findViewById(R.id.btnDestroy);
        Button btnKvno = (Button) findViewById(R.id.btnVno);
        
        // Register our button onClick listeners
        button.setOnClickListener(mButtonListener);
        btnKlist.setOnClickListener(klistButtonListener);
        btnKdestroy.setOnClickListener(kdestroyButtonListener);
        btnKvno.setOnClickListener(kvnoButtonListener);
        
        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setTextSize(12);
    }
    
    /**
     * Counts the number of words in input string
     * @param input
     * @return Number of words in string, delimited by a space
     */
    public int countWords(String input)
    {
    	String[] words = input.split(" ");
    	return words.length;
    }
    
    private void callback(int test){
    	Log.i("---JAVA JNI---", "Callback from native function!");
    	TextView tv = (TextView) findViewById(R.id.textView);
    	tv.append("From native, test = " + test + "\n");
    }
    
    /**
     * Appends text to the main output TextView
     * @param input
     */
    private void appendText(String input){
    	System.out.println("Entered appendText, text input = " + input);
    	TextView tv = (TextView) findViewById(R.id.textView);
    	tv.append(input);
    }
}
