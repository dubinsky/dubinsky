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

## X Money support ticket

- **Open X Money support ticket (`CSP-70424`):**
  - **Not about PayPal.** Ticket `CSP-70424` (opened in-app November 4, 2026; tracked by Orders and Chief of Staff) is the only open X Money support ticket in agent memory and Gmail.
    It covers three unrelated items: (1) displaying Leonid's name in normal case rather than all caps; (2) showing both owners' names on joint account statements; and (3) making joint account statements visible to Nina.
  - **Support reply status:** **No human reply yet** as of November 23, 2026.
    X Money Support (`xm-support@x.com`, Cameron) sent an acknowledgment on November 4 promising a follow-up within 1–2 business days.
    Leonid sent his own follow-up reply on November 10 restating all three items after that window passed.
    Orders' weekday watch last checked on November 23 and confirmed still no human reply.
  - (The only other 2026 X Money support ticket in Gmail, `CSP-25527` from September 13 about physical card name embossing, was answered the same day and is unrelated to PayPal.)

## Credentials

Phone. An X Money passkey is already there. It was created in the X app: Money tab, menu, Settings, Security and Privacy, Credentials.

Desktop sign-in can use it by scanning Chrome’s QR code and approving with face, fingerprint, or the phone PIN. A saved username and password does not answer that prompt.

The Bitwarden passkey lives in the vault.
The phone lets the Bitwarden app supply it, so login on the phone works.

On desktop Linux, Chrome uses its own passkey path and a plugged-in security key.
Bitwarden's system passkey provider is the desktop app on Windows 11 and macOS.
Linux is outside that. On this Linux desktop the extension still cannot supply that passkey to X Money - even though passkeys are enabled in the extension.
X Money’s sign-in allows only a phone or a USB security key.
With the YubiKey plugged in, Chrome takes the security key and shows no choice of Bitwarden.

Bitwarden only answers when the site allows an internal passkey, which is the kind Bitwarden stores. The extension sees the request, steps aside, and Chrome takes over. The vault item is never offered.


Chrome Google Password Manager. Off. Both credentials_enable_service and password_manager_enabled are false. Leave it off for the procedure below. Chrome has one switch for this, Offer to save passwords and passkeys. Turning it on also makes Chrome ask to save passwords.

Why each screen appeared

With the YubiKey plugged in, Chrome asks for the FIDO PIN and a touch, then fails. With it unplugged, Chrome shows a phone or tablet QR code. The X Money page itself only shows Try again and I can’t access my credential.

Register your credential / This is to ensure the safety of your account / Continue is X Money’s web-passkey step. With Bitwarden passkeys on, Continue does nothing, because the extension takes navigator.credentials.create and never hands it to Chrome. With Bitwarden passkeys off, Continue reaches Chrome and Chrome says: “Google Password Manager is turned off. To create a passkey for money.x.com, Google Password Manager must be on.” That button stores a passkey in the Google account. It does not write the YubiKey.

The security keys section on the same Credentials page is the YubiKey enrollment. It does not need Google Password Manager.

Settings for the YubiKey procedure

┌──────────────────────────────────────┬──────────────┬─────────┐
│ Setting                              │ During sign  │ After   │
│                                      │ -in and      │ the key │
│                                      │ enrollment   │ is      │
│                                      │              │ listed  │
├──────────────────────────────────────┼──────────────┼─────────┤
│ Bitwarden → Settings → Notifications │ Off          │ On      │
│ → Ask to save and use passkeys       │              │ again   │
├──────────────────────────────────────┼──────────────┼─────────┤
│ Chrome → chrome://password-          │ Off (leave   │ Off     │
│ manager/settings → Offer to save     │ it)          │         │
│ passwords and passkeys               │              │         │
├──────────────────────────────────────┼──────────────┼─────────┤
│ YubiKey                              │ Unplugged    │ Plugged │
│                                      │ until the    │ in to   │
│                                      │ Credentials  │ enroll, │
│                                      │ page is open │ then    │
│                                      │              │ either  │
│                                      │              │ way     │
└──────────────────────────────────────┴──────────────┴─────────┘

Bitwarden passkeys go back on at the end. Other sites’ Bitwarden passkeys keep working. X Money on this desktop still goes to Chrome (phone QR or this YubiKey). The copy of the passkey already in the Bitwarden vault stays unused for desktop X Money sign-in.

Add the YubiKey

1. In the Bitwarden extension, open Settings → Notifications and turn Ask to save and use passkeys off.
2. Leave Chrome’s Offer to save passwords and passkeys off. If a dialog offers Settings and Close for Google Password Manager, click Close.
3. Unplug the YubiKey.
4. In Chrome profile Default, open X Money and sign in. Chrome shows the phone QR code. Scan it with the phone that holds the X Money passkey and approve it. On iPhone, Bitwarden must be the passkey provider under Settings → Passwords → AutoFill Passwords and Passkeys if that passkey lives in Bitwarden. On Android: Bitwarden → Settings → Autofill → Passkey management.
5. Open Money → Settings → Security and Privacy → Credentials. Use the security keys section.
6. Plug in the YubiKey.
7. Use that section’s add control. Ignore Register your credential / Continue. That is the web passkey.
8. Chrome asks for a USB security key. Enter the YubiKey FIDO PIN and touch the key. If no dialog is visible, check the other desktops.
9. Confirm the new key is listed in the security keys section. The SSH credential on the key stays. One slot is now used.
10. Turn Bitwarden Ask to save and use passkeys back on.

Sign in next time

This laptop, key plugged in. Open X Money. Chrome asks for the FIDO PIN and a touch. Bitwarden may appear for a moment and then hand the request to Chrome. That is the transport check, and the key can succeed once it holds the credential.

Phone instead. Unplug the YubiKey first, then sign in and approve the QR code on the phone. Leave the key unplugged for the whole sign-in. If it is plugged in, Chrome asks the key and the phone QR does not come up.

Web passkey (the other button)

Register your credential / Continue creates a passkey in Google Password Manager for nina@podval.org. To finish that path you would turn Offer to save passwords and passkeys on, click Continue again, confirm Create, and set a Google Password Manager PIN the first time. That PIN is only for Google Password Manager passkeys. Chrome will also start asking to save passwords. Leave Bitwarden passkeys off afterward, or add money.x.com under Bitwarden → Settings → Notifications → Excluded domains. An excluded domain also stops Bitwarden’s save-login and update-login notices for that domain. With Bitwarden passkeys left on and the domain not excluded, Bitwarden takes the next X Money sign-in, does not find the Google passkey in the vault, and does not hand the request back to Chrome.
