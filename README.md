
# Kerberos Android NDK Sample Application #

This is a sample Android NDK application which provides a GUI wrapper
around the MIT Kerberos kinit, klist, kvno, and kdestroy client 
applications. It also provides a sample client which uses the Java 
GSS-API interface. The GSS-API interface is a Java interface for the existing 
native MIT GSS-API library.

This package includes cross-compiled versions of the 
[MIT Kerberos libraries](https://github.com/krb5/krb5) as well as the 
[CyaSSL Embedded SSL Library](http://www.wolfssl.com/yaSSL/Products-cyassl.html). 
It should be helpful to Android developers who are interested in using the 
Kerberos libraries or the GSS-API interface in their own Android NDK 
Applications.

By default, this package uses pre-built static Kerberos and CyaSSL 
libraries which are needed in order to be linked to the KerberosApp 
application's native library (libkerberosapp.so).

For detailed information on the Java GSS-API interface, please see the
GSSAPI_README file included in this project's root directory or see the 
[kerberos-java-gssapi](https://github.com/cconlon/kerberos-java-gssapi) project 
on GitHub.

##CONTENTS:##

1. [Requirements](#requirements)
2. [Building](#building)
3. [Installing](#installing)
4. [Usage](#usage)
5. [Default Storage Locations](#default-storage-locations)
6. [Library Versions](#library-versions)
7. [Licenses](#licenses)
8. [Support](#support)

##REQUIREMENTS:##

Before building or installing this package, you must have the Android SDK
and NDK installed and set up on your system. It is also helpful to have the
Android Emulator set up and configured with an Android platform greater
than or equal to version **2.3.3** (Gingerbread). For details on downloading and 
setting these up, please see the following links:

[Android SDK](http://developer.android.com/sdk/index.html)<br/>
[Android NDK](http://developer.android.com/sdk/ndk/index.html)<br/>
[Android Emulator](https://developer.android.com/guide/developing/tools/emulator.html)

SWIG will also need to be installed in order to build the underlying
GSS-API wrapper. To download and install SWIG, please visit see the project
homepage at [http://www.swig.org](http://www.swig.org). This project has been 
developed using SWIG version 2.0.11 running on Linux.

##BUILDING:##

To build and install this package, including Java GSS-API bindings, run
the following commands. 

		android update project -p . -s
		swig -java -package edu.mit.jgss.swig -outdir ./src/edu/mit/jgss/swig
			-o ./jni/gsswrapper_wrap.c ./jni/gsswrapper.i
		ndk-build
		ant debug

If you want to rebuild the pre-built Kerberos libraries, please use the
**android-config.sh** shell script. This will allow the MIT Kerberos libraries
to be cross-compiled for the Android platform. More detailed instructions 
can be found in the script comments.

##Installing:##

To install this package in a running Android emulator, run:

		ant <debug> install

Where <debug> is either "debug" or "release", depending on what build
configuration used with ndk-build.

Before running the KerberosApp application, the user needs to install both
a Keytab file and a Kerberos configuration file. Reference the MIT Kerberos
documentation for guidelines for creating these files. Once created, they
can be installed using the adb push command, using:

		adb push <local-file-path> <destination-file-path>

For example, to load a krb5.conf and krb5.keytab file from the current 
directory to an Android emulator under the /data/local/kerberos 
directory, run:

		adb push krb5.conf /data/local/kerberos/
		adb push krb5.keytab /data/local/kerberos/

If the application is set to use a client keytab instead of a password, the
keytab file needs to contain an entry for the client principal (whose
TGT will be obtained by using the "kinit" button in the sample Application).

####NOTE: hosts file####
----------------
If you need to edit the hosts file on the android emulator to accomidate for
KDC locations, use the following steps:

		emulator -avd <name> -partition-size 128
		adb remount
		adb pull /system/etc/hosts ./
		<< edit hosts file on local machine >>
		adb push ./hosts /system/etc

##Usage:##

This NDK application's GUI is split into three tabs:

1. Client Info
2. Server Info
3. Client App

These tabs should be addressed in the order they are listed above. A short
summary of each is below.

###1. Client Info###
---
This tab displays the wrappers around native kinit, klist, kvno, and kdestroy
application code. It provides the functionality to get a ticket for a given
client principal using either a keytab or password for principal
authentication. The default configuration file and credential cache locations
are listed on this screen as well.

###2. Server Info###
---
This tab allows the user to enter information about the server which the
client application will attempt to make a GSS-API connection with in Tab 3.
Server principal name, IP address, and port number should be given in
this tab.

###3. Client App###
---
This tab allows the user to start the client GSS-API appliation. The client
application will attempt to connect to the GSS-API server given in Tab 2, 
using the client principal info gathered in **Tab 1**. This client application
was designed to connect to the example server from the kerberos-java-gssapi
package. The client app will do the following:

a. Establish a GSS-API context with the example server
b. Sign, encrypt, and send a message to the server
c. Verify the signature block returned by the server

##Default Storage Locations:##

The following locations are the default paths used for the Kerberos
sample application.

		Kerberos config file:   /data/local/kerberos/krb5.conf
		Credentials cache:      /data/local/kerberos/ccache/krb5cc_<uid>
		Keytab:                 /data/local/kerberos/krb5.keytab

The Kerberos configuration file and credentials cache locations may be
changed in the KerberosAppActivity.java file. The configuration file
(krb5.conf) location is changed by altering the static defaultKRB5_CONFIG
variable. This is used in native JNI code to set the KRB5_CONFIG
environment variable. The credentials cache location is set in the
onCreate() method, specifically with the nativeSetKRB5CCNAME() method.

The Kerberos keytab file location may be changed by editing the default
locations in ./include/osconf.h when cross compiling the MIT kerberos
libraries. The default Kerberos configuration file (krb5.conf) location
may also be set in osconf.h before cross compiling the MIT Kerberos
libraries by modifying the DEFAULT_SECURE_PROFILE_PATH define.

##Library Versions:##

The CyaSSL and Kerberos libraries used in this package:

**CyaSSL 2.0.0rc3**   
[http://www.wolfssl.com](http://www.wolfssl.com)

**Kerberos (cconlon krb5 fork) GitHub master**<br/>
Repository: [http://github.com/cconlon/krb5](http://github.com/cconlon/krb5)<br/>
Homepage: [http://web.mit.edu/kerberos/](http://web.mit.edu/kerberos/)

##Licenses:##

####CyaSSL Embedded SSL Library

		* Copyright (C) 2006-2013 wolfSSL Inc.
		*
		* CyaSSL is free software; you can redistribute it and/or modify
		* it under the terms of the GNU General Public License as published by
		* the Free Software Foundation; either version 2 of the License, or
		* (at your option) any later version.
		*
		* CyaSSL is distributed in the hope that it will be useful,
		* but WITHOUT ANY WARRANTY; without even the implied warranty of
		* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
		* GNU General Public License for more details.
		*
		* You should have received a copy of the GNU General Public License
		* along with this program; if not, write to the Free Software
		* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA

 
####MIT Kerberos Libraries

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

##Support:##

If you have any questions or comments, please post to the [krbdev mailing
list](http://web.mit.edu/kerberos/mail-lists.html) or contact
**support@wolfssl.com**.

