## Direct deposit from Social Security

What had already failed:

- X Money's own direct-deposit switch was blocked.
- The Social Security phone representative could not make the change either.

Grok Bot found no first-hand report of anyone finishing the change online.
Articles that say to type Cross River Bank's routing number into my Social Security are generic guides.

Grok Bot's inference, which Social Security has not confirmed: Social Security may send an online change for this kind
of account to an in-person check.
The CFPB lists it as the
[X Money Stored Value Account](https://www.consumerfinance.gov/data-research/prepaid-accounts/search-agreements/detail/621701/),
issued by Cross River Bank, program manager X Payments LLC, product type digital wallet.
Trying the change in my Social Security costs nothing.
When that is blocked, the walk-in visit is the route that has worked for other people.

People who did get a deposit, from Grok Bot's October watch of posts on X:

- [Sweet_T02](https://x.com/Sweet_T02), 2026-09-03: the deposit landed in X Money six days early.
  Social Security would not change it online or through X Money, so they went to an office with ID.
  They told the front desk they were only there to change direct deposit, were seen without an appointment, and had no
  gap in payments.
- [spremark](https://x.com/spremark), 2026-09-11: the deposit arrived.
- One more success, logged 2026-10-09 on
  [The Bonus Stack](https://thebonusstack.com/learn/what-counts-as-direct-deposit-us-bank).

Those posts are collected in the Google Drive doc "SSA Visit Prep".

## PayPal


## Passkeys

I want the X Money passkey kept in Bitwarden.
That works on the phone.
On the desktop I had to register a second passkey, stored on the YubiKey.

- I signed up for X Money on the phone, and Bitwarden stored the passkey.
  How do I use that passkey on the desktop?
- The passkey for X is in Bitwarden on the phone and is not in the desktop browser extension.
- X Money says "Authenticate with your credential".
  Continue does not ask whether to use a passkey or a security key, or where the passkey comes from, and authentication
  fails.

The unplug-the-YubiKey sequence, and when to let Chrome offer passkeys, is not in any session I can still read.

What follows from how the tools work, which that lost session may have said and which I cannot quote:

The Bitwarden passkey lives in the vault.
The phone lets the Bitwarden app supply it, so login on the phone works.
A passkey cannot be copied out of Bitwarden onto a YubiKey, so the desktop login is a second credential, created on the
YubiKey and registered with X Money.
On desktop Linux, Chrome uses its own passkey path and a plugged-in security key.
Bitwarden's system passkey provider is the desktop app on Windows 11 and macOS.
Linux is outside that.
With the YubiKey plugged in, Chrome takes the security key and shows no choice of Bitwarden.
That matches the August screen.
