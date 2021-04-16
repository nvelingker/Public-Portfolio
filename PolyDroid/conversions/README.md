# android-debloater

-- TO CHANGE VERSIONS --
use command : python3 manager.py app_name version_number

-- TO CREATE PATCH FILE --
to use the diff generation tool, you should have two versions of an app that you want to find the diff of. 
for example, say you have App_Version0 and App_Version2 and you've changed res/activity.xml and main/fragment.java

the generalized command :
python3 diff_generator.py app_name version_orig version_new filepath_orig filepath_new filepath_orig filepath_new

use example command : 
python3 diff_generator.py App 0 2 ../../apps/App_Version0/res/activity.xml ../../apps/App_Version2/res/activity.xml ../../apps/App_Version1/main/fragment.java ../../apps/App_Version1/main/fragment.java

Keep in mind that one of your versions should always be the original version 0, since we will always alternate between 0 and a modified version in the program
