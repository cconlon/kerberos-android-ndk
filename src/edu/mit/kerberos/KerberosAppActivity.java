package edu.mit.kerberos;

/*
 * KerberosAppActivity.java
 *
 * Copyright (C) 2012 by the Massachusetts Institute of Technology.
 * All rights reserved.
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
 * Description: 
 * 
 * Android application to perform the basic functionality of Kerberos' Kinit, 
 * Klist, Kvno, and Kdestroy, as well as acting as a simple example GSSAPI 
 * client application. In other words,
 *      (1) Get a ticket with Kinit
 *      (2) List the ticket with Klist
 *      (3) Get service ticket with Kvno
 *      (4) Destroy the ticket with Kdestroy
 *      (5) Start example GSSAPI client to do the following:
 *          a) Establish a GSSAPI context with the example server
 *          b) Send a wrapped message to the server and verify the returned
 *             signature block. Using gss_wrap / gss_verify_mic.
 *          c) Repeat step b) but using gss_seal / gss_verify
 *          d) Perform misc. GSSAPI function tests
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
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

public class KerberosAppActivity extends Activity implements gsswrapperConstants
{
    /* Global GSS-API context */
    public static gss_ctx_id_t_desc context = new gss_ctx_id_t_desc();

    public native int nativeSetKRB5CCNAME(String path);
	public native int nativeKinit(String argv, int argc);
	public native int nativeKlist(String argv, int argc);
	public native int nativeKvno(String argv, int argc);
	public native int nativeKdestroy(String argv, int argc);

    /* Server Information for Client Application */    
    static int port = 11115;
    static String server = "10.211.55.6";
    static String clientName = "myuser";
    static String serviceName = "service@myhost.local";
    static int uid;

    /**
	 * Button listener for kinit ("Get Ticket") button.
	 */
	private OnClickListener mButtonListener = new OnClickListener() 
    {
		public void onClick(View v) {
			
			TextView tv = (TextView) findViewById(R.id.textView);
			EditText principal = (EditText) findViewById(R.id.editText1);
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
	private OnClickListener kvnoButtonListener = new OnClickListener() 
    {
		public void onClick(View v) {
			
			TextView tv = (TextView) findViewById(R.id.textView);
			EditText principal = (EditText) findViewById(R.id.editText1);
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
	
	/**
	 * Button listener for "Start Client App" button.
	 */
	private OnClickListener clientAppButtonListener = new OnClickListener() 
    {
		public void onClick(View v) {
			
			TextView tv = (TextView) findViewById(R.id.textView);
            int ret = 0;
			
			/* Clear TextView */
			tv.setText("");
            
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
        
        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setTextSize(11);
	    
        uid = android.os.Process.myUid();
        ret = nativeSetKRB5CCNAME("/data/local/kerberos/ccache/krb5cc_" + uid);
        if (ret == 0) {
            tv.append("Successfully set KRB5CCNAME path\n");
        } else {
            tv.append("Failed to set KRB5CCNAME path correctly\n");
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
    	TextView tv = (TextView) findViewById(R.id.textView);
    	tv.append("From native, test = " + test + "\n");
    }
    
    /**
     * Appends text to the main output TextView.
     * @param input
     */
    private void appendText(String input) 
    {
    	TextView tv = (TextView) findViewById(R.id.textView);
    	tv.append(input);
    }

    /**
     * Starts the example client.
     */
    private int startClient() throws Exception
    {
    	TextView tv = (TextView) findViewById(R.id.textView);
        
        /* Return/OUTPUT variables */
        long maj_status = 0;
        long[] min_status = {0};
        long[] ret_flags = {0};
        long[] time_rec = {0};
   
        int ret = 0;
        
        /* Customize this if a specific mechanisms should be negotiated, 
           otherwise set neg_mech_set to GSS_C_NO_OID_SET */
        gss_OID_set_desc neg_mech_set = new gss_OID_set_desc();
        gss_OID_desc neg_mech = new gss_OID_desc("{ 1 2 840 113554 1 2 2 }");
        maj_status = gsswrapper.gss_add_oid_set_member(min_status, 
                neg_mech, neg_mech_set);

        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "adding oid to set", min_status, maj_status);
            return -1;
        }
        
        Socket clientSocket = null;
        OutputStream serverOut = null;
        InputStream serverIn = null;
        String serverMsg;
       
        /* testing gss_oid_to_str */
        gss_buffer_desc buffer = new gss_buffer_desc();
        maj_status = gsswrapper.gss_oid_to_str(min_status, 
                gsswrapper.getGSS_C_NT_EXPORT_NAME(), buffer);

        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "calling gss_oid_to_str", 
                    min_status, maj_status);
            return -1;
        }
        gsswrapper.gss_release_buffer(min_status, buffer);

        /* create socket to connect to the server */
        try {
            clientSocket = new Socket(server, port);
            tv.append("Connected to " + server + " at port " + port + "\n");

            /* get input and output streams */
            serverOut = clientSocket.getOutputStream();
            serverIn = clientSocket.getInputStream();
        
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + server);
            tv.append("Unknown host: " + server + "\n");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("I/O error for the connection to " + server);
            tv.append("I/O error for the connection to " + server + "\n");
            e.printStackTrace();
        }

        /* Stores created context in global static "context" variable */
        ret = Authenticate(clientSocket, serverIn, serverOut, clientName, 
                serviceName, neg_mech_set);

        if (ret == 0) {
            tv.append("Finished Authentication\n");
            ret = PrintContextInfo();
        
            if (ret == 0) {
                ret = Communicate(clientSocket, serverIn, serverOut);
       
                if (ret == 0) {
                    tv.append("Finished first communication with server\n");
                    ret = AltCommunicate(clientSocket, serverIn, serverOut);

                    if (ret == 0) {
                        tv.append("Finished second communication with " +
                                "server\n");
                        ret = MiscFunctionTests();
                    } else {
                        tv.append("Failed during second communication " +
                                "with server\n");
                    }
      
                } else {
                    tv.append("Failed during first communication " +
                            "with server\n");
                }
            } else {
                tv.append("Failed during PrintContextInfo()\n");
            }
        } else {
            tv.append("Failed during Authentication\n");
        }
        
        if (ret == 0) {
            tv.append("SUCCESS!\n");
            ret = 0;

            gss_buffer_desc output_token = GSS_C_NO_BUFFER;
            gss_OID_desc tmp = context.getMech_type();
            maj_status = gsswrapper.gss_delete_sec_context(min_status, 
                    context, output_token);

            if (maj_status != GSS_S_COMPLETE) {
                Util.displayError(tv, "deleting security context", 
                        min_status, maj_status);
                ret = -1;
            }

            gsswrapper.gss_release_buffer(min_status, output_token);
        } else {
            tv.append("FAILURE!\n");
            ret = -1;
        }

        gsswrapper.gss_release_oid(min_status, neg_mech);
        gsswrapper.gss_release_oid_set(min_status, neg_mech_set);
    
        serverIn.close();
        serverOut.close();
        clientSocket.close();

        return ret;
    }
    
    private int Authenticate(Socket clientSocket,
            InputStream serverIn, OutputStream serverOut,
            String inClientName, String inServiceName,
            gss_OID_set_desc neg_mech_set) 
    {
    	TextView tv = (TextView) findViewById(R.id.textView);
        
        long maj_status = 0;
        long[] min_status = {0};
        gss_name_t_desc clientName = new gss_name_t_desc();
        gss_name_t_desc serverName = new gss_name_t_desc();
        gss_cred_id_t_desc clientCredentials = GSS_C_NO_CREDENTIAL;
        gss_ctx_id_t_desc context_tmp = GSS_C_NO_CONTEXT;
        long[] actualFlags = {0};
        int err = 0;
        int ret[] = {0};
        long[] time_rec = {0};
        /* kerberos v5 */
        gss_OID_desc gss_mech_krb5 = new gss_OID_desc("1.2.840.113554.1.2.2");

        byte[] inputTokenBuffer = null;
        gss_buffer_desc inputToken = new gss_buffer_desc();

        tv.append("Authenticating [client]\n");

        /* Testing gss_indicate_mechs */
        gss_OID_set_desc mech_set = GSS_C_NO_OID_SET;
        maj_status = gsswrapper.gss_indicate_mechs(min_status, mech_set);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "gss_indicate_mechs(mech_set)", min_status,
                              maj_status);
            gsswrapper.gss_release_oid_set(min_status, mech_set);
            return -1;
        }
        maj_status = gsswrapper.gss_release_oid_set(min_status, mech_set);

        /* Client picks client principal it wants to use.  Only done if we
         * know what client principal will get the service principal we need. 
         */
        if (inClientName != null) {
            gss_buffer_desc nameBuffer = new gss_buffer_desc();
            nameBuffer.setLength(inClientName.length());
            nameBuffer.setValue(inClientName);

            maj_status = gsswrapper.gss_import_name(min_status, nameBuffer,
                    gsswrapper.getGSS_C_NT_USER_NAME(), clientName);
            if (maj_status != GSS_S_COMPLETE) {
                Util.displayError(tv, "gss_import_name(inClientName)", 
                                  min_status, maj_status);
                return -1;
            }

            maj_status = gsswrapper.gss_acquire_cred(min_status, clientName,
                    GSS_C_INDEFINITE,
                    GSS_C_NO_OID_SET,
                    GSS_C_INITIATE, clientCredentials,
                    null, time_rec);
            if (maj_status != GSS_S_COMPLETE) {
                Util.displayError(tv, "gss_acquire_cred", min_status,
                                  maj_status);
                return -1;
            }

            /* Did we want a specific mechanism to be used? */
            if (neg_mech_set != GSS_C_NO_OID_SET) {
                maj_status = gsswrapper.gss_set_neg_mechs(min_status, 
                        clientCredentials,
                        neg_mech_set);

                if (maj_status != GSS_S_COMPLETE) {
                    Util.displayError(tv, "setting negotiation mechanism", 
                                      min_status, maj_status);
                    return -1;
                } else {
                    tv.append("Successfully set neg. mechanism\n");
                }

            }
        }

        /* checking for valid import. Remember to run "kinit <clientName>" */
        long[] lifetime = {0};
        int[] cred_usage = {0};
        gss_name_t_desc name = new gss_name_t_desc();
        gss_OID_set_desc temp_mech_set = new gss_OID_set_desc();
        maj_status = gsswrapper.gss_inquire_cred(min_status,
                clientCredentials, name, lifetime, cred_usage, temp_mech_set);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "gss_inquire_cred(temp_mech_set)", 
                              min_status, maj_status);
            return -1;
        }
        maj_status = gsswrapper.gss_release_oid_set(min_status,
                temp_mech_set);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "gss_release_oid_set(temp_mech_set)", 
                              min_status, maj_status);
            return -1;
        }

        /* Test gss_duplicate_name function */
        gss_name_t_desc clientName_dup = new gss_name_t_desc();
        maj_status = gsswrapper.gss_duplicate_name(min_status,
                clientName, clientName_dup);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "duplicating client name", 
                              min_status, maj_status);
            return -1;
        }
        gsswrapper.gss_release_name(min_status, clientName_dup);

        /* Test gss_canonicalize_name function */
        gss_name_t_desc clientCanonicalized = new gss_name_t_desc();
        maj_status = gsswrapper.gss_canonicalize_name(min_status,
                clientName, gss_mech_krb5, clientCanonicalized);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "canonicalizing client name", 
                              min_status, maj_status);
            return -1;
        }

        /* Test gss_export_name function */
        gss_buffer_desc clientName_export = new gss_buffer_desc();
        maj_status = gsswrapper.gss_export_name(min_status, 
                clientCanonicalized,
                clientName_export);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "exporting client name", 
                    min_status, maj_status);
            return -1;
        }
        gsswrapper.gss_release_name(min_status, clientCanonicalized);
        gsswrapper.gss_release_buffer(min_status, clientName_export);
       

        /* Client picks the service principal it will try to use to connect
         * to the server.  The server principal is given at the top of this
         * file.*/
        gss_buffer_desc nameBuffer = new gss_buffer_desc();
        nameBuffer.setLength(inServiceName.length());
        nameBuffer.setValue(inServiceName);

        maj_status = gsswrapper.gss_import_name(min_status, nameBuffer,
                   gsswrapper.getGSS_C_NT_HOSTBASED_SERVICE(), serverName);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "gss_import_name(inServiceName)", 
                              min_status, maj_status);
            return -1;
        }

        /* The main authentication loop. Because GSS is a multimechanism
         * API, we need to loop calling gss_init_sec_context - passing
         * in the "input tokens" received from the server and send the
         * resulting "output tokens" back until we get GSS_S_COMPLETE
         * or an error. */

        maj_status = GSS_S_CONTINUE_NEEDED;
        while (maj_status != GSS_S_COMPLETE) {

            gss_buffer_desc outputToken = new gss_buffer_desc();
            outputToken.setLength(0);
            outputToken.setValue(null);

            long requestedFlags = (
                    GSS_C_MUTUAL_FLAG ^
                    GSS_C_REPLAY_FLAG ^
                    GSS_C_SEQUENCE_FLAG ^
                    GSS_C_CONF_FLAG ^
                    GSS_C_INTEG_FLAG );

            tv.append("Calling gss_init_sec_context...\n");
            gss_OID_desc actual_mech_type = new gss_OID_desc();

            maj_status = gsswrapper.gss_init_sec_context(min_status,
                    clientCredentials, context_tmp, serverName,
                    GSS_C_NO_OID, 
                    requestedFlags,
                    GSS_C_INDEFINITE,
                    GSS_C_NO_CHANNEL_BINDINGS,
                    inputToken,
                    actual_mech_type, outputToken, actualFlags, time_rec);

            if (outputToken.getLength() > 0) {
                /* 
                 * Send the output token to the server (even on error), 
                 * using a Java byte[]
                 */
                byte[] temp_token = new byte[(int)outputToken.getLength()];
                temp_token = gsswrapper.getDescArray(outputToken);
                tv.append("Generated Token Length = " + 
                          temp_token.length + "\n");
                err = Util.WriteToken(serverOut, temp_token);

                /* free the output token */
                gsswrapper.gss_release_buffer(min_status, outputToken);
            }

            if (err == 0) {
                if (maj_status == GSS_S_CONTINUE_NEEDED) {
                    
                    /* Protocol requires another packet exchange */
                    tv.append("Protocol requires another packet exchange\n");

                    /* Clean up old input buffer */
                    if (inputTokenBuffer != null)
                        inputTokenBuffer = null; 

                    /* Read another input token from the server */
                    inputTokenBuffer = Util.ReadToken(serverIn); 

                    if (inputTokenBuffer != null) {
                        gsswrapper.setDescArray(inputToken, inputTokenBuffer);
                        inputToken.setLength(inputTokenBuffer.length);
                        tv.append("Received Token Length = " + 
                                  inputToken.getLength() + "\n");
                    }
                } else if (maj_status != GSS_S_COMPLETE) {
                    Util.displayError(tv, "gss_init_sec_context", 
                                      min_status, maj_status);
                    return -1;
                }
            }

        } /* end while loop */

        /* Test gss_compare_name - client and server names should differ */
        maj_status = gsswrapper.gss_compare_name(min_status, 
                clientName, serverName, ret);
        if (ret[0] == 1) {
            tv.append("TEST:  clientName == serverName\n");
        } else {
            tv.append("TEST:  clientName != serverName\n");
        }

        /* Save our context */
        context = context_tmp;
        
        return 0;

    } /* end Authenticate() */

    private int Communicate(Socket clientSocket,
            InputStream serverIn, OutputStream serverOut)
    {
    	TextView tv = (TextView) findViewById(R.id.textView);
        
        long maj_status = 0;
        long[] min_status = {0};
        long[] qop_state = {0};
        int[] state = {0};
        int err = 0;
        gss_buffer_desc in_buf = new gss_buffer_desc("Hello Server!");
        gss_buffer_desc out_buf = new gss_buffer_desc();
        byte[] sigBlockBuffer = null;

        tv.append("Beginning communication with server\n");

        /* Sign and encrypt plain message */
        maj_status = gsswrapper.gss_wrap(min_status, context, 1, 
                GSS_C_QOP_DEFAULT, in_buf, 
                state, out_buf);

        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "wrapping message, gss_wrap", 
                              min_status, maj_status);
            return -1;
        } else if (state[0] == 0) {
            tv.append("Warning! Message not encrypted.\n");
        }

        /* Send wrapped message to server */
        byte[] temp_token = new byte[(int)out_buf.getLength()];
        temp_token = gsswrapper.getDescArray(out_buf);
        err = Util.WriteToken(serverOut, temp_token);
        if (err != 0) {
            tv.append("Error sending wrapped message to server, " +
                      "WriteToken\n");
            return -1;
        }
       
        /* Read signature block from the server */ 
        sigBlockBuffer = Util.ReadToken(serverIn); 

        if (sigBlockBuffer != null) {
            gsswrapper.setDescArray(out_buf, sigBlockBuffer);
            out_buf.setLength(sigBlockBuffer.length);
        }

        /* Verify signature block */
        maj_status = gsswrapper.gss_verify_mic(min_status, context,
                in_buf, out_buf, qop_state);

        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "verifying signature, gss_verify_mic", 
                              min_status, maj_status);
            return -1;
        } else {
            tv.append("Signature Verified\n");
        }
       
        gsswrapper.gss_release_buffer(min_status, in_buf);
        gsswrapper.gss_release_buffer(min_status, out_buf); 
        
        return 0;

    } /* end Communicate() */
    
    private int AltCommunicate(Socket clientSocket, 
            InputStream serverIn, OutputStream serverOut)
    {
    	TextView tv = (TextView) findViewById(R.id.textView);
        
        long maj_status = 0;
        long[] min_status = {0};
        int[] qop_state = {0};
        int[] state = {0};
        int err = 0;
        gss_buffer_desc in_buf = new gss_buffer_desc("Hello Server!");
        gss_buffer_desc out_buf = new gss_buffer_desc();
        byte[] sigBlockBuffer = null;

        gss_buffer_desc context_token = new gss_buffer_desc();

        /* Test context export/import functions */
        maj_status = gsswrapper.gss_export_sec_context(min_status,
                context, context_token);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "exporting security context", 
                              min_status, maj_status);
            return -1;
        } else {
            tv.append("Successfully exported security context\n");
        }

        maj_status = gsswrapper.gss_import_sec_context(min_status,
                context_token, context);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "importing security context", 
                              min_status, maj_status);
            return -1;
        } else {
            tv.append("Successfully imported security context\n");
        }
        gsswrapper.gss_release_buffer(min_status, context_token);

        /* Sign and encrypt plain message */
        maj_status = gsswrapper.gss_seal(min_status, context, 1, 
                GSS_C_QOP_DEFAULT, in_buf, 
                state, out_buf);

        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "wrapping message, gss_seal", 
                              min_status, maj_status);
            return -1;
        } else if (state[0] == 0) {
            tv.append("Warning!  Message not encrypted.\n");
        }

        /* Send wrapped message to server */
        byte[] temp_token = new byte[(int)out_buf.getLength()];
        temp_token = gsswrapper.getDescArray(out_buf);
        err = Util.WriteToken(serverOut, temp_token);
        if (err != 0) {
            tv.append("Error sending wrapped message to server, " +
                      "WriteToken\n");
            return -1;
        }
       
        /* Read signature block from the server */ 
        sigBlockBuffer = Util.ReadToken(serverIn); 

        if (sigBlockBuffer != null) {
            gsswrapper.setDescArray(out_buf, sigBlockBuffer);
            out_buf.setLength(sigBlockBuffer.length);
        }

        /* Verify signature block */
        maj_status = gsswrapper.gss_verify(min_status, context,
                in_buf, out_buf, qop_state);

        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "verifying signature, gss_verify", 
                              min_status, maj_status);
            return -1;
        } else {
            tv.append("Signature Verified\n");
        }
       
        gsswrapper.gss_release_buffer(min_status, in_buf);
        gsswrapper.gss_release_buffer(min_status, out_buf); 
        
        return 0;

    } /* end AltCommunicate() */

    private int PrintContextInfo() 
    {
    	TextView tv = (TextView) findViewById(R.id.textView);
        
        long maj_status = 0;
        long[] min_status = {0};
        gss_name_t_desc src_name = new gss_name_t_desc();
        gss_name_t_desc targ_name = new gss_name_t_desc();
        gss_buffer_desc sname = new gss_buffer_desc();
        gss_buffer_desc tname = new gss_buffer_desc();
        gss_buffer_desc oid_name = new gss_buffer_desc();
        gss_buffer_desc sasl_mech_name = new gss_buffer_desc();
        gss_buffer_desc mech_name = new gss_buffer_desc();
        gss_buffer_desc mech_description = new gss_buffer_desc();
        long lifetime[] = {0};
        long[] time_rec = {0};
        gss_OID_desc mechanism = new gss_OID_desc();
        gss_OID_desc name_type = new gss_OID_desc();
        gss_OID_desc oid = new gss_OID_desc();
        long context_flags[] = {0};
        int is_local[] = {0};
        int is_open[] = {0};
        gss_OID_set_desc mech_names = new gss_OID_set_desc();
        gss_OID_set_desc mech_attrs = new gss_OID_set_desc();
        gss_OID_set_desc known_attrs = new gss_OID_set_desc();

        /* Get context information */
        maj_status = gsswrapper.gss_inquire_context(min_status, context,
                src_name, targ_name, lifetime, mechanism, context_flags,
                is_local, is_open);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "Inquiring context:  gss_inquire_context", 
                              min_status, maj_status);
            return -1;
        }

        /* Check if our context is still valid */
        maj_status = gsswrapper.gss_context_time(min_status, context, 
                time_rec);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "checking for valid context", 
                              min_status, maj_status);
            return -1;
        }

        /* Get context source name */
        maj_status = gsswrapper.gss_display_name(min_status, 
                src_name, sname, name_type);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "Displaying source name:  gss_display_name", 
                              min_status, maj_status);
            return -1;
        }

        /* Get context target name */
        maj_status = gsswrapper.gss_display_name(min_status, 
                targ_name, tname, name_type);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "Displaying target name:  gss_display_name", 
                              min_status, maj_status);
            return -1;
        }

        tv.append("---------------------- Context Information -----------" +
                "-----------\n");
        tv.append("Context is valid for " + time_rec[0] 
                    + " seconds\n");
        tv.append(sname.getValue() + " to " + tname.getValue() + "\n");
        tv.append("Lifetime: " + lifetime[0] + " seconds\n");
        tv.append("Initiated: " + ((is_local[0] == 1) 
                    ? "locally\n" : "remotely\n"));
        tv.append("Status: " + ((is_open[0] == 1) 
                    ? "open\n" : "closed\n"));

        gsswrapper.gss_release_name(min_status, src_name);
        gsswrapper.gss_release_name(min_status, targ_name);
        gsswrapper.gss_release_buffer(min_status, sname);
        gsswrapper.gss_release_buffer(min_status, tname);

        maj_status = gsswrapper.gss_oid_to_str(min_status, 
                name_type, oid_name);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "Converting oid->string:  gss_oid_to_str", 
                              min_status, maj_status);
            return -1;
        }

        tv.append("Name type of source name: " + oid_name.getValue() + "\n");
        gsswrapper.gss_release_buffer(min_status, oid_name);

        /* Get mechanism attributes */
        maj_status = gsswrapper.gss_inquire_attrs_for_mech(min_status, 
                mechanism, mech_attrs, known_attrs);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "Inquiring mechanism attributes", 
                              min_status, maj_status);
            return -1;
        }
        tv.append("  Mechanism Attributes:\n");
        for (int j = 0; j < mech_attrs.getCount(); j++)
        {
            gss_buffer_desc name = new gss_buffer_desc();
            gss_buffer_desc short_desc = new gss_buffer_desc();
            gss_buffer_desc long_desc = new gss_buffer_desc();

            maj_status = gsswrapper.gss_display_mech_attr(min_status, 
                    mech_attrs.getElement(j), name, short_desc, long_desc);
            if (maj_status != GSS_S_COMPLETE) {
                Util.displayError(tv, "Displaying mechanism attributes", 
                                  min_status, maj_status);
                return -1;
            }
            tv.append("    " + name.getValue() + "\n");
            gsswrapper.gss_release_buffer(min_status, name);
            gsswrapper.gss_release_buffer(min_status, short_desc);
            gsswrapper.gss_release_buffer(min_status, long_desc);
        }

        /* Get known attributes */
        tv.append("  Known Attributes:\n");
        for (int k = 0; k < known_attrs.getCount(); k++)
        {
            gss_buffer_desc name = new gss_buffer_desc();
            gss_buffer_desc short_desc = new gss_buffer_desc();
            gss_buffer_desc long_desc = new gss_buffer_desc();

            maj_status = gsswrapper.gss_display_mech_attr(min_status,
                    known_attrs.getElement(k), name, short_desc, long_desc);
            if (maj_status != GSS_S_COMPLETE) {
                Util.displayError(tv, "Displaying known attributes", 
                                  min_status, maj_status);
                return -1;
            }
            tv.append("    " + name.getValue() + "\n");
            gsswrapper.gss_release_buffer(min_status, name);
            gsswrapper.gss_release_buffer(min_status, short_desc);
            gsswrapper.gss_release_buffer(min_status, long_desc);

        }
        gsswrapper.gss_release_oid_set(min_status, mech_attrs);
        gsswrapper.gss_release_oid_set(min_status, known_attrs);

        /* Get names supported by the mechanism */
        maj_status = gsswrapper.gss_inquire_names_for_mech(min_status, 
                mechanism, mech_names);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "Inquiring mech names", 
                              min_status, maj_status);
            return -1;
        }

        maj_status = gsswrapper.gss_oid_to_str(min_status, 
                mechanism, oid_name);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "Converting oid->string", 
                              min_status, maj_status);
            return -1;
        }

        tv.append("Mechanism " + oid_name.getValue() 
                + " supports " + mech_names.getCount() + " names\n");
        for (int i = 0; i < mech_names.getCount(); i++)
        {
            maj_status = gsswrapper.gss_oid_to_str(min_status, 
                    mech_names.getElement(i), oid_name);
            if (maj_status != GSS_S_COMPLETE) {
                Util.displayError(tv, "Converting oid->string", 
                                  min_status, maj_status);
                return -1;
            }
            tv.append("  " + i + ": " + oid_name.getValue() + "\n");
            gsswrapper.gss_release_buffer(min_status, oid_name);
        }
        gsswrapper.gss_release_oid_set(min_status, mech_names);

        /* Get SASL mech */
        maj_status = gsswrapper.gss_inquire_saslname_for_mech(min_status, 
                mechanism, sasl_mech_name, mech_name, mech_description);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "Inquiring SASL name", 
                              min_status, maj_status);
            return -1;
        }
        tv.append("SASL mech: " + sasl_mech_name.getValue() + "\n");
        tv.append("Mech name: " + mech_name.getValue() + "\n");
        tv.append("Mech desc: " + mech_description.getValue() + "\n");


        /* Inquire Mech for SASL name - to test */
        maj_status = gsswrapper.gss_inquire_mech_for_saslname(min_status,
                sasl_mech_name, oid);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "Inquiring mechs for SASL name", 
                              min_status, maj_status);
            return -1;
        }

        if (oid == GSS_C_NO_OID) {
            tv.append("Got different OID for mechanism\n");
        } 
        
        /* Determine largest message that can be wrapped,
           assuming max output token size of 100 (just for testing) */
        long[] max_size = {0};        
        maj_status = gsswrapper.gss_wrap_size_limit(min_status, context, 1,
                GSS_C_QOP_DEFAULT,
                100, max_size);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "determining largest wrapped message size",
                              min_status, maj_status);
            return -1;
        } else {
            tv.append("Largest message size able to be wrapped: " 
                      + max_size[0] + "\n");
        }

        gsswrapper.gss_release_buffer(min_status, sasl_mech_name);
        gsswrapper.gss_release_buffer(min_status, mech_name);
        gsswrapper.gss_release_buffer(min_status, mech_description);
        gsswrapper.gss_release_oid(min_status, oid);

        tv.append("--------------------------------------------------" +
                "-------------------------\n");

        return 0;
    }
    
    private int MiscFunctionTests() 
    {
    	TextView tv = (TextView) findViewById(R.id.textView);
        
        long maj_status = 0;
        long[] min_status = {0};
        gss_cred_id_t_desc cred_handle = GSS_C_NO_CREDENTIAL;
        /* kerberos v5 */
        gss_OID_desc mech_type = new gss_OID_desc("1.2.840.113554.1.2.2");
        gss_name_t_desc name = new gss_name_t_desc();
        long[] init_lifetime = {0};
        long[] accept_lifetime = {0};
        int[] cred_usage = {0};

        tv.append("--------------------------- MISC TESTS -------------" +
                "-------------\n");
        
        /* 
         * FUNCTION: gss_inquire_cred_by_mech
         * Get info about default cred for default security mech.
         */
        maj_status = gsswrapper.gss_inquire_cred_by_mech(min_status,
                cred_handle, mech_type, name, init_lifetime,
                accept_lifetime, cred_usage);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "inquiring credential info from mech", 
                              min_status, maj_status);
            return -1;
        } else {
            tv.append("Credential Principal Name: " 
                      + name.getExternal_name().getValue() + "\n");
            tv.append("Credential Valid for Initiating Contexts for " 
                      + init_lifetime[0] + " seconds\n");
            tv.append("Credential Valid for Accepting Contexts for " 
                      + accept_lifetime[0] + " seconds\n");
            tv.append("Credential Usage: " + cred_usage[0] + "\n");
        }

       
        /* FUNCTION: gss_pseudo_random */ 
        gss_buffer_desc prf_out = new gss_buffer_desc();
        gss_buffer_desc prf_in = new gss_buffer_desc("gss prf test");
        maj_status = gsswrapper.gss_pseudo_random(min_status,
                context, 0, prf_in, 19, prf_out);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "testing gss_pseudo_random function", 
                              min_status, maj_status);
            return -1;
        }

        gsswrapper.gss_release_buffer(min_status, prf_out);
        gsswrapper.gss_release_buffer(min_status, prf_in);

        /* FUNCTION: gss_indicate_mechs_by_attrs */
        gss_OID_set_desc desired_mech_attrs = GSS_C_NO_OID_SET;
        gss_OID_set_desc except_mech_attrs =  GSS_C_NO_OID_SET;
        gss_OID_set_desc critical_mech_attrs =  GSS_C_NO_OID_SET;
        gss_OID_set_desc mechs = new gss_OID_set_desc();
        maj_status = gsswrapper.gss_indicate_mechs_by_attrs(min_status,
                desired_mech_attrs, except_mech_attrs, critical_mech_attrs,
                mechs);
        if (maj_status != GSS_S_COMPLETE) {
            Util.displayError(tv, "gss_indicate_mechs_by_attrs", 
                              min_status, maj_status);
            return -1;
        }

        tv.append("---------------------------------------------------" +
                "--------------\n");

        return 0;
    }
}
