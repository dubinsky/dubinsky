---
title: Docker
tags:
  - sysadmin
---
- [Documentation](https://docs.docker.com/reference/)
- [Fedora Instructions](https://linuxconfig.org/how-to-install-and-configure-docker-ce-moby-engine-on-fedora-32)

Install:
```shell
$ sudo dnf install moby-engine docker-compose
$ sudo systemctl enable docker
$ sudo usermod -aG docker $USER
# Test (after restart)
$ docker run hello-world

(GUI: lazydocker
$ sudo dnf copr enable atim/lazydocker
$ sudo dnf install lazydocker)
```

For Docker volumes that mount a host directory to not suffer from permission issues, `"Z"` needs to be added at the end of the volume string: `/path/to/host/directory:/path/to/mount/in/container:Z`.