---
layout: post
title: DevPod is Wonderful!!
author: Leonid Dubinsky
tags:
  - devpod
  - gcp
  - intellij
  - devcontainer
  - life-in-the-cloud
date: 2024-07-04
---
DevPod: the best way to run development workstations in the cloud (and outside of it), a way to set it up and some ideas on how to make it even better.

* TOC
{:toc}
## Introduction
 
Some time in 2021, I looked into developing programs in the cloud instead of my local machine. In the end, I stayed with my local machine, since cloud-based setup comparable in power to my desktop is too expensive, and affordable alternatives too slow - decision that I may reconsider if the prices go down and performance goes up ;)

Even though I normally do not need access to my development environment from anywhere other than my desk, there is something appealing about the cloud-based development environment - if using is is _convenient_; and here is another (real?) reason I did not switch to cloud-based development in 2021: it is very involved to set it up - and to use...

I perused various recipes that floated around at the time (for instance: [How I’ve slashed the cost of my DEV environments by 90%](https://itnext.io/how-ive-slashed-the-cost-of-my-dev-environments-by-90-9c1082ad1baf)) and came up with my own approach. My notes are in the [Appendix: The Manual Way](#the-manual-way); witness the number of GUI actions to perform, commands to run and unanswered questions mentioned ;)

Spinning up transient, fully configured cloud development environments _manually_ is just too clunky!

I _did_ look into approaches that automate away the complexity, like:
- [GitHub Codespaces](https://github.com/features/codespaces), which does not seem to support my IDE of choice, [IntelliJ Idea]() natively;
- [Google Cloud Workstations](https://cloud.google.com/workstations?hl=en), which, with the complexity of running a Kubernetes cluster, seems geared towards enterprises rather than individual developers;
- [GitPod](https://www.gitpod.io/)...
and stayed on my desktop ;)

In July 2024 I stumbled onto [DevPod](https://devpod.sh/) - and it is a game-changer!

TODO
- Open Source
- Client Only
- Unopinionated

## Google Cloud Platform Setup

TODO Submit a pull request documenting all this in the [provider code repository](https://github.com/loft-sh/devpod-provider-gcloud) - and open an issue to do this for the other providers.

To use `devpod` with a cloud provider, some resources need to be prepared in that provider. For the Google Cloud Platform (GCP), the prerequisites are:
### Project
Create a GCP _project_ for running DevPod workspaces.

_Enable_ `Compute Engine API` (`"compute"`) for the project.
### Service Account for devpod
Although it is possible to run `devpod` on your machine using your personal GCP account, [Principle of least privilege](https://en.wikipedia.org/wiki/Principle_of_least_privilege) dictates that a separate service account be used for this purpose - and so does GCP documentation: [Dedicated Service Accounts](https://cloud.google.com/iam/docs/best-practices-for-managing-service-account-keys#dedicated-service-accounts), [Using IAM Securely](https://cloud.google.com/iam/docs/using-iam-securely). So:

Create a _service account_ (`devpod`) and grant to it the following IAM _roles_ on the project:

| Role                              | Needed for                                     |
| --------------------------------- | ---------------------------------------------- |
| serviceusage.serviceUsageConsumer | Compute Engine billing                         |
| compute.instanceAdmin.v1          | Compute Engine instance operations             |
| iam.serviceAccountUser            | attaching service accounts to virtual machines |

### Service Account for Virtual Machines
If access to GCP services from _within_ the virtual machines is needed, create a _service account_ (`devpod-vm`) that will be attached to the virtual machines - and grant to it required IAM roles; see https://cloud.google.com/compute/docs/access/create-enable-service-accounts-for-instances.

If this is not needed, `iam.serviceAccountUser` role does not need to be granted to the `devpod` service account ;)

## Authentication
Application Default Credentials (ADC)
https://cloud.google.com/docs/authentication/application-default-credentials

TODO not global; no key for non-service account.

Generate, retrieve and stash the JSON _key_ for the account you use to run `devpod` on your machine; this can be done using [Google Cloud Console UI](http://console.cloud.google.com) - or with a `gcloud` CLI command:
```shell
$ gcloud iam service-accounts keys create \
  ./devpod.json 
  --iam-account=<service account email>
```

TODO
gcloud auth application-default login --impersonate-service-account SERVICE_ACCT_EMAIL

Set `GOOGLE_APPLICATION_CREDENTIALS` environment variable to the path to the key to the service account for running `devpod` on your machine (alternatively, `GCLOUD_JSON_AUTH` environment variable can be set to the JSON key itself?).

TODO file an issue
If started from the command line in a shell where `GOOGLE_APPLICATION_CREDENTIALS` environment variable is set correctly, DevPod GUI works for the `gcloud` provider - but not the workspaces created with it ;)

TODO file an issue
When a non-ssh GIT URL is used, but there is no GIT token in the environment, error message from the container does not make this clear...
## Context

TODO
Precedence:
- explicitly supplied command-line option;
- environment variable;
- option set permanently;
- option default - if one exists, in which case it needs to be specified both in the documentation and in the command-line tool's help
- ask the user

TODO
- motivation!
- traditional approach: application-scoped environment variables have the highest precedence and override corresponding "permanent" settings - see `GIT_`, `GCLOUD_*`, `GOOGLE_*`, etc.
- `devpod context`: no documentation, no UI
- everything scoped by
- I use [DirEnv](https://direnv.net/) `.envrc` files to set the environment variables.
- quote such a file
- supplying the context on each command is tedious and error-prone; using `devpod context use` is global; better approach would be `devpod` looking at `DEVPOD_CONTEXT` environment variable...
- `devpod-context-create`, `devpod-context-use`

TODO https://containers.dev/implementors/spec/#merge-logic
TODO devpod breaks when VPN is active...

## Provider: GCloud

It [turns out](https://github.com/loft-sh/devpod-provider-gcloud/issues/23) that setting environment variables does not work as a way to override provider options; this is by (in my opinion - unfortunate :)) design. The easiest way to set the options is to supply them when adding the provider:
```shell
devpod provider add gcloud \  
  -o PROJECT=<project> \  
  -o ZONE=<zone> \  
  -o SERVICE_ACCOUNT=<email of the service account for running VMs> \  
  -o MACHINE_TYPE="c2-standard-8" \  
  -o DISK_SIZE=50  
  
devpod provider options gcloud
devpod provider use gcloud
```

To reuse the same virtual machine for all workspaces:
```shell
$ devpod provider use gcloud --single-machine
```

To update the provider:
```shell
$ devpod provider update gcloud
```

TODO document this in the [provider code repository](https://github.com/loft-sh/devpod-provider-gcloud/pull/25) (and then - everywhere :)).

## Devcontainer

Code repository of the workspace should contain `.devcontainer/devcontainer.json` file;
for JVM projects, the minimum is:
```json
{
  "image": "mcr.microsoft.com/devcontainers/java:1-21"
}
```

If additional [features]( https://containers.dev/features) are needed:
```json
"features": {
  "ghcr.io/devcontainers/features/java:1": {},
  "ghcr.io/devcontainers/features/docker-in-docker:2": {}
}
```

To expose - say - Jekyll web  server running in the container to a local browser:
```json
"forwardPorts": [4000]
```

I am not sure that Java feature is needed when Java image is used, but with non-Java image (e.g., Jekyll), IntelliJ starts but displays an error:
> Unable to find JDK and set project SDK on current Docker image. Please change Docker image definition to proceed with project setup.

So it seems Java should be present in the container regardless ;)

IntelliJ has a [Devcontainer plugin](https://www.jetbrains.com/help/idea/connect-to-devcontainer.html). JetBrains also seems to be actively working on defining customizations specific to its IDEs.

TODO "works" in #1 - will Idea eventually self-apply its customizations directly from  `devcontainer.json`?

## IDE: IntelliJ

Until this [issue](https://github.com/loft-sh/devpod/issues/1153) is resolved, the way I install plugins in IntelliJ is by running the following script with the GIT repository URL as parameter:
```shell
PLUGIN_SCALA="org.intellij.scala"  
PLUGIN_DRACULA_THEME="com.vermouthx.idea"  
PLUGINS="$PLUGIN_SCALA $PLUGIN_DRACULA_THEME"  
  
REPOSITORY_URL=$1  
  
# create workspace but do not start the IDE;  
# use SSH to check out the repository  
devpod up $REPOSITORY_URL --ide intellij --open-ide=false 
  
# install plugins - works, but devpod prints:
#   Error tunneling to container:
#   wait: remote command exited without exit status or exit signal
# see https://www.jetbrains.com/help/idea/work-inside-remote-project.html#plugins
REPO=`echo $REPOSITORY_URL | awk -F/ '{print $NF}'`
WORKSPACE=`basename $REPO .git`
COMMAND="/home/vscode/.cache/JetBrains/RemoteDev/dist/intellij/bin/remote-dev-server.sh"
devpod ssh $WORKSPACE --command "$COMMAND installPlugins /workspaces/$WORKSPACE $PLUGINS"
```

**TODO does not work!**

TODO document this in the [code repository](https://github.com/loft-sh/devpod).

TODO does DevPod set IntelliJ options correctly or do I need to override the memory limits? `devpod up ... --ide-option`? Idea command line options; `devops` facility to transfer IDE configuration files and run commands... dot-files?

TODO Ideally, there should be a way to add personal stuff to the `devcontainer.json`!

## Workspace

To start the IDE and connect to it:
```shell
$ devpod up <workspace>
```

To recreate workspace to reflect all changes to its configuration:
```shell
$ devpod up <workspace> --recreate
```
TODO Does it update the IDE?

Secrets and other environment variables can be made available within the workspace by putting them into a `key=value` file(s) and running:
```shell
$ devpod up <workspace> --workspace-env-file ... 
```

Even though my SSH key is on a Yubikey token, since `devpod` sets up SSH agent forwarding, it works for `git push` in the workspace! Straight SSH from the workspace works only with explicitly supplied user name, since the `USER` in the workspace is `vscode`; I guess GitHub ignores the username and just looks at the key ;)

(SSH key can be specified to the `devpod up` command using a hidden `ssh-key` option - but I do not see any reason to...)

## Machine

Until this [issue](https://github.com/loft-sh/devpod-provider-gcloud/issues/21) is resolved, machines do not stop automatically after configured inactivity period elapses, and need to be stopped manually with `devpod machine stop <name>`!

Until this [issue](https://github.com/loft-sh/devpod/issues/1152) is resolved, the only way to verify what values are actually used by a virtual machine instance is to use a non-`devpod` command specific to the provider, for instance, for GCloud:
```shell
$ gcloud compute instances describe devpod-<machine name> --project=<project> --zone=<zone>
```
where:
- `<machine name>` is copied from the output of `devpod list` (note that `devpod-` has to be pre-pended to it);
- `<project>` is the PROJECT that was used when the machine was created;
- `<zone>` is the ZONE that was used when the machine was created.

## Appendix

### The Manual Way
#### Set Up Virtual Machine
```shell
# Project
$ gcloud auth login <admin>@<domain>.org
$ gcloud projects create <project>
$ gcloud config set project <project>
$ gcloud config set compute/region us-east1
$ gcloud config set compute/zone us-east1-b

# Compute
## in GCP console, enable billing
$ gcloud services enable compute.googleapis.com # enable Compute Engine
$ gcloud compute project-info add-metadata --metadata enable-oslogin=TRUE
$ gcloud compute project-info describe

# SSH Key
$ gcloud compute os-login ssh-keys add --key-file=<ssh key>.pub --project=<project>
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
$ sudo dnf -y install wget git mc java-11-openjdk
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
```

#### Set Up JetBrains Gateway
```shell
# install Dracula theme on the gateway client

# VM options are in ~/.cache/JetBrains/RemoteDev/dist/.../bin/idea64.vmoptions
# script      is in ~/.cache/JetBrains/RemoteDev/dist/.../bin/remote-dev-server.sh
$ .../remote-dev-server.sh --help

# install Scala plugin on the host:
$ .../remote-dev-server.sh installPlugins ~/Projects/run org.intellij.scala

# server-to-client workflow:
$ .../remote-dev-server.sh run <path/to/project> --ssh-link-host <host>
```

### The DevPod Way

```shell
$ gcloud auth login <EMAIL>
$ gcloud auth application-default login

# set up GCloud project, services and roles (once)
$ gcloud projects create <PROJECT ID>
$ gcloud services enable compute --project <PROJECT ID>
$ gcloud projects add-iam-policy-binding <PROJECT ID> --member=user:<EMAIL> --role=roles/compute.instanceAdmin.v1
$ gcloud projects add-iam-policy-binding <PROJECT ID> --member=user:<EMAIL> --role=roles/serviceusage.serviceUsageConsumer

# set up GCloud DevPod provider (once)
$ devpod provider add gcloud -o PROJECT=<PROJECT ID> -o ZONE=<ZONE>

# spin up a workspace
$ devpod up <GIT REPOSITORY URL>
```

### Speed and Price

```
OpenTorah local: Gradle build ~2 min; Idea: 12 sec

type           CPUs  Gb  Gradle     Idea         $/month
                                    mem  time
e2-standard-2   2     8  ~5.5 min                $ 49
e2-highcpu-4    4     4  ~4   min   ~70%         $ 74
e2-highcpu-8    8     8  ~3   min   ~40% 23s     $142
e2-standard-4   4    16                          $ 98
e2-standard-8   8    32                          $196
e2-highcpu-16  16    16                          $289
```

TODO re-test with `e2` and `c4` machines...