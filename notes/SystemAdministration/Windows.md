Sometimes running some Windows application is unavoidable; example: [Vellue O2Ring](https://getwellue.com/products/o2ring-wearable-pulse-oximete) 's [O2 Insight ​Pro](https://elasticbeanstalk-us-west-2-697648770036.s3.us-west-2.amazonaws.com/exe/O2+Insight+Pro+Setup+v1.8.14+-+signed.exe).

I do not want to double-boot Windows side-by-side with my Linus install, so I need a Virtual Machine running Windows.

Windows installation ISO image can be freely downloaded.

Windows can be run without the product key for a little bit, and since I only need it once every five years on the average, that's fine.

There is no need to install Oracle Virtual Box: Gnome Boxes is enough.

The thing is: naive attempt at installing Windows fails with "your PC does not meet the requirements".

Turns out, some checks need to be disabled. I found that out from a [video](https://www.youtube.com/watch?v=do7zTvZ0TA4&t=164s) by [LadyTechTalks](https://www.youtube.com/@LadyTechTalks) - thank you! Later I found similar information in a Microsoft forum [post](https://techcommunity.microsoft.com/discussions/windows11/how-to-bypass-windows-11-system-requirements-during-installation-on-an-old-lapto/4060758).

To do that, at the beginning of installation:
- press Shift-F10
- at the resulting command line prompt, run `regedit`
- under `HKEY_LOCAL_MACHINE\SYSTEM\Setup`, create a new key `LabConfig`
- in that key, create `DWORD` values an set them to 1:
	- BypassTPMCheck
	- BypassSecureBootCheck
	- BypassCPUCheck
	- BypassRamCheck
	- BypassStorageCheck

Network interface needs to be passed through to the VM in its settings for it to be able to connect tot he internet.

Screen resolution needs to be adjusted manually.

To be able to pass through USB devices, the user has to be in the `kvm` group: `usermod $USER -a -G kvm`.