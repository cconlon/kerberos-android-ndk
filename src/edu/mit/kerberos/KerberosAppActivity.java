/* KerberosAppActivity.java - Example Kerberos Android App */
/*
 * Copyright (C) 2012 by the Massachusetts Institute of Technology.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Original source developed by yaSSL (http://www.yassl.com)
 *
 * Description: 
 * 
 * Android application to perform the basic functionality of Kerberos' Kinit, 
 * Klist, Kvno, and Kdestroy, as well as acting as a simple example GSSAPI 
 * client application. This functionality is spread across three GUI tabs.
 * Functionality includes:
 *      - Get a ticket with kinit
 *      - List the ticket with klist
 *      - Get service ticket with kvno
 *      - Destroy the ticket cache with kdestroy
 *      - Start example GSS-API client to do the following:
 *          a) Establish a GSSAPI context with the example server
 *          b) Sign, encrypt, and send a message to the server
 *          c) Verify the signature block returned by the server
 */
package edu.mit.kerberos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.TabActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.LinearLayout;
import android.widget.TabHost;

import org.ietf.jgss.*;

public class KerberosAppActivity extends TabActivity
{
    /* Native JNI function declarations */
    public native int nativeSetKRB5CCNAME(String path);
    public native int nativeSetKRB5CONFIG(String path);
	public native int nativeKinit(String argv, int argc);
	public native int nativeKlist(String argv, int argc);
	public native int nativeKvno(String argv, int argc);
	public native int nativeKdestroy(String argv, int argc);

    /* Server Information for Client Application */    
    private static int port                = 0;
    private static String server           = null;
    private static String servicePrincipal = null;
    private static String clientPrincipal  = null;
    private static int uid;

    /* Global GSS-API objects */
    private static GSSCredential clientCred = null;
    private static GSSContext context       = null;
    private static GSSManager mgr = GSSManager.getInstance();

    /* using null to request default mech, krb5 */
    private static Oid mech                 = null;

    /* Connection objects */
    private static Socket clientSocket      = null;
    private static OutputStream serverOut   = null;
    private static InputStream serverIn     = null;

    /* Return values */
    private static int FAILURE = -1;
    private static int SUCCESS = 0;

    /* default kerberos configuration file location, used to set
       native KRB5_CONFIG environment variable */
    private static String defaultKRB5_CONFIG = "/data/local/kerberos/krb5.conf";

    /* Load our native library for SWIG stuff and native JNI functions */
    static {
        try {
            System.loadLibrary("kerberosapp");
        } catch(UnsatisfiedLinkError e) {
            System.err.println("Unable to load libkerberosapp. " + 
                    "Check LD_LIBRARY_PATH environment variable.\n" + e);
            System.exit(1);
        }
    }

    /**
	 * Button listener for kinit ("Get Ticket") button.
	 */
	private OnClickListener mButtonListener = new OnClickListener() 
    {
		public void onClick(View v) {
			
			TextView tv = (TextView) findViewById(R.id.textViewClient);
			EditText principal = (EditText) findViewById(R.id.etClientPrincipal);
			String prinValue = principal.getText().toString();
            String argString;
            int ret = 0;
			
			/* Clear TextView */
			tv.setText("");

            RadioButton authPass = (RadioButton) findViewById(R.id.radio_password);
			
            if (!authPass.isChecked())
			{
	            argString = "-V -c /data/local/kerberos/ccache/krb5cc_"
	                    + uid + " -k -t /data/local/kerberos/krb5.keytab " + prinValue;
			}
			else
			{
    			argString = "-V -c /data/local/kerberos/ccache/krb5cc_"
    					+ uid + " " + prinValue;
			}
	                
	        int t = nativeKinit(argString, countWords(argString));        
	        
	        Log.i("---JAVA JNI---", "Return value from native lib: " + t);
	        	        
	        if(t == 0) {
	        	tv.append("Got Ticket!\n");
	        }
	        else if(t == 1)
	        	tv.append("Failed to get Ticket!\n");   
		}
	};
	
	private String[] kinitPrompter(String name, String banner,
			final Prompt[] prompts)
	{
		final String[] results = new String[prompts.length];

		/* Ignore prompts and multi-prompt scenarios, which a real
		   implementation would need to handle. */
		if (prompts.length > 1)
		{
			appendText("ERROR: Multi-prompt support not implemented!");
			return results;
		}

		EditText editText = (EditText) findViewById(R.id.password);
		results[0] = editText.getText().toString();

		return results;
	}

	
	/**
	 * Button listener for klist ("List Ticket") button.
	 */
	private OnClickListener klistButtonListener = new OnClickListener() 
    {
		public void onClick(View v) {
			
			TextView tv = (TextView) findViewById(R.id.textViewClient);
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
	private OnClickListener kvnoButtonListener = new OnClickListener() 
    {
		public void onClick(View v) {
			
			TextView tv = (TextView) findViewById(R.id.textViewClient);
			EditText principal = (EditText) findViewById(R.id.etClientPrincipal);
			int uid = android.os.Process.myUid();
			String prinValue = principal.getText().toString();
			
			/* Clear TextView */
			tv.setText("");
			
			String argString = "-c /data/local/kerberos/ccache/krb5cc_" + 
                uid + " -k /data/local/kerberos/krb5.keytab " + prinValue;
	                
	        int t = nativeKvno(argString, countWords(argString));
	        Log.i("---JAVA JNI---", "Return value from native lib: " + t);
	        	        
	        if(t == 0)
	        	tv.append("Finished!\n");
		}
	};
	
	/**
	 * Button listener for kdestroy ("Destroy Ticket") button
	 */
	private OnClickListener kdestroyButtonListener = new OnClickListener() 
    {
		public void onClick(View v) {
			
			TextView tv = (TextView) findViewById(R.id.textViewClient);
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
	
	/**
	 * Button listener for "Start Client App" button.
	 */
	private OnClickListener clientAppButtonListener = new OnClickListener() 
    {
		public void onClick(View v) {
			
			TextView tv = (TextView) findViewById(R.id.textViewApp);
            int ret = 0;

            /* clear Client App TextView */
			tv.setText("");
		
            /* set server info */    
            EditText serverPrincipal = (EditText) findViewById(R.id.etServerPrincipal);
            EditText serverIpAddress = (EditText) findViewById(R.id.etServerIpAddress);
            EditText serverPort = (EditText) findViewById(R.id.etServerPort);
			String serverP = serverPrincipal.getText().toString();
			String serverIp = serverIpAddress.getText().toString();
			String serverPt = serverPort.getText().toString();
            if (serverP.matches("")) {
                tv.append("You need to specify a server principal in tab 2.\n");
                ret = 1;
            } else {
                servicePrincipal = serverP;
            }
            if (serverIp.matches("")) {
                tv.append("You need to specify a server IP address in tab 2.\n");
                ret = 1;
            } else {
                server = serverIp;
            }
            if (serverPt.matches("")) {
                tv.append("You need to specifiy a server port number in tab 2.\n");
                ret = 1;
            } else {
                port = Integer.valueOf(serverPt);
            }

            /* set client info */
            EditText clientPrin = (EditText) findViewById(R.id.etClientPrincipal);
            String clientP = clientPrin.getText().toString();
            if (clientP.matches("")) {
                tv.append("You need to specify a client principal in tab 1.\n");
                ret = 1;
            } else {
                clientPrincipal = clientP;
            }

            /* if input is bad, exit early */
            if (ret != 0)
                return;
            
            try {
                ret = startClient();
            } catch (Exception e) {
                tv.append("Caught Exception\n");
                e.printStackTrace();
            }

            if (ret != 0)
                tv.append("Client Did Not Finish Successfully!\n");
		}
	};

    /**
     * Called when the application is exited.
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
	    String argString = "-c /data/local/kerberos/ccache/krb5cc_" + uid;
	    int t = nativeKdestroy(argString, countWords(argString));
    }
	
    /**
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        int ret = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TabHost mTabHost = getTabHost();

        mTabHost.addTab(mTabHost.newTabSpec("tab_test1")
                .setIndicator("1. Client Info")
                .setContent(R.id.tabview1));
        mTabHost.addTab(mTabHost.newTabSpec("tab_test2")
                .setIndicator("2. Server Info")
                .setContent(R.id.tabview2));
        mTabHost.addTab(mTabHost.newTabSpec("tab_test3")
                .setIndicator("3. Client App")
                .setContent(R.id.tabview3));

        mTabHost.getTabWidget().getChildAt(0).getLayoutParams().height = 50;
        mTabHost.getTabWidget().getChildAt(1).getLayoutParams().height = 50;
        mTabHost.getTabWidget().getChildAt(2).getLayoutParams().height = 50;

        mTabHost.setCurrentTab(0);
        
        // Capture our buttons from layout
        Button button = (Button) findViewById(R.id.button1);
        Button btnKlist = (Button) findViewById(R.id.btnList);
        Button btnKdestroy = (Button) findViewById(R.id.btnDestroy);
        Button btnKvno = (Button) findViewById(R.id.btnVno);
        Button startButton = (Button) findViewById(R.id.startButton);
                
        // Register our button onClick listeners
        button.setOnClickListener(mButtonListener);
        btnKlist.setOnClickListener(klistButtonListener);
        btnKdestroy.setOnClickListener(kdestroyButtonListener);
        btnKvno.setOnClickListener(kvnoButtonListener);
        startButton.setOnClickListener(clientAppButtonListener);

        // Capture some text fields (to toggle visibility)
        final TextView tvPasswordLbl = (TextView) findViewById(R.id.password_label);
        final TextView tvKeytabLbl = (TextView) findViewById(R.id.keytab_label);
        final EditText etPassword = (EditText) findViewById(R.id.password);

        tvKeytabLbl.setVisibility(View.GONE);

        // Register our RadioGroup onChecked listener
        RadioGroup authChoice = (RadioGroup) findViewById(R.id.authGroup);
        authChoice.setOnCheckedChangeListener(
            new RadioGroup.OnCheckedChangeListener() {
                
            public void onCheckedChanged(RadioGroup group, int checkedVal) {
                switch(checkedVal) {
                    case R.id.radio_password:
                        tvPasswordLbl.setVisibility(View.VISIBLE);
                        etPassword.setVisibility(View.VISIBLE);
                        tvKeytabLbl.setVisibility(View.GONE);
                        break;
                    case R.id.radio_keytab:
                        tvPasswordLbl.setVisibility(View.GONE);
                        etPassword.setVisibility(View.GONE);
                        tvKeytabLbl.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
        
        TextView tv = (TextView) findViewById(R.id.textViewApp);
        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setTextSize(11);
        TextView tv2 = (TextView) findViewById(R.id.textViewClient);
        tv2.setMovementMethod(new ScrollingMovementMethod());
        tv2.setTextSize(11);
	   
        /* Set location of Kerberos ticket cache */ 
        uid = android.os.Process.myUid();
        ret = nativeSetKRB5CCNAME("/data/local/kerberos/ccache/krb5cc_" + uid);
        if (ret == 0) {
            tv2.append("Successfully set KRB5CCNAME path\n");
        } else {
            tv2.append("Failed to set KRB5CCNAME path correctly\n");
        }

        /* Set location of Kerberos configuration file (krb5.conf) */
        ret = nativeSetKRB5CONFIG(defaultKRB5_CONFIG);
        if (ret == 0) {
            tv2.append("Successfully set KRB5_CONFIG path\n");
        } else {
            tv2.append("Failed to set KRB5_CONFIG path correctly\n");
        }
        
    }
    
    /**
     * Counts the number of words in input string.
     * @param input
     * @return Number of words in string, delimited by a space
     */
    public int countWords(String input)
    {
    	String[] words = input.split(" ");
    	return words.length;
    }
    
    private void callback(int test) 
    {
    	Log.i("---JAVA JNI---", "Callback from native function!");
    	TextView tv = (TextView) findViewById(R.id.textViewClient);
    	tv.append("From native, test = " + test + "\n");
    }
    
    /**
     * Appends text to the main output TextView.
     * @param input
     */
    private void appendText(String input) 
    {
    	TextView tv = (TextView) findViewById(R.id.textViewClient);
    	tv.append(input);
    }

    /**
     * Starts the example client.
     */
    private int startClient() throws Exception
    {
    	TextView tv = (TextView) findViewById(R.id.textViewApp);
        
        int ret = 0;
        String serverMsg;

        tv.append("Starting GSS-API Client App\n");

        ret = connectToServer(tv);

        if (ret == SUCCESS)
            ret = initializeGSS(tv);

        if (ret == SUCCESS)
            ret = establishContext(tv, serverIn, serverOut);

        if (ret == SUCCESS)
            ret = doCommunication(tv, serverIn, serverOut);

        /* shutdown */
        context.dispose();
        clientCred.dispose();    
        serverIn.close();
        serverOut.close();
        clientSocket.close();

        tv.append("\nShut down GSS-API and closed connection to server\n");

        return ret;
    }

    /**
     * Connect to example GSS-API server, using specified port and 
     * service name.
     */
    private int connectToServer(TextView tv) {

        try {
            clientSocket = new Socket(server, port);
            tv.append("Connected to " + server + " at port " + port + "\n");

            /* get input and output streams */
            serverOut = clientSocket.getOutputStream();
            serverIn = clientSocket.getInputStream();

        } catch (UnknownHostException e) {
            tv.append("Unknown host: " + server + "\n");
            e.printStackTrace();
            return FAILURE;
        } catch (IOException e) {
            tv.append("I/O error for the connection to " + server + "\n");
            e.printStackTrace();
            return FAILURE;
        }

        return SUCCESS;

    } /* end connectToServer() */

    /**
     * Set up GSS-API in preparation for context establishment. Creates
     * GSSName and GSSCredential for client principal.
     */
    private int initializeGSS(TextView tv) {

        try {
            GSSName clientName = mgr.createName(clientPrincipal,
                    GSSName.NT_USER_NAME);

            /* create cred with max lifetime */
            clientCred = mgr.createCredential(clientName,
                    GSSCredential.INDEFINITE_LIFETIME, mech,
                    GSSCredential.INITIATE_ONLY);

            tv.append("GSSCredential created for " 
                    + clientCred.getName().toString() + "\n");
            tv.append("Credential lifetime (sec) = "
                    + clientCred.getRemainingLifetime() + "\n");

        } catch (GSSException e) {
            tv.append("GSS-API error in credential acquisition: " +
                    e.getMessage() + "\n");
            e.printStackTrace();
            return FAILURE;
        }

        return SUCCESS;

    } /* end initializeGSS() */

    /**
     * Establish a GSS-API context with example server, calling
     * initSecContext() until context.isEstablished() is true.
     *
     * This method also tests exporting and re-importing the security
     * context.
     */
    private int establishContext(TextView tv, InputStream serverIn,
            OutputStream serverOut) {

        byte[] inToken  = new byte[0];
        byte[] outToken = null;
        int err = 0;

        try {
            GSSName peer = mgr.createName(servicePrincipal,
                    GSSName.NT_HOSTBASED_SERVICE);

            context = mgr.createContext(peer, mech, clientCred,
                    GSSContext.INDEFINITE_LIFETIME);

            context.requestConf(true);
            context.requestReplayDet(true);
            context.requestMutualAuth(true);

            while (!context.isEstablished()) {

                tv.append("Calling initSecContext\n");
                outToken = context.initSecContext(inToken, 0, inToken.length);

                if (outToken != null && outToken.length > 0) {
                    err = GssUtil.WriteToken(tv, serverOut, outToken);
                    if (err == 0) {
                        tv.append("Sent token to server...\n");
                    } else {
                        tv.append("Error sending token to server...\n");
                    }
                }

                if (!context.isEstablished()) {
                    inToken = GssUtil.ReadToken(tv, serverIn);
                    tv.append("Received token from server...\n");
                }
            }

            GSSName peerName = context.getTargName();
            GSSName srcName = context.getSrcName();
            tv.append("Security context established with " + peer + "\n");
            GssUtil.printSubString(tv, "Source Name", srcName.toString());
            GssUtil.printSubString(tv, "Mechanism", context.getMech().toString());
            GssUtil.printSubString(tv, "AnonymityState", context.getAnonymityState());
            GssUtil.printSubString(tv, "ConfState", context.getConfState());
            GssUtil.printSubString(tv, "CredDelegState", context.getCredDelegState());
            GssUtil.printSubString(tv, "IntegState", context.getIntegState());
            GssUtil.printSubString(tv, "Lifetime", context.getLifetime());
            GssUtil.printSubString(tv, "MutualAuthState", context.getMutualAuthState());
            GssUtil.printSubString(tv, "ReplayDetState", context.getReplayDetState());
            GssUtil.printSubString(tv, "SequenceDetState", context.getSequenceDetState());
            GssUtil.printSubString(tv, "Is initiator?", context.isInitiator());
            GssUtil.printSubString(tv, "Is Prot Ready?", context.isProtReady());

            /* Test exporting/importing established security context */
            byte[] exportedContext = context.export();
            context = mgr.createContext(exportedContext);
            GSSName serverInfo2 = context.getTargName();

        } catch (GSSException e) {
            tv.append("GSS-API error during context establishment: "
                    + e.getMessage() + "\n");
            e.printStackTrace();
            return FAILURE;
        }

        return SUCCESS;

    } /* end establishContext() */

    /**
     * Communicate with the server. First send a message that has been
     * wrapped with context.wrap(), then verify the signature block which
     * the server sends back.
     */
    private int doCommunication(TextView tv, InputStream serverIn,
            OutputStream serverOut) {

        MessageProp messagInfo = new MessageProp(false);
        byte[] inToken  = new byte[0];
        byte[] outToken = null;
        byte[] buffer;
        int err = 0;

        try {
            
            String msg = "Hello Server, this is the client!";
            buffer = msg.getBytes();

            /* Set privacy to "true" and use the default QOP */
            messagInfo.setPrivacy(true);

            outToken = context.wrap(buffer, 0, buffer.length, messagInfo);
            err = GssUtil.WriteToken(tv, serverOut, outToken);
            if (err == 0) {
                tv.append("Sent message to server ('" +
                        msg + "')\n");

                /* Read signature block from the server */ 
                inToken = GssUtil.ReadToken(tv, serverIn);
                tv.append("Received sig block from server...\n");

                GSSName serverInfo = context.getTargName();
                tv.append("Message from " + serverInfo.toString() +
                    " arrived.\n");
                GssUtil.printSubString(tv, "Was it encrypted? ", messagInfo.getPrivacy());
                GssUtil.printSubString(tv, "Duplicate Token? ", messagInfo.isDuplicateToken());
                GssUtil.printSubString(tv, "Old Token? ", messagInfo.isOldToken());
                GssUtil.printSubString(tv, "Gap Token? ", messagInfo.isGapToken());

                /* Verify signature block */
                context.verifyMIC(inToken, 0, inToken.length, buffer, 0, 
                    buffer.length, messagInfo);
                tv.append("Verified MIC from server\n");

            } else {
                tv.append("Error sending message to server...\n");
            }

        } catch (GSSException e) {
            tv.append("GSS-API error in per-message calls: " +
                    e.getMessage() + "\n");
            e.printStackTrace();
            return FAILURE;
        }

        return SUCCESS;

    } /* end doCommunication() */
}
