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

- [ ] [[TODO]] How to pre-configure Scala plugin? In some post-script? see https://www.jetbrains.com/help/idea/work-inside-remote-project.html#plugins
- [ ] [[TODO]] Do I need any of the features: https://containers.dev/features

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

DevPod UI does not find the ADC, so I used DevPod CLI:
```shell
$ devpod ide use intellij

$ devpod provider add gcloud
$ devpod provider use gcloud -o ZONE=us-east4-b
$ devpod provider use gcloud -o SERVICE_ACCOUNT=devpod@opentorah-devpod.iam.gserviceaccount.com
```

Finally:
```shell
$ devpod up github.com/opentorah/opentorah
```

And it works!!