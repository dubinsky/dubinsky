---
title: DevPod
---
On Omarchy, to install DevPod CLI:
```
$ yay -S --needed --noconfirm devpod-community-bin
```

Install JetBrains Gateway using JetBrains Toolbox.

[Toolbox CLI](https://www.jetbrains.com/help/toolbox-app/toolbox-app-cli.html#obtain_cli) is not installed by default, and it is not clear if it can be used to script the installation of the Gateway even if it was.

Desktop file for the Remote Gateway are broken; spurious quotes around the `Exec` command in  `~/.local/share/applications/jetbrains-gateway.desktop` must be removed manually - otherwise, although a scheme handler for `jetbrains-gateway` is registered (which can be verified with `$ xdg-mime query default x-scheme-handler/jetbrains-gateway`), Gateway can not be started from the command line nor from tools like DevPod that use `xdg-open` to start it: `$ xdg-open jetbrains-gateway://connect` opens web browser instead (see [bug report](https://youtrack.jetbrains.com/projects/GTW/issues/IJPL-226400/Remote-Gateway-desktop-file-is-broken-Gateway-does-not-start))!

To see what IDEs are available and which is the default one:
```shell
$ devpod-cli ide list
```

To use IntelliJ IDEA by default:
```shell
$ devpod-cli ide use intellij
```

To configure running in Docker on the ssh host `box`:
```shell
$ devpod-cli provider add ssh -o HOST=box
```

On first ssh connection (resulting from a `devpod-cli up`) to the newly configured host, Gateway popup asking for approval to connect pops up.

Plugins need to be installed for each project.

https://www.jetbrains.com/help/idea/work-inside-remote-project.html#manual_install


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

TODO does DevPod set IntelliJ options correctly or do I need to override the memory limits? `devpod up ... --ide-option`? Idea command line options; `devops` facility to transfer IDE configuration files and run commands... dot-files?

TODO Ideally, there should be a way to add personal stuff to the `devcontainer.json`!