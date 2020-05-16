

#### JSCH bug with openssh new format
JSCH does not support new *ed25519* kind keys (OpenSsh 7.8 and above):  
https://stackoverflow.com/questions/53134212/invalid-privatekey-when-using-jsch/55740276#55740276
https://sourceforge.net/p/jsch/bugs/129/  
GitHub does not support old ones: 
![pic](GitHub_does_not_support_old_format_ssh_keys.png)  
So will have to use https with user/password scheme.