---
title: DevPod
tags:
  - devpod
  - gcp
  - intellij
  - devcontainers
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

I perused various recipes that floated around at the time (for instance: [How I’ve slashed the cost of my DEV environments by 90%](https://itnext.io/how-ive-slashed-the-cost-of-my-dev-environments-by-90-9c1082ad1baf)) and came up with my own approach; conclusion: spinning up transient, fully configured cloud development environments _manually_ is just too clunky!

I _did_ look into approaches that automate away the complexity, like:
- [GitHub Codespaces](https://github.com/features/codespaces), which did not seem to support my IDE of choice - [IntelliJ Idea](https://www.jetbrains.com/idea/) -natively;
- [Google Cloud Workstations](https://cloud.google.com/workstations?hl=en), which, with the complexity of running a Kubernetes cluster, seems geared towards enterprises rather than individual developers;
- [GitPod](https://www.gitpod.io/)

...and stayed on my desktop ;)

In July 2024 I stumbled onto [DevPod](https://devpod.sh/) - and it is a game-changer!

DevPod is:
- Open Source: No vendor lock-in. 100% free and open source built by developers for developers.
- Client Only: No server side setup needed. Download the desktop app or the CLI to get started.
- Unopinionated: Repeatable development environment for any infrastructure, any IDE, and any programming language.

## Devcontainers

DevPod is build for [devcontainers](https://containers.dev/), a standard supported by modern IDEs and other tools; using it, code repository describes what it needs from the container where developer works on it (in the `.devcontainer/devcontainer.json` file); for details, see [devcontainer reference](https://containers.dev/implementors/json_reference/).

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

## DevPod is Great!

My IDE of choice, IntelliJ IDEA, already lets me run workspaces on Docker via SSH,
without involving any cloud providers - for instance, in my home lab -
using [JetBrains Gateway](https://www.jetbrains.com/remote-development/gateway/).
Starting the Gateway via DevPod is more straightforward,
and some Gateway bugs
([SSH port missing](https://youtrack.jetbrains.com/issue/IJPL-63403/SSH-default-port-missing-with-OpenSSH-config), 
[SSH does not work with Yubikey](https://youtrack.jetbrains.com/issue/IJPL-231964/SSH-integration-does-not-query-the-SSH-agent-for-SSH-keys-stored-on-hardware-tokens-e.g.-YubiKey))
are no longer a problem ;)

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
$ gcloud config set project <PROJECT ID>
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
## DevPod is Great Again!

One of the things impressed me about DevPod was the immediate attention from the DevPod developers to the issues I raised and suggestions I made:
- minor corrections and clarifications were accepted: [1150](https://github.com/loft-sh/devpod/pull/1150), [gcloud/21](https://github.com/loft-sh/devpod-provider-gcloud/issues/21), [gcloud/22](https://github.com/loft-sh/devpod-provider-gcloud/pull/22), [gcloud/24](https://github.com/loft-sh/devpod-provider-gcloud/pull/24), [gcloud/25](https://github.com/loft-sh/devpod-provider-gcloud/pull/25/changes);
- my [suggestion](https://github.com/loft-sh/devpod/issues/1152) for a `devpod machine describe` resulted in `devpod machine inspect` being implemented in *two days*;
- my [suggestion](https://github.com/loft-sh/devpod/issues/1170) to inject GIT author/committer environment variables into workspaces was met with agreement;
- my [suggestion](https://github.com/loft-sh/devpod/issues/1153) to automate installation of IntelliJ plugins was met with agreement.

Unfortunately, implementation of my suggestions stalled:
- `devpod machine describe` was never implemented;
- injection of the GIT author/committer environment variables was never implemented;
- automatic installation of the IntelliJ plugins was never implemented.

I understand that the best way to improve an open-source project is to submit pull requests, but since DevPod uses the `Go` programming language that I am not familiar with (and do not really want to get familiar with ;)), and since I was not familiar with DevPod's architecture, I didn't even try developing the enhancements I wanted on my own.

Also, my [suggestion](https://github.com/loft-sh/devpod-provider-gcloud/issues/23) to allow setting values for the command-line options via environment variables and overriding them on the command line was *rejected*; without this, I couldn't use DevPod the way I want (see [CLI Workflow](https://github.com/skevetter/devpod-provider-gcloud#cli-workflow)).

In December 2025, with no progress for a year and a half, I decided to implement a wrapper around DevPod CLI that handles environment variables the way all the other tools (e.g. git, gcloud) do ;)

Of course, using DevPod via such a wrapper is not ideal, but I saw no other way to make it work for me. It turned out that DevPod had a bug: it ignored `--context` supplied on the command line, making my wrapper implementation impossible...

On December 25, 2025, while pocking around DevPod issues in desperate hope for some way out, I discovered that:
- there were no DevPod releases for months and pull requests were not being merged;
- in September people [realized](https://github.com/loft-sh/devpod/issues/1915) that the project is dead; [skevetter](https://github.com/skevetter/devpod) mentioned a [community fork](https://github.com/loft-sh/devpod/issues/1915#issuecomment-3340878120);
- in November the [fork](https://github.com/skevetter/devpod) went [live](https://github.com/loft-sh/devpod/issues/1946); in December, forked releases started and some of the DevPod providers were forked too.

Had I decided to come back to DevPod just one month sooner than I did, before the community fork happened, I probably would have given up on DevPod completely!

The fork is *very much* alive: skevetter
is cleaning up the setup and the code,
merging pull requests and implements new features,
answering questions and plans a [re-branding](https://github.com/skevetter/devpod/discussions/523);
people are discovering and using the fork and contributing pull requests, bug reports, and enhancement suggestions;
there is an Arch Linux [package](https://aur.archlinux.org/packages/devpod-community-bin) for the fork etc.
See the forked repositories for details of this wonderful story.

When I saw this, my mental barriers broke down; I felt that I can contribute - and I did,
including all the things that I wanted since the beginning:
- [fix](https://github.com/skevetter/devpod/pull/161) `--context` bug that killed my wrapper project;
- [fix](https://github.com/skevetter/devpod/pull/163) `devpod provider list-available` and its [source]( https://github.com/skevetter/devpod/pull/528);
- [auto-install](https://github.com/skevetter/devpod/pull/229) IntelliJ plugins;
- [propagate](https://github.com/skevetter/devpod/pull/253) GIT environment variables
- add `devpod machine describe` in [devpod](https://github.com/skevetter/devpod/pull/602), [gcloud](https://github.com/skevetter/devpod-provider-gcloud/pull/34), and [aws](https://github.com/skevetter/devpod-provider-aws/pull/82);
- environment variables for flags and options: [195](https://github.com/skevetter/devpod/pull/195), [251](https://github.com/skevetter/devpod/pull/251), [252](https://github.com/skevetter/devpod/pull/252), [256](https://github.com/skevetter/devpod/pull/256), [257](https://github.com/skevetter/devpod/pull/), [615](https://github.com/skevetter/devpod/pull/615), [gcloud/2](https://github.com/skevetter/devpod-provider-gcloud/pull/2), [gcloud/44](https://github.com/skevetter/devpod-provider-gcloud/pull/44).
- [document](https://github.com/skevetter/devpod-provider-gcloud/pull/44) `gcloud` provider properly.

## Speed and Price
Here are rough estimates of speed and price when working with one of my projects (OpenTorah) as calculated around 2024:

```
type           CPUs  Gb  Gradle     Idea         $/month
                                    mem  time
local                    ~2   min        12s     $  0

e2-standard-2   2     8  ~5.5 min                $ 49
e2-highcpu-4    4     4  ~4   min   ~70%         $ 74
e2-highcpu-8    8     8  ~3   min   ~40% 23s     $142
e2-standard-4   4    16                          $ 98
e2-standard-8   8    32                          $196
e2-highcpu-16  16    16                          $289
```
## TODO

Visible:
- gcloud: update documentation for the multi-provider CLI workflow
- make context options inherit from the environment just like the provider and ide optios
- convey context name to the provider
- there is too much logging on the info level, including image pulling
- UI tries to call "dpkg"!
- When JetBrains Gateway is started from the UI, SSH connection dialog pops up - but not from the CLI...
- Where can I see that --single-machine was set (`devpod provider use gcloud --single-machine`)? It should be an option, not a flag, and on the context, not provider!
- for the provider name in add, delete, rename, options, set_options, update, look at `--provider
- for provider add `--name` option, look at `--provider`; deprecate `--name`
- for provider add, use name as source

Internal:
- gcloud: retrieve options from the environment on demand
- factor out status and description constants (from client) and option retrieval (from gcloud) into a package shared between devpod and providers
- factor out agent options into a package shared between providers - better yet, remove all such options!

Note: Even when the machine is stopped, its persistent disk costs money - around $1/10Gb on GCP.

## How to propagate GPG signing of commits into workspace?
Written by IDEA when signing is enabled in IDE:
```
[commit]
gpgSign = true                                                                                                                                                    [user]
signingkey = <key-id>    
```
Global via GIT:
```
$ git config --global commit.gpgsign true`
$ git config --global user.signingkey <key-id>
```
There does not seem to exist a way to describe this via environment variables (which is needed to accommodate signing for multiple organizations), and even if there was such a way, IDEA only looks at GIT config...

devpod up:
 --git-ssh-signing-key git config user.signingkey   The ssh key to use when signing git commits. Used to explicitly setup DevPod's ssh signature forwarding with given key. Should be same format as value of git config user.signingkey
--gpg-agent-forwarding                             If true forward the local gpg-agent to the DevPod workspace

## IntelliJ IDEA COnfiguration

Does DevPod set IntelliJ options correctly or do I need to override the memory limits? `devpod up ... --ide-option`? Idea command line options; `devops` facility to transfer IDE configuration files and run commands... dot-files?

Install client-side (UI) IntelliJ plugins automatically?

## JetBrains Gateway desktop files on Omarchy
Install JetBrains Gateway using JetBrains Toolbox.

[Toolbox CLI](https://www.jetbrains.com/help/toolbox-app/toolbox-app-cli.html#obtain_cli) is not installed by default, and it is not clear if it can be used to script the installation of the Gateway even if it was.

Desktop file for the Remote Gateway are broken; spurious quotes around the `Exec` command in  `~/.local/share/applications/jetbrains-gateway.desktop` must be removed manually - otherwise, although a scheme handler for `jetbrains-gateway` is registered (which can be verified with `$ xdg-mime query default x-scheme-handler/jetbrains-gateway`), Gateway can not be started from the command line nor from tools like DevPod that use `xdg-open` to start it: `$ xdg-open jetbrains-gateway://connect` opens web browser instead (see [bug report](https://youtrack.jetbrains.com/projects/GTW/issues/IJPL-226400/Remote-Gateway-desktop-file-is-broken-Gateway-does-not-start))!

It looks like Omarchy upgrade - or some Arch thingy it triggers - keep adding quotes to the  desktop files that have UUIDs in their names and no MIME association in them - quotes that the same OS can not handle to open... 3