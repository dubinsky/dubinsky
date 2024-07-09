---
layout: post
title: "Yubikey: 2-factor authentication (U2F, OATH and OTP) and SSH"
date: 2015-01-14T20:15:00.000-05:00
author: Leonid Dubinsky
tags:
  - authentication
  - linux
  - life-in-the-cloud
modified_time: 2018-11-11T03:44:06.602-05:00
blogger_id: tag:blogger.com,1999:blog-8681083740214020499.post-3229372978474069876
blogger_orig_url: https://blog.dub.podval.org/2015/01/yubikey-neo-2-factor-authentication-u2f.html
---
* TOC
{:toc}

## Why Yubikey ##

I've been using 2-factor authentication with my Google account for a few years: when logging in, in addition to my
password, I need to enter a code that an application on my phone displays.

Recently Google introduced an alternative to the codes (for Chrome users): a Yubikey USB token that supports
[U2F](http://fidoalliance.org/adoption/video/yubico-fido-alliance-universal-2nd-factor-u2f-demonstration) -
Universal 2nd Factor - protocol. That sounded cool, and I decided to get me one :)

There is no new information in this post; everything is already available on the Internet - and probably in more popular
and easier to find places :)   Consider this as a note to self - so that I know what to do next time :)

[Yubico](https://www.yubico.com/) has different tokens, some of which support U2F, but
[Yubikey Neo](http://www.amazon.com/Yubico-Y-072-YubiKey-NEO/dp/B00LX8KZZ8/) supports everything other tokens do - plus
NFC, so it can be used with the smartphones (if they are
[not from Apple](https://www.yubico.com/2014/09/neo-work-iphone-6-nfc/) :)).

Yubikey Neo is also a SmartCard, which comes with OATH and GPG applets installed, and seems to allow applet installation.
This is important, since firmware on Yubico tokens is not upgradeable, but on Yubikey Neo new functionality can be
deployed via applets. Hopefully, that means that I won't have to replace the $50 Yubikey Neo every time Yubico releases
new firmware :)

Recently, I bought myself a Yubikey 4C, which doesn't have NFC, but it plugs directly into my phone's USB-C port, so
that's ok.

### udev ###

It seems that recent Linux releases recognize the token out of the box, but custom udev rules are needed to allow
non-root users accessing the USB device representing the YubiKey (which is needed to use the smart-card functionality).

On current Fedora releases, you need to install package "ykpers" (or "yubikey-personalization-gui" that drags it in);
it puts appropriate udev rules in `/lib/udev/rules.d/69-yubikey.rules`. Reboot or force udev daemon to reload the rules
in some other way.

### Enabling yubikey features ###

Not all features are enabled initially; to turn the Yubikey into “OTP/U2F/CCID composite device” issue command (from the
 “ykpers” package that you already installed):
```
  $ ykpersonalize -m6
``` 
(Adding 80 to the mode activates an “eject flag” that I do not think I need.)

U2F is currently far from being universally supported, so for different sites different approaches to security
enhancements have to be used.

## Yubikey Authenticator ##

Yubikey provides two "slots", both of which can be used for OATH credentials. Length of the tap on the token determines
which one the user intends. Although convenient - the user doesn't need to copy codes from the phone - this approach is
limited to 2 distinct OATH credentials (also, varying-length taps are weird :)).

OATH applet installed on Yubikey Neo can be used to store many more than 2 credentials.

To use it, standard Google Authenticator application needs to be replaced by Yubikey Authenticator. Just like the
Google's one, it shows codes that the user has to enter when logging into a site, but unlike Google one, nothing is
stored on the phone: Yubikey Neo has to be tapped to the phone to see the codes (or add credentials). Yubikey 4C needs
to be plugged into the phone, since it doesn't support NFC. (Direct plugging of Yubikey wasn't supported on early
versions of the Yubikey Authenticator; it is now.)

2-factor authentication of this nature can be used with sites that support it but do not (yet) support U2F - which used
to include, for instance, GitHub and WordPress (it seems that Github does support U2F now).

Inconvenience of entering codes manually can theoretically be alleviated by the
[yubioath-desktop](https://github.com/Yubico/yubioath-desktop) application, which in practice didn't work for me.

## Password Manager ##

For sites that do not support U2F or codes-based 2-factor authentication, the only line of defence is the password, so
it needs to be strong - which means, long, weird, unique - and hard to remember.

While we are waiting for U2F to become universal so that we can go back to having the same short and easy to remember
password on all the sites, strong passwords are only practical if you use a password manager - a service that stores
your passwords, detects when you are logging into a site it knows about, and supplies the password.

I stored login information about the sites I
frequent in the password manager, drained passwords from the browser's built-in password manager and told the browser to stop collecting
my passwords.

When all the passwords are stored in a password manager, the question arises: how secure is the password manager itself? Current password managers support multiple flavors of 2-factor authentication, including the Authenticator codes - and the OTP (one-time passwords) that Yubikey supports directly.

Usually, Yubikey support requires a for paid accounts, but the price is reasonable: around $40/year for a family. Multiple tokens can be registered for one account, to allow for the recovery in case the token is lost. One can get a (cheaper) OTP-only Yubico token for this purpose - or get Yubike Neo/4C tokens for the family members and
cross-register them :)

Originally, I chose [LastPass](https://www.lastpass.com/) as the password manager because of raving reviews; later, after a few security breaches in LastPass, I switched to [BitWarden](https://bitwarden.com/).

## SSH ##

Remaining piece of the puzzle: SSH. I use SSH to log into my home server remotely. I also use it to push code into my
GitHub repositories.

I want to store my private SSH key on the Yubikey. With this configuration, I do not need my private key on the machine
I open SSH connection from, which increases my feeling of security. On top of that, the key can be deleted from any
machine that I ssh into and then open further ssh connection from! All it takes is - add `ForwardAgent yes` in
`~./ssh/config` for the particular host! This is really cool!

## Yubikey as a PIV Smart Card ##

I do not know how did I overlook the PIV functionality of the Yubikey and decided to use GPG instead (see below), but
here is the current state of the PIV support.

To figure out how to set this up, I used
[Yubikey Handbook](https://ruimarinho.gitbooks.io/yubikey-handbook/content/ssh/authenticating-ssh-with-piv-and-pkcs11-client/)
and Yubico [documentation](https://developers.yubico.com/yubico-piv-tool/).

We need the smart-card demon installed and running (as far as I can tell, it doesn't interfere with the GPG
functionality any more):
```
  # dnf install pcsc-lite opensc yubico-piv-tool
  # systemctl enable pcscd; systemctl start pcscd
  $ opensc-tool -l
```
Change PIN and PUK:
```
  $ yubico-piv-tool -a change-pin   # from default 123456
  $ yubico-piv-yool -a change-puk   # from default 12345678
```
Default Management Key is: `010203040506070801020304050607080102030405060708`

(It is possible to generate the private key directly on the Yubikey:
```
  $ yubico-piv-tool -s 9a -a generate -o key.pub.pem
```
but I prefer to do it on the outside, so that I can put the same private key onto multiple tokens, stash it somewhere
etc.)

Generate the keypair in the current directory:
```
  $ ssh-keygen -f key
```
Export the public key in PEM format (known to ssh-keygen as PKCS8, not PEM :)):
```
  $ ssh-keygen -f key -e -m PKCS8 > key.pub.pem
```
Import the key:
```
  $ yubico-piv-tool -s 9a -a import-key -i key
```
Certify the key (for one year):
```
  $ yubico-piv-tool -s 9a -a verify-pin -a selfsign-certificate -S '/CN=login@domain SSH key' -i key.pub.pem -o cert.pem
```
If you get `error: Failed to begin pcsc transaction, rc=80100003` see Temporary Detour below :)

Import the certificate:
```
  $ yubico-piv-tool -s 9a -a import-certificate -i cert.pem
```
Check status:
```
  $ yubico-piv-tool -a status
```
Export public key from the token in OpenSSH format:
```
  $ ssh-keygen -D /usr/lib64/opensc-pkcs11.so -e
```
To use the key from the token, add to  `~/.ssh/config` (for specific hosts or globally):
```
  PKCS11Provider /usr/lib64/opensc-pkcs11.so
```
This way, SSH agent is running, but identity isn't added to it - and so it can't be reused on machines SSHed into; to
add Yubikey SSH identity to SSH agent:
```
  $ ssh-add -s /usr/lib64/opensc-pkcs11.so
```
See it:
```
  $ ssh-add -L
```   

Running gpg-agent (needed for the GPG approach below) may cause problems.

When Yubikey is removed and re-inserted, ssh-agent needs to be killed and the key re-added for some reason. I use this
alias in `~./bashrc:`
```
  alias yk='pkill ssh-agent; pkill gpg-agent; ssh-add -s /usr/lib64/opensc-pkcs11.so'
```
**QUESTIONS:**
- Is there a way to avoid this?
- How to hook graphical pin-entry into this flow?
- How to make the identity from the token default one?

## Yubikey as a GPG Smart Card ##

Yubikey has a GPG Applet that can be used to store GPG keys. It doesn’t seem to be possible to turn an existing SSH key
into a GPG key, but it is possible to use a GPG key for SSH authentication, and that is what I decided to do.

Since I do not use GPG for email, I did not have a GPG key. It is possible to generate the key on the Neo itself, but it
is impossible to extract the key from it (for obvious reasons). I want to have a backup of the key (to stash in a safe
place), so I needed to generate the key in a normal way and put it on the Neo.

There is a lot of stuff out there about the gpg-agent configuration, ssh support, sshcontrol, keygrips etc.
This [document](https://github.com/herlo/ssh-gpg-smartcard-config/blob/master/YubiKey_NEO.rst) by
[Aric Gardner](https://github.com/Aricg) helped me get the things running.
  
### Configure GPG to use agent ###

Make sure file `~/.gnupg/gpg.conf` exists and contains a line `use-agent`.

Make sure file `~/.gnupg/gpg-agent.conf` exists and contains a line `enable-ssh-support`.

Make sure file `~/.gnupg/scdaemon.conf` exists and contains a line `allow-admin`.

### Ensure GPG agent starts ###

Put into `.bashrc`:
```
  if [ ! -f /tmp/gpg-agent.env ]; then
      killall gpg-agent;
      eval $(gpg-agent --daemon --enable-ssh-support > /tmp/gpg-agent.env);
  fi
  . /tmp/gpg-agent.env
```

### Generate Key and Subkeys ###
  
When generating the key, I did not add a photo to it, nor did I publish it to the key servers. People who actually use
GPG would know how to do all that :)

To generate the key:
```
  $ gpg2 --gen-key
```
I picked “RSA and RSA (default)”, 4096 bits long key, key does not expire.
  
To create a backup of the generated key XXXXXX:
```
  $ gpg2 --armor --export XXXXXX > XXXXXX-master.txt
```  
To generate subkeys:
```
  $ gpg2 --expert --key-edit XXXXXX
```
In gpg2, I issued command “adkey”, chose “RSA (set your own capabilities)” and 2048 bits long key (Neo can't store keys
longer than that).

Although only authentication key is needed for the SSH logins, I generated subkeys of each flavor: signing, encryption
and authentication.
  
To create backups:
```
  $ gpg2 --armor --export XXXXXX > XXXXXX-master-with-subkeys.txt
  $ gpg2 --armor --export-secret-keys XXXXXX > XXXXXX-secret-keys.txt
  $ gpg2 --armor --export-secret-subkeys XXXXXX > XXXXXX-secret-subkeys.txt
```
I am sure that not all the backups are needed, but so far did not learn enough about the subject to figure out which are
not ;)

### Set Up Yubikey GPG SmartCard ###
  
To set up the GPG SmartCard on the Neo, I did:
```
  $ gpg2 --card-edit
```    
Enable admin commands with “admin”; use “passwd” to change the Admin PIN from default 12345678 and PIN from default
123456; use “name”, “lang”, “sex” and “login” to set the card’s parameters.

### Move Keys onto the Card ###
```  
  $ gpg2 --expert --key-edit XXXXXX
```
  
Switch to secret keys with the “toggle” command.

Select the key to move to the card using “key n” commands (toggles the marker on subkey n).

Use “keytocard” command to move the marked key into appropriate slot on the card.

### It Works! ###

When the smart card is connected, “ssh-add -L” should output the public key corresponding to the authentication key from
the card (no need to insert any "keygrips" into the sshcontrol file :)). It needs to be listed on the machine you are
logging into :)

I never had to do this before, but on fc28 in order for the ssh key to work, I have to do (see
[https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=835394](https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=835394)):
```
  $ gpg-connect-agent updatestartuptty /bye
```
If the public key is published somewhere, GPG applet on the token can be configured with the URL so that it can fetch
the key. On a machine where GPG doesn't know about my the key, I used "fetch" in gpg2 --card-edit.

## Obsolete ##

Previously, additional steps were required to set things up. In recent releases, some of them are no longer needed - but
I preserved previous instructions here just in case :)

### udev ###

Previously, I had to manually install the following udev rule:
```
  ACTION=="add|change"
  SUBSYSTEM=="usb",
  ATTRS{idVendor}=="1050",
  ATTRS{idProduct}=="0010|0110|0111|0113|0114|0115|0116|0120|0401|0402|0403|0405|0406|0407|0410",
  TAG+="uaccess"
```
Above rule works for Fedora, but not for Debian-based Raspbian running on my Raspberry Pi. There, instead of TAG, I use
`MODE:="0666"`. It is ugly, and only works with "final" assignment (:=), but it works...

### Install GnuPG2 ###

Both version 1 (gpg) and version 2 (gpg2) of the Gnu Privacy Guard can be used for the key generation, but only gpg2 is
capable of writing the keys to the Neo. Previously, I had to make sure that the following packages are installed:
gnupg2, gnupg2-smime (contains scdaemon, which is needed) and pinentry-gtk (GTK UI for PIN entry) (on Raspbian, I had
to install packages gnupg2, gnupg-agent and scdaemon).

### Uninstall pcsc-lite ###

There is some kind of a race for the card access between GPG and pcsc-lite (which is installed by default). Previously,
I removed pcsc-lite, pcsc-lite-libs, pcsc-lite-ccid, pcsc-perl and dependent packages: opensc, coolkey, openconnect,
NetworkManager-openconnect, NetworkManager-openconnect-gnome. I hope this gets resolved soon, and package
uninstallation won't be necessary.

### Disable Gnome keyring agent ###
  
On Gnome desktop (Fedora, not Raspberry Pi) gnome-keyring-daemon used to interfere with the gpg-agent and had to be
disabled before smart-card applet on the Neo could be used:

```shell
  $ if [​[ $(gconftool-2 --get /apps/gnome-keyring/daemon-components/ssh) != "false" ]]; then
        gconftool-2 --type bool --set /apps/gnome-keyring/daemon-components/ssh false
    fi
```
