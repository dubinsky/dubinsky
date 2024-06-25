---
title: Cloud Development
tags: [computer]
---
Create a VM on Google Cloud Platform

Inspiration: https://itnext.io/how-ive-slashed-the-cost-of-my-dev-environments-by-90-9c1082ad1baf

Setup
```shell
# Project
$ gcloud auth list
$ gcloud auth login dub@opentorah.org
$ gcloud projects list
$ gcloud projects create intellij-cloud
$ gcloud config set project intellij-cloud
$ gcloud config set compute/region us-east1
$ gcloud config set compute/zone us-east1-b

# Compute
## in GCP console, enable billing
$ gcloud services list --available
$ gcloud services enable compute.googleapis.com # enable Compute Engine
$ gcloud compute project-info add-metadata --metadata enable-oslogin=TRUE
$ gcloud compute project-info describe

# SSH Key
$ gcloud compute os-login ssh-keys add --key-file=dublo.pub --project=intellij-cloud
# in the BROWSER!!!, in API  Explorer, use Directory to set POSIX user name and home directory
# https://developers.google.com/admin-sdk/directory/reference/rest/v1/users/update
$ gcloud compute os-login describe-profile

# Static IP Address
# QUESTION: maybe use dynamic DNS update client instead?
# or instance options --hostname=box.podval.org --public-ptr --public-ptr-domain [DOMAIN_NAME]
$ gcloud compute addresses create box-ip --network-tier=STANDARD
$ gcloud compute addresses describe box-ip

$ BOX_IP=$(gcloud compute addresses list \
 --filter="name:box-ip" \
 --format="value(address_range())"
 )
$ echo $BOX_IP

# startup-script:
$ sudo dnf -y install wget git mc java-11-openjdk  # *not* -headless because FOP
$ sudo dnf -y install xorg-x11-server-Xwayland xorg-x11-xauth libXrender libXtst fontconfig
$ sudo dnf -y update

# see results:
$ sudo journalctl -u google-startup-scripts.service
# re-run:
$ sudo google_metadata_script_runner startup

# Create VM instance (in ~/box) until I switch to --metadata start-script-url=...
$ gcloud compute instances create box \
 --network-tier=STANDARD \
 --address=$BOX_IP \
 --subnet=default \
 --machine-type=e2-highcpu-8 \
 --image-project "rocky-linux-cloud" \
 --image-family "rocky-linux-8" \
 --boot-disk-size=20GB \
 --boot-disk-type=pd-standard \
 --boot-disk-device-name=box \
 --disk=auto-delete=no,name=home,device-name=home \
 --metadata-from-file startup-script=startup-script.sh

$ gcloud compute instances list
$ gcloud compute instances describe|start|stop|delete box

# when not running:
$ gcloud compute instances set-machine-type box --machine-type <type>

OpenTorah local: Gradle build ~2 min; Idea: 12 sec

type           CPUs  Gb  Gradle     Idea         $/month
                                    mem  time
e2-standard-2   2     8  ~5.5 min                $ 49
e2-highcpu-4    4     4  ~4   min   ~70%         $ 74
e2-highcpu-8    8     8  ~3   min   ~40% 23s     $142
e2-standard-4   4    16                          $ 98
e2-standard-8   8    32                          $196
e2-highcpu-16  16    16                          $289

# In ~/.ssh/config:
Host box
  HostName <IP_ADDRESS_DEV_MACHINE>
  User dub
  ForwardAgent yes

# /home
$ gcloud compute disks create home --size 20GB --type pd-standard
$ gcloud compute instances attach-disk box --disk home
# resize
$ gcloud compute disks resize home --size 20GB


## in the box:
$ lsblk
# format - when new :)
$ sudo mkfs.ext4 -m 0 -E lazy_itable_init=0,lazy_journal_init=0,discard /dev/sdb
# mount
$ sudo mkdir /mnt/home
$ sudo mount -o discard,defaults /dev/sdb /mnt/home
$ sudo blkid /dev/sdb
## add to /etc/fstab: UUID=UUID_VALUE /mnt/home ext4 discard,defaults,nofail 0 2
## link /mnt/home into /home

# resize
$ sudo df -Th
$ sudo lsblk
$ sudo resize2fs /dev/sdb

# user configuration:
$ git config --global user.name "Leonid Dubinsky"
$ git config --global user.email "dub@podval.org"
OpenTorah $ git config -f ./.gitconfig user.email "dub@opentorah.org"
```

JetBrains Gateway
```shell
# SSH connection:
# - fails to connect using the PIV SSH key from my Yubikey...
# - even with a key in a file, connection fails when host name from the .ssh/config is used (but only the real connection; test connection in the full configuration dialog works).
# - on modern Linux distributions that the gateway targets, ssh-rsa is disabled;
#   command-line ssh client login works, but not the gateway's - unless
#   on the server ssh-rsa is added to the PubkeyAcceptedKeyTypes line in the
# /etc/crypto-policies/back-ends/opensshserver.config

PubkeyAcceptedKeyTypes ecdsa-sha2-nistp256,ecdsa-sha2-nistp256-cert-v01@openssh.com,sk-ecdsa-sha2-nistp256@openssh.com,sk-ecdsa-sha2-nistp256-cert-v01@openssh.com,ecdsa-sha2-nistp384,ecdsa-sha2-nistp384-cert-v01@openssh.com,ecdsa-sha2-nistp521,ecdsa-sha2-nistp521-cert-v01@openssh.com,ssh-ed25519,ssh-ed25519-cert-v01@openssh.com,sk-ssh-ed25519@openssh.com,sk-ssh-ed25519-cert-v01@openssh.com,rsa-sha2-256,rsa-sha2-256-cert-v01@openssh.com,rsa-sha2-512,rsa-sha2-512-cert-v01@openssh.com
Then restart your sshd

# install Dracula theme on the gateway client

# VM options are in ~/.cache/JetBrains/RemoteDev/dist/.../bin/idea64.vmoptions
# script      is in ~/.cache/JetBrains/RemoteDev/dist/.../bin/remote-dev-server.sh
$ .../remote-dev-server.sh --help

# install Scala plugin on the host:
$ .../remote-dev-server.sh installPlugins ~/Projects/run org.intellij.scala

# server-to-client workflow:
$ .../remote-dev-server.sh run <path/to/project> --ssh-link-host <host>
```
