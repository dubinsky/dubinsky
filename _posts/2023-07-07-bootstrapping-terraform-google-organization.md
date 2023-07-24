---
layout: post
title: 'Bootstrapping Terraform for Google Organization'
author: Leonid Dubinsky
tags: [Terraform, GCP]
date: '2023-07-07'
---

* TOC
{:toc}
## Introduction ##

`gcloud` needs to be installed.

Terraform CLI is installed from https://learn.hashicorp.com/tutorials/terraform/install-cli


## Manual Start ##

In Google Domains:
- transfer Workspace subscription from Google Domains to Google Workspace

In GCP Console:
- GCP organization gets auto-created upon login (?)
- start GCP trial if applicable
- set up billing

In [Admin Console](https://admin.google.com/ac/apps/sites/address):
- set up billing
- turn off automatic Google Workspace licensing
- activate Google Groups for Business (optional)
- activate Cloud Identity Free (optional)
 [Cloud Identity](https://cloud.google.com/identity/docs/set-up-cloud-identity-admin) 
 [Identity Setup](https://cloud.google.com/identity/docs/how-to/setup)


## Bootstrap ##

The idea here is to do the minimum required to bootstrap Terraform.

```shell
# log in as a super-admin
$ gcloud auth login admin@domain.tld

# create project
$ gcloud projects create "domain-infra" --name="Domain Cloud Infrastructure" --no-enable-cloud-apis

# find out billing `ACCOUNT_ID`
$ gcloud beta billing accounts list

# link the project to the billing account
$ gcloud beta billing projects link "domain-infra" --billing-account ACCOUNT_ID
$ gcloud config set project "domain-infra"

# enable APIs used by Terraform
$ gcloud services list --available # all
$ gcloud services list             # enabled
$ gcloud services enable admin.googleapis.com                 # " Admin SDK API"
$ gcloud services enable cloudbilling.googleapis.com          # "Cloud Billing API"
$ gcloud services enable cloudresourcemanager.googleapis.com  # "Cloud Resource Manager API": project operations
$ gcloud services enable iam.googleapis.com                   # "Identity and Access Management (IAM) API": Service Account creation;
                                                              # also enables iamcredentials.googleapis.com
$ gcloud services enable serviceusage.googleapis.com          # "Service Usage API": listing/enabling/disabling services

# create Terraform Service Account
$ gcloud iam service-accounts create terraform --display-name="terraform" --description="Service Account for Terraform"

# obtain the organization id (org_id)
$ gcloud organizations list

# grant the Terraform Service Account roles needed to bootstrap the rest
$ gcloud organizations add-iam-policy-binding org_id --member="serviceAccount:terraform@domain-infra.iam.gserviceaccount.com" \
  --role="roles/resourcemanager.organizationAdmin"

$ gcloud organizations add-iam-policy-binding org_id --member="serviceAccount:terraform@domain-infra.iam.gserviceaccount.com" \
  --role="roles/billing.admin"

$ gcloud organizations add-iam-policy-binding org_id --member="serviceAccount:terraform@domain-infra.iam.gserviceaccount.com" \
  --role="roles/iam.serviceAccountAdmin"
  
# optional: bulk export of the existing Google Cloud Platform setup in Terraform format
$ gcloud beta resource-config bulk-export --path=entire-tf-output --organization=org_id --resource-format=terraform
```

## Keys ##

In addition to running `terraform` from the command line locally, it should be possible to run it from `gradle`
and from GitHub Actions. Giving the service account key to Terraform in an environment variable should enable all the
scenarios of running it (in GitHub Actions, environment variable is set from a secret).

One way of setting this up is:
- define a Terraform variable `terraform_cognomath_infra_key`
- set an environment variable `TF_VAR_terraform_domain_infra_key` which `terraform` will assign to its `terraform_cognomath_infra_key` variable 
- set `credentials` parameter for the `google` and `googleworkspace` providers and the `gcs` backend to `terraform_cognomath_infra_key`

This way, multiple domain-specific environment variables can be defined in `.bash_profile` and
in `~/.gradle/gradle.properties` (with a backslash after each line of the key except the last one, and with backslash-n replaced with backslash-backslash-n :));
one problem with this approach is that unlike `gcs` backend's credentials probably can't be set from a variable...

Another way is to set `GOOGLE_CREDENTIALS` environment variable: `google` and `googleworkspace` providers and
the `gcs` backend use credentials in this variable. To scope it by the specific project, it has to be set for that project -
for instance, using `direnv` with `.envrc` file in the project repository.
This is the approach I use.

Create and retrieve service account key:
```shell
$ gcloud iam service-accounts keys create /path/to/keys/terraform-domain-infra.json --iam-account=terraform@domain-infra.iam.gserviceaccount.com
```

Create `.envrc` file containing:
```shell
export GOOGLE_CREDENTIALS=$(cat /path/to/keys/terraform-domain-infra.json)
```

## Manual Intermission ##

To be able to terraform subdomain-like Google Storage Buckets,
Terraform service account `terraform@domain-infra.iam.gserviceaccount.com` has to be added to the owners of the domain in
[Webmaster Central](https://www.google.com/webmasters/verification/details?hl=en&domain=domain.tld)
(see also https://xebia.com/blog/how-to-automate-google-site-verification-with-terraform/).
This is required even with the domain in Google Cloud Domains.
To be able to do this, one needs to first add the property in the
[Search Console](https://search.google.com/search-console) - which is not a bad idea regardless,
and is also needed to later create organization, account and properties in the
[Marketing Platform Console](https://marketingplatform.google.com).

To be able to Terraform Google Workspace, assign "User Management Admin" and "Group Admin" roles to
the Terraform service account `terraform@domain-infra.iam.gserviceaccount.com`
in [Google Admin Console](https://admin.google.com/ac/roles).


## Core Terraform Files ##

`main.tf` file:
```terraform
locals {
  gcp_region = "us-east1"
}

terraform {
  required_version = ">= 0.14"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = ">=4.35.0"
    }

    googleworkspace = {
      source  = "hashicorp/googleworkspace"
      version = ">=0.7.0"
    }
  }

# This stays commented out until we get to the "State Bucket" step:
#  # store state in a bucket
#  # see also https://registry.terraform.io/providers/hashicorp/terraform/latest/docs/data-sources/remote_state
#  backend "gcs" {
#    bucket = "state.domain.tld" # locals are not allowed here
#    prefix = "terraform"
#  }
}

# Note: see https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket
# "If the project id is not set on the resource or in the provider block
#  it will be dynamically determined which will require enabling the compute api."
provider "google" {
  region      = local.gcp_region
  # zone        =
  # scopes = []
}

data "google_organization" "organization" {
  domain = "domain.tld"
  # org_id -> "..."
  # directory_customer_id -> "..."
}

data "google_billing_account" "account" {
#  display_name    = "My Billing Account"
  open            = true
  billing_account = "...." # id
}

# TODO
# Note: removed IAM that was in place from before the organization resource took over:
// domain: Project Creator and Billing Account Creator roles
#resource "google_organization_iam_member" "domain" {
#  org_id = data.google_organization.org.org_id
#  member = "domain:${local.domain}"
#  for_each = toset([
#    "resourcemanager.projectCreator",
#    "billing.creator"
#  ])
#  role = "roles/${each.value}"
#}

provider "googleworkspace" {
  customer_id = data.google_organization.organization.directory_customer_id
  # TODO cut the scopes down
  oauth_scopes = [
    "https://www.googleapis.com/auth/admin.directory.user",
    "https://www.googleapis.com/auth/admin.directory.userschema",
    "https://www.googleapis.com/auth/admin.directory.group",
    "https://www.googleapis.com/auth/apps.groups.settings",
    "https://www.googleapis.com/auth/admin.directory.domain",
    "https://www.googleapis.com/auth/admin.directory.rolemanagement",
    "https://www.googleapis.com/auth/gmail.settings.basic",
    "https://www.googleapis.com/auth/gmail.settings.sharing",
    "https://www.googleapis.com/auth/chrome.management.policy",
    "https://www.googleapis.com/auth/cloud-platform",
    "https://www.googleapis.com/auth/admin.directory.customer",
    "https://www.googleapis.com/auth/admin.directory.orgunit",
    "https://www.googleapis.com/auth/userinfo.email",
    "https://www.googleapis.com/auth/cloud-identity.groups",
  ]
}
```

`project-infra.tf` file:
```terraform
resource "google_project" "infra" {
  name                = "Domain Cloud Infrastructure"
  project_id          = "domain-infra"
  org_id              = data.google_organization.organization.org_id
  billing_account     = data.google_billing_account.account.id
  auto_create_network = true
  skip_delete         = true
  timeouts {}
 # number -> "..."
}

# Looping approach from https://blog.gruntwork.io/terraform-tips-tricks-loops-if-statements-and-gotchas-f739bbae55f9
resource "google_project_service" "infra" {
  project            = google_project.infra.project_id
  disable_on_destroy = true
  service            = "${each.value}.googleapis.com"
  for_each = toset([
    "admin",                # "Admin SDK API" for user/group operations
    "cloudasset",           #  for 'gcloud beta resource-config bulk-export'
    "cloudbilling",         # "Cloud Billing API"
    "cloudidentity",        #
    "cloudresourcemanager", # "Cloud Resource Manager API" for project operations
    "dns",                  #
    "domains",              #
    "drive",                # "Google Drive" for rclone
    "groupssettings",       # "Groups Settings API" for group settings
    "iam",                  # "Identity and Access Management (IAM) API" for Service Account creation
    "iamcredentials",       # "IAM Service Account Credentials API"
    "logging",              #
    "monitoring",           #
    "serviceusage",         # "Service Usage API" for listing/enabling/disabling services
    "storage",              #
    "storage-api",          #
    "storage-component",    #
  ])
}
```

`sa-terraform.tf` file:
```terraform
resource "google_service_account" "terraform" {
  account_id   = "terraform"
  display_name = "terraform"
  description  = "Service Account for Terraform"
}

resource "google_organization_iam_member" "terraform" {
  org_id = data.google_organization.organization.org_id
  member = "serviceAccount:${google_service_account.terraform.email}"
  role   = "roles/${each.value}"
  for_each = toset([
    "billing.admin",
    "compute.admin",
    "dns.admin",
    "domains.admin",
    "iam.organizationRoleAdmin",
    "iam.serviceAccountAdmin",
    "managedidentities.admin",
    "resourcemanager.organizationAdmin",
    "resourcemanager.folderAdmin",
    "resourcemanager.projectCreator",
    "serviceusage.serviceUsageAdmin",
    "storage.admin"
  ])
}
```

`bucket-state.domain.tld.tf` file:
```terraform
# bucket to store state
resource "google_storage_bucket" "state" {
  project                     = google_project.infra.project_id
  force_destroy               = true
  location                    = local.gcp_region
  name                        = "state.domain.tld"
  storage_class               = "STANDARD"
  uniform_bucket_level_access = true
}
```

`user-admin.tf` file:
```terraform
resource "googleworkspace_user" "admin" {
  primary_email  = "admin@domain.tld"
  timeouts {}
}

resource "google_organization_iam_member" "admin" {
  org_id = data.google_organization.organization.org_id
  member = "user:${googleworkspace_user.admin.primary_email}"
  role   = "roles/${each.value}"
  for_each = toset([
    #    "appengine.appAdmin",
    #    "appengine.appCreator",
    #    "billing.creator",
    #    "billing.admin", // everything but create
    #    "cloudasset.owner", // for bulk Terraform export
    #    "compute.admin",
    #    "firebase.admin", // needed to enable Identity Platform
    #    "iam.securityAdmin",
    #    "iam.serviceAccountKeyAdmin",
    #    "iam.workloadIdentityPoolAdmin",
    #    "identityplatform.admin",
    #    "logging.admin",
    #    "oauthconfig.editor",
    #    "resourcemanager.projectMover",
    #    "servicemanagement.admin",
    #    "serviceusage.serviceUsageAdmin",
    #    "serviceusage.apiKeysAdmin",
    #    "servicedirectory.admin"
  ])
}
```

## Initialize and Import ##

Now we are ready to initialize Terraform:

```shell
$ cd terraform

# initialize
$ terraform init
```

At this point, existing resources are imported into Terraform.

```shell
# project(s)
$ terraform import google_project.infra "projects/domain-infra"

# service account
$ terraform import google_service_account.terraform "projects/domain-infra/serviceAccounts/terraform@domain-infra.iam.gserviceaccount.com"

# Google Workspace user(s)
$ terraform import googleworkspace_user.admin admin@domain.tld

# pre-existing buckets
# TODO

# enabled APIs: instead of importing them individually like this
#   $ terraform import google_project_service.admin_googleapis_com domain-infra/admin.googleapis.com
# I rely on the idempotency and just Terraform the whole map google_project_service.project["..."] over;
# as a result, initial `terraform apply` might fail and will need to be repeated - depending on the order of modifications.
```

And finally, the state described by the Terraform files is applied - which means, see the output of `terraform plan` first
and make sure that - for instance - that Google Workspace user's last and first names are reflected in the files
and do not get wiped out on `terraform apply` :)

```shell
$ terraform apply
```


## State Bucket ##

Migrating Terraform state into a GCS Bucket.

In `main.tf`, uncomment `backend "gcs" {...}`.

```shell
#  authenticate
$ gcloud auth application-default login
```
***IMPORTANT*** if Terraform reports
"querying Cloud Storage failed ... oauth2: cannot fetch token: 400 Bad Request ... invalid_grant" -
do this again (it lasts 60 days)

TODO how do I configure this to use my service account (and its credentials) instead?!

```shell
# move the state to the bucket
$ terraform init -migrate-state
```



## Domains ##

Domains can be imported from Google Domains into Cloud Domains
by the owner of the domains (not by the Terraform Service Account).
Prices in Cloud Domains are the same as in Google Domains.
Domains can be exported out of the Cloud Domains.

Once imported, domain disappears from Google Domains' list,
but is visible at `https://domains.google.com/registrar?d=domain.tld`,
and [can be added back](https://support.google.com/domains/answer/12299086?hl=en) by clicking "Add Project".

Since Google Domains goes away at the end of 2023, I need to move all my domains to Cloud Domains anyway ;)

But since Google Domains itself will be going away in 2024 - why bother?

Google Terraform provider [does not support Cloud Domains](https://github.com/hashicorp/terraform-provider-google/issues/7696) -
but it does support management of the DNS records for the domains configured to use Google Cloud DNS.
For each such domain a zone must be Terraformed and then associated with the domain.

Domain forwarding [is not supported](https://issuetracker.google.com/issues/229955999) on Google Cloud Domains,
but we can probably get by with setting CNAME records www.XXX -> www.domain.tld, what with browsers assuming `www.`.

```shell
$ gcloud auth login admin@domain.tld
$ gcloud domains registrations list-importable-domains

$ gcloud domains registrations import domain.tld
$ # TODO terraform import
# assuming zones are terraformed:
$ gcloud domains registrations configure dns domain.tld --cloud-dns-zone=domain-tld
...
```

To import a zone into Terraform:
$ terraform import google_dns_managed_zone.domain_tld projects/domain-infra/managedZones/domain-tld


## Failure to bootstrap for Google Workspace ##

I tried to use Terraform to assign _GROUPS_ADMIN_ROLE and _USER_MANAGEMENT_ADMIN_ROLE roles to the
Terraform Service Account; even if it worked, it is probably easier to use the Admin Console - but it didn't work:

```shell
$ gcloud auth application-default login --scopes "https://www.googleapis.com/auth/admin.directory.rolemanagement"
```
results in:
```text
    This app is blocked
    This app tried to access sensitive info in your Google Account. To keep your account safe, Google blocked this access.
```
and `terraform apply` (with all the scopes enabled in the Google Workspace provider!) of
```hcl
data "googleworkspace_role" "groups-admin" {
  name = "_GROUPS_ADMIN_ROLE"
}
resource "googleworkspace_role_assignment" "terraform-groups-admin" {
  role_id     = data.googleworkspace_role.groups-admin.id
  assigned_to = google_service_account.terraform.unique_id
  scope_type  = "CUSTOMER"
}

data "googleworkspace_role" "user-management-admin" {
  name = "_USER_MANAGEMENT_ADMIN_ROLE"
}
resource "googleworkspace_role_assignment" "terraform-user-management-admin" {
  role_id     = data.googleworkspace_role.user-management-admin.id
  assigned_to = google_service_account.terraform.unique_id
  scope_type  = "CUSTOMER"
}
```
results in:
```text
│ Error: googleapi: Error 403: Request had insufficient authentication scopes.
│ Details:
│ [
│   {
│     "@type": "type.googleapis.com/google.rpc.ErrorInfo",
│     "domain": "googleapis.com",
│     "metadata": {
│       "method": "ccc.hosted.frontend.directory.v1.DirectoryRoles.List",
│       "service": "admin.googleapis.com"
│     },
│     "reason": "ACCESS_TOKEN_SCOPE_INSUFFICIENT"
│   }
│ ]
│
│ More details:
│ Reason: insufficientPermissions, Message: Insufficient Permission
│
│
│   with data.googleworkspace_role.groups-admin,
│   on main.tf line 165, in data "googleworkspace_role" "groups-admin":
│  165: data "googleworkspace_role" "groups-admin" {
│
╵
```
References:
- [domain-wide delegation](https://admin.google.com/ac/owl/domainwidedelegation)
- [Pre-built administrator roles](https://support.google.com/a/answer/2405986?product_name=UnuFlow&visit_id=637986396850085932-3642428519&rd=1&src=supportwidget0)
- [rolse.list](https://developers.google.com/admin-sdk/directory/reference/rest/v1/roles/list)
- [List of roles](https://developers.google.com/admin-sdk/directory/reference/rest/v1/roles/list?apix_params=%7B%22customer%22%3A%22my_customer%22%7D&apix=true)
- [OAuth 2.0 Scopes for Google APIs](https://developers.google.com/identity/protocols/oauth2/scopes)
- [API](https://github.com/jay0lee/google-api-tracker/blob/master/admin-directory_v1.json)
- [Google Workspace Terraform Provider](https://registry.terraform.io/providers/hashicorp/googleworkspace/latest/docs)
- [its scopes](https://github.com/hashicorp/terraform-provider-googleworkspace/blob/v0.6.0/internal/provider/provider.go#L17-L30)
