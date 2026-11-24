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

## From Grok Bot memory

Compiled on November 23, 2026 from Grok Bot agent memory across Chief of Staff, Cash, Taxes, Orders, and the remaining active bots, together with the Google Drive SSA notes and Gmail history checked alongside them.

### Direct deposit from Social Security

- **Target:** Move both Leonid's and Nina's Social Security direct deposits from Fidelity Cash Management to the joint X Money checking account (at Cross River Bank).
  Each spouse must update their own deposit.
  (An early September 2026 Cash ledger row assumed Leonid's deposit would go to his personal X Money Cash account; on October 5 he confirmed both should go to the joint account.)
- **What was tried:**
  - *X Money's direct-deposit switch:* X Money generated Leonid's direct-deposit form and submitted from its side, but Social Security blocked it (exact reason unconfirmed).
  - *`my Social Security` online (`ssa.gov`):* Tried for Leonid on October 6, 2026; failed with `"We require more information to process your request."`
  - *SSA phone line (`1-800-772-1213`):* Tried on October 6, 2026 after getting a one-time code at `ssa.gov/PIN`; the representative could not change direct deposit.
    Expected the same outcome for Nina.
  - *Office visits:* An earlier Cambridge walk-in with citizenship proof worked to get Leonid's retirement claim approved (awarded September 22, 2026; new Social Security card with the same number mailed; October 27 Cambridge appointment cancelled), with direct deposit set to Fidelity Cash Management.
    A separate visit to switch both spouses' deposits to the joint X Money account was initially planned for October 13, 2026 (Cambridge primary, Waltham backup), then unscheduled on October 9 (date left TBD; calendar event deleted).
- **What worked so far:** Only Fidelity Cash Management is active for both Leonid (benefits starting November 2026, first payment around December 23, 2026, then roughly the fourth Wednesday monthly; none received yet) and Nina (already receiving benefits on the third-Wednesday schedule, sometimes a day early).
  Neither switch to X Money has succeeded yet.
  X Money's 6% APY is already active via X Ultra / SuperGrok Heavy and does not depend on direct deposit, so on October 12 Leonid decided to move recurring bills to the joint X Money account without waiting for Social Security.
  (Community walk-in success reports—including `@zanna640`'s September 19 test deposit alongside the posts listed in `## Direct deposit from Social Security` above—are logged in `SSA Visit Prep`.)
- **What is still pending (as of November 23, 2026):**
  - Completing the in-person SSA office visit for both Leonid and Nina (primary: Cambridge SSA, 10 Fawcett St, 1st Floor; backup: Waltham SSA, 135 Beaver St, Suite 120; Mon–Fri 9:00 AM–4:00 PM).
    Ask the clerk to verify account acceptance at the counter and check whether either record has a Direct Deposit Fraud Prevention or eServices block that can be lifted.
  - Proving Nina's joint ownership: the September 2026 statement showed only Leonid's name and the X Money connector listed the joint account as a secondary account, so the October 9 `SSA Status Notes` left Nina's proof open.
    Since then, a November 1, 2026 official confirmation letter from X Payments LLC / X Money (`Gnezdo/Money/X Money Account Confirmation.pdf`) names both Leonid and Nina as joint owners (opened September 24, 2026, via Cross River Bank), though monthly joint-statement visibility for Nina remains open under support ticket `CSP-70424`.
  - Keep Fidelity Cash Management open until SSA deposits actually land in the joint X Money account.
    Chief of Staff's weekly Monday SSA reminder is paused at Leonid's request.
- **Where `SSA Visit Prep` is:** Google Drive folder `Gnezdo/SSA`, doc `SSA Visit Prep` (prepared October 9, last updated October 12, 2026), alongside the consolidated history doc `SSA Status Notes` (last updated October 9, 2026) and supporting SSA PDFs; the November 1 joint-account confirmation letter lives in `Gnezdo/Money/X Money Account Confirmation.pdf`.

### PayPal and open X Money support ticket

- **Linking X Money as a bank account in PayPal:** No Grok Bot agent (Chief of Staff, Cash, Taxes, Orders, or any other bot) has a record of Leonid attempting to link the joint X Money checking account directly as a bank account in PayPal, and Gmail has no PayPal bank-linking, micro-deposit, or decline emails for X Money or Cross River Bank.
- **What was tried in PayPal:**
  - *October 13, 2026:* When opening PayPal to move spending off Fidelity, Fidelity Rewards Visa was preferred and Fidelity Cash Management checking was the only bank on file (showing a warning flag), with X Money not linked as a bank.
    Instead of linking X Money as a bank account, Leonid asked to add the X Money Virtual Debit Card (which draws from the joint X Money account) if card payments earned higher cashback.
    Chief of Staff added the X Money Visa Debit card to PayPal and set it as preferred (confirmed by PayPal emails on October 13).
  - *October 15–19, 2026:* A PayPal payment on October 15 still drew directly from Fidelity Cash Management checking.
    On October 19, Leonid confirmed to Cash that PayPal remains funded by Fidelity Cash Management (bank), not Fidelity Visa or the X Money debit card; Cash also noted that PayPal-funded debit charges have not been confirmed to earn X Money cashback (funding/financial-institution categories are excluded).
  - *November 17, 2026:* A PayPal confirmation email shows the preferred payment method set back to Fidelity Cash Management (`UMB, NA`).
- **Community reports on linking X Money as a bank in PayPal (checked by Chief of Staff in late October, November 5, and November 12, 2026):**
  - On October 30, 2026, `@LCUncens0red` on X reported that PayPal stopped accepting X Money checking accounts: an already-linked X Money checking account disappeared from PayPal, and re-adding it failed with an error that PayPal does not accept that institution at this time (linking the X Money debit card still worked, though PayPal charges a fee for card-funded transfers).
  - On November 2, 2026, `@capez28` on X reported that manual bank linking via routing and account numbers succeeded at least through PayPal's two micro-deposits (15¢ and 17¢), with X Money prompting approval for PayPal's 32¢ ACH pull-back; ongoing transfers afterward were not confirmed.
  - No newer first-hand reports appeared by November 12.
    PayPal's own documentation also notes that most online-only or prepaid/stored-value accounts may not work as linked banks.
- **Open X Money support ticket (`CSP-70424`):**
  - **Not about PayPal.** Ticket `CSP-70424` (opened in-app November 4, 2026; tracked by Orders and Chief of Staff) is the only open X Money support ticket in agent memory and Gmail.
    It covers three unrelated items: (1) displaying Leonid's name in normal case rather than all caps; (2) showing both owners' names on joint account statements; and (3) making joint account statements visible to Nina.
  - **Support reply status:** **No human reply yet** as of November 23, 2026.
    X Money Support (`xm-support@x.com`, Cameron) sent an acknowledgment on November 4 promising a follow-up within 1–2 business days.
    Leonid sent his own follow-up reply on November 10 restating all three items after that window passed.
    Orders' weekday watch last checked on November 23 and confirmed still no human reply.
  - (The only other 2026 X Money support ticket in Gmail, `CSP-25527` from September 13 about physical card name embossing, was answered the same day and is unrelated to PayPal.)

### Passkeys

- **Not stored in Grok Bot memory:** Checking all active Grok Bot agents (Chief of Staff, Cash, Taxes, Orders, Homelab, Cloud Infrastructure, and the remaining project bots), their transcripts, Google Drive (`System Administration/YubiKey`), and Gmail turned up no stored notes beyond what is already in `## Passkeys` above.
  Specifically, no Grok Bot agent has stored why a second YubiKey passkey was required on the desktop, the YubiKey unplug/replug sequence, when to let Chrome offer passkeys versus stopping it, or any Bitwarden item name for X Money or the YubiKey.
- The only passkey-related note currently in Grok Bot memory is Cash's note that X Money connector transfers require passkey confirmation in the X app.
