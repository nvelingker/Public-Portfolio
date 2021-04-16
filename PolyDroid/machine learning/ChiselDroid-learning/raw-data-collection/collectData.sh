#!/usr/local/bin/bash
# The user should pass in the name of the process as the first argument

UIHPATH=./uihstats
UIPATH=./uistats
NETPATH=./netstats
UIDPATH=./uid
USAGE="pass in the name of the target app as the first argument. e.g. io.plaidapp"

if [ $# -ne 1 ]; then
    echo $USAGE
    exit 2
fi 
 
echo "initializing paths"
DIR=${1//[-._]/}
mkdir -p $DIR
UIHPATH=./$DIR/uihstats
UIPATH=./$DIR/uistats
NETPATH=./$DIR/netstats
UIDPATH=./$DIR/uid

rm -f "$UIPATH"
rm -f "$NETPATH"
rm -f "$UIHPATH"
rm -f "$UIDPATH"

touch "$UIPATH"
touch "$NETPATH"
touch "$UIHPATH"
touch "$UIDPATH"

echo "press s key to stop collect data"
while true; do
  echo "saving UI snapshot"
  date +%s >> $UIHPATH
  # adb exec-out uiautomator dump /dev/tty >> $UIHPATH 
  
  read -t 0.5 -rsn1 input
  if [ "$input" = "s" ]; then
    # adb shell dumpsys gfxinfo "$1" framestats > $UIPATH
    # adb shell dumpsys netstats detail > $NETPATH
    # adb shell dumpsys package "$1" | grep userId > $UIDPATH
    echo "collect complete"
    break
  fi
done

