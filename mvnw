#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file to
# you under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup shell script, version 3.3.2
# ----------------------------------------------------------------------------

# If specified, validates the SHA-256 sum of the Maven distribution zip file.
distributionSha256Sum=""

set -e

# OS specific support.
darwin=false
case "$(uname)" in
  Darwin*) darwin=true ;;
esac

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
app_path=$0

# Need this for daisy-chained symlinks.
while
  APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing /; empty if no leading path
  [ -h "$app_path" ]
do
  ls=$( ls -ld "$app_path" )
  link=${ls#*' -> '}
  case $link in             #(
    /*)   app_path=$link ;; #( absolute
    *)    app_path=$APP_HOME$link ;; #( relative
  esac
done

# This is reliable if the script is invoked directly:
APP_HOME=$( cd "${APP_HOME:-./}" && pwd -P ) || exit

MAVEN_PROJECTBASEDIR=${MAVEN_BASEDIR:-"$APP_HOME"}

MVNW_VERBOSE=false
if [ "$MVNW_VERBOSE" = true ]; then
  echo $APP_HOME
fi

wrapperJarPath="$APP_HOME/.mvn/wrapper/maven-wrapper.jar"
wrapperUrl=$(grep -o 'wrapperUrl=.*' "$APP_HOME/.mvn/wrapper/maven-wrapper.properties" | cut -d= -f2-)

if [ ! -e "$wrapperJarPath" ]; then
  if command -v wget > /dev/null; then
    echo "Downloading $wrapperUrl"
    if [ "$MVNW_VERBOSE" = false ]; then
      wget -q -O "$wrapperJarPath" "$wrapperUrl" || rm -f "$wrapperJarPath"
    else
      wget -O "$wrapperJarPath" "$wrapperUrl" || rm -f "$wrapperJarPath"
    fi
  elif command -v curl > /dev/null; then
    echo "Downloading $wrapperUrl"
    if [ "$MVNW_VERBOSE" = false ]; then
      curl -q -o "$wrapperJarPath" "$wrapperUrl" || rm -f "$wrapperJarPath"
    else
      curl -o "$wrapperJarPath" "$wrapperUrl" || rm -f "$wrapperJarPath"
    fi
  else
    echo "ERROR: Couldn't find wget or curl. Can't download Maven Wrapper."
    exit 1
  fi
fi

exec "$JAVA_HOME/bin/java" \
  -classpath "$APP_HOME/.mvn/wrapper/maven-wrapper.jar" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
