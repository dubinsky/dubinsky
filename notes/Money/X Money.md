## Direct deposit from Social Security

- **Target:** Move both Leonid's and Nina's Social Security direct deposits from Fidelity Cash Management to the joint X Money checking account (at Cross River Bank).
  Each spouse must update their own deposit.

- **What was tried:**
  - *X Money's direct-deposit switch:* X Money generated Leonid's direct-deposit form and submitted from its side, but Social Security blocked it (exact reason unconfirmed).
  - *`my Social Security` online (`ssa.gov`):* Tried for Leonid on October 6, 2026; failed with `"We require more information to process your request."`
  - *SSA phone line (`1-800-772-1213`):* Tried on October 6, 2026 after getting a one-time code at `ssa.gov/PIN`; the representative could not change direct deposit.
    Expected the same outcome for Nina.
  

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

- **What is still pending (as of November 23, 2026):**
  - Completing the in-person SSA office visit for both Leonid and Nina (primary: Cambridge SSA, 10 Fawcett St, 1st Floor; backup: Waltham SSA, 135 Beaver St, Suite 120; Mon–Fri 9:00 AM–4:00 PM).
    Ask the clerk to verify account acceptance at the counter and check whether either record has a Direct Deposit Fraud Prevention or eServices block that can be lifted.
  - Proving Nina's joint ownership: the September 2026 statement showed only Leonid's name and the X Money connector listed the joint account as a secondary account, so the October 9 `SSA Status Notes` left Nina's proof open.
    Since then, a November 1, 2026 official confirmation letter from X Payments LLC / X Money (`Gnezdo/Money/X Money Account Confirmation.pdf`) names both Leonid and Nina as joint owners (opened September 24, 2026, via Cross River Bank), though monthly joint-statement visibility for Nina remains open under support ticket `CSP-70424`.

- **Where `SSA Visit Prep` is:** Google Drive folder `Gnezdo/SSA`, doc `SSA Visit Prep` (prepared October 9, last updated October 12, 2026), alongside the consolidated history doc `SSA Status Notes` (last updated October 9, 2026) and supporting SSA PDFs; the November 1 joint-account confirmation letter lives in `Gnezdo/Money/X Money Account Confirmation.pdf`.

## PayPal

- **Linking X Money as a bank account in PayPal:** No Grok Bot agent (Chief of Staff, Cash, Taxes, Orders, or any other bot) has a record of Leonid attempting to link the joint X Money checking account directly as a bank account in PayPal, and Gmail has no PayPal bank-linking, micro-deposit, or decline emails for X Money or Cross River Bank.
- **Community reports on linking X Money as a bank in PayPal (checked by Chief of Staff in late October, November 5, and November 12, 2026):**
  - On October 30, 2026, `@LCUncens0red` on X reported that PayPal stopped accepting X Money checking accounts: an already-linked X Money checking account disappeared from PayPal, and re-adding it failed with an error that PayPal does not accept that institution at this time (linking the X Money debit card still worked, though PayPal charges a fee for card-funded transfers).
  - On November 2, 2026, `@capez28` on X reported that manual bank linking via routing and account numbers succeeded at least through PayPal's two micro-deposits (15¢ and 17¢), with X Money prompting approval for PayPal's 32¢ ACH pull-back; ongoing transfers afterward were not confirmed.
  - No newer first-hand reports appeared by November 12.
    PayPal's own documentation also notes that most online-only or prepaid/stored-value accounts may not work as linked banks.


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


## X Money support ticket

- **Open X Money support ticket (`CSP-70424`):**
  - **Not about PayPal.** Ticket `CSP-70424` (opened in-app November 4, 2026; tracked by Orders and Chief of Staff) is the only open X Money support ticket in agent memory and Gmail.
    It covers three unrelated items: (1) displaying Leonid's name in normal case rather than all caps; (2) showing both owners' names on joint account statements; and (3) making joint account statements visible to Nina.
  - **Support reply status:** **No human reply yet** as of November 23, 2026.
    X Money Support (`xm-support@x.com`, Cameron) sent an acknowledgment on November 4 promising a follow-up within 1–2 business days.
    Leonid sent his own follow-up reply on November 10 restating all three items after that window passed.
    Orders' weekday watch last checked on November 23 and confirmed still no human reply.
  - (The only other 2026 X Money support ticket in Gmail, `CSP-25527` from September 13 about physical card name embossing, was answered the same day and is unrelated to PayPal.)
