---
title: DevPod
---
For JVM projects, minimal `.devcontainer.json` is:
```json
{  
  "image": "mcr.microsoft.com/devcontainers/java:1-21"  
}
```

One created by [IntelliJ Devcontainer plugin](https://www.jetbrains.com/help/idea/connect-to-devcontainer.html) is more elaborate :)

- [ ] [[TODO]] How to pre-configure Scala plugin? In some post-script? see https://www.jetbrains.com/help/idea/work-inside-remote-project.html#plugins:
```shell
$ ./remote-dev-server.sh installPlugins /workspaces/pulumi/  org.intellij.scala
```
Use devpod to run commands on the instance

- [ ] [[TODO]] Do I need any of the features: https://containers.dev/features?

In the OpenTorah infrastructure, I:
- created a GCP project for `devpod`: `opentorah-devpod`;
- enabled `Compute Engine API` for it (`"compute"`);
- created a service account for `devpod`: `devpod@opentorah-devpod.iam.gserviceaccount.com`;
- assigned `serviceusage.services.use` permission on the project to it (using `serviceusage.serviceUsageConsumer` role);
- assigned `compute.admin` role on the project to the service account;
- assigned `iam.serviceAccountUser` role on the project to the service account, since I want to be able to configure the instances to run as a service account;

For GCP (provider: gcloud), `devpod` command looks for the Google Application Default Credentials (ADC), so I:
- generated, retrieved and stashed the key for the service account (`devpod.json`);
- in `.envrc`, set `GOOGLE_APPLICATION_CREDENTIALS` to the key; 
- in `.envrc`, set `CLOUDSDK_CORE_PROJECT` to `opentorah-devpod`;

GCLOUD_JSON_AUTH variable: JSON key itself...

DevPod UI does not find the ADC, so I used DevPod CLI:
```shell
$ devpod ide use intellij

$ devpod provider add gcloud
$ devpod provider use gcloud
$ devpod provider set-options gcloud -o ZONE=us-east4-b
$ devpod provider set-options gcloud -o SERVICE_ACCOUNT=devpod@opentorah-devpod.iam.gserviceaccount.com
```
 
Finally:
```shell
$ devpod up github.com/opentorah/opentorah
```

And it works!!

- [ ] [[TODO]] Does my ssh key propagate? Can I check in from over there?
- [ ] [[TODO]] How do I make other secrets and credentials available over there - for instance, for artifact publishing? - `--workspace-env-file`


shared vm option in CLI?

How to update Idea?

devpod up my-workspace --recreate

devpod up https://github.com/example/repo --dotfiles https://github.com/my-user/my-dotfiles-repo

https://github.com/loft-sh/devpod-provider-gcloud

`devpod provider add gcloud` does not work even with `PROJECT` set; 
`CLOUDSDK_CORE_PROJECT` needs to be set, and if it is, its value becomes the value of the provider's PROJECT property. And although documentation states that PROJECT and ZONE will be required ("Follow the on-screen instructions to complete the setup." ) at provider initialization, neither is asked about: PROJECT is set as above, and ZONE defaults to something European.

Environment variables do not override the provider options: DISK_SIZE, ZONE, SERVICE_ACCOUNT...

Pre-requisites (project, service account) and required roles should be documented for GCload and other providers, preferably - in their help or init message...

SSH:
allow-remote-pkcs11      and  no-restrict-websafe