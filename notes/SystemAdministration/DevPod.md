---
title: DevPod
---
On Omarchy, to install DevPod CLI:
```
$ yay -S --needed --noconfirm devpod-community-bin
```

Install JetBrains Gateway using JetBrains Toolbox.

[Toolbox CLI](https://www.jetbrains.com/help/toolbox-app/toolbox-app-cli.html#obtain_cli) is not installed by default, and it is not clear if it can be used to script the installation of the Gateway even if it was.

Gateway drops *two* desktop files in `~/.local/share/applications/`:

`jetbrains-gateway-xxx.desktop`:
```properties
[Desktop Entry]
Name=Gateway 2025.3
Exec="/home/dub/.local/share/JetBrains/Toolbox/apps/gateway-2/bin/gateway" %u
Version=1.0
Type=Application
Categories=Development;IDE;
Terminal=false
Icon=/home/dub/.local/share/JetBrains/Toolbox/dotDesktopIcons/jetbrains-gateway-xxx.desktop.icon.svg
Comment=A remote development hub
StartupWMClass=jetbrains-gateway
StartupNotify=true
```

and `jetbrains-gateway.desktop`:
```properties
[Desktop Entry]
Exec="/home/dub/.local/share/JetBrains/Toolbox/apps/gateway-2/bin/gateway.sh" %u
Version=1.0
Type=Application
Categories=Development
Name=JetBrains Gateway
StartupWMClass=jetbrains-ide
Terminal=false
MimeType=x-scheme-handler/jetbrains-gateway;
```

There are differences between the two in `Name`,  `Exec`, `Categories`, `Icon`, `Comment`, `StartupNotify`.

TODO something about this...

The unnumbered `jetbrains-gateway.desktop` gets registered as a scheme handler for `jetbrains-gateway`, as can be seen with:
```shell
$ xdg-mime query default x-scheme-handler/jetbrains-gateway
```

Because of the spurious quotes around the `Exec` command, Gateway does not start when invoked via `xdg-open`:
```shell
$ xdg-open jetbrains-gateway://connect
```

The spurious quotes must be removed.

TODO something about it...

To use IntelliJ IDEA by default:
```shell
$ devpod-cli ide use intellij
```

There seems to be no way to see what IDE is set as the default ;)   TODO

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