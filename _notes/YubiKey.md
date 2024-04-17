  * #computer
  * FIDO2 SSH key:```shell
# generate
$ ssh-keygen -t ed25519-sk -O resident -O application=ssh:dub@podval.org-yk5c-fido2

# list
$ ykman fido credentials list

# delete
$ ykman fido credentials delete <key substring>

# activate
$ ssh-add -K

# list
$ ssh-add -L

# deactivate
$ ssh-add -D

# activate permanently
# writes identity files for all resident keys disamgiuating by the `application` parameter
$ ssh-keygen -K```
  * Key comment:
    * `ssh-keygen -C "comment"` is not stored in the key
    * public key written during key generation has the default comment `user@host`
    * public key retrieved by `ssh-keygen -K` has the comment from the `-O application` parameter
    * `ykman fido credentials list` shows hex representation of the value of the `-O user` parameter 
  * By default, FIDO2 SSH keys require touch (presence detection) for each operation.
It is possible to disable this at generation time with `ssh-keygen ... -O no-touch-required`,
but such keys are rejected by default, and sshd needs to be re-configured to accept them.
I am not going to fight this since: 
    * it is more surprising (compared to PIV keys) than annoying,
    * it requires re-configuration of SSHD,
    * GitHub rejects `no-touch-required` keys,
    * it is more secure
    * even when the key is generated with -touch-required AND is prefixed in the authorized_keys file with -touch-required,
rclone still requires touch, and sometimes - multiple...
  * [ ] Message about the touch wait is not being shown - link to the bug
  * Note about backup keys
  * [ ] update blog post