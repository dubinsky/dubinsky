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
DevPod: the best way to run development workstations in the cloud (and outside of it), a way to set it up, and some ideas on how to make it even better.

* TOC
{:toc}
## Introduction
 
Some time in 2021, I looked into developing software in the cloud instead of on my local machine. In the end, I stayed with my local machine, since cloud-based setup comparable in power to my desktop is too expensive, and affordable alternatives too slow - decision that I may reconsider if the prices go down and performance goes up ;)

Even though I normally do not need access to my development environment from anywhere other than my desk, there is something appealing about the cloud-based development environment - if using is is _convenient_.

The main reason I did not switch to cloud-based development in 2021: it is not convenient; it is very involved to set it up - and to use...

I perused various recipes that floated around at the time (for instance: [How I’ve slashed the cost of my DEV environments by 90%](https://itnext.io/how-ive-slashed-the-cost-of-my-dev-environments-by-90-9c1082ad1baf)) and came up with my own approach. My notes are in the [Appendix: The Manual Way](#the-manual-way); witness the number of GUI actions to perform, commands to run and unanswered questions mentioned ;)

Spinning up transient, fully configured cloud development environments _manually_ is just too clunky!

I _did_ look into approaches that automate away the complexity, like:
- [GitHub Codespaces](https://github.com/features/codespaces), which did not seem to support my IDE of choice - [IntelliJ Idea](https://www.jetbrains.com/idea/) -natively;
- [Google Cloud Workstations](https://cloud.google.com/workstations?hl=en), which, with the complexity of running a Kubernetes cluster, seems geared towards enterprises rather than individual developers;
- [GitPod](https://www.gitpod.io/)
and stayed on my desktop ;)

In July 2024 I stumbled onto [DevPod](https://devpod.sh/) - and it is a game-changer!

DevPod is:
- Open Source: No vendor lock-in. 100% free and open source built by developers for developers.
- Client Only: No server side setup needed. Download the desktop app or the CLI to get started.
- Unopinionated: Repeatable development environment for any infrastructure, any IDE, and any programming language.

## Devcontainer

DevPod is build for [devcontainers](https://containers.dev/), a standard supported by modern IDEs and other tools; using it, code repository describes what it need from the container where developer works on it (in the `.devcontainer/devcontainer.json` file); for details, see [devcontainer reference](https://containers.dev/implementors/json_reference/).

Example below illustrates how to:
- specify container's [image](https://containers.dev/implementors/json_reference/#image-specific)
- expose a [port](https://containers.dev/implementors/json_reference/#general-properties) (e.g., Jekyll web  server running in the container)
- add a [feature](https://containers.dev/implementors/features/) that installs `Gradle`
- adds a [customization](https://containers.dev/supporting) that, *if* IntelliJ IDEA is used, installs the Scala Plugin

```json
{
  "image": "mcr.microsoft.com/devcontainers/java:3.0.0-21",
  "features": {
    "ghcr.io/devcontainers/features/java:1": {
      "version": "none",
      "installGradle": "true",
      "installMaven": "false"
    }
  },
  "customizations" : {
    "jetbrains" : {
      "backend": "IntelliJ",
      "plugins" : ["org.intellij.scala"]
    }
  },
  "forwardPorts": [4000]
}
```

## The Promise
### SSH Setup

One scenario for using DevPod is to run the containers on Docker via SSH - e.g., in your home lab. This scenario does not involve any cloud providers, but DevPod approach is still simpler than the alternatives like using JetBrains Remote Gateway.

Also, with this approach I can use my SSH key stored in a YubiKey hardware token whereas JetBrains Gateway does not support it (see bug [IDEA-383958](https://youtrack.jetbrains.com/projects/IDEA/issues/IDEA-383958/SSH-integration-does-not-query-the-SSH-agent-for-SSH-keys-stored-on-hardware-tokens-e.g.-YubiKey)).

### Google Cloud Setup

DevPod really shines when running workspaces on virtual machines in some cloud.
For instance, when using Google Cloud, once the infrastructure (project, service account, key, roles) is set up (see https://github.com/skevetter/devpod-provider-gcloud), this is what it takes to spin up a workspace using DevPod:
```shell
# set up GCloud DevPod provider (once)
$ devpod provider add gcloud -o PROJECT=<PROJECT ID> -o ZONE=<ZONE>

# spin up a workspace
$ devpod up <GIT REPOSITORY URL>
```

Below is what it takes to spin up a workspace *without* DevPod; it is quite possible that some of this can be shortened and/or scripted, but it is *not* going to be one cloud-independent command ;)
```shell
$ gcloud config set project <project>
$ gcloud config set compute/region us-east1
$ gcloud config set compute/zone us-east1-b
$ gcloud compute project-info add-metadata --metadata enable-oslogin=TRUE
$ gcloud compute os-login ssh-keys add --key-file=<ssh key>.pub
# in the BROWSER!!!, in API  Explorer, use Directory to set POSIX user name and home directory
# https://developers.google.com/admin-sdk/directory/reference/rest/v1/users/update
# Static IP Address
[DOMAIN_NAME]
$ gcloud compute addresses create box-ip --network-tier=STANDARD
$ gcloud compute addresses describe box-ip

$ BOX_IP=$(gcloud compute addresses list \
 --filter="name:box-ip" \
 --format="value(address_range())"
 )

# Create VM instance
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

# In ~/.ssh/config:
Host box
  HostName <IP_ADDRESS_DEV_MACHINE>
  ForwardAgent yes

$ gcloud compute disks create home --size 20GB --type pd-standard
$ gcloud compute instances attach-disk box --disk home


## in the box:
$ sudo mkfs.ext4 -m 0 -E lazy_itable_init=0,lazy_journal_init=0,discard /dev/sdb
$ sudo mkdir /mnt/home
$ sudo mount -o discard,defaults /dev/sdb /mnt/home
$ sudo blkid /dev/sdb
## add to /etc/fstab: UUID=UUID_VALUE /mnt/home ext4 discard,defaults,nofail 0 2
## link /mnt/home into /home

# install Scala plugin on the host:
$ .../remote-dev-server.sh installPlugins ~/Projects/run org.intellij.scala

# server-to-client workflow:
$ .../remote-dev-server.sh run <path/to/project> --ssh-link-host <host>
```
## The Decline

Updated in January 2026.
*Update December 2025*: I still think something like DevPod is a great idea, but I an starting to have doubts about DevPod's implementation: one of the things I loved about DevPod was the immediate attention from the DevPod developers to the issues I raised and suggestions I made. Unfortunately, a year and a half later none of those issues are fixed :(

Plot thickens: even though official DevPod developers claim that they are not abandoning the open source DevPod project, it does seem to be [abandoned](https://github.com/loft-sh/devpod/issues/1915); [skevetter](https://github.com/skevetter) [stepped up](https://github.com/loft-sh/devpod/issues/1946),
[forked](https://github.com/skevetter/devpod) the project, shipped some updates and invited collaboration.

## Make DevPod Great Again

TODO mention the effort to package the community fork.


## Speed and Price

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
## TODO

- gcloud: update documentation for the KEY environment variables and multi-provider CLI workflow
- retrieve options from the environment on demand
- gcloud: turn keys into options, like in AWS?
- for the provider name in add, delete, rename, options, set_options, update, use look at `--provider
- for provider add `--name` option, look at `--provider`; deprecate `--name`
- for provider add, use name as source

file an issue
If started from the command line in a shell where `GOOGLE_APPLICATION_CREDENTIALS` environment variable is set correctly, DevPod GUI works for the `gcloud` provider - but not the workspaces created with it ;)

file an issue
When a non-ssh GIT URL is used, but there is no GIT token in the environment, error message from the container does not make this clear...

To reuse the same virtual machine for all workspaces:
```shell
$ devpod provider use gcloud --single-machine
```
TODO where can I see that --single-machine is set?

Note: Even when the machine is stopped, its persistent disk costs money - around $1/10Gb on GCP.


TODO make context options inherit from the environment just like the provider and ide optios?

TODO does DevPod set IntelliJ options correctly or do I need to override the memory limits? `devpod up ... --ide-option`? Idea command line options; `devops` facility to transfer IDE configuration files and run commands... dot-files?


TODO Floating modal Gateway window blocks input:
```
class: jetbrains-gateway
title: Gateway to /workspaces/cloud-run@cloud-run.devpod
xwayland: 1
pinned: 0
floating: 1
fullscreen: 0
tags: jb*
```

On Omarchy, to install DevPod CLI:
```
$ yay -S --needed --noconfirm devpod-community-bin
```

Install JetBrains Gateway using JetBrains Toolbox.

[Toolbox CLI](https://www.jetbrains.com/help/toolbox-app/toolbox-app-cli.html#obtain_cli) is not installed by default, and it is not clear if it can be used to script the installation of the Gateway even if it was.

Desktop file for the Remote Gateway are broken; spurious quotes around the `Exec` command in  `~/.local/share/applications/jetbrains-gateway.desktop` must be removed manually - otherwise, although a scheme handler for `jetbrains-gateway` is registered (which can be verified with `$ xdg-mime query default x-scheme-handler/jetbrains-gateway`), Gateway can not be started from the command line nor from tools like DevPod that use `xdg-open` to start it: `$ xdg-open jetbrains-gateway://connect` opens web browser instead (see [bug report](https://youtrack.jetbrains.com/projects/GTW/issues/IJPL-226400/Remote-Gateway-desktop-file-is-broken-Gateway-does-not-start))!
