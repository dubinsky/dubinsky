---
title: Docker
tags:
  - sysadmin
---
- [Documentation](https://docs.docker.com/reference/)
- [Fedora Instructions](https://linuxconfig.org/how-to-install-and-configure-docker-ce-moby-engine-on-fedora-32)

## Install
```shell
$ sudo dnf install moby-engine docker-compose
$ sudo systemctl enable docker
$ sudo usermod -aG docker $USER
# Test (after restart)
$ docker run hello-world
```

## Volume Permissions
For Docker volumes that mount a host directory to not suffer from permission issues, `"Z"` needs to be added at the end of the volume string: `/path/to/host/directory:/path/to/mount/in/container:Z`.

## Managers

Now that I use [[ProxMox]] to run [[UniFi]] and [[Home Assistant]], I do not see much need for running applications in Docker containers; I do run a virtual machine dedicated to running Docker containers for development, but they are transient and do not need to be managed; if at some point I need a Docker manager:
- [Portainer](https://github.com/portainer/portainer)
- [Dockge](https://github.com/louislam/dockge)
- [Runtipio](https://runtipi.io/)
## Contexts
[Contexts](https://docs.docker.com/engine/manage-resources/contexts/)
```console
$ docker context ls
$ docker context inspect <context>
$ docker context use <context>
$ docker context export
$ docker context import
$ docker context update <context> --description "..."
```
You can also set the current context using the `DOCKER_CONTEXT` environment variable. The environment variable overrides the context set with `docker context use`.
```console
$ export DOCKER_CONTEXT=<context>
```
You can also use the global `--context` flag to override the context.
```console
$ docker --context <context> container ls
```

### Direct
If Docker is exposed on a TCP socket, it can be used directly, without SSH:
```console
$ docker context create <context> --docker host=tcp://box:2375
```

See [[Docker#Expose]] on how to expose Docker.

### SSH
[SSH Contexts](https://docs.docker.com/engine/security/protect-access/#use-ssh-to-protect-the-docker-daemon-socket)
```console
$ docker context create <context> --docker host=ssh://dub@box
```

For an SSH context to start working, you have to first SSH into the Docker host manually, so that the host becomes known.

For the best user experience with SSH, configure `~/.ssh/config` as follows to allow reusing a SSH connection for multiple invocations of the `docker` CLI:
```text
ControlMaster     auto
ControlPersist    1m
ControlPath       ~/.ssh/control/ssh-%r@%h:@p
```
Directory `control` must exist; it should not be readable by others.

## Expose

Ability to expose Docker so that it can be used remotely without authentication will be removed, but while it is still there, this is how to configure it on the host running Docker daemon:
```console
$ sudo mkdir -p /etc/systemd/system/docker.service.d
$ sudo cat /lib/systemd/system/docker.service | grep ExecStart > /etc/systemd/system/docker.service.d/expose-over-tcp.conf
```

In `/etc/systemd/system/docker.service.d/expose-over-tcp.conf`, add `[Service]` header, `ExecStart=` reset, and `-H tcp://0.0.0.0:2375`, so that it looks like this:
```text
[Service]
ExecStart=
ExecStart=/usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sock -H tcp://0.0.0.0:2375
```

Then:
```console
$ sudo systemctl daemon-reload
$ sudo systemctl restart docker
$ sudo systemctl show --property=ExecStart docker
```