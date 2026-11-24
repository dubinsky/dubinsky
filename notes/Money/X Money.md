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

Compiled on November 23, 2026 from Grok Bot agent memory across Chief of Staff, Cash, Taxes, Orders, and the other active bots (plus the consolidated SSA notes in Google Drive and Gmail status for the open support ticket).

### Social Security direct deposit

Goal: move both Leonid's and Nina's Social Security direct deposits from Fidelity Cash Management to the joint X Money checking account (**Onion**, at Cross River Bank). Each spouse must update their own benefits.

**What was tried:**
- **X Money's built-in direct-deposit switch:** Tried for Leonid; Social Security blocked the switch from X Money's side (exact reason unconfirmed).
- **`my Social Security` online (`ssa.gov`):** Failed for Leonid with `"We require more information to process your request."`
- **SSA phone line (`1-800-772-1213`):** Tried on October 6, 2026 after getting a one-time code at `ssa.gov/PIN`. The phone representative could not change direct deposit. Expected the same outcome for Nina and concluded an in-person office visit is required.
- **Office visit:** Leonid previously walked into an SSA office with his documents, where SSA approved his retirement claim (awarded September 22, 2026; citizenship proof accepted; new Social Security card with the same number mailed; October 27 Cambridge appointment cancelled) and set direct deposit to Fidelity Cash Management. A separate office visit to switch both Leonid's and Nina's deposits to **Onion** was initially planned for October 13, 2026 (Cambridge primary, Waltham backup), then unscheduled on October 9, 2026 (date to be decided; calendar event removed).

**What worked:**
- For Leonid and Nina so far, only Fidelity Cash Management is active: Leonid's claim is approved (benefits begin November 2026, first payment around December 23, 2026, then around the fourth Wednesday monthly) and Nina already receives benefits on the third-Wednesday schedule (sometimes a day early). Neither switch to **Onion** has gone through yet.
- In community reports tracked by Grok Bot, no first-hand reports were found of anyone switching SSA direct deposit to X Money online or by phone. Every confirmed success report (including X posts from `@Sweet_T02` on September 3, `@spremark` on September 11, two additional X posts logged October 12, and a report on The Bonus Stack on October 9) succeeded via an in-person SSA office visit with photo ID. `@Sweet_T02` noted that telling the front desk you are there only to change direct deposit got them seen as a walk-in without an appointment and with no payment gap (consistent with SSA's walk-in rule for direct-deposit-only visits).

**What is still pending:**
- In-person SSA office visit for both Leonid and Nina (date TBD; primary: Cambridge SSA Office, 10 Fawcett St, 1st floor; backup: Waltham SSA Office, 135 Beaver St; Mon–Fri 9:00 AM–4:00 PM). No SSA deposit has landed in **Onion** as of November 23, 2026.
- Proof of Nina's joint ownership on **Onion**: X Money does not issue paper checks, **Onion**'s monthly statement previously showed only Leonid's name (not Nina's), and the X Money connector listed **Onion** as a secondary account rather than a joint account. Before going, confirm whether Nina can see **Onion** from her own login and download a statement/direct-deposit form with her name, or get a signed joint-owner letter from X Money support (overlapping with ticket **CSP-70424**).
- Counter verification at SSA: have the clerk run **Onion**'s Cross River Bank routing and account numbers at the counter to confirm acceptance (the CFPB lists X Money as a stored-value/prepaid account at Cross River Bank; whether SSA treats **Onion** as prepaid or regular checking, and whether it satisfies SSA's prepaid-account criteria, remains unverified), and ask whether either record has a direct-deposit fraud-prevention or online-services block that can be lifted.
- Keep Fidelity Cash Management open until SSA deposits actually land in **Onion**. (X Money's 6% APY is already active via X Ultra / SuperGrok Heavy and does not depend on direct deposit; Chief of Staff's weekly Monday SSA reminder is paused.)

**Where the SSA Visit Prep doc is:**
- Google Drive: `Gnezdo/SSA/SSA Visit Prep` (alongside the consolidated history doc `Gnezdo/SSA/SSA Status Notes`).

### PayPal bank account setup and support ticket

**What was tried (worked / failed):**
- **October 13, 2026 — X Money Virtual Debit Card added in PayPal (worked, but PayPal did not stay on it):** When PayPal was opened to switch funding to **Onion**, the preferred method was the Fidelity Rewards credit card and the only bank on file was Fidelity Cash Management checking (showing a warning flag); **Onion** was not yet linked as a bank account. Rather than linking **Onion** as a bank, Leonid asked to add the X Money Virtual Debit Card (which debits **Onion**) to try for higher cashback. That debit card was added and set as PayPal's preferred payment method on October 13.
- **October 15–19, 2026 — PayPal still drew from Fidelity Cash Management:** A PayPal payment on October 15 still came straight out of Fidelity Cash Management checking. On October 19, Leonid confirmed to Cash that PayPal remains funded from Fidelity Cash Management (bank), not Fidelity Visa or X Money Debit. Cash also noted that PayPal-funded debit charges have not been confirmed to earn X Money cashback (funding/financial-institution categories are excluded).
- **Linking X Money as a bank account in PayPal:** No Grok Bot agent has a record of Leonid attempting to link **Onion** as a bank account in PayPal directly (and Gmail has no PayPal bank-linking or micro-deposit emails). Instead, Chief of Staff checked for first-hand community reports (late October, November 5, and November 12, 2026):
  - On October 30, 2026, `@LCUncens0red` on X reported that PayPal stopped accepting X Money checking accounts — their previously linked X Money checking account disappeared from PayPal, and trying to re-add it failed with an error that PayPal does not accept that institution at this time (linking the X Money debit card still worked, though PayPal charges a fee to send money with a card).
  - On November 2, 2026, `@capez28` on X reported that manual bank linking via routing and account numbers succeeded at least through PayPal's two micro-deposits (15¢ and 17¢), and that X Money prompted them to approve PayPal's 32¢ ACH withdrawal afterward; they did not confirm whether ongoing PayPal transfers worked.
  - No newer first-hand reports were found on the November 12 check. PayPal also warns that most online-only or prepaid/stored-value accounts may not work as linked banks (and X Money's CFPB agreement classifies it as a stored-value/prepaid account at Cross River Bank).

**Open X Money support ticket (CSP-70424):**
- **Not about PayPal.** Ticket **CSP-70424** is the only open X Money support ticket in Grok Bot memory and Gmail, and it is about two separate issues: (1) Leonid's name displaying in ALL CAPS instead of normal case, and (2) joint account (**Onion**) statement visibility (Nina not appearing on / not able to see joint statements).
- **Support reply status:** **No human reply yet** as of November 23, 2026. X Money Support (`xm-support@x.com`, Cameron) sent an acknowledgment on November 4, 2026 promising a reply within 1–2 business days. Leonid sent a follow-up reply on November 10, 2026 (replacing an unsent Gmail draft). Orders' weekday morning watch last checked on November 23, 2026 and still saw no human reply.

### X Money passkey

- **Not stored in Grok Bot memory:** Checking all active Grok Bot agents (Chief of Staff, Cash, Taxes, Orders, Homelab, Cloud Infrastructure, and the remaining project bots) and their transcripts turned up no stored notes on the X Money passkey setup — specifically nothing about why the Bitwarden passkey works on the phone while a second passkey had to be registered on the YubiKey for the desktop, when to unplug and plug the YubiKey back in, when to let Chrome offer passkeys versus stopping it, or the Bitwarden item name for X Money.
- **Only passkey details currently in memory:** Cash notes that X Money transfers confirmed through X Money require passkey confirmation in the X app; the YubiKey's own credentials are tracked in Bitwarden (`YubiKey 5C`, per `notes/System Administration/YubiKey.md`).
- **TODO:** Fill in the X Money Bitwarden item name, why a second YubiKey passkey is required on the desktop, and the YubiKey unplug/replug and Chrome passkey prompt procedure.
