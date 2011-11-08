#! /bin/bash

#
# Program : android-configure.sh
# Author  : Chris Conlon, yaSSL (www.yassl.com)
#
# Date    : October 5, 2011
#
# Description: This script will configure the MIT Kerberos library
#              for cross-compilation by the Android NDK stand-
#              alone toolchain.
#
# Instructions:
#   1) Download, install, and set up Android NDK standalone
#      toolchain.
#   2) Place this script in the /src directory of the kerberos
#      source directory.
#   3) Run ./autoconf if needed
#   4) Run ./android-configure.sh
#   5) Remove pkinit plugin:
#      rm -r ./plugins/preauth/pkinit
#   6) Run make
#   7) Install in desired location:
#      make DESTDIR=<staging/path/here> install
#   8) Copy built libraries from staging location
#      to desired Android project.
#

## Add Android NDK Cross Compile toolchain to path
export PATH=/Users/chrisc/android/toolchains/android-9-toolchain/bin:$PATH

## Set up variables to point to Cross-Compile tools
export CCBIN="/Users/chrisc/android/toolchains/android-9-toolchain/bin"
export CCTOOL="$CCBIN/arm-linux-androideabi-"

## Export our ARM/Android NDK Cross-Compile tools
export CC="${CCTOOL}gcc"
export CXX="${CCTOOL}g++"
export NM="${CCTOOL}nm"
export STRIP="${CCTOOL}strip"
export RANLIB="${CCTOOL}ranlib"
export AR="${CCTOOL}ar"
export LD="${CCTOOL}ld"

## Set up proper FLAGS
export CYASSL_LIB="/Users/chrisc/android/projects/cyassl-android-ndk/obj/local/armeabi"
export CYASSL_INC="/Users/chrisc/android/projects/cyassl-android-ndk/jni/cyassl/include"
export LDFLAGS="-L$CYASSL_LIB -lm"
export CFLAGS="-I$CYASSL_INC -DANDROID"

## Configure the library

ac_cv_func_malloc_0_nonnull=yes ac_cv_func_realloc_0_nonnull=yes krb5_cv_attr_constructor_destructor=yes ac_cv_func_regcomp=yes ac_cv_printf_positional=no ./configure --target=arm-linux-androideabi --host=arm-linux-androideabi --enable-static --disable-shared --with-crypto-impl=cyassl --with-prng-alg=os

sed -i '' 's/#define KRB5_DNS_LOOKUP 1/#undef KRB5_DNS_LOOKUP/g' include/autoconf.h

