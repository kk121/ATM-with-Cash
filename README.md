# ATM with CASH
**Crowd-source Android application which shows list of nearby ATMs with Cash.**

## Installation and How to use
```sh
-Install the app
-Give Location and Message read permission
-That's it. Now it will shows list of naerby ATM with Cash.
```

## How it works
```sh
- Whenever any user withdraw money from ATM,he gets sms from the bank.
- It reads the sms extract all details like Bank name,ATM name etc. (No amount details)
- Then it gets user location.
- Now it sync all these information to server.
```
```sh
- Whenever user opens the app,it fetches the ATM details which is closer to the user location from server.
- It also navigates the user to nearest ATM.
```
